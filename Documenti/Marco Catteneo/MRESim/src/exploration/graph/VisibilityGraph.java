package exploration.graph;

import environment.OccupancyGrid;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VisibilityGraph extends ExplorationGraph {

    //il range dei sensori è 200, scritto da qualche parte nelle configurazioni. ottenibile runtime da ogni agente
    private int sensingRange = 200;
    private double margin = 1.02; //margine giusto per non essere precisi con il range dei sensori, potrebbe essere inutile

    /**
     * Converts the graph in input into a visibility graph. The occupancy grid of the environment has to be provided
     * to do this transformation
     * @param graph the graph to transform
     * @return the visibility graph having as nodes, the nodes of the graph in input and the edges are computed
     * according to the relation of visibility in the environment provided
     */
    VisibilityGraph transform(ExplorationGraph graph){

        VisibilityGraph visibilityGraph = new VisibilityGraph();
        visibilityGraph.setNodeMap(graph.getNodeMap());
        visibilityGraph.connect();
        return visibilityGraph;
    }

    VisibilityGraph mergeGraphs(List<ExplorationGraph> graphs){

        VisibilityGraph visGraph = new VisibilityGraph();
        Map<SimpleNode, Node> cumulativeMap = new LinkedHashMap<>();

        for(ExplorationGraph graph : graphs){
            cumulativeMap.putAll(graph.getNodeMap());
        }

        visGraph.setNodeMap(cumulativeMap);
        visGraph.connect();

        return visGraph;
    }

    //TODO il numero di ostacoli dovrebbe essere parametrico
    private void connect(){
        List<SimpleNode> nodes = new LinkedList<>(nodeMap.keySet());
        for(int i=0;i<nodes.size()-1;i++){
            for(int j=i+1;j<nodes.size();j++){
                Node node1 = this.nodeMap.get(nodes.get(i));
                Node node2 = this.nodeMap.get(nodes.get(j));
                double distance = euclideanDistance(node1,node2);
                if(inRange(node1,node2)){
                    node1.addAdjacent(new SimpleNode(node2),distance);
                    node2.addAdjacent(new SimpleNode(node1),distance);
                }
                else
                    removeEdge(node1,node2);
            }
        }
    }

    /**
     * Checks if two nodes are visible one from another. To tell that, it checks whether their distance is lower than
     * the sensing range of the agents, multiplied by a margin, and the number of obstacles between the straight line
     * connecting them is lower than a fixed threshold
     *
     * @param n1 the first node
     * @param n2 the second node
     * @return true if they satisfy the visibility conditions, false otherwise
     */
    private boolean inRange(SimpleNode n1, SimpleNode n2){
        return euclideanDistance(n1,n2)<=sensingRange*margin &&
                GraphHandler.getEnvironment().numObstaclesOnLine(n1.x,n1.y,n2.x, n2.y)<3;
    }

    /**
     * Implementation of the method to link frontiers of the node in the case of a visibility graph. In particular,
     * here the frontier is linked only to the nodes visible from it
     *
     * @param node the Node object representing the frontier node
     */
    @Override
    void linkFrontier(Node node) {
        for(SimpleNode simpleNode : nodeMap.keySet())
            if (inRange(node, simpleNode)) this.nodeMap.put(simpleNode, createEdge(node, simpleNode));
    }

    /**
     * Adds the node to the graph if not present, otherwise updates the information about the presence of the agent in
     * the node. In both cases, links it to all the visible nodes
     *
     * @param simpleNode the node to add to the graph or to update
     * @param agentName the name of the agent going through the node
     * @param time the time at which the agent is at the node
     */
    @Override
    void addNode(SimpleNode simpleNode, String agentName, Integer time) {
        Node node = getNode(simpleNode, agentName, time, false);
        for(SimpleNode simpleVisibleNode : this.nodeMap.keySet())
            if(inRange(simpleNode,simpleVisibleNode))
                this.nodeMap.put(simpleNode, createEdge(node, simpleVisibleNode));
        this.lastAddedNodes.put(agentName,simpleNode); //vaguely useless attribute for this kind of graph
    }
}
