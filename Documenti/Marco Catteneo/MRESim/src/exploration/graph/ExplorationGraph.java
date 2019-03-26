package exploration.graph;

import java.util.*;

/**
 *
 */
public class ExplorationGraph {

    //TODO modificare con set anziché con una lista e assicurarsi che gli hashset funzionino con l'equals
    private List<Node> nodes;
    private Set<Node> nodeSet;
    private Map<SimpleNode, Node> nodeMap;
    private SimpleNode lastNode;

    ExplorationGraph() {
        this.nodes = new ArrayList<>();
        this.nodeSet = new HashSet<>();
        this.nodeMap = new LinkedHashMap<>();
        this.lastNode = null;
    }

    /**
     * Provides the real node in the graph, provided a SimpleNode element. A SimpleNode object is composed only by its
     * coordinates x and y. Through this we can have access also to its list of adjacent nodes.
     * @param node the SimpleNode to retrieve
     * @return the Node in the adjacency list, corresponding to the SimpleNode in input. Returns null if no Node
     * object is found with the coordinates of the SimpleNode
     */
    Node getNode(SimpleNode node){
        return nodeMap.get(node);
    }


    /**
     * Provides the edge connecting the two SimpleNode in input. Based on the assumption that the graph is undirected,
     * thus if an edge connecting n1 to n2 exists, also an edge connecting n2 to n1 does.
     * @param n1 node in the graph
     * @param n2 node adjacent to n1
     * @return a SimpleEdge of n1 with n2 as adjacent. Returns null if n1 is not a node of the graph or if n2 is not in
     * its list of adjacent nodes.
     */
    /*
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
    */

    Node getLastNode(){
        return getNode(lastNode);
    }

    private Double euclideanDistance(SimpleNode node1, SimpleNode node2){
        return Math.sqrt(
                Math.pow(node1.x-node2.x,2)+Math.pow(node1.y-node2.y,2)
        );
    }

    void addNode(Node node) {
        double distance = euclideanDistance(node, lastNode);
        addNode(node, distance);
    }

    void addNode(Node node, double distance){
        if(lastNode != null) {
            Node lastNode = getLastNode();
            node.addAdjacent(lastNode, distance);
            lastNode.addAdjacent(node, distance);
        }
        nodeMap.put(new SimpleNode(node), node);
        lastNode = node;
    }

    /**
     * Computes the distance between two nodes in the graph, provided their coordinates. If one of the two nodes is not
     * in the graph, then a negative value is returned.
     * @param n1 first node
     * @param n2 second node
     * @return the length of the path connecting the
     */
    double distanceNodes(SimpleNode n1, SimpleNode n2){
        double distance = 0;
        List<SimpleNode> path = getPath(n1,n2);
        if(path == null)
            return -1;
        for(int i=0; i<path.size()-1; i++){
            distance += getNode(path.get(i)).getDistance(path.get(i+1));
        }
        return distance;
    }

    List<SimpleNode> getPath(SimpleNode n1, SimpleNode n2){
        if(!nodeMap.containsKey(n1) || !nodeMap.containsKey(n2))
            return null;

        if(getNode(n1).getAdjacents().contains(n2)){
            List<SimpleNode> path = new ArrayList<>();
            path.add(n1);
            path.add(n2);
            return path;
        }

        HashMap<SimpleNode, SimpleNode> parentMap = new HashMap<>();
        HashSet<SimpleNode> visited = new HashSet<>();
        Map<SimpleNode, Double> distances = new HashMap<>();
        Queue<SimpleNode> priorityQueue = new PriorityQueue<>(
                (SimpleNode o1, SimpleNode o2) -> distances.get(o1).compareTo(distances.get(o2))
        );

        distances.put(n1, (double) 0);
        priorityQueue.add(n1);
        SimpleNode current;

        while(!priorityQueue.isEmpty()){
            current = priorityQueue.remove();
            if(!visited.contains(current)){
                visited.add(current);
                if(current.equals(n2)) return reconstructPath(parentMap, n1, n2); //TODO
            }
            Set<SimpleNode> neighbors = getNode(current).getAdjacents();
            for(SimpleNode neighbor : neighbors){
                if(!visited.contains(neighbor)){
                    double predictedDistance = euclideanDistance(neighbor,n2);
                    double neighborDistance = getNode(current).getDistance(neighbor);
                    double totalDistance = distances.get(current) + neighborDistance + predictedDistance;
                    if(!distances.containsKey(neighbor) || totalDistance < distances.get(neighbor)){
                        distances.put(neighbor, totalDistance);
                        parentMap.put(neighbor, current);
                        priorityQueue.add(neighbor);
                    }
                }
            }
        }
        return reconstructPath(parentMap, n1, n2);
    }

    private List<SimpleNode> reconstructPath(HashMap<SimpleNode, SimpleNode> parentMap, SimpleNode n1, SimpleNode n2) {
        LinkedList<SimpleNode> path = new LinkedList<>();
        path.addFirst(n2);
        while(path.getFirst()!=n1){
            path.addFirst(parentMap.get(path.getFirst()));
        }
        return path;
    }

}
