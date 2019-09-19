package exploration.graph;

import environment.Frontier;
import environment.OccupancyGrid;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ExplorationGraph {

    Map<SimpleNode, Node> nodeMap;
    private Map<String, SimpleNode> lastAddedNodes;
    private SimpleNode lastNode;
    //lastNode could be dropped if the edges are generated not in an historical way, i.e. in the visibility graph

    public ExplorationGraph() {
        this.nodeMap = new LinkedHashMap<>();
        this.lastAddedNodes = new HashMap<>();
        this.lastNode = null;
    }

    Map<SimpleNode, Node> getNodeMap() {
        return nodeMap;
    }

    void setNodeMap(Map<SimpleNode, Node> nodeMap) {
        this.nodeMap = nodeMap;
    }

    /**
     * Initializes the map with an entry for each agent
     *
     * @param agents the names of the agents in the team
     */
    void initializeAgents(List<String> agents){
        for(String a : agents)
            this.lastAddedNodes.put(a,null);
    }

    /**
     * Provides the real node in the graph, provided a SimpleNode element. A SimpleNode object is composed only by its
     * coordinates x and y. Through this we can have access also to its list of adjacent nodes
     *
     * @param node the SimpleNode to retrieve
     * @return the Node in the adjacency list, corresponding to the SimpleNode in input. Returns null if no Node
     * object is found with the coordinates of the SimpleNode
     */
    Node getNode(SimpleNode node){
        return nodeMap.get(node);
    }

    private Node getLastNode(){
        return getNode(lastNode);
    }

    private void setLastNode(SimpleNode lastNode) {
        this.lastNode = lastNode;
    }

    Double euclideanDistance(SimpleNode node1, SimpleNode node2){
        return Math.sqrt(
                Math.pow(node1.x-node2.x,2)+Math.pow(node1.y-node2.y,2)
        );
    }

    /**
     * Adds the node to the graph by linking it to the last added node. The distance is computed as the euclidean
     * distance from the node to which it is linked. Should be used only with non-frontier nodes
     *
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
    private void addNode(Node node, double distance){
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
     * Adds a list of points to the graph as nodes. Doesn't take into account edges up to now, might be extended
     * according to the graph type.
     * @param nodes the list of coordinates
     */
    void addNodesList(LinkedList<Point> nodes){
        for(Point n : nodes)
            this.nodeMap.put(new SimpleNode(n.x, n.y), new Node(n.x, n.y));
    }

    /**
     * Adds the node to the graph. If already present, only updates the information about the presence of the
     * agent
     *
     * @param simpleNode the node to add to the graph or to update
     * @param agentName the name of the agent going through the node
     * @param time the time at which the agent is at the node
     */
    void addNode(SimpleNode simpleNode, String agentName, Integer time){
        Node node = new Node(simpleNode.x, simpleNode.y, agentName, time);
       if(this.nodeMap.containsKey(simpleNode)) {
           node = this.nodeMap.get(simpleNode);
           node.addAgentTime(agentName, time);
       }

        SimpleNode simpleLastNode = this.lastAddedNodes.get(agentName);
        double distance = euclideanDistance(simpleNode,simpleLastNode);
        node.addAdjacent(simpleLastNode, distance);
        this.nodeMap.get(simpleLastNode).addAdjacent(node, distance);
        this.nodeMap.put(simpleNode, node);
       this.lastAddedNodes.put(agentName,simpleNode);
    }

    //TODO implementare quello scritto sul foglio
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
    private void addFrontierNode(Node frontier, SimpleNode adj, double distance){
        nodeMap.putIfAbsent(new SimpleNode(frontier,true), frontier);
        getNode(adj).addAdjacent(new SimpleNode(frontier,true), distance);
        frontier.addAdjacent(adj, distance);
    }

    /**
     * Adds a list of frontiers to the graph as frontier nodes. Doesn't take into account edges up to now, might be
     * extended according to the graph type.
     * @param frontiers the list of coordinates of the frontiers
     */
    void addFrontiersList(LinkedList<Point> frontiers){
        for(Point n : frontiers)
            this.nodeMap.put(new SimpleNode(n.x, n.y), new Node(n.x, n.y));
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
    private GraphPath getPath(SimpleNode n1, SimpleNode n2){
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

    Map<SimpleNode, Map<SimpleNode, List<SimpleNode>>> allPairsShortestPaths(Map<SimpleNode, Map<SimpleNode,Double>> graphDistanceMatrix,
                                                                             Map<SimpleNode, Map<SimpleNode, Integer>> spCountMatrix){
        List<SimpleNode> nodes = new LinkedList<>(this.nodeMap.keySet());
        double[][] distances = new double[nodes.size()][nodes.size()];
        int[][] spCounts = new int[nodes.size()][nodes.size()];
        Map<SimpleNode, Map<SimpleNode, List<SimpleNode>>> childMap = new HashMap<>();
        Map<SimpleNode, List<SimpleNode>> matrixRow;
        List<SimpleNode> matrixCell;

        //matrices of distances and counts initialization
        for(int i=0; i<nodes.size()-1;i++){
            for(int j=i; j<nodes.size();j++){

                if(i==j) {
                    distances[i][j] = 0.0;
                    spCounts[i][j] = 0;
                }
                else {
                    //works because the returned distance is infinite, if the two nodes are not adjacent
                    distances[i][j] = getNode(nodes.get(i)).getDistance(nodes.get(j));
                    distances[j][i] = distances[i][j]; //undirected graph
                    spCounts[i][j] = (distances[i][j] == java.lang.Double.MAX_VALUE ? 0 : 1);
                    spCounts[j][i] = spCounts[i][j];
                }
            }
        }

        //childMap initialization
        for(SimpleNode n : nodes){
            matrixRow = new HashMap<>();
            //child(n,n) = n;
            matrixCell = new LinkedList<>();
            matrixCell.add(n);
            matrixRow.put(n,matrixCell);
            //child(n,adj) = adj;
            for(SimpleNode adj : getNode(n).getAdjacents()){
                matrixCell = new LinkedList<>();
                matrixCell.add(adj);
                matrixRow.put(adj,matrixCell);
            }
            childMap.put(n,matrixRow);
        }

        //Floyd-Warshall
        for(int k=0; k<nodes.size(); k++){
            for(int i=0; i<nodes.size(); i++){
                matrixRow = childMap.get(nodes.get(i));
                for(int j=i+1; j<nodes.size(); j++){
                    matrixCell = new LinkedList<>();
                    if(distances[i][j] == distances[i][k] + distances[k][j]) {
                        matrixCell = matrixRow.get(nodes.get(j));
                        spCounts[i][j] +=1;
                    }
                    if(distances[i][j] > distances[i][k] + distances[k][j]) {
                        distances[i][j] = distances[i][k] + distances[k][j];
                        distances[j][i] = distances[i][j];
                        spCounts[i][j] = 1;
                    }
                    if(matrixCell!=null){
                        matrixCell.add(nodes.get(k));
                        matrixRow.put(nodes.get(j),matrixCell);
                        childMap.put(nodes.get(i),matrixRow);
                    }
                    spCounts[j][i] = spCounts[i][j];
                }
            }
        }

        fillSpCountMatrix(spCountMatrix, spCounts, nodes);
        fillGraphDistanceMatrix(graphDistanceMatrix,distances,nodes);
        return childMap;
    }

    private void fillSpCountMatrix(Map<SimpleNode, Map<SimpleNode, Integer>> spCountMatrix, int[][] spCounts, List<SimpleNode> nodes) {
        Map<SimpleNode, Integer> matrixCell = new HashMap<>();
        for(int i=0;i<nodes.size()-1;i++){
            for(int j=i+1;j<nodes.size();j++){
                matrixCell.put(nodes.get(j),spCounts[i][j]);
            }
            spCountMatrix.put(nodes.get(i),matrixCell);
            matrixCell = new HashMap<>();
        }
    }

    private void fillGraphDistanceMatrix(Map<SimpleNode, Map<SimpleNode,Double>> graphDistanceMatrix,
                                         double[][] distances, List<SimpleNode> nodes ){
        Map<SimpleNode, Double> matrixRow;
        for(SimpleNode n : nodes){
            int idx = nodes.indexOf(n);
            matrixRow = new HashMap<>();
            for(int j=0;j<nodes.size();j++){
                matrixRow.put(nodes.get(j),distances[idx][j]);
            }
            graphDistanceMatrix.put(n,matrixRow);
        }
    }

    //si potrebbe fare anche nel Builder ma lo implemento qua perché almeno può essere chiamata da chiunque abbia accesso al grafo
    /**
     * Removes all the adjacent nodes from each frontier node, leaving only the nearest ones
     */
    void shortestPathFrontiers(){
        List<Node> frontierList = new LinkedList<>();

        //initialization
        for (SimpleNode n : nodeMap.keySet()){
            if(n.isFrontier())
                frontierList.add(nodeMap.get(n));
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
     * Removes all the nodes which are not visible from the frontier nodes. To do this it checks whether there is a
     * direct line possible in the occupancy grid between each frontier node and its adjacent nodes.
     * @param environment the occupancy grid of the environment
     */
    void cleanVisibleFrontiers(OccupancyGrid environment){
        List<Node> frontierList = new LinkedList<>();

        //initialization
        for (SimpleNode n : nodeMap.keySet()){
            if(n.isFrontier())
                frontierList.add(nodeMap.get(n));
        }

        //retrieve list of adjacent nodes and remove all the non visible ones
        for(Node f : frontierList){
            List<SimpleNode> nodes = f.getAdjacentsList();
            for(SimpleNode n : nodes){
                //debug
                log("f=("+f.x+","+f.y+") n="+n+"obs= "+environment.numObstaclesOnLine(f.x,f.y,n.x,n.y));
                //fine debug

                if(environment.numObstaclesOnLine(f.x,f.y,n.x,n.y)>0)
                    removeEdge(f, n);
            }
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

    /**
     * Removes the node from the graph and disconnects it from every other node it is linked to
     * @param n the node to remove
     */
    void removeNode(SimpleNode n){
        for (SimpleNode adjacent: getNode(n).getAdjacents())
            removeEdge(n,adjacent);
        this.nodeMap.remove(n);
    }

    /**
     * Removes all the frontier nodes from the graph and all their edges
     */
    void removeFrontiers(){
        List<SimpleNode> frontiersList = nodeMap.keySet().stream().
                filter(SimpleNode::isFrontier).collect(Collectors.toCollection(LinkedList::new));
        for(SimpleNode n: frontiersList)
            this.removeNode(n);
    }

    /**
     * Provides a copy of this graph.
     * @return an identical copy of this graph
     */
    ExplorationGraph copy(){
        ExplorationGraph copy = new ExplorationGraph();
        copy.setNodeMap(this.nodeMap);
        copy.setLastNode(this.lastNode);
        return copy;
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
