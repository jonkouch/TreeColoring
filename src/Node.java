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
                if (!checkNeighbors()) {
                    color = findMinColor();
                    messageNeighbors(id + " " + color);
                    stop();
                }
                try {
                    Thread.sleep(1); // sleep for 1 milliseconds
                } catch (InterruptedException e) {
                    // If we've been interrupted, it might be because we were told to stop
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                }
            }
        }
    }


    /**
     * Send a message to all active neighbors.
     * @param message: The message to send.
     */
    private void messageNeighbors(String message) {
        for (int i = 0; i < activeNeighbors.size(); i++) {
            int neighborId = activeNeighbors.get(i)[0];
            manager.sendMessage(neighborId, message);
        }
    }


    /**
     * Process the received message and update neighbor state accordingly.
     *
     * @param message The received message.
     */
    public void processMessage(String message) {
        String[] parts = message.split(" ");

        int senderId = Integer.parseInt(parts[0]);
        int color = Integer.parseInt(parts[1]);
        neighborState.put(senderId, color);
        usedColors.add(color);

        // Find the index of the neighbor's ID in activeNeighbors and remove it
        for (int i = 0; i < activeNeighbors.size(); i++) {
            int[] neighbor = activeNeighbors.get(i);
            if (neighbor[0] == senderId) {
                activeNeighbors.remove(i);
                break;
            }
        }
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

    public int getId(){return id;}

    public int getColor(){return color;}

}
