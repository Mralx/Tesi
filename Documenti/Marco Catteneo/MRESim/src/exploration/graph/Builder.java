package exploration.graph;

import environment.OccupancyGrid;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

class Builder {

    private List<ExplorationGraph> graphs;
    private ExplorationGraph graph;
    private Map<SimpleNode, Node> frontierMap;
    private List<Node> lastAddedFrontiers;
    private OccupancyGrid environment;

    Builder() {
        this.graphs = new LinkedList<>();
        this.graph = new ExplorationGraph();
        this.frontierMap = new HashMap<>();
        this.lastAddedFrontiers = new LinkedList<>();
        this.environment = null;
    }

    /**
     * Provides the built graph
     * @return the graph
     */
    public ExplorationGraph getGraph() {
        addFrontiers();
        this.graphs.add(graph);
        ExplorationGraph result = graph.copy();
        this.reset();
        return result;
    }

    ExplorationGraph getVisibilityGraph(){
        addFrontiers();
        this.graphs.add(graph);
        VisibilityGraph result = (new VisibilityGraph()).transform(graph, environment);
        this.reset();
        return result;
    }

    VisibilityGraph getMergedGraph(){
        VisibilityGraph visibilityGraph = new VisibilityGraph();
        return visibilityGraph.mergeGraphs(graphs,environment);
    }

    private void reset(){
        this.graph = new ExplorationGraph();
        this.frontierMap = new HashMap<>();
        this.lastAddedFrontiers = new LinkedList<>();
    }

    private void computeOccupancyGrid(int env){
        OccupancyGrid envGrid = new OccupancyGrid(800,600);
        BufferedImage bi;
        try{
            System.out.println(System.getProperty("user.dir") + "/environments/Tesi/env_"+env+".png");
            bi = ImageIO.read(new File(System.getProperty("user.dir") + "/environments/Tesi/env_"+env+".png"));
            for (int i = 0; i < 800; i++) {
                for (int j = 0; j < 600; j++) {
                    if (bi.getRGB(i,j)==Color.WHITE.getRGB()) envGrid.setFreeSpaceAt(i,j);
                    else envGrid.setObstacleAt(i,j);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        this.environment = envGrid;
    }

    /**
     * Returns the graph after having removed all the non nearest adjacent nodes from frontiers adjacency list
     * @return the shortest path graph
     */
    ExplorationGraph getShortestPathGraph() {
        addFrontiers();
        graph.shortestPathFrontiers();
        this.graphs.add(graph);
        ExplorationGraph result = graph.copy();
        this.reset();
        return result;
    }

    void parseLine(String line){
        if(line==null)
            return;
        if(this.environment==null)
            computeOccupancyGrid(Integer.parseInt(line.substring(0,1)));
        Node node = getLocation(line);
        parseFrontiers(line,node);
        this.graph.addNode(node);
    }

    private void parseFrontiers(String line, Node node){
        this.lastAddedFrontiers.clear();

        line = line.substring(line.indexOf('f'));
        String fronts = line.substring(0,line.indexOf(']'));
        String dists = line.substring(line.indexOf('d')+2);

        while(fronts.contains("(")){
            fronts = fronts.substring(fronts.indexOf('('));

            int x = Integer.parseInt(fronts.substring(1,fronts.indexOf(',')));
            int y = Integer.parseInt(fronts.substring(fronts.indexOf(',')+1,fronts.indexOf(')')));
            double distance;

            if(dists.contains(","))
                distance = Double.parseDouble(dists.substring(0,dists.indexOf(',')));
            else
                distance = Double.parseDouble(dists.substring(0,dists.length()-1));

            Node frontier = new Node(x,y);
            frontier.setFrontier(true);
            SimpleNode frontCoordinates = new SimpleNode(frontier);
            //node.addAdjacent(frontCoordinates, distance);

            if(frontierMap.containsKey(frontCoordinates)){
                frontierMap.get(frontCoordinates).addAdjacent(new SimpleNode(node), distance);
            }
            else {
                frontier.addAdjacent(new SimpleNode(node), distance);
                frontierMap.put(frontCoordinates, frontier);
            }

            lastAddedFrontiers.add(frontierMap.get(frontCoordinates));
            fronts = fronts.substring(fronts.indexOf(')'));
            dists = dists.substring(dists.indexOf(',')+1);
        }
    }

    /**
     * Adds each frontier in the map to the graph
     */
    private void addFrontiers(){
        for(SimpleNode n : lastAddedFrontiers)
            this.graph.addFrontierNode(frontierMap.get(n));
    }

    //used to create a Node object given a file where the first element in square brackets is the coordinates of the
    // point to return
    private Node getLocation (String line){

        int startingIdx = line.indexOf('l')+2;
        int endingIdx = line.indexOf(']');
        line = line.substring(startingIdx,endingIdx);

        int x = Integer.parseInt(line.substring(0,line.indexOf(',')));
        int y = Integer.parseInt(line.substring(line.indexOf(',')+1));
        return new Node(x,y);
    }
}
