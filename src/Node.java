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
    private Map<Integer, ServerSocket> serverSockets = new HashMap<>();
    static final int UNDECIDED_VALUE = -1;
    private Manager manager;
    private Map<Integer, Thread> neighborThreads = new HashMap<>();


    public Node(int id, int numNodes, int maxDeg, int[][] neighbors, Manager manager) throws IOException {
        this.id = id;
        color = -1;
        this.numNodes = numNodes;
        this.maxDeg = maxDeg;
        this.manager = manager;
        for (int i = 0; i < neighbors.length; i++) {
            activeNeighbors.add(neighbors[i]);
            neighborState.put(neighbors[i][0], UNDECIDED_VALUE);
        }

        for (int i = 0; i < neighbors.length; i++) {
            int neighborId = neighbors[i][0];
            try {
                // Assign each pair of Nodes a unique port
                ServerSocket serverSocket = new ServerSocket(neighbors[i][2]);
                serverSockets.put(neighborId, serverSocket);
                // Start a thread to listen for incoming connections from this neighbor
                Thread neighborThread = new Thread(() -> {
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            Socket socket = serverSocket.accept();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            String message = reader.readLine();
                            processMessage(message);
                            socket.close();
                        } catch (IOException e) {
//                            e.printStackTrace();
                            if (Thread.currentThread().isInterrupted()) {
                                // break out of the loop if the thread is interrupted
                                break;
                            }
                        }
                    }
                });
                neighborThread.start();
                neighborThreads.put(neighborId, neighborThread);
            } catch (IOException e) {
//                e.printStackTrace();
            }
        }
    }

    /**
     * The run function of the Runnable Interface.
     * Implements the pseudocode of the asynchronous "Reduce" algorithm learned in the homework.
     */
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (!checkNeighbors()) {
                    color = findMinColor();
                    messageNeighbors(id + " " + color);
                    stop();
                } else {
                    while (color == -1) {
                        if (!checkNeighbors()) {
                            color = findMinColor();
                            messageNeighbors(id + " " + color);
                            stop();
                        }
                        Thread.sleep(1); // sleep for 1 milliseconds
                    }
                }
            }
        } catch (InterruptedException e) {
            // Handling InterruptedException properly
        }
    }


    /**
     * Send a message to all active neighbors.
     * @param message: The message to send.
     */
    private void messageNeighbors(String message) {
        for (int i = 0; i < activeNeighbors.size(); i++) {
            try {
                // Connect to the neighbor's ServerSocket and send the message
                Socket socket = new Socket("localhost", activeNeighbors.get(i)[1]);
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println(message);
                socket.close();
            } catch (IOException e) {
//                e.printStackTrace();
            }
        }
        Thread curThread = Thread.currentThread();
        curThread.interrupt();
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
        // Interrupt the thread handling this neighbor
        Thread senderThread = neighborThreads.get(senderId);
        if(senderThread != null) {
            ServerSocket serverSocket = serverSockets.get(senderId);
            if (serverSocket != null) {
                try {
                    serverSocket.close();  // Close ServerSocket which will force SocketException on accept()
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            senderThread.interrupt();
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
        for(Map.Entry<Integer, Thread> entry : neighborThreads.entrySet()) {
            ServerSocket serverSocket = serverSockets.get(entry.getKey());
            if (serverSocket != null) {
                try {
                    serverSocket.close();  // Close ServerSocket which will force SocketException on accept()
                } catch (IOException e) {
//                    e.printStackTrace();
                }
            }
            entry.getValue().interrupt();
        }
        Thread.currentThread().interrupt();
    }
}