package exploration.graph;

import path.Path;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 *
 */
public class ExplorationGraph {

    private Map<SimpleNode, Node> nodeMap;
    private SimpleNode lastNode;
    private boolean multiplePaths;

    ExplorationGraph() {
        this.nodeMap = new LinkedHashMap<>();
        this.lastNode = null;
        this.multiplePaths = false;
    }

    Map<SimpleNode, Node> getNodeMap() {
        return nodeMap;
    }

    void setNodeMap(Map<SimpleNode, Node> nodeMap) {
        this.nodeMap = nodeMap;
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
    // * @param n1 node in the graph
    // * @param n2 node adjacent to n1
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

    private Node getLastNode(){
        return getNode(lastNode);
    }

    public boolean isMultiplePaths() {
        return multiplePaths;
    }

    private Double euclideanDistance(SimpleNode node1, SimpleNode node2){
        return Math.sqrt(
                Math.pow(node1.x-node2.x,2)+Math.pow(node1.y-node2.y,2)
        );
    }

    /**
     * Adds the node to the graph by linking it to the last added node. The distance is computed as the euclidean
     * distance from the node to which it is linked. Should be used only with non-frontier nodes
     * @param node node to add to the graph
     */
    void addNode(Node node) {
        double distance = 0;
        if(lastNode!=null){
            distance = euclideanDistance(node, lastNode);
        }

        addNode(node, distance);
    }

    /**
     * Adds the node to the graph by linking it to the last added node. Should be used only with non-frontier nodes
     * @param node node to add to the graph
     * @param distance the distance from the last added node
     */
    void addNode(Node node, double distance){
        SimpleNode simpleNode = new SimpleNode(node);
        if(lastNode != null) {
            if(nodeMap.containsKey(simpleNode)) node = nodeMap.get(simpleNode);
            Node lastNode = getLastNode();
            node.addAdjacent(lastNode, distance);
            lastNode.addAdjacent(node, distance);
        }
        nodeMap.put(simpleNode, node);
        lastNode = node;
    }

    /**
     * Adds a frontier node to the graph, if not already present and links it to each of its adjacent nodes
     * @param frontier the frontier node to add to the graph
     */
    void addFrontierNode(Node frontier){
        for(SimpleNode n : frontier.getAdjacents()){
            addFrontierNode(frontier, n, frontier.getDistance(n));
        }
    }

    /**
     * Adds a frontier node to the graph, if not already present and links it to a node
     * @param frontier the frontier node to add to the graph
     * @param adj a node in the graph to which the frontier is linked
     * @param distance distance between the node and the frontier node
     */
    void addFrontierNode(Node frontier, SimpleNode adj, double distance){
        nodeMap.putIfAbsent(new SimpleNode(frontier,true), frontier);
        getNode(adj).addAdjacent(new SimpleNode(frontier,true), distance);
        frontier.addAdjacent(adj, distance);
    }

    /**
     * Computes the distance between two nodes in the graph, provided their coordinates. If one of the two nodes is not
     * in the graph, then a negative value is returned.
     * @param n1 first node
     * @param n2 second node
     * @return the length of the path connecting the two nodes. It returns -1 if at least one of the nodes is not in
     * the graph
     */
    double distanceNodes(SimpleNode n1, SimpleNode n2){
        if(n1.equals(n2))
            return 0;

        GraphPath path = getPath(n1,n2);

        if(path == null)
            return -1;
        else return path.getLength();
    }

    /**
     * Calculates the shortest path connecting two nodes in the graph. If there are multiple paths, then only the first
     * found one is returned.
     * @param n1 starting node
     * @param n2 arrival node
     * @return a path connecting the two nodes in input. Returns null if at least one of the two input nodes is not in
     * the graph.
     */
    GraphPath getPath(SimpleNode n1, SimpleNode n2){
        if(!nodeMap.containsKey(n1) || !nodeMap.containsKey(n2))
            return null;

        if(n1.equals(n2)) {
            GraphPath path = new GraphPath();
            path.addNode(n1, 0);
            return path;
        }

        if(getNode(n1).getAdjacents().contains(n2)){
            GraphPath path = new GraphPath();
            path.addNode(n1, 0);
            path.addNode(n2, getNode(n1).getDistance(n2));
            return path;
        }

        this.multiplePaths = false; //reset of the variable, otherwise it is not meaningful
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
                if(current.equals(n2)) return reconstructPath(parentMap, n1, n2);
            }

            Set<SimpleNode> neighbors = getNode(current).getAdjacents();
            for(SimpleNode neighbor : neighbors){
                if(!visited.contains(neighbor)){
                    double predictedDistance = euclideanDistance(neighbor,n2);
                    double neighborDistance = getNode(current).getDistance(neighbor);
                    double totalDistance = distances.get(current) + neighborDistance + predictedDistance;
                    if(neighbor.equals(n2) && distances.containsKey(neighbor) && totalDistance == distances.get(neighbor))
                        //if the total computed distance is equal to the one stored for the arrival node, there might be multiple paths
                        multiplePaths = true;
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

    /**
     * Computes the path according to the map of a node to its parent.
     * @param parentMap mapping of each node to its parent in the path
     * @param n1 starting node
     * @param n2 arrival node
     * @return the path connecting the two nodes
     */
    private GraphPath reconstructPath(HashMap<SimpleNode, SimpleNode> parentMap, SimpleNode n1, SimpleNode n2) {
        GraphPath path = new GraphPath();
        path.setStartingNode(n2, 0);
        while(path.getStartingNode()!=n1){
            SimpleNode father = parentMap.get(path.getStartingNode());
            //useless to specify, just for debugging
            Node fatherNode = getNode(father);
            SimpleNode startingNode = path.getStartingNode();
            //end debugging
            double distance = fatherNode.getDistance(startingNode);
            path.setStartingNode(father, distance);
        }
        return path;
    }

    /**
     * Provides all the the paths going from the starting node to the arrival node in input
     * @param startNode the starting node for the paths
     * @param arrivalNode the arrival node for the paths
     * @return a list containing all the shortest paths from the source to the arrival node
     */
    List<GraphPath> getMultiplePaths(SimpleNode startNode, SimpleNode arrivalNode) {
        List<GraphPath> pathsList = new LinkedList<>();
        HashMap<SimpleNode, Boolean> visited = new HashMap<>();
        GraphPath path = getPath(startNode, arrivalNode);
        pathsList.add(path);
        double minDistance = path.getLength();
        path = new GraphPath();

        //Visited map initialization
        for (SimpleNode node : nodeMap.keySet())
            visited.put(node,node.isFrontier());
        visited.put(arrivalNode,false);

        path.setStartingNode(startNode, 0);
        log("Starting search from "+startNode.toString()+" to "+arrivalNode.toString());
        depthFirstSearch(startNode, arrivalNode, visited, path, (double) 0, (double) 0, minDistance, pathsList);

        //removes every non shortest path -> might be useless, but it's just to be sure
        minDistance = pathsList.stream().mapToDouble(GraphPath::getLength).min().getAsDouble();
        List<GraphPath> longerPaths = new LinkedList<>();
        for(GraphPath p : pathsList){
            if(p.getLength()>minDistance)
                longerPaths.add(p);
        }
        pathsList.removeAll(longerPaths);
        return pathsList;
    }

    private void depthFirstSearch(SimpleNode startNode, SimpleNode arrivalNode, HashMap<SimpleNode, Boolean> visited,
                          GraphPath path, double delta, double length, double minDist, List<GraphPath> pathsList){
        log("   "+startNode.toString()+" ----> "+arrivalNode.toString());
        if(startNode.equals(arrivalNode)) {
            if(!pathsList.contains(path))
                pathsList.add(new GraphPath(path));
            path.removeNode(startNode, delta);
            return;
        }
        if(startNode.isFrontier()){
            path.removeNode(startNode,delta);
            return;
        }

        visited.put(startNode,true);

        for(SimpleNode adj : getNode(startNode).getAdjacents()){
            double newDelta = getNode(startNode).getDistance(adj);
            double newLength = length + newDelta;

            if(!visited.get(adj)) {
                if(newLength < minDist || (newLength == minDist && adj.equals(arrivalNode))){
                    path.addNode(adj, newDelta);
                    depthFirstSearch(adj, arrivalNode, visited, path, newDelta, newLength, minDist, pathsList);
                }
            }
        }

        visited.put(startNode, false);
        path.removeNode(startNode, delta);
    }

    //si potrebbe fare anche nel Builder ma lo implemento qua perché almeno può essere chiamata da chiunque abbia accesso al grafo
    /**
     * Removes all the adjacent nodes from each frontier node, leaving only the nearest ones
     */
    void cleanFrontiers(){
        List<Node> frontierList = new LinkedList<>();

        //initialization
        for (SimpleNode n : nodeMap.keySet()){
            if(n.isFrontier())
                frontierList.add(nodeMap.get(n)); //aggiungo la frontiera
        }

        //retrieve list of nearest nodes and remove all the others
        for(Node f : frontierList){
            List<SimpleNode> nonNearestNodes = f.getAdjacentsList();
            nonNearestNodes.removeAll(f.getNearestNodes());
            for(SimpleNode n : nonNearestNodes)
                removeEdge(f, n);
        }

    }

    /**
     * Removes the edge comprised between the two nodes provided
     * @param n1 starting node
     * @param n2 arrival node
     * @return true if the edge was in the graph and it has been removed, false if there is no such edge
     */
    boolean removeEdge(SimpleNode n1, SimpleNode n2){
        boolean result;
        result = nodeMap.get(n1).removeAdjacent(n2);
        if(!result)
            result = nodeMap.get(n2).removeAdjacent(n1);
        return !result;
    }

    public static void log(String data){
        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            File file;
            file = new File(System.getProperty("user.dir")+"/logs/search log.txt");
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            bw.write(data);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}
