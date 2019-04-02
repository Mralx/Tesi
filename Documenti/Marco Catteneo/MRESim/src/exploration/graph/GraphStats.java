package exploration.graph;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class GraphStats {

    //vecchia funzione per calcolare il grado utilizzando il file
    static void f_degree(String src_file, String dest_file){

        int min_d=100, max_d=0, d=0;
        double avg_d=0;
        ArrayList<Integer> degrees = new ArrayList<>();
        File file;
        FileWriter fw;
        BufferedWriter bw;
        BufferedReader br;

        try{
            file = new File(dest_file);
            fw =  new FileWriter(file.getAbsoluteFile(),true);
            br = new BufferedReader(new FileReader(src_file));
            bw = new BufferedWriter(fw);

            String line= br.readLine();
            while(line!=null){
                line = line.substring(line.indexOf('f'),line.indexOf('d'));
                line = line.substring(line.indexOf('('),line.lastIndexOf(")"));
                while(line.contains("(")){
                    d+=1;
                    line = line.substring(line.indexOf("(")+1);
                }
                //list of degrees at each time elapsed
                degrees.add(d);
                //average degree through incremental mean
                avg_d = (d+degrees.size()*avg_d)/(degrees.size()+1);
                //update min and max
                if(d<min_d)
                    min_d = d;
                if(d>max_d)
                    max_d = d;

                d = 0;
                line = br.readLine();
            }

            bw.write("Degree stats: total count = "+degrees.size()+
                    ", min = "+min_d+
                    ", max = "+max_d+
                    ", average = "+avg_d);
            bw.newLine();
            bw.write(degrees.toString());
            bw.newLine();
            bw.close();
            fw.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Computes the number of nodes of the graph, including frontier nodes
     * @param graph the graph to analyze
     * @return the number of nodes of the graph
     */
    static int nodeCount(ExplorationGraph graph){
        return graph.getNodeMap().size();
    }

    /**
     * Computes the number of frontier nodes of the graph
     * @param graph the graph to analyze
     * @return the number of nodes of the graph
     */
    static int frontierNodeCount(ExplorationGraph graph){
        return (int) graph.getNodeMap().keySet().stream().
                filter(SimpleNode::isFrontier).count();
    }

    /**
     * Computes the number of edges of the graph
     * @param graph the graph to analyze
     * @return the number of edges of the graph
     */
    static int edgeCount(ExplorationGraph graph){
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
    static Map<SimpleNode, Map<SimpleNode,Double>> graphDistanceMatrix(ExplorationGraph graph){
        int dim = (int) (GraphStats.nodeCount(graph)*1.3);
        Map<SimpleNode, Map<SimpleNode, Double>> matrix = new HashMap<>(dim);
        for(SimpleNode n: graph.getNodeMap().keySet())
            matrix.put(n,computeRow(graph, n, dim));
        return matrix;
    }

    /**
     * Starting from a node, stocks the distances from this node to every other node in the graph into a map and returns
     * it
     * @param graph the graph to analyze
     * @param node the starting node
     * @param dim the dimension of the map
     * @return a map containing as key the nodes of the graph and as values the distances from each node to the one in
     * input
     */
    private static Map<SimpleNode, Double> computeRow(ExplorationGraph graph, SimpleNode node, int dim){
        Map<SimpleNode, Double> row = new HashMap<>(dim);
        for(SimpleNode n : graph.getNodeMap().keySet()){
            row.put(node, graph.distanceNodes(node,n));
        }
        return row;
    }

    /**
     * Computes the degree of each node the graph
     * @param graph the graph which degree to compute
     * @return a mapping from each node in the graph to its degree
     */
    static Map<SimpleNode, Integer> degree(ExplorationGraph graph){
        Map<SimpleNode, Integer> deg = new HashMap<>();
        for(SimpleNode n : graph.getNodeMap().keySet() ){
            deg.put(n,graph.getNode(n).getAdjacents().size());
        }
        return deg;
    }

    /**
     * Computes respectively the maximum, the minimum and the average degree of the nodes in the graph
     * @param map the mapping from each node to its degree
     * @return a list of three integers where the first one is the maximum degree, the second one is the minimum degree
     * and the third one is the average degree of the nodes in the map
     */
    static List<Integer> degMaxMinAvg(Map<SimpleNode, Integer> map){
        List<Integer> stats = new ArrayList<>();
        int min=0, max=100, avg=0, count=0;
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

    /**
     * Computes the distance between two nodes in the graph
     * @param graph
     * @param n1
     * @param n2
     * @return
     */
    static double distance(ExplorationGraph graph, SimpleNode n1, SimpleNode n2) {
        return graph.distanceNodes(n1, n2);
    }

}
