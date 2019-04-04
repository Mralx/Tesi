package exploration.graph;

import java.util.*;

class Builder {

    private ExplorationGraph graph;
    private Map<SimpleNode, Node> frontierMap;
    private List<Node> lastAddedFrontiers;

    Builder() {
        this.graph = new ExplorationGraph();
        this.frontierMap = new HashMap<>();
        this.lastAddedFrontiers = new LinkedList<>();
    }

    /**
     * Provides the built graph
     * @return the graph
     */
    public ExplorationGraph getGraph() {
        addFrontiers();
        return graph;
    }

    /**
     * Returns the graph after having removed all the non nearest adjacent nodes from frontiers adjacency list
     * @return the shortest path graph
     */
    public ExplorationGraph getShortestPathGraph() {
        addFrontiers();
        graph.cleanFrontiers();
        return graph;
    }

    void parseLine(String line){
        if(line==null)
            return;
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

        int statingIdx = line.indexOf('l')+2;
        int endingIdx = line.indexOf(']');
        line = line.substring(statingIdx,endingIdx);

        int x = Integer.parseInt(line.substring(0,line.indexOf(',')));
        int y = Integer.parseInt(line.substring(line.indexOf(',')+1));
        return new Node(x,y);
    }
}
