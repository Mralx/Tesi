package exploration.graph;

import environment.OccupancyGrid;

public class GraphHandler {

    private static GraphHandler handler;
    private static ExplorationGraph graph;
    private static OccupancyGrid environment;

    public static GraphHandler getInstance(){
        if (handler == null){
            handler = new GraphHandler();
            graph = new ExplorationGraph();
            environment = null;
        }
        return handler;
    }

    public static ExplorationGraph getGraph(){
        return graph;
    }

    public static OccupancyGrid getEnvironment() {
        return environment;
    }

    public static void setEnvironment(OccupancyGrid environment) {
        GraphHandler.environment = environment;
    }
}

