import java.util.ArrayList;
import java.util.Arrays;

public class Node implements Runnable{
    private int id;
    private int color;
    private int numNodes;
    private int maxDeg;
    private ArrayList<int[]> activeNeighbors;
    private boolean startFlag = true;
    static final String UNDECIDED = "undecided";

    public Node(int id, int numNodes,int maxDeg,int[][] neighbors){
        this.id = id;
        this.color = id;
        this.numNodes = numNodes;
        this.maxDeg = maxDeg;
        for(int i=0 ; i<neighbors.length ; i++){
            activeNeighbors.add(neighbors[i]);
        }
    }

    public void run(){
        if(startFlag){
            String undecided = String.valueOf(id)+" "+UNDECIDED;
            for(int i=0 ; i<activeNeighbors.size() ; i++){
                sendMessage(activeNeighbors.get(i)[0], activeNeighbors.get(i)[1], undecided);
                break;
            }
        }
        if(checkNeighbors()){
            for(int i=0 ; i<activeNeighbors.size() ; i++){
                sendMessage(activeNeighbors.get(i)[0], activeNeighbors.get(i)[1], UNDECIDED);
            }
        }
        else{
            color = findMinColor();
            for(int i=0 ; i<activeNeighbors.size() ; i++){
                sendMessage(activeNeighbors.get(i)[0], activeNeighbors.get(i)[1], String.valueOf(color));
                stop();
            }
        }
    }

    private void sendMessage(int nodeId, int socketNumber, String message){

    }

    private void readMessage(){

    }

    private boolean checkNeighbors(){

    }

    private int findMinColor(){

    }

    private void stop(){

    }


}
