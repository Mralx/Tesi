package exploration.graph;

import agents.RealAgent;
import agents.sets.ActiveSet;
import agents.sets.IdleSet;
import config.Constants;
import environment.Frontier;
import environment.OccupancyGrid;
import environment.TopologicalMap;
import exploration.SimulationFramework;
import path.TopologicalNode;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.Semaphore;

public class GraphHandler {

    private static GraphHandler handler;
    private static GraphStats stats;
    private static ExplorationGraph graph;
    private static OccupancyGrid environment;
    private static Semaphore semaphore;

    public static GraphHandler getInstance(){
        if (handler == null){
            handler = new GraphHandler();
            stats = new GraphStats();
            graph = new ExplorationGraph();
            environment = null;
            semaphore = new Semaphore(1);
        }
        return handler;
    }

    public static ExplorationGraph getGraph(){
        return graph;
    }

    public static int getGraphSize(){
        return graph.getNodeMap().size();
    }

    public static OccupancyGrid getEnvironment() {
        return environment;
    }

    public static Semaphore getSemaphore(){ return semaphore;}

    public static void setGraph(ExplorationGraph graph) {
        GraphHandler.graph = graph;
    }

    public static void setEnvironment(OccupancyGrid environment) {
        GraphHandler.environment = environment;
    }

    /**
     * Initializes the graph through the array of agents. It is assumed the array contains the communication station
     * as first agent and drops it before using the array
     *
     * @param agents the array of agents in the exploration process, the first one is dropped because it is assumed to
     *               be the communication station
     */
    public static void initializeGraph(RealAgent[] agents){
        GraphHandler.getInstance();
        RealAgent[] realAgents = new RealAgent[agents.length-1];
        System.arraycopy(agents, 1, realAgents, 0, agents.length - 1);
        graph.initialize(realAgents);
    }

    /**
     * Initializes the graph through the array of agents. It is assumed the array contains the communication station
     * as first agent and drops it before using the array
     *
     * @param agents the array of agents in the exploration process, the first one is dropped because it is assumed to
     *               be the communication station
     */
    public static void initializeVisGraph(RealAgent[] agents){
        GraphHandler.getInstance();
        RealAgent[] realAgents = new RealAgent[agents.length-1];
        System.arraycopy(agents, 1, realAgents, 0, agents.length - 1);
        graph = new VisibilityGraph();
        graph.initialize(realAgents);
    }

    public static void updateFrontiers(List<Frontier> frontiers, Integer time) {
        List<SimpleNode> frontierNodes = new LinkedList<>();
        List<SimpleNode> oldFrontierNodes = new LinkedList<>();

        for(Frontier f : frontiers){
            SimpleNode simpleNode = new SimpleNode(f.getCentre().x, f.getCentre().y);
            simpleNode.setFrontier(true);
            frontierNodes.add(simpleNode);
        }

        for(SimpleNode n : graph.getNodeMap().keySet())
            if(n.isFrontier())
                oldFrontierNodes.add(new SimpleNode(n));

        if(oldFrontierNodes.size()>0)
            for(SimpleNode n : oldFrontierNodes)
                if (!frontierNodes.contains(n)) graph.removeNode(n);

        for(SimpleNode n : frontierNodes)
            graph.addFrontierNode(n,time);

    }

    private static void discretize(){
        for(int i=0;i<environment.width;i=i + Constants.DISCRETIZATION_STEP)
            for (int j = 0; j < environment.height; j=j + Constants.DISCRETIZATION_STEP)
                if (environment.freeSpaceAt(i, j) && !environment.obstacleWithinDistance(i,j,Constants.MIN_DISTANCE))
                    GraphHandler.graph.addNode(new SimpleNode(i, j), null, 0);
    }

    static void test(int n){
        String statsFile = System.getProperty("user.dir") + "/logs/Discretization/Topo/" + n + " stats test.txt";
        GraphHandler.getInstance();
        System.out.println("Computing occupancy grid");
        GraphHandler.setEnvironment(computeOccupancyGrid(n));
        System.out.println("Occupancy grid computed");
        TopologicalMap tMap = new TopologicalMap(environment);
        tMap.generateSkeleton();
        tMap.findKeyPoints();
        tMap.generateKeyAreas();
        createTopologicalGraph(null, tMap);
        System.out.println("Topological graph created");
        GraphHandler.stats.logStats(GraphHandler.graph,statsFile);
    }

    private static OccupancyGrid computeOccupancyGrid(int env){
        OccupancyGrid envGrid = new OccupancyGrid(800,600);
        BufferedImage bi;
        try{
            bi = ImageIO.read(new File(System.getProperty("user.dir") + "/environments/Tesi/env_"+env+".png"));
            for (int i = 0; i < 800; i++) {
                for (int j = 0; j < 600; j++) {
                    if (bi.getRGB(i,j)==Color.BLACK.getRGB()) envGrid.setObstacleAt(i,j);
                    else envGrid.setFreeSpaceAt(i,j);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return envGrid;
    }

    public static void updateNodes(Set<RealAgent> agents ){
        for(RealAgent agent : agents){
            SimpleNode node = new SimpleNode(agent.getX(), agent.getY());
            if(!graph.nearAnotherNode(node))
                graph.addNode(node, agent.getName(), agent.getTimeElapsed());
        }
        if(!GraphHandler.graphCorrectness()){
            GraphHandler.graphUncorrectnessPrint();
            System.exit(-3);
        }
    }

    public static void createTopologicalGraph(RealAgent agent, TopologicalMap tMap){
        Map<SimpleNode, Node> graphMap = new LinkedHashMap<>();
        SimpleNode simpleNode, adjNode;
        Node node;

        for(TopologicalNode tNode : tMap.getTopologicalNodes().values()){
            simpleNode = new SimpleNode(tNode.getPosition().x,tNode.getPosition().y);
            if(simpleNode.x>-1 && simpleNode.y>-1){
                node = new Node(tNode.getPosition().x,tNode.getPosition().y);
                for(TopologicalNode adjTopNode : tNode.getListOfNeighbours()){
                    adjNode = new SimpleNode(adjTopNode.getPosition().x,adjTopNode.getPosition().y);
                    if(adjNode.x>-1 && adjNode.y>-1){
                        //double distance = agent.calculatePath(tNode.getPosition(),adjTopNode.getPosition()).getLength(); //TODO sistemare post testing
                        SimpleNode n1 = new SimpleNode(tNode.getPosition().x,tNode.getPosition().y);
                        SimpleNode n2 = new SimpleNode(adjTopNode.getPosition().x,adjTopNode.getPosition().y);
                        double distance = graph.euclideanDistance(n1,n2);
                        node.addAdjacent(adjNode,distance);
                    }
                }
                graphMap.put(simpleNode,node);
            }
        }
        graph = new ExplorationGraph();
        graph.setNodeMap(graphMap);
    }

    private static void refreshStats(){
       if(graph.isDirty())
            stats.computeStats(graph);
        graph.setDirty(false);
    }

    public static Map<SimpleNode, Double>  highestCNodes(){
        refreshStats();
        return stats.getHighestClosenessNodes();
    }

    public static Map<SimpleNode, Double> highestBNodes(){
        refreshStats();
        return stats.getHighestBetweennessNodes();
    }

    private static boolean graphCorrectness(){
        for(SimpleNode n : graph.getNodeMap().keySet()) {
            if (!n.equals(graph.getNode(n)))
                return false;
            if (graph.getNode(n).getAdjacents().isEmpty() && !n.isFrontier() && !IdleSet.getInstance().getPool().isEmpty())
                return false;
        }
        return true;
    }

    private static void graphUncorrectnessPrint(){
        for(SimpleNode n : graph.getNodeMap().keySet()) {
            if (!n.equals(graph.getNode(n)))
                System.out.println("Mismatched "+n.isFrontier()+" node "+n.toString()+"; graph node "+graph.getNode(n).toString());
            if (graph.getNode(n).getAdjacents().isEmpty() && !n.isFrontier()) {
                System.out.println("Empty adjacent list node " + graph.getNode(n));
                for(int i=-200;i<200;i++)
                    for(int j=-200;j<200;j++)
                        if(graph.getNodeMap().containsKey(new SimpleNode(n.x+i,n.y+j)) && (i!=0 || j!=0)){
                            Node node = graph.getNode(new SimpleNode(n.x+i,n.y+j));
                            System.out.println("Near node [" +node.x+","+node.y+"]");
                        }
            }
        }
    }
}

