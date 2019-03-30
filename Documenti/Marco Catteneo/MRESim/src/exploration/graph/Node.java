package exploration.graph;

import java.text.DecimalFormat;
import java.util.*;

public class Node extends SimpleNode {

    //List of adjacent nodes, used to implement the graph as an adjacency list
    private Map<SimpleNode, java.lang.Double> adj;

    public Node(int x, int y) {
        super(x,y);
        this.adj = new HashMap<>();
    }

    public Node(int x, int y, Map<SimpleNode, java.lang.Double> adj) {
        super(x, y);
        this.adj = adj;
    }

    Set<SimpleNode> getAdjacents(){
        return adj.keySet();
    }

    public Map<SimpleNode, java.lang.Double> getAdjacentMap() {
        return adj;
    }

    void addAdjacent(SimpleNode node, double distance){
        adj.put(new SimpleNode(node), distance);
    }

    public boolean isAdjacent(SimpleNode node){
        return adj.containsKey(node);
    }

    public boolean removeAdjacent(SimpleNode node){
        return adj.remove(node)==null;
    }

    double getDistance(SimpleNode node){
        if(adj.containsKey(node))
            return adj.get(node);
        return java.lang.Double.MAX_VALUE;
    }

    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat(".##");
        String toString = "Node    [" + x + ',' + y + "] -> {" ;
        for(SimpleNode n: adj.keySet()){
            toString = toString + n + '=' + df.format(adj.get(n)) + "  ";
        }
        return toString + "}";
    }
}
