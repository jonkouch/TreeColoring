import java.io.File;
import java.util.*;

public class Manager {
    private Node nodes[];
    static final String DIR_PATH = "inputFiles/";
    private int currNodes;
    private Map<Integer, Integer> nodeColors = new HashMap<>(); // A map to store Node ID - Color pairs
    private List<Thread> nodeThreads = new ArrayList<>(); // A list to store Node threads
    private static final int UNDECIDED = -1;

    public Manager() {
        currNodes = 0;
    }

    public void processRow(String row, int numNodes, int maxDeg){
        String[] parts = row.split("\\s+", 2);
        int id = Integer.parseInt(parts[0]);
        String arrayPart = parts[1].replaceAll("\\[|\\]", "");
        String[] arrayItems = arrayPart.split(",\\s+");

        int[][] neighbors = new int[arrayItems.length / 3][3];
        int index = 0;
        for (int i = 0; i < neighbors.length; i++) {
            for (int j = 0; j < 3; j++) {
                neighbors[i][j] = Integer.parseInt(arrayItems[index]);
                index++;
            }
        }
        nodes[currNodes] = new Node(id, numNodes, maxDeg, neighbors, this);
        nodeThreads.add(new Thread(nodes[currNodes])); // Add new Node's thread to the list
        currNodes++;

    }


    public void readInput(String path) {
        try {
            Scanner scannerInput = new Scanner(new File(DIR_PATH + path));
            int numNodes = Integer.parseInt(scannerInput.next());
            int maxDeg = Integer.parseInt(scannerInput.next());
            nodes = new Node[numNodes];

            scannerInput.nextLine();
            while (scannerInput.hasNextLine()){
                String row = scannerInput.nextLine();
                processRow(row, numNodes, maxDeg);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public String start() {
        for(Thread t : nodeThreads) {
            t.start();
        }

        for(Thread t : nodeThreads){
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return printColors(); // Print the colors when all nodes have finished
    }


    public void sendMessage(int nodeId, String message) {
        Node node = getNodeById(nodeId);
        if (node != null) {
            node.processMessage(message);
        }
    }

    private Node getNodeById(int nodeId) {
        for (Node node : nodes) {
            if (node.getId() == nodeId) {
                return node;
            }
        }
        return null;
    }


    public void terminateNode(int id, int color) {
        nodeColors.put(id, color); // Update nodeColors map when a node terminates
    }

    public String terminate() {
        return printColors();
    }

    private String printColors() {
        StringBuilder output = new StringBuilder();
        for(Map.Entry<Integer, Integer> entry : nodeColors.entrySet()) {
            output.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
        }
        return output.toString();
    }
}
