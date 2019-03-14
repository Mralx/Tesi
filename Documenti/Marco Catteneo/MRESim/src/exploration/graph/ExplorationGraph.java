package exploration.graph;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ExplorationGraph {

    //TODO modificare con set anziché con una lista e assicurarsi che gli hashset funzionino con l'equals
    private List<Node> nodes;

    public ExplorationGraph() {
        this.nodes = new ArrayList<>();
    }

    /**
     * Provides the real node in the graph, provided a SimpleNode element. A SimpleNode object is composed only by its
     * coordinates x and y. Through this we can have access also to its list of adjacent nodes.
     * @param node the SimpleNode to retrieve
     * @return the Node in the adjacency list, corresponding to the SimpleNode in input. Returns null if no Node
     * object is found with the coordinates of the SimpleNode
     */
    Node getNode(SimpleNode node){
        for(Node n: nodes){
            if(node.equals(n))
                return n;
        }
        return null;
    }

    /**
     * Provides the edge connecting the two SimpleNode in input. Based on the assumption that the graph is undirected,
     * thus if an edge connecting n1 to n2 exists, also an edge connecting n2 to n1 does.
     * @param n1 node in the graph
     * @param n2 node adjacent to n1
     * @return a SimpleEdge of n1 with n2 as adjacent. Returns null if n1 is not a node of the graph or if n2 is not in
     * its list of adjacent nodes.
     */
    SimpleEdge getEdge(SimpleNode n1, SimpleNode n2){
        if(this.getNode(n1)!=null) {
            List<SimpleEdge> edges = this.getNode(n1).getAdjacents();
            for (SimpleEdge e : edges) {
                if (e.getAdjacent().equals(n2))
                    return e;
            }
        }

        return null;
    }

    List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    Node getLastNode(){
        return nodes.get(nodes.size()-1);
    }

    void addNode(Node node) {
        if(nodes.size()>1) {
            Node lastNode = getLastNode();
            node.addAdjacent(new SimpleEdge(new SimpleNode(lastNode)));
            lastNode.addAdjacent(new SimpleEdge(new SimpleNode(node)));
        }
        nodes.add(node);
    }

    boolean containsNode(SimpleNode node){
        for(Node n: nodes){
            if(node.equals(n))
                return true;
            else //optimization in the search (is it true?)
                for(SimpleEdge e: n.getAdjacents()){
                    if(e.getAdjacent().equals(node))
                        return true;
                }
        }
        return false;
    }

    //ricontrollare
    double distanceNodes(SimpleNode n1, SimpleNode n2){
        if(!containsNode(n1) || !containsNode(n2))
            return -1;
        return getEdge(n1,n2).getDistance();
    }

    List<SimpleEdge> getPath(SimpleNode n1, SimpleNode n2){
        if(!containsNode(n1) || !containsNode(n2))
            return null;

        List<SimpleEdge> path = new ArrayList<SimpleEdge>();
        if(getNode(n1).getAdjacents().contains(n2)){
            path.add(getEdge(n1,n2));
            return path;
        }

        List<SimpleEdge> unsettled = new ArrayList<>();
        List<SimpleEdge> settled = new ArrayList<>();
        unsettled.add(new SimpleEdge(n1, 0));
        while(!unsettled.isEmpty()){
            double min_d = unsettled.get(0).getDistance();
            SimpleEdge s_edge = unsettled.get(0);
            for(SimpleEdge e: unsettled){
                if(e.getDistance()<min_d){
                    min_d = e.getDistance();
                    s_edge = e;
                }
            }
            unsettled.remove(s_edge);
            Node n_node = get
        }

        return null; //TODO implement with dijkstra
    }
}
