package exploration.graph;

import environment.OccupancyGrid;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class VisibilityGraph extends ExplorationGraph {

    //il range dei sensori è 200, scritto da qualche parte nelle configurazioni. ottenibile runtime da ogni agente
    private int sensingRange = 200;
    private double margin = 1.02; //margine giusto per non essere precisi con il range dei sensori, potrebbe essere inutile

    /**
     * Converts the graph in input into a visibility graph. The occupancy grid of the environment has to be provided
     * to do this transformation
     * @param graph the graph to transform
     * @param environment the OccupancyGrid of the environment of referral
     * @return the visibility graph having as nodes, the nodes of the graph in input and the edges are computed
     * according to the relation of visibility in the environment provided
     */
    VisibilityGraph transform(ExplorationGraph graph, OccupancyGrid environment){

        VisibilityGraph visibilityGraph = new VisibilityGraph();
        visibilityGraph.setNodeMap(graph.getNodeMap());
        visibilityGraph.connect(environment);
        return visibilityGraph;
    }

    VisibilityGraph mergeGraphs(List<ExplorationGraph> graphs, OccupancyGrid environment){

        VisibilityGraph visGraph = new VisibilityGraph();
        Map<SimpleNode, Node> cumulativeMap = new LinkedHashMap<>();

        for(ExplorationGraph graph : graphs){
            cumulativeMap.putAll(graph.getNodeMap());
        }

        visGraph.setNodeMap(cumulativeMap);
        visGraph.connect(environment);

        return visGraph;
    }

    //TODO il numero di ostacoli dovrebbe essere parametrico
    private void connect(OccupancyGrid environment){
        List<SimpleNode> nodes = new LinkedList<>(nodeMap.keySet());
        for(int i=0;i<nodes.size()-1;i++){
            for(int j=i+1;j<nodes.size();j++){
                Node node1 = this.nodeMap.get(nodes.get(i));
                Node node2 = this.nodeMap.get(nodes.get(j));
                double distance = euclideanDistance(node1,node2);
                if(distance<=sensingRange*margin &&
                    environment.numObstaclesOnLine(node1.x,node1.y,node2.x, node2.y)<3){
                    node1.addAdjacent(new SimpleNode(node2),distance);
                    node2.addAdjacent(new SimpleNode(node1),distance);
                }
                else
                    removeEdge(node1,node2);
            }
        }
    }

}
