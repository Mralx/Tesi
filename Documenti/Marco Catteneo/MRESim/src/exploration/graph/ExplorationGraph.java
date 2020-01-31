package exploration.graph;

import agents.RealAgent;
import config.Constants;
import environment.OccupancyGrid;

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
    private boolean dirty;
    //lastNode could be dropped if the edges are generated not in an historical way, e.g. in the visibility graph

    ExplorationGraph() {
        this.nodeMap = new LinkedHashMap<>();
        this.lastAddedNodes = new HashMap<>();
        this.lastNode = null;
        this.dirty = true;
    }

    public Map<SimpleNode, Node> getNodeMap() {
        return nodeMap;
    }

    void setNodeMap(Map<SimpleNode, Node> nodeMap) {
        this.nodeMap = nodeMap;
        this.dirty = true;
    }

    public Map<String, SimpleNode> getLastAddedNodes() {
        return lastAddedNodes;
    }

    public void setLastAddedNodes(Map<String, SimpleNode> lastAddedNodes) {
        this.lastAddedNodes = lastAddedNodes;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /**
     * Initializes the graph by creating a node for each initial location of the agents. All the node are then linked
     * together otherwise the graph would likely be not connected. The communication station is not considered as an
     * agent here, so should not be present in the array
     *
     * @param agents an array of RealAgent objects such that each element is an agent of the exploration process
     */
    void initialize(RealAgent[] agents){
        Node node;
        List<Node> nodes = new LinkedList<>();

        for (RealAgent agent : agents) {
            node = new Node(agent.getX(),agent.getY(),agent.getName(),agent.getTimeElapsed());
            nodes.add(node);
            this.lastAddedNodes.put(agent.getName(), new SimpleNode(node));
        }

        for(int i=0; i<nodes.size()-1; i++){
            node = nodes.get(i);
            for(int j=i+1; j<nodes.size(); j++){
                Node node1;
                node1 = nodes.get(j);
                double distance = euclideanDistance(node, node1);
                node.addAdjacent(node1,distance);
                node1.addAdjacent(node,distance);
            }
            this.nodeMap.put(new SimpleNode(node),node);
        }
        //the previous loop doesn't adds the last node, it has to be added manually
        this.nodeMap.put(new SimpleNode(nodes.get(nodes.size()-1)),nodes.get(nodes.size()-1));
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

    /**
     * Provides the real node in the graph if already present and it updates the time an agent visits it. If it is not
     * present, creates a new node and returns it
     *
     * @param simpleNode the node to retrieve
     * @param agentName the name of the agent visiting the node. If a frontier node, has to be set to null
     * @param time the time at which the request is done, allows to update the agents time in the node
     * @param isFrontier true if the node is a frontier
     * @return the Node in the graph if present, otherwise creates a new node and returns it without linking it to the
     * graph
     */
    Node getNode(SimpleNode simpleNode, String agentName, Integer time, boolean isFrontier){
        Node node;
        if(this.nodeMap.containsKey(simpleNode)) {
            node = this.nodeMap.get(simpleNode);
            if (!isFrontier) node.addAgentTime(agentName,time);
        }
        else{
            node = new Node(simpleNode.x, simpleNode.y, agentName, time);
            node.setFrontier(isFrontier);
            if(isFrontier) setDirty(true);
        }

        return node;
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

    // <editor-fold defaultstate="collapsed" desc="Old methods to add nodes">
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

    //</editor-fold>

    /**
     * Adds the node to the graph if not present, otherwise updates the information about the presence of the agent in
     * the node. In both cases, links it to the last added nodes for that agent
     *
     * @param simpleNode the node to add to the graph or to update
     * @param agentName the name of the agent going through the node
     * @param time the time at which the agent is at the node
     */
    void addNode(SimpleNode simpleNode, String agentName, Integer time){
        Node node = getNode(simpleNode, agentName, time, false);
        SimpleNode simpleLastNode = this.lastAddedNodes.get(agentName);
        for(SimpleNode f : this.nodeMap.keySet().stream().filter(SimpleNode::isFrontier).collect(Collectors.toList()))
            node = createEdge(node, f);
        this.nodeMap.put(simpleNode, createEdge(node, simpleLastNode));
        this.lastAddedNodes.put(agentName,simpleNode);
    }

    /**
     * Adds the frontier node to the graph, if not already present. The parameter time is used only in the case
     * in which the frontier node is not present, by setting it as the time at which the frontier has been
     * discovered. If it was already in the graph, it is not used
     *
     * @param frontierSNode the frontier node to add to the graph
     * @param time the time at which this method is called
     */
    void addFrontierNode(SimpleNode frontierSNode, Integer time){
        Node node = getNode(frontierSNode, null, time, true);
        linkFrontier(node);
        this.nodeMap.put(frontierSNode, node);
    }

    /**
     * Checks if the node in input is within a certain distance from any other node already in the graph
     *
     * @param node1 the node to check
     * @return true if there is another node in the graph within a certain distance, false otherwise
     */
    boolean nearAnotherNode(SimpleNode node1){
        for(SimpleNode node2 : nodeMap.keySet()){
            if(euclideanDistance(node1,node2) < Constants.DISTANCE_THRESHOLD)
                return true;
        }
        return false;
    }

    /**
     * Creates an edge between a node and a node of the graph, if not already present. The first node in input doesn't
     * need to be already in the graph, this only holds for the second one. After having linked the two nodes, returns
     * the first node in input with the updated adjacency list. The distance between the two nodes is computed as the
     * euclidean distance between them
     *
     * @param node the node to link, doesn't need to be in the graph
     * @param adjacentNode the node in the graph the first node links to
     * @return the first node in input with the updated adjacency list
     */
    Node createEdge(Node node, SimpleNode adjacentNode){
        if(node.equals(adjacentNode) || node.isAdjacent(adjacentNode))
            return node;

        double distance = euclideanDistance(node,adjacentNode);
        node.addAdjacent(adjacentNode, distance);
        this.nodeMap.get(adjacentNode).addAdjacent(node, distance);
        return node;
    }

    /**
     * Method implementing the logic of connection between frontiers and nodes. It has to be modified according to the
     * particular implementation of the graph. In particular, here frontiers are linked to all the nodes visited after
     * the first time the frontier is known. This means that if a frontier node f_1 is created at time t, then it is
     * linked to all the nodes n_i visited between t and T, where T is the time at which f_1 is explored
     *
     * @param node the frontier node
     */
    void linkFrontier(Node node){
        for(SimpleNode simpleLastNode : lastAddedNodes.values())
            createEdge(node, simpleLastNode);
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

    // <editor-fold defaultstate="collapsed" desc="Incremental Floyd-Warshall">
    void allPairsShortestPaths(GraphStats stats) {
        System.out.println("Distances computation through complete FW");

        List<SimpleNode> nodes = new LinkedList<>(this.nodeMap.keySet());
        int update = -1;
        double[][] distances = new double[nodes.size()][nodes.size()];
        int[][] spCounts = new int[nodes.size()][nodes.size()];
        Map<SimpleNode, Map<SimpleNode, List<SimpleNode>>> childMap = new HashMap<>();
        Map<SimpleNode, List<SimpleNode>> matrixRow;
        List<SimpleNode> matrixCell;
        SimpleNode iNode, jNode, kNode;

        //matrices of distances and counts initialization
        for (int i = 0; i < nodes.size() - 1; i++) {
            for (int j = i; j < nodes.size(); j++) {

                if (i == j) {
                    distances[i][j] = 0.0;
                    spCounts[i][j] = 0;
                } else {
                    //works because the returned distance is infinite, if the two nodes are not adjacent
                    distances[i][j] = getNode(nodes.get(i)).getDistance(nodes.get(j));
                    distances[j][i] = distances[i][j]; //undirected graph
                    spCounts[i][j] = (distances[i][j] == java.lang.Double.MAX_VALUE ? 0 : 1);
                    spCounts[j][i] = spCounts[i][j];
                }
            }
        }

        //childMap initialization
        for (SimpleNode n : nodes) {
            addChildMapRow(childMap,n);
        }

        //Floyd-Warshall
        //double n = nodes.size(); //TODO aggiunto per controllare avanzamento da console
        for (int k = 0; k < nodes.size(); k++) {
            kNode = nodes.get(k);
            for (int i = 0; i < nodes.size(); i++) {
                iNode = nodes.get(i);
                matrixRow = childMap.get(iNode);
                for (int j = i + 1; j < nodes.size(); j++) {
                    jNode = nodes.get(j);
                    matrixCell = new LinkedList<>();
                    if (!kNode.isFrontier() && k!=i && k!=j) {
                        if (distances[i][j] == distances[i][k] + distances[k][j] && distances[i][j] != Double.MAX_VALUE) {
                            update = 0;
                            matrixCell = matrixRow.get(jNode);
                            if(matrixCell == null) matrixCell = new LinkedList<>();
                            spCounts[i][j] += 1;
                        }
                        if (distances[i][j] > distances[i][k] + distances[k][j]) {
                            update = 1;
                            distances[i][j] = distances[i][k] + distances[k][j];
                            distances[j][i] = distances[i][j];
                            spCounts[i][j] = spCounts[i][k]*spCounts[k][j];
                        }
                        if (update!=-1) symmetricChildAdd(childMap, matrixRow, matrixCell, iNode, jNode, kNode,update==1);
                        spCounts[j][i] = spCounts[i][j];
                        update = -1;
                    }
                }
            }
            //TODO aggiunto per controllare avanzamento da console
            //double percentage = (k / n) * 10000;
            //percentage = (percentage % 10000) / 100;
            //System.out.println("k " + percentage + "%");
        }
        Map<SimpleNode, Map<SimpleNode, Double>> graphDistanceMatrix = new HashMap<>();
        Map<SimpleNode, Map<SimpleNode, Integer>> spCountMatrix = new HashMap<>();

        fillSpCountMatrix(spCountMatrix, spCounts, nodes);
        fillGraphDistanceMatrix(graphDistanceMatrix, distances, nodes);

        stats.setGraphDistanceMatrix(graphDistanceMatrix);
        stats.setSpCountMatrix(spCountMatrix);
        stats.setChildMap(childMap);
    }

    /*
    REF https://www.mpi-inf.mpg.de/fileadmin/inf/d1/teaching/summer16/polycomp/polycomp09.pdf
     */
    /**
     * Dynamic version of the Floyd-Warshall algorithm. In particular, it works only if there are only insertions and
     * no deletions from the graph. Updates the data structures used by the FW algorithm, plus the matrix counting the
     * number of shortest paths between each pair of nodes. The update is done based on the list of new nodes given in
     * input, assuming they have already been added to the graph but not to the three data structures
     *
     * @param childMap the old childMap computed in the previous iterations of the FW algorithm
     * @param graphDistanceMatrix the old graph distance matrix computed in the previous iterations of the FW algorithm
     * @param spCountMatrix the old spCountsMatrix computed in the previous iterations of the FW algorithm
     * @param newNodes the list of new nodes added to the graph
     */
    void incrementalFW(Map<SimpleNode, Map<SimpleNode, List<SimpleNode>>> childMap,
                       Map<SimpleNode, Map<SimpleNode, Double>> graphDistanceMatrix,
                       Map<SimpleNode, Map<SimpleNode, Integer>> spCountMatrix,
                       List<SimpleNode> newNodes){
        System.out.println("Distances computation through incremental FW");

        List<SimpleNode> nodes = new LinkedList<>(this.nodeMap.keySet());
        LinkedList<SimpleNode> neighbors, nonNeighbors;
        extendMatrices(newNodes, childMap, graphDistanceMatrix, spCountMatrix);

        double[][] distances = new double[nodes.size()][nodes.size()];
        int[][] spCounts = new int[nodes.size()][nodes.size()];

        for(int i=0; i<nodes.size()-1; i++)
            for(int j=i; j<nodes.size(); j++){
                if(i==j) {
                    distances[i][i] = 0;
                    spCounts[i][i] = 0;
                }
                else{
                    distances[i][j] = graphDistanceMatrix.get(nodes.get(i)).get(nodes.get(j));
                    distances[j][i] = distances[i][j];
                    spCounts[i][j] = spCountMatrix.get(nodes.get(i)).get(nodes.get(j));
                    spCounts[j][i] = spCounts[i][j];
                }
            }

        for(SimpleNode v : newNodes){

            neighbors = new LinkedList<>(this.nodeMap.get(v).getAdjacents());
            nonNeighbors = new LinkedList<>();
            for(SimpleNode node : this.nodeMap.keySet())
                if(!getNode(v).isAdjacent(node)) nonNeighbors.add(node);
            nonNeighbors.remove(v);

            for(SimpleNode adj : getNode(v).getAdjacents()){
                List<SimpleNode> fakeList = new LinkedList<>();
                fakeList.add(v);
                updateDataStructures(nonNeighbors,fakeList,nodes,childMap,distances,spCounts,adj);
                updateDataStructures(fakeList,nonNeighbors,nodes,childMap,distances,spCounts,adj);
            }
            if(!v.isFrontier()) {
                updateDataStructures(neighbors, nodes, nodes, childMap, distances, spCounts, v);
                updateDataStructures(nonNeighbors, nonNeighbors, nodes, childMap, distances, spCounts, v);
            }

            fillGraphDistanceMatrix(graphDistanceMatrix,distances,nodes);
            fillSpCountMatrix(spCountMatrix,spCounts,nodes);

            // <editor-fold defaultstate="collapsed" desc="Versione probabilmente ottimizzabile, ma non ne sono sicuro">
            /*
            for(SimpleNode n : neighbors){
                idx1 = nodes.indexOf(n);
                Map<SimpleNode, List<SimpleNode>> matrixRow = childMap.get(n);
                if(matrixRow == null) matrixRow = new HashMap<>();

                for(SimpleNode t : nodes) {
                    idx2 = nodes.indexOf(t);
                    update = updateRule(idx1, idx2, idx3, distances,spCounts);

                    List<SimpleNode> matrixCell = matrixRow.get(t);

                    if(matrixCell == null || update == 1)
                        matrixCell = new LinkedList<>();

                    if(update != -1) {
                        matrixCell.add(v);
                        matrixRow.put(t,matrixCell);
                        childMap.put(n,matrixRow);
                    };
                }
            }

            for(SimpleNode s : nonNeighbors){
                idx1 = nodes.indexOf(s);
                for(SimpleNode t : nonNeighbors){
                    idx2 = nodes.indexOf(t);
                    update = updateRule(idx1, idx2, idx3, distances,spCounts);
                    if(update == 1) ;
                    if(update == 0) ;
                    if(update != -1) ;
                }
            }
            */
            // </editor-fold>
        }
    }

    /**
     * Wrapper method to factorize the iterations over two data sets, after fixing the intermediate node. It loops over
     * the two data sets by taking the starting node in the first one, and the arrival node in the second one. Then,
     * applies the update rule of the Floyd-Warshall algorithm to update the three data structures used, that are the
     * childMap, the graph distance matrix and the shortest paths count matrix
     *
     * @param dataset1 the dataset containing the possible starting nodes
     * @param dataset2 the dataset containing the possible arrival nodes
     * @param nodes the list of nodes, used to fix an ordering over the nodes
     * @param childMap the childMap for the FW algorithm
     * @param distances the matrix containing the distances between each pair of nodes
     * @param spCounts the matrix containing the number of shortest paths between each pair of nodes
     * @param v the fixed intermediate node
     */
    private void updateDataStructures(List<SimpleNode> dataset1, List<SimpleNode> dataset2, List<SimpleNode> nodes,
                                      Map<SimpleNode, Map<SimpleNode, List<SimpleNode>>> childMap,
                                      double[][] distances, int[][] spCounts, SimpleNode v){
        int update, idx1, idx2, idx3;
        idx3 = nodes.indexOf(v);

        for(SimpleNode n : dataset1){
            idx1 = nodes.indexOf(n);
            Map<SimpleNode, List<SimpleNode>> matrixRow = childMap.get(n);
            if(matrixRow == null) matrixRow = new HashMap<>();

            for(SimpleNode t : dataset2) {
                idx2 = nodes.indexOf(t);
                update = updateRule(idx1, idx2, idx3, distances,spCounts);

                List<SimpleNode> matrixCell = matrixRow.get(t);

                if(matrixCell == null || update == 1)
                    matrixCell = new LinkedList<>();

                if(update != -1) {
                    if(update == 0 && alreadyFoundPath(matrixRow, childMap.get(t), n, t, v)){
                        spCounts[idx1][idx2] -= 1;
                        spCounts[idx2][idx1] = spCounts[idx1][idx2];
                    }

                    symmetricChildAdd(childMap,matrixRow,matrixCell,n,t,v,update==1);
                };
            }
        }
    }

    private boolean alreadyFoundPath(Map<SimpleNode, List<SimpleNode>> row1, Map<SimpleNode, List<SimpleNode>> row2,
                              SimpleNode node1, SimpleNode node2, SimpleNode intN){
        return  ((row1.get(node2)!=null && row1.get(node2).contains(intN)) ||
                (row2.get(node1)!=null && row2.get(node1).contains(intN)));
    }

    private void symmetricChildAdd(Map<SimpleNode, Map<SimpleNode, List<SimpleNode>>> childMap,
                                   Map<SimpleNode, List<SimpleNode>> matrixRow, List<SimpleNode> matrixCell,
                                   SimpleNode n1, SimpleNode n2, SimpleNode n3, boolean clear){

        for(SimpleNode child13 : childMap.get(n1).get(n3))
            if(!matrixCell.contains(child13))
                matrixCell.add(child13);
        matrixRow.put(n2, matrixCell);
        childMap.put(n1, matrixRow);

        Map<SimpleNode, List<SimpleNode>> tRow = childMap.get(n2);
        List<SimpleNode> tChildren = tRow.get(n1);
        if(tChildren == null || clear) tChildren = new LinkedList<>();
        for(SimpleNode child23 : childMap.get(n2).get(n3))
            if(!tChildren.contains(child23))
                tChildren.add(child23);
        tRow.put(n1, tChildren);
        childMap.put(n2, tRow);
    }

    /**
     * Factorization of the update rule on which the Floyd-Warshall algorithm is based. Considering three nodes s,t and
     * k, the update rule checks the ordering relation between dist(s,t) and dist(s,k)+dist(k,t). In this case, being
     * the FW algorithm implemented here a variation of the original one, also the number of shortest paths between two
     * nodes is stored. The maintaining of its matrix and of the one of the distances is done here. The return value is
     * used to discriminate among the three possible cases
     *
     * @param s the starting node
     * @param t the arrival node
     * @param k the node checked as an intermediate node
     * @param distances the matrix containing the distances between each pair of nodes
     * @param spCounts the matrix containing the number of shortest paths between each pair of nodes
     * @return 1 if the path from s to t going through the intermediate k is shorter, 0 if it is equal, -1 otherwise
     */
    private int updateRule(int s, int t, int k, double[][] distances, int[][] spCounts){
        if (k==s || k==t)
            return -1;

        if (distances[s][t] > distances[s][k] + distances[k][t]) {
            distances[s][t] = distances[s][k] + distances[k][t];
            distances[t][s] = distances[s][t];
            spCounts[s][t] = spCounts[s][k]*spCounts[k][t];
            spCounts[t][s] = spCounts[s][t];
            return 1;
        }

        if (distances[s][t] == distances[s][k] + distances[k][t] && distances[s][t]!=Double.MAX_VALUE) {
            spCounts[s][t] += 1;
            spCounts[t][s] = spCounts[s][t];
            return 0;
        }

        return -1;
    }

    /**
     * Takes the list of new nodes added to the graph and extends the three matrices used to apply the Floyd-Warshall
     * algorithm. The childMap is extended using the appropriate method addChildMapRow. The other two are updated
     * simply considering the same as above definitions for the graph distance matrix and for the shortest paths matrix.
     * This method also exploits the symmetry of the undirected graph to update the corresponding cells for the old
     * nodes in the graph
     *
     * @param newNodes list of new nodes added to the graph
     * @param childMap the childMap to update
     * @param graphDistanceMatrix the graphDistanceMatrix of the graph
     * @param spCountMatrix the matrix of the number of shortest paths between any two given nodes
     */
    private void extendMatrices(List<SimpleNode> newNodes,
                                Map<SimpleNode, Map<SimpleNode, List<SimpleNode>>> childMap,
                                Map<SimpleNode, Map<SimpleNode, Double>> graphDistanceMatrix,
                                Map<SimpleNode, Map<SimpleNode, Integer>> spCountMatrix){
        Node node;
        for(SimpleNode n : newNodes){
            node = getNode(n);
            //add row to the childMap
            addChildMapRow(childMap,n);

            //extend graphDistanceMatrix and spCountMatrix
            Map<SimpleNode, Double> distanceRow = new HashMap<>();
            Map<SimpleNode, Integer> spRow = new HashMap<>();
            double distance;
            int spVal;
            for(SimpleNode n1 : nodeMap.keySet()){
                distance = node.getDistance(n1);
                distanceRow.put(n1, distance);
                spVal = (distance == Double.MAX_VALUE ? 0 : 1);
                if(!n.equals(n1)) {
                    spRow.put(n1, spVal);
                }

                //exploiting symmetry if n1 is not a new node, otherwise is redundant
                if(!newNodes.contains(n1)) {
                    Map<SimpleNode, Double> distanceRow1 = graphDistanceMatrix.get(n1);
                    if (distanceRow1 == null) distanceRow1 = new HashMap<>();
                    distanceRow1.put(n,distance);
                    graphDistanceMatrix.put(n1,distanceRow1);

                    Map<SimpleNode, Integer> spRow1 = spCountMatrix.get(n1);
                    if (spRow1 == null) spRow1 = new HashMap<>();
                    spRow1.put(n, spVal);
                    spCountMatrix.put(n1,spRow1);
                }
            }

            graphDistanceMatrix.put(n, distanceRow);
            spCountMatrix.put(n, spRow);
        }
    }

    /**
     * Adds a new node to the childMap, doesn't check if the node is already there. The row is filled considering that,
     * being n the added node, child(n,n) = n and child(n,adj) = adj, being the child relation such that child(x,y) is
     * the next node (or nodes) on the shortest paths from x to y
     *
     * @param childMap the childMap being updated, assumed to be non null
     * @param n the node to add to the childMap
     */
    private void addChildMapRow(Map<SimpleNode, Map<SimpleNode, List<SimpleNode>>> childMap, SimpleNode n){
        Map<SimpleNode, List<SimpleNode>> matrixRow, nodeRow;
        List<SimpleNode> matrixCell, nodeCell;

        matrixRow = new HashMap<>();
        //child(n,n) = n;
        matrixCell = new LinkedList<>();
        matrixCell.add(n);
        matrixRow.put(n, matrixCell);
        //child(n,node) = predecessor of node on the path from n to node
        matrixRow = new HashMap<>();
        for(SimpleNode node : childMap.keySet()){
            if(!getNode(n).isAdjacent(node)){
                matrixCell = null;
                nodeCell = null;
            }
            else {
                matrixCell = new LinkedList<>();
                nodeCell = new LinkedList<>();
                matrixCell.add(node);
                nodeCell.add(n);
            }
            matrixRow.put(node, matrixCell);

            nodeRow = childMap.get(node);
            if(nodeRow == null) nodeRow = new HashMap<>();
            nodeRow.put(n, nodeCell);
            childMap.put(node, nodeRow);

        }
        childMap.put(n, matrixRow);
    }

    /**
     * Provided the matrix containing the count of all the shortest paths computed by the Floyd-Warshall algorithm,
     * fills the same matrix with a representation based on the Map interface. This is necessary because to simplify the
     * implementation for the FW algorithm, both the distance matrix and the shortest paths count matrix used are based
     * on a ordering over the nodes, applied through the mapping from the set of keys into a list of nodes. Thus, this
     * method is useful to overcome this ordering and make the matrix more versatile
     * @param spCountMatrix the matrix representation based on the Map interface
     * @param spCounts the matrix representation based simply on their int value
     * @param nodes the list used as a reference for the ordering among the nodes
     */
    private void fillSpCountMatrix(Map<SimpleNode, Map<SimpleNode, Integer>> spCountMatrix, int[][] spCounts, List<SimpleNode> nodes) {
        Map<SimpleNode, Integer> matrixCell1, matrixCell2;
        SimpleNode iNode, jNode;

        for(int i=0;i<nodes.size()-1;i++){
            iNode = nodes.get(i);
            matrixCell1 = spCountMatrix.get(iNode);
            if(matrixCell1 == null) matrixCell1 = new HashMap<>();
            for(int j=i+1;j<nodes.size();j++){
                jNode = nodes.get(j);
                matrixCell1.put(jNode,spCounts[i][j]);
                matrixCell2 = spCountMatrix.get(jNode);
                if(matrixCell2 == null) matrixCell2 = new HashMap<>();
                matrixCell2.put(iNode,spCounts[i][j]);
                spCountMatrix.put(jNode,matrixCell2);
            }
            spCountMatrix.put(iNode,matrixCell1);
        }
    }

    /**
     * Provided the matrix containing the distances between each pair of nodes computed by the Floyd-Warshall algorithm,
     * fills the same matrix with a representation based on the Map interface. This is necessary because to simplify the
     * implementation for the FW algorithm, both the distance matrix and the shortest paths count matrix used are based
     * on a ordering over the nodes, applied through the mapping from the set of keys into a list of nodes. Thus, this
     * method is useful to overcome this ordering and make the matrix more versatile
     * @param graphDistanceMatrix the matrix representation based on the Map interface
     * @param distances the matrix representation based simply on their double value
     * @param nodes the list used as a reference for the ordering among the nodes
     */
    private void fillGraphDistanceMatrix(Map<SimpleNode, Map<SimpleNode,Double>> graphDistanceMatrix,
                                         double[][] distances, List<SimpleNode> nodes ){
        Map<SimpleNode, Double> matrixRow, matrixCell;
        SimpleNode iNode, jNode;

        for(int i=0; i<nodes.size()-1;i++){
            iNode = nodes.get(i);
            matrixRow = graphDistanceMatrix.get(iNode);
            if(matrixRow == null) matrixRow = new HashMap<>();
            for(int j=i;j<nodes.size();j++) {
                jNode = nodes.get(j);
                matrixCell = graphDistanceMatrix.get(jNode);
                if(matrixCell == null) matrixCell = new HashMap<>();
                matrixCell.put(iNode, distances[i][j]);
                graphDistanceMatrix.put(jNode, matrixCell);

                matrixRow.put(jNode, distances[i][j]);
            }
            graphDistanceMatrix.put(iNode, matrixRow);
        }
    }

    // </editor-fold>

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
        if (!result)
            result = nodeMap.get(n2).removeAdjacent(n1);
        return !result;
    }

    /**
     * Removes the node from the graph and disconnects it from every other node it is linked to
     * @param n the node to remove
     */
    void removeNode(SimpleNode n){
        List<SimpleNode> adjacentnodes = new LinkedList<>();
        for (SimpleNode adjacent: getNode(n).getAdjacents())
            adjacentnodes.add(new SimpleNode(adjacent));
        for(SimpleNode adjacent : adjacentnodes)
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
        copy.setLastAddedNodes(this.lastAddedNodes);
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
