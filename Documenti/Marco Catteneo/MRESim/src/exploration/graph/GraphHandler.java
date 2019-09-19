package exploration.graph;

import java.awt.*;
import java.util.LinkedList;
import java.util.Map;

public class GraphHandler {

    private static GraphHandler handler;
    private static ExplorationGraph graph;

    public static GraphHandler getInstance(){
        if (handler == null){
            handler = new GraphHandler();
            graph = new ExplorationGraph();
        }
        return handler;
    }

    public static ExplorationGraph getGraph(){
        return graph;
    }

    public static void updateLocations(LinkedList<Point> locations){
        graph.addNodesList(locations);
    }

    public static void updateFrontiers(LinkedList<Point> frontiers){
        graph.addFrontiersList(frontiers);
    }


}

