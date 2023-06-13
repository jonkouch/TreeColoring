import java.io.File;
import java.util.Scanner;

public class Manager {
    private Node nodes[];
    static final String DIR_PATH = "inputFiles/";
    private int currNodes;


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
        nodes[currNodes++] = new Node(id, numNodes, maxDeg, neighbors);
    }


    public void readInput(String path) {
        try {
            Scanner scannerInput = new Scanner(new File(DIR_PATH + path));
            int numNodes = Integer.parseInt(scannerInput.next());
            int maxDeg = Integer.parseInt(scannerInput.next());
            nodes = new Node[numNodes];

            while (scannerInput.hasNextLine()){
                String row = scannerInput.nextLine();
                processRow(row, numNodes, maxDeg);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public String start() {
        // your code here
        return "coloring massage in output format";

    }

    public String terminate() {
        // your code here
        return "coloring massage in output format";
    }
}
