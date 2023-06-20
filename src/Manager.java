import java.io.File;
import java.io.IOException;
import java.util.*;

public class Manager {
    private Node nodes[];
    static final String DIR_PATH = "inputFiles/";
    private int currNodes;
    private Map<Integer, Integer> nodeColors = new HashMap<>(); // A map to store Node ID - Color pairs
    private List<Thread> nodeThreads = new ArrayList<>(); // A list to store Node threads

    public Manager() {
        currNodes = 0;
    }


    /**
     * processes a row from the input file, adds the nodes to the nodes array and creates a thread for each node.
     * @param row row from input file
     * @param numNodes number of nodes in graph
     * @param maxDeg maximum degree in graph
     * @throws IOException
     */
    public void processRow(String row, int numNodes, int maxDeg) throws IOException {
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

    /**
     * iterates over the input rows and processes each one
     * @param path path to the location of the input file
     */
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

    /**
     * starts the node threads and joins them
     * @return the result of the coloring algorithm
     */
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

    /**
     * Updates nodeColors map when a node terminates
     * @param id id of node
     * @param color the color the algorithm colored the node with this id with
     */
    public void terminateNode(int id, int color) {
        nodeColors.put(id, color);
    }

    /**
     * interrupts all node threads
     * @return the result of the coloring algorithm
     */
    public String terminate() {
        for(Thread t : nodeThreads) {
            t.interrupt();
        }
        return printColors();
    }

    /**
     * creates StringBuilder of the coloring results of the algorithm
     * @return the StringBuilder as a string
     */
    private String printColors() {
        StringBuilder output = new StringBuilder();
        for(Map.Entry<Integer, Integer> entry : nodeColors.entrySet()) {
            output.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
        }
        return output.toString();
    }
}
