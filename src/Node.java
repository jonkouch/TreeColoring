import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;
import java.net.*;

public class Node implements Runnable{
    private int id;
    private int color;
    private int numNodes;
    private int maxDeg;
    private Set<Integer> usedColors = new HashSet<>();
    private ArrayList<int[]> activeNeighbors = new ArrayList<>();
    private Dictionary<Integer, Integer> neighborState = new Hashtable<>();
    private boolean startFlag = true;
    static final String UNDECIDED = "undecided";
    static final int UNDECIDED_VALUE = -1;

    public Node(int id, int numNodes,int maxDeg,int[][] neighbors){
        this.id = id;
        this.color = id;
        this.numNodes = numNodes;
        this.maxDeg = maxDeg;
        for(int i=0 ; i<neighbors.length ; i++){
            activeNeighbors.add(neighbors[i]);
            neighborState.put(i, UNDECIDED_VALUE);
        }
    }

    /**
     * The run function of the Runnable Interface.
     * Implements the pseudocode of the "Reduce" algorithm learned in class.
     */
    public void run(){
        if(startFlag){
            startFlag = false;
            String undecided = String.valueOf(id)+" "+UNDECIDED;
            messageNeighbors(undecided);
        }
        else if(checkNeighbors()){
            messageNeighbors(String.valueOf(id)+" "+UNDECIDED);
        }
        else{
            color = findMinColor();
            messageNeighbors(String.valueOf(id)+" "+String.valueOf(color));
            stop();
        }
    }


    /**
     * Send a message to a neighbor node given the writing socket for the given node.
     * @param socketNumber: The writing socket for the given node.
     * @param message: The message to send.
     */
    private void sendMessage(int socketNumber, String message){
        try {
            Socket socket = new Socket("localhost", socketNumber);
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            writer.println(message);
            writer.flush();
            writer.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a message to all active neighbors.
     * @param message: The message to send.
     */
    private void messageNeighbors(String message){
        for(int i=0 ; i<activeNeighbors.size() ; i++){
            int socketNumber = activeNeighbors.get(i)[1];
            sendMessage(socketNumber, message);
        }
    }


    /**
     * Read a pending message from a different node.
     */
    private void readMessage(){

    }

    /**
     * Process the received message and update neighbor state accordingly.
     * @param message The received message.
     */
    private void processMessage(String message) {
        String[] parts = message.split(" ");
        int senderId = Integer.parseInt(parts[0]);
        if (parts[1].equals(UNDECIDED)) {
            neighborState.put(senderId, UNDECIDED_VALUE);
        } else {
            int color = Integer.parseInt(parts[1]);
            neighborState.put(senderId, color);
            activeNeighbors.remove(senderId);
        }
    }


    /**
     * Check if v has undecided neighbor u with ID(u) > ID(v).
     * @return: if v has undecided neighbor u with ID(u) > ID(v) then True.
     * else False.
     */
    private boolean checkNeighbors(){
        for(int i=0 ; i< activeNeighbors.size() ; i++){
            int neighborID = activeNeighbors.get(i)[0];
            if(neighborID>id && (neighborState.get(neighborID) == UNDECIDED_VALUE)){
                return true;
            }
        }
        return false;
    }

    /**
     * Find the minimum non-negative integer that is not used by a neighbor.
     * The values for this method are held in the activeNeighbors dictionary.
     * @return The integer as described above.
     */
    private int findMinColor(){
        int minColor = 0;
        while(usedColors.contains(minColor))
            minColor++;
        return minColor;
    }

    /**
     * Stop the process of the node.
     */
    private void stop(){
        String message = String.valueOf(color);
        messageNeighbors(message);
        System.out.println("Node " + id + " has stopped.");
    }
}
