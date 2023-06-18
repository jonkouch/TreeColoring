import java.io.*;
import java.util.*;
import java.net.*;

public class Node implements Runnable {
    private int id;
    private int color;
    private int numNodes;
    private int maxDeg;
    private Set<Integer> usedColors = new HashSet<>();
    private ArrayList<int[]> activeNeighbors = new ArrayList<>();
    private Dictionary<Integer, Integer> neighborState = new Hashtable<>();
    static final int UNDECIDED_VALUE = -1;
    private Manager manager;

    public Node(int id, int numNodes, int maxDeg, int[][] neighbors, Manager manager) {
        this.id = id;
        color = -1;
        this.numNodes = numNodes;
        this.maxDeg = maxDeg;
        this.manager = manager;
        for (int i = 0; i < neighbors.length; i++) {
            activeNeighbors.add(neighbors[i]);
            neighborState.put(neighbors[i][0], UNDECIDED_VALUE);
        }
    }

    /**
     * The run function of the Runnable Interface.
     * Implements the pseudocode of the asynchronous "Reduce" algorithm learned in the homework.
     */
    public void run() {
        if (!checkNeighbors()) {
            color = findMinColor();
            messageNeighbors(id + " " + color);
        } else {
            while (color == -1) {
                readMessages();
                if (!checkNeighbors()) {
                    color = findMinColor();
                    messageNeighbors(id + " " + color);
                    stop();
                }
            }
        }
    }


    /**
     * Send a message to a neighbor node given the writing socket for the given node.
     *
     * @param socketNumber: The writing socket for the given node.
     * @param message:      The message to send.
     */
    private void sendMessage(int socketNumber, String message) {
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
     *
     * @param message: The message to send.
     */
    private void messageNeighbors(String message) {
        for (int i = 0; i < activeNeighbors.size(); i++) {
            int socketNumber = activeNeighbors.get(i)[1];
            sendMessage(socketNumber, message);
        }
    }


    /**
     * Reads all current pending messages and updates activeNeighbors and neighborState accordingly.
     */
    private void readMessages() {
        for (int i = 0; i < activeNeighbors.size(); i++) {
            int[] neighbor = activeNeighbors.get(i);
            int neighborId = neighbor[0];
            int readingPort = neighbor[2];

            try {
                // Create a socket and connect to the neighbor's reading port
                Socket socket = new Socket("localhost", readingPort);

                // Get the input stream from the socket
                InputStream inputStream = socket.getInputStream();

                // Read the message from the input stream
                byte[] buffer = new byte[1024]; // Adjust the buffer size as needed
                int bytesRead = inputStream.read(buffer);
                String message = new String(buffer, 0, bytesRead);

                // Process the message and update activeNeighbors and neighborState accordingly
                if(message.compareTo("")!=0)
                    processMessage(message);

                // Close the socket and input stream
                socket.close();
                inputStream.close();
            } catch (IOException e) {
                // Handle any exceptions that occur during socket operations
                e.printStackTrace();
            }

        }
    }

    /**
     * Process the received message and update neighbor state accordingly.
     *
     * @param message The received message.
     */
    private void processMessage(String message) {
        String[] parts = message.split(" ");

        int senderId = Integer.parseInt(parts[0]);
        int color = Integer.parseInt(parts[1]);
        neighborState.put(senderId, color);
        activeNeighbors.remove(senderId);
    }


    /**
     * Check if v has undecided neighbor u with ID(u) > ID(v).
     *
     * @return: if v has undecided neighbor u with ID(u) > ID(v) then True.
     * else False.
     */
    private boolean checkNeighbors() {
        for (int i = 0; i < activeNeighbors.size(); i++) {
            int neighborID = activeNeighbors.get(i)[0];
            if (neighborID > id && (neighborState.get(neighborID) == UNDECIDED_VALUE)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find the minimum non-negative integer that is not used by a neighbor.
     * The values for this method are held in the activeNeighbors dictionary.
     *
     * @return The integer as described above.
     */
    private int findMinColor() {
        int minColor = 0;
        while (usedColors.contains(minColor))
            minColor++;
        return minColor;
    }

    /**
     * Stop the process of the node.
     */
    private void stop() {
        manager.terminateNode(id, color); // Pass id and color to the Manager's terminate method
        Thread.currentThread().interrupt(); // End the thread
    }
}
