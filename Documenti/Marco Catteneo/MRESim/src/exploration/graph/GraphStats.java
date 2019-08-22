package exploration.graph;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

class GraphStats {

    private Map<SimpleNode, Map<SimpleNode, Integer>> spCountMatrix;
    private Map<SimpleNode, Map<SimpleNode,Double>> graphDistanceMatrix;

    GraphStats() {
        this.graphDistanceMatrix = new HashMap<>();
        this.spCountMatrix = new HashMap<>();
    }

    /**
     * Computes the number of nodes of the graph, including frontier nodes
     * @param graph the graph to analyze
     * @return the number of nodes of the graph
     */
    private int nodeCount(ExplorationGraph graph){
        return graph.getNodeMap().size();
    }

    /**
     * Computes the number of frontier nodes of the graph
     * @param graph the graph to analyze
     * @return the number of nodes of the graph
     */
    private int frontierNodeCount(ExplorationGraph graph){
        return (int) graph.getNodeMap().keySet().stream().
                filter(SimpleNode::isFrontier).count();
    }

    /**
     * Computes the number of edges of the graph
     * @param graph the graph to analyze
     * @return the number of edges of the graph
     */
    private int edgeCount(ExplorationGraph graph){
        return graph.getNodeMap().values().stream().
                mapToInt(node -> node.getAdjacentsList().size()).
                sum()/2;
    }

    /**
     * Computes the distance between each node in the graph and every other node
     * @param graph the graph to analyze
     * @return a map containing as key the node, and as value another map. This second map has as key another node in
     * the graph and as value, the distance between the key of the first map and the key of this one
     */
    private Map<SimpleNode, Map<SimpleNode,Double>> graphDistanceMatrix(ExplorationGraph graph){
        Map<SimpleNode, Map<SimpleNode,Double>> graphDistanceMatrix = new HashMap<>();
        graph.allPairsShortestPaths(graphDistanceMatrix,spCountMatrix);
        return graphDistanceMatrix;
    }

    private Set<SimpleNode> restrictedNodeSet(ExplorationGraph graph){

        Set<SimpleNode> frontNodes = graph.getNodeMap().keySet().
                stream().filter(SimpleNode::isFrontier).collect(Collectors.toSet());
        Set<SimpleNode> worthNodes = new HashSet<>(frontNodes);
        for(SimpleNode node : frontNodes){
            worthNodes.addAll(graph.getNode(node).getAdjacents());
        }

        //transitive closure over the nodes adjacent to the nodes linked to a frontier
        frontNodes.addAll(worthNodes);
        for(SimpleNode node : frontNodes){
            worthNodes.addAll(graph.getNode(node).getAdjacents());
        }

        return worthNodes;
    }

    /**
     * Computes the closeness centrality of the graph. It is defined for each vertex as the average distance from that
     * vertex to all other vertices connected to it. The closeness centrality for isolated vertices is taken to be zero.
     * @param graph the graph to analyze
     * @return a map with key the vertex and as value the value of its closeness
     */
    private Map<SimpleNode, Double> closenessCentrality(ExplorationGraph graph){
        Map<SimpleNode, Double> closeness = new HashMap<>();
        Set<SimpleNode> worthNodes = restrictedNodeSet(graph);

        //computing restricted graph distance matrix
        System.out.println("Matrix computation");
        this.graphDistanceMatrix = graphDistanceMatrix(graph);

        //closeness computation
        for(SimpleNode n : worthNodes){
            double avgDist = this.graphDistanceMatrix.get(n).values().stream().
                    mapToDouble(Double::doubleValue).average().orElse(0);
            closeness.put(n, 1/avgDist);
        }

        //sorting by value
        return closeness
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));
    }

    /**
     * Computes the betweenness of the graph provided in input. To compute this measure a two steps procedure is
     * performed. The first one consists in retrieving all the shortest paths between each couple of nodes in the graph.
     * For efficiency reasons, this are stored to be reused in further analysis. After all of these are obtained, for
     * each node i in the graph, two values are computed: one representing the number of shortest paths from node s to
     * node t which contain i as intermediate node; and the second one is the number of shortest paths from node s to
     * node t. Once they are found for each node, the betweenness of each node is calculated.
     *
     * @param graph the graph to analyze
     */
    Map<SimpleNode, Double> betweennessCentrality(ExplorationGraph graph){

        Map<SimpleNode, Double> betweenness = nodeBetweenness(graph,restrictedNodeSet(graph));

        //sorting by values
        return betweenness
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));
    }

    private Map<SimpleNode, Integer> retrieveSigmaS(ExplorationGraph graph, SimpleNode s){
        Map<SimpleNode, Integer> sigmaS = new HashMap<>();
        for(SimpleNode n: graph.getNodeMap().keySet()){
            if(!n.equals(s)){
                if (spCountMatrix.containsKey(s) && spCountMatrix.get(s).containsKey(n))
                    sigmaS.put(n, spCountMatrix.get(s).get(n));
                else sigmaS.put(n, spCountMatrix.get(n).get(s));
            }
        }
        if(sigmaS.containsValue(null))
            System.out.println("Null sigma found!");
        return sigmaS;
    }

    private Map<SimpleNode, Double> nodeBetweenness(ExplorationGraph graph, Set<SimpleNode> worthNodes){
        int sigma_st;
        double val;
        Map<SimpleNode, Integer> sigmaS, sigmaT;
        Map<SimpleNode, Double> betweenness = new HashMap<>();

        for(SimpleNode node : worthNodes)
            betweenness.put(node, 0.0);

        for(SimpleNode s : graph.getNodeMap().keySet()){
            sigmaS = retrieveSigmaS(graph, s);
            for(SimpleNode t : graph.getNodeMap().keySet().stream().filter(node -> !node.equals(s)).collect(Collectors.toList())){
                sigmaT = retrieveSigmaS(graph, t);
                sigma_st = sigmaS.get(t);
                for(SimpleNode v : worthNodes.stream().filter(node -> !node.equals(s) && !node.equals(t)).collect(Collectors.toList())){
                    val = betweenness.get(v);
                    if(shortestPathLength(s,t)>= shortestPathLength(s,v)+ shortestPathLength(v,t))
                        val += (sigmaS.get(v)*sigmaT.get(v))/sigma_st;
                    betweenness.put(v,val);
                }
            }
        }

        for(SimpleNode n : worthNodes)
            betweenness.put(n,betweenness.get(n)/2);
        return betweenness;
    }

    private double shortestPathLength(SimpleNode n1, SimpleNode n2){
        return (this.graphDistanceMatrix.containsKey(n1) &&
                this.graphDistanceMatrix.get(n1).containsKey(n2) ?
            this.graphDistanceMatrix.get(n1).get(n2) :
                this.graphDistanceMatrix.get(n2).get(n1));
    }

    /**
     * Computes the degree of each node the graph
     * @param graph the graph which degree to compute
     * @return a mapping from each node in the graph to its degree
     */
    private Map<SimpleNode, Integer> degree(ExplorationGraph graph){
        Map<SimpleNode, Integer> deg = new HashMap<>();
        for(SimpleNode n : graph.getNodeMap().keySet() ){
            deg.put(n,graph.getNode(n).getAdjacents().size());
        }
        //sorting by values
        return deg
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));
    }

    /**
     * Computes respectively the maximum, the minimum and the average degree of the nodes in the graph
     * @param map the mapping from each node to its degree
     * @return a list of three integers where the first one is the maximum degree, the second one is the minimum degree
     * and the third one is the average degree of the nodes in the map
     */
    private List<Integer> degMaxMinAvg(Map<SimpleNode, Integer> map){
        List<Integer> stats = new ArrayList<>();
        int min=100, max=0, avg=0, count=0;
        for(SimpleNode n: map.keySet()){
            count+=1;
            int d = map.get(n);
            if(d<min) min = d;
            if(d>max) max = d;
            avg += d;
        }
        stats.add(max);
        stats.add(min);
        stats.add(avg / count);
        return stats;
    }

    /**
     * Computes a new graph such that it is connected. It only deals with disconnected frontiers, no node can be
     * disconnected according to how graph is built. In particular, this is useful with intermediary visibility graphs
     * because they might have some frontiers not linked to any node.
     * @param graph the graph to transform
     * @return a new graph where each frontier is connected to at least a non-frontier node
     */
    private ExplorationGraph dropDisconnectedFrontiers(ExplorationGraph graph){
        ExplorationGraph connectedGraph = graph.copy();
        for(SimpleNode frontier : graph.getNodeMap().keySet().stream().
                filter(SimpleNode::isFrontier).collect(Collectors.toSet())){
            for(SimpleNode adjacentFrontier : graph.getNode(frontier).getAdjacents().stream().
                    filter(SimpleNode::isFrontier).collect(Collectors.toSet()))
                connectedGraph.removeEdge(frontier,adjacentFrontier);
            if(connectedGraph.getNode(frontier).getAdjacents().size()==0)
                connectedGraph.removeNode(frontier);
        }

        return connectedGraph;

    }

    void logStats(ExplorationGraph graph, String statsFile){
        File file;
        FileWriter fw;
        long time;

        System.out.println("Ensuring connectivity of the graph");
        graph = dropDisconnectedFrontiers(graph);
        System.out.println("Done");

        System.out.println("Degree computation");
        try {
            file = new File(statsFile);
            fw = new FileWriter(file, false);
            BufferedWriter bw = new BufferedWriter(fw);
            DecimalFormat df = new DecimalFormat("#.#####");

            bw.write("Total nodes: "+this.nodeCount(graph)+
                    "   Frontiers: "+this.frontierNodeCount(graph)+
                    "   Edges: "+this.edgeCount(graph));
            bw.newLine();

            time = System.currentTimeMillis();
            bw.write("Nodes degrees");
            Map<SimpleNode,Integer> degrees = this.degree(graph);
            for(SimpleNode node : degrees.keySet()){
                bw.newLine();
                bw.write("   "+node.toString()+"    "+degrees.get(node));
            }
            bw.newLine();
            List<Integer> maxMinAvg = this.degMaxMinAvg(degrees);
            bw.write("Max: "+maxMinAvg.get(0)+
                    "   Min: "+maxMinAvg.get(1)+
                    "   Avg: "+maxMinAvg.get(2));
            bw.newLine();
            time = System.currentTimeMillis() - time;
            bw.write("Degree computed in " + time + " ms");
            bw.newLine();
            bw.newLine();
            bw.flush();

            System.out.println("Closeness computation");
            time = System.currentTimeMillis();
            bw.write("Closeness centrality:");
            Map<SimpleNode,Double> centralities = this.closenessCentrality(graph);
            for(SimpleNode node: centralities.keySet()){
                bw.newLine();
                bw.write("   "+node.toString()+"    "+df.format(centralities.get(node)));
            }
            bw.newLine();
            time = System.currentTimeMillis() - time;
            bw.write("Closeness computed in " + time + " ms");
            bw.newLine();
            bw.newLine();
            bw.flush();


            System.out.println("Bet computation");
            time = System.currentTimeMillis();
            bw.write("Betweenness centrality:");
            centralities = this.betweennessCentrality(graph);
            for(SimpleNode node: centralities.keySet()){
                bw.newLine();
                bw.write("   "+node.toString()+"    "+df.format(centralities.get(node)));
            }
            time = System.currentTimeMillis() - time;
            bw.newLine();
            bw.write("Betweenness computed in " + time + " ms");
            bw.newLine();
            bw.flush();
            //this.writeAdjacencyMatrix(graph);

            bw.close();
            fw.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    //Useless methods up to now
    /*
    private Map<SimpleNode, Double> normalization(Map<SimpleNode,Double> centralityMeasure){
        //max and min computation
        double cMin = Double.MAX_VALUE, cMax=0;
        for(SimpleNode node: centralityMeasure.keySet()){
            if(centralityMeasure.get(node)<cMin)
                cMin = centralityMeasure.get(node);
            if(centralityMeasure.get(node)>cMax)
                cMax = centralityMeasure.get(node);
        }

        //normalization
        for(SimpleNode node: centralityMeasure.keySet()){
            double c = centralityMeasure.get(node);
            centralityMeasure.put(node, (c-cMin)/(cMax-cMin));
        }

        return centralityMeasure;
    }

    void writeAdjacencyMatrix(ExplorationGraph graph){
        List<SimpleNode> nodes = new LinkedList<>(graph.getNodeMap().keySet());
        int dim = nodes.size();
        int[][] adjMatrix = new int[dim][dim];
        for(int i=0;i<dim;i++)
            for(int j=0;j<dim;j++)
                adjMatrix[i][j] = 0;
        for(SimpleNode n : nodes){
            int idx1 = nodes.indexOf(n);
            adjMatrix[idx1][idx1]=0;
            int idx2;
            for(SimpleNode adj : graph.getNode(n).getAdjacents()){
                idx2 = nodes.indexOf(adj);
                adjMatrix[idx1][idx2] = 1;
                adjMatrix[idx2][idx1] = 1;
            }
        }

        FileWriter fw;
        String fileName = System.getProperty("user.dir")+"/logs/matrix.txt";

        try {
            File file;
            file = new File(fileName);
            fw = new FileWriter(file.getAbsoluteFile(), false);
            BufferedWriter bw = new BufferedWriter(fw);
            for(SimpleNode n: nodes)
                bw.write(n.toString());
            for(int i=0;i<dim;i++) {
                for (int j = 0; j < dim; j++) {
                    int n = adjMatrix[i][j];
                    bw.write(" "+n);

                    if (j != dim - 1)
                        bw.write(",");
                }
                bw.newLine();
            }
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
     */

}