package exploration.graph;

import javafx.collections.transformation.SortedList;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

class GraphStats {

    HashMap<SimpleNode, HashMap<SimpleNode, List<GraphPath>>> spgMatrix;
    String statsFile;

    GraphStats() {
        this.spgMatrix = new HashMap<>();
        this.statsFile = null;
    }

    void setStatsFile(String statsFile) {
        this.statsFile = statsFile;
    }

    /**
     * Computes the number of nodes of the graph, including frontier nodes
     * @param graph the graph to analyze
     * @return the number of nodes of the graph
     */
    int nodeCount(ExplorationGraph graph){
        return graph.getNodeMap().size();
    }

    /**
     * Computes the number of frontier nodes of the graph
     * @param graph the graph to analyze
     * @return the number of nodes of the graph
     */
    int frontierNodeCount(ExplorationGraph graph){
        return (int) graph.getNodeMap().keySet().stream().
                filter(SimpleNode::isFrontier).count();
    }

    /**
     * Computes the number of edges of the graph
     * @param graph the graph to analyze
     * @return the number of edges of the graph
     */
    int edgeCount(ExplorationGraph graph){
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
    Map<SimpleNode, Map<SimpleNode,Double>> graphDistanceMatrix(ExplorationGraph graph, Set<SimpleNode> restrictedNodeSet){
        if(spgMatrix.size()==0)
            fillMatrix(graph);
        Map<SimpleNode, Map<SimpleNode, Double>> matrix = new HashMap<>();
        for(SimpleNode n: restrictedNodeSet)
            matrix.put(n,computeRow(graph, n));
        return matrix;
    }

    /**
     * Starting from a node, stocks the distances from this node to every other node in the graph into a map and returns
     * it
     * @param graph the graph to analyze
     * @param node the starting node
     * @return a map containing as key the nodes of the graph and as values the distances from each node to the one in
     * input
     */
    private Map<SimpleNode, Double> computeRow(ExplorationGraph graph, SimpleNode node){
        Map<SimpleNode, Double> row = new HashMap<>();
        Set<SimpleNode> nodes = new HashSet<>(graph.getNodeMap().keySet());
        nodes.remove(node);
        for(SimpleNode n : nodes){
            List<GraphPath> paths;
            double distance;
            if(spgMatrix.containsKey(node) && spgMatrix.get(node).containsKey(n))
                paths = spgMatrix.get(node).get(n);
            else
                paths = spgMatrix.get(n).get(node);

            if(paths==null)
                System.exit(2);
            distance = paths.get(0).getLength();
            row.put(n, distance);
        }
        return row;
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
    Map<SimpleNode, Double> closenessCentrality(ExplorationGraph graph){
        Map<SimpleNode, Double> closeness = new HashMap<>();
        Set<SimpleNode> worthNodes = restrictedNodeSet(graph);

        //computing restricted graph distance matrix
        System.out.println("Matrix computation");
        Map<SimpleNode, Map<SimpleNode, Double>> distanceMatrix = graphDistanceMatrix(graph,restrictedNodeSet(graph));

        //closeness computation
        for(SimpleNode n : worthNodes){
            double avgDist = distanceMatrix.get(n).values().stream().
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
     * For efficiency reasons, this are stored to be reused in further analysis. After all of this are obtained, for
     * each node i in the graph, two values are computed: one representing the number of shortest paths from node s to
     * node t which contain i as intermediate node; and the second one is the number of shortest paths from node s to
     * node t. Once they are found for each node, the betweenness of each node is calculated.
     *
     * @param graph the graph to analyze
     */
    Map<SimpleNode, Double> betweennessCentrality(ExplorationGraph graph){
        if(spgMatrix.size()==0)
            fillMatrix(graph);

        Map<SimpleNode, Double> betweenness = new HashMap<>();
        for(SimpleNode n: restrictedNodeSet(graph)) {
            double b = computeNodeBetweenness(n);
            betweenness.put(n, b);
        }

        //sorting by values
        return betweenness
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));
    }

    /**
     * Computes the betweenness of the node provided in input.
     *
     * @param entryNode the node whose betweenness is looked for
     * @return the betweenness of the node
     */
    private double computeNodeBetweenness(SimpleNode entryNode){

        double betweenness = (double) 0;
        double occurrences = (double) 0;
        double diffPaths;
        for(SimpleNode n : spgMatrix.keySet().stream().filter(node-> node!=entryNode).collect(Collectors.toList())){
            for (SimpleNode m : spgMatrix.get(n).keySet().stream().filter(node -> node != entryNode).collect(Collectors.toList())) {
                List<GraphPath> paths = spgMatrix.get(n).get(m);
                for (GraphPath p : paths) {
                    if (p.contains(entryNode)) occurrences += 1;
                }
                diffPaths = paths.size();
                if(diffPaths!=0)
                    betweenness += occurrences / diffPaths;
                occurrences = 0;
            }
        }
        return betweenness;
    }

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

    private void fillMatrix(ExplorationGraph graph){
        long time = System.currentTimeMillis();
        List<SimpleNode> nodes = new ArrayList<>(graph.getNodeMap().keySet());
        SimpleNode starter, arrival;

        for(int i=0; i<nodes.size()-1;i++){
            HashMap<SimpleNode, List<GraphPath>> subMatrix = new HashMap<>();
            starter = nodes.get(i);
            //System.out.println("Starting sub matrix computation for "+starter.toString());
            for(int j=i+1; j<nodes.size(); j++){
                arrival = nodes.get(j);
                //double percentage = (i*(nodes.size()-1)+j)/((nodes.size()-1)*(nodes.size()-1))*100;
                //System.out.println("Completed "+percentage+"%");
                List<GraphPath> paths = graph.getMultiplePaths(starter,arrival);
                subMatrix.put(arrival, paths);
            }
            //System.out.println("Retrieved sub matrix for "+starter.toString());
            this.spgMatrix.put(starter,subMatrix);
        }
        time = System.currentTimeMillis() - time;
        System.out.println("Fill matrix took "+time+" ms to be computed");
    }

    /**
     * Computes the degree of each node the graph
     * @param graph the graph which degree to compute
     * @return a mapping from each node in the graph to its degree
     */
    Map<SimpleNode, Integer> degree(ExplorationGraph graph){
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
    List<Integer> degMaxMinAvg(Map<SimpleNode, Integer> map){
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
        stats.add(avg/count);
        return stats;
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

    void logStats(ExplorationGraph graph, String statsFile){
        File file;
        FileWriter fw;
        long time;
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
            //this.writeAdjacencyMatrix(graph);

            bw.close();
            fw.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private void log(String string){
        FileWriter fw;

        try {
            File file;
            file = new File(this.statsFile);
            fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(string);
            bw.newLine();
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
