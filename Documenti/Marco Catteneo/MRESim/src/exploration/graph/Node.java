package exploration.graph;

import java.text.DecimalFormat;
import java.util.*;

public class Node extends SimpleNode {

    //List of adjacent nodes, used to implement the graph as an adjacency list
    private Map<SimpleNode, java.lang.Double> adj;
    //Map used to log the time at which an agent goes through this node
    private Map<Integer, String> agentTime;

    public Node(int x, int y) {
        super(x,y);
        this.adj = new HashMap<>();
        this.agentTime = new HashMap<>();
    }

    public Node(int x, int y, String agentName, Integer time){
        super(x,y);
        this.adj = new HashMap<>();
        this.agentTime = new HashMap<>();
        this.agentTime.put(time, agentName);
    }

    Set<SimpleNode> getAdjacents(){
        return adj.keySet();
    }

    List<SimpleNode> getAdjacentsList() {
        return new LinkedList<>(adj.keySet());
    }

    public Map<SimpleNode, java.lang.Double> getAdjacentMap() {
        return adj;
    }

    /**
     * Provides all the timestamps at which the agent has gone through this node.
     *
     * @param agentName the name of the agent which timestamps are queried
     * @return an array containing all the timestamps, null if the agent never went through this
     */
    public List<Integer> getAgentTime(String agentName) {
        List<Integer> timestamps = new LinkedList<>();
        for(Integer t : this.agentTime.keySet()){
            if(this.agentTime.get(t).equals(agentName))
                timestamps.add(t);
        }

        return timestamps.size() == 0 ? null : timestamps;
    }

    /**
     * Provides the time at which the node has been visited for the first time by an agent
     *
     * @return the integer value of the time of first visit by an agent
     */
    public Integer getNodeFirstVisit(){
        if(this.agentTime.keySet().size()==0)
            return null;
        return this.agentTime.keySet().stream().min(Integer::compareTo).get();
    }

    /**
     * Provides the name of the last agent gone through the node
     *
      * @return the name of the agent or null if there is no mapping (should never happen, unless it is a frontier)
     */
    public String getLastVisitor(){
        if(this.agentTime.keySet().size()==0)
            return null;
        return this.agentTime.get(this.agentTime.keySet().stream().max(Integer::compareTo).get());
    }

    /**
     * Adds an entry in the map for the agent at a certain timestamp
     *
     * @param agentName the agent that has gone through the node
     * @param time the time at which the agent did it
     */
    void addAgentTime(String agentName, Integer time){
        this.agentTime.put(time, agentName);
    }

    void addAdjacent(SimpleNode node, double distance){
        if(distance>0 && !(this.isFrontier() && node.isFrontier()))
            adj.put(new SimpleNode(node), distance);
    }

    boolean isAdjacent(SimpleNode node){
        return adj.containsKey(node);
    }

    boolean removeAdjacent(SimpleNode node){
        return adj.remove(node)==null;
    }

    double getDistance(SimpleNode node){
        if(adj.containsKey(node))
            return adj.get(node);
        return java.lang.Double.MAX_VALUE;
    }

    /**
     * Provides the list of the nearest nodes to this, excluding frontier nodes. This is done to avoid that the nearest
     * node to a frontier node is another frontier node
     *
     * @return list containing the nearest non frontier nodes, null if no adjacent nodes are present (should never
     * happen)
     */
    List<SimpleNode> getNearestNodes(){
        if(adj.values().size() == 0) return null;

        double minDist = adj.values().stream().min(Comparator.comparing(java.lang.Double::doubleValue)).get();
        List<SimpleNode> nodes = new LinkedList<>();

        for(SimpleNode n: adj.keySet()){
            if (adj.get(n)==minDist && !n.isFrontier())
                nodes.add(n);
        }
        return nodes;
    }

    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat(".###");
        String toString = "Node    [" + x + ',' + y + "] -> {" ;
        for(SimpleNode n: adj.keySet()){
            toString = toString + n + '=' + df.format(adj.get(n)) + "  ";
        }
        if(isFrontier())
            toString = toString.replace("Node  ", "Node f");
        return toString + "}";
    }
}
