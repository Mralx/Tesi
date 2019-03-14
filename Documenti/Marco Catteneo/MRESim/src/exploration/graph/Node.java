package exploration.graph;

import java.util.ArrayList;
import java.util.List;

public class Node extends SimpleNode {

    //List of adjacents nodes, used to implement the graph as an adjacency list
    private List<SimpleEdge> adjacents;

    public Node(int x, int y) {
        super(x,y);
        this.adjacents = new ArrayList<>();
    }

    public Node(int x, int y, List<SimpleEdge> adjacents) {
        super(x,y);
        this.adjacents = adjacents;
    }

    public List<SimpleEdge> getAdjacents() {
        return adjacents;
    }

    public void setAdjacents(List<SimpleEdge> adjacents) {
        this.adjacents = adjacents;
    }

    void addAdjacent(SimpleEdge adjacent){
        this.adjacents.add(adjacent);
    }

}
