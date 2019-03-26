package exploration.graph;

import java.util.*;

public class Node extends SimpleNode {

    //List of adjacents nodes, used to implement the graph as an adjacency list
    private Map<SimpleNode, java.lang.Double> adj;

    public Node(int x, int y) {
        super(x,y);
        this.adj = new HashMap<>();
    }

    public Node(int x, int y, Map<SimpleNode, java.lang.Double> adj) {
        super(x, y);
        this.adj = adj;
    }

    public Set<SimpleNode> getAdjacents(){
        return adj.keySet();
    }

    public Map<SimpleNode, java.lang.Double> getAdjacentMap() {
        return adj;
    }

    public void addAdjacent(SimpleNode node, double distance){
        adj.put(node, distance);
    }

    public boolean isAdjacent(SimpleNode node){
        return adj.containsKey(node);
    }

    public boolean removeAdjacent(SimpleNode node){
        return adj.remove(node)==null;
    }

    public double getDistance(SimpleNode node){
        if(adj.containsKey(node))
            return adj.get(node);
        return java.lang.Double.MAX_VALUE;
    }
}
