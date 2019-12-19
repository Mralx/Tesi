package exploration.graph;

import jdk.dynalink.NamedOperation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Testing {

    public static void main(String[] args){

        testGraph();
    }

    private static void testGraph(){
        SimpleNode n1, n2, n3, n4, n5, n6;
        n1 = new SimpleNode(1,1);
        n2 = new SimpleNode(3,1);
        n3 = new SimpleNode(5,1);
        n4 = new SimpleNode(5,3);
        n5 = new SimpleNode(3,3);
        n6 = new SimpleNode(4,2);

        Node node1, node2, node3, node4, node5, node6;
        node1 = new Node(n1.x, n1.y);
        node2 = new Node(n2.x, n2.y);
        node3 = new Node(n3.x, n3.y);
        node4 = new Node(n4.x, n4.y);
        node5 = new Node(n5.x, n5.y);
        node6 = new Node(n6.x, n6.y);

        node1.addAdjacent(n2,2);
        node2.addAdjacent(n1,2);
        node2.addAdjacent(n3,2);
        //node2.addAdjacent(n4,2.828427125);
        node3.addAdjacent(n2,2);
        node3.addAdjacent(n4,2);
        node4.addAdjacent(n3,2);
        //node4.addAdjacent(n2,2.828427125);

        Map<SimpleNode, Node> nodeMap = new HashMap<>();
        nodeMap.put(n1,node1);
        nodeMap.put(n2,node2);
        nodeMap.put(n3,node3);
        nodeMap.put(n4,node4);

        GraphHandler.getInstance();
        VisibilityGraph graph = new VisibilityGraph();
        graph.setNodeMap(nodeMap);
        GraphHandler.setGraph(graph);

        Map<SimpleNode, Map<SimpleNode, List<SimpleNode>>> childMap;
        Map<SimpleNode, Map<SimpleNode, Double>> distanceMatrix = new HashMap<>();
        Map<SimpleNode, Map<SimpleNode, Integer>> spMatrix = new HashMap<>();

        childMap = graph.allPairsShortestPaths(distanceMatrix, spMatrix);
        for(SimpleNode n : childMap.keySet()){
            System.out.println("Node ["+n.x+","+n.y+"] -> "+childMap.get(n).toString());
            //System.out.println("Node ["+n.x+","+n.y+"] -> "+distanceMatrix.get(n).toString());
            System.out.println("Node ["+n.x+","+n.y+"] -> "+spMatrix.get(n).toString());
        }
        System.out.println("\n\n");

        node5.addAdjacent(n2,2);
        node5.addAdjacent(n4,2);
        node2.addAdjacent(n5,2);
        node4.addAdjacent(n5,2);
        nodeMap.put(n2,node2);
        nodeMap.put(n4,node4);
        nodeMap.put(n5,node5);
        graph.setNodeMap(nodeMap);
        GraphHandler.setGraph(graph);
        List<SimpleNode> newNodes = new LinkedList<>();
        newNodes.add(n5);
        graph.incrementalFW(childMap,distanceMatrix,spMatrix,newNodes);
        //TODO rivedere definizione childMap, poi correggere gli errori riguardo spcounts e i risultati non sono coerenti
        //TODO tra i due metodi

        //childMap = graph.allPairsShortestPaths(distanceMatrix,spMatrix);
        for(SimpleNode n : childMap.keySet()){
            System.out.println("Node ["+n.x+","+n.y+"] -> "+childMap.get(n).toString());
            //System.out.println("Node ["+n.x+","+n.y+"] -> "+distanceMatrix.get(n).toString());
            System.out.println("Node ["+n.x+","+n.y+"] -> "+spMatrix.get(n).toString());
        }

    }
}
