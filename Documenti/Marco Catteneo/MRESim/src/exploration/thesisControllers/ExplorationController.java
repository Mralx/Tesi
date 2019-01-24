package exploration.thesisControllers;

import agents.sets.ActiveSet;
import agents.sets.IdleSet;
import agents.RealAgent;
import config.Constants;
import environment.ContourTracer;
import environment.Environment;
import environment.Frontier;
import exploration.RandomWalk;
import exploration.SimulationFramework;
import path.Path;

import java.awt.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by marco on 05/04/2017.
 */
public class ExplorationController {

    // <editor-fold defaultstate="collapsed" desc="Variables">
    public static boolean starterSelected = false;

    public static ConcurrentHashMap<Integer, Set<Point>> frontiers = new ConcurrentHashMap<>();


    // </editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Calculate and set agent's frontiers">

    public static LinkedList<Frontier> calculateFrontiers(RealAgent agent,Environment env) {

        // If recalculating frontiers, must set old frontiers dirty for image rendering
        for(Frontier f : agent.getFrontiers())
            agent.addDirtyCells(f.getPolygonOutline());

        LinkedList <LinkedList> contours = ContourTracer.findAllContours(agent.getOccupancyGrid());
        PriorityQueue<Frontier> frontiers = new PriorityQueue();
        Frontier currFrontier;

        for(LinkedList<Point> currContour : contours) {
            currFrontier = new Frontier(agent.getX(), agent.getY(), currContour);

            if (!agent.isBadFrontier(currFrontier)) {
                if(currFrontier.getArea() >= Constants.MIN_FRONTIER_SIZE){
                    {
                        frontiers.add(currFrontier);
                    }
                }
            }
        }
        agent.setFrontiers(frontiers);

        return filterCleanFrontiers(agent,env);
    }

    // <editor-fold defaultstate="collapsed" desc="Filter clean frontiers">

    public static LinkedList<Frontier> filterCleanFrontiers(RealAgent agent, Environment env) {
        PriorityQueue<Frontier> frontiers = agent.getFrontiers();
        LinkedList<Frontier> clean = new LinkedList<>();
        for(Frontier f: frontiers){
            if(!closeToObstacle(f.getCentre(),env)) {
                clean.add(f);
            }
        }

        return clean;
    }

    public static boolean closeToObstacle(Point p,Environment env){
        int x = (int)p.getX();
        int y = (int)p.getY();
        int margin = 2;

        if(env.obstacleAt(x,y+margin) || env.obstacleAt(x,y-margin) || env.obstacleAt(x+margin,y) || env.obstacleAt(x-margin,y)){
            return true;
        }
        return false;
    }

    // </editor-fold>

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Calculate agent's team positioning">

    public static LinkedList<Point> calculateTeamPositioning(){
        IdleSet iSet = IdleSet.getInstance();
        ActiveSet aSet = ActiveSet.getInstance();

        LinkedList<Point> positioning = new LinkedList<>();
        for(RealAgent a: iSet.getPool()){
            positioning.add(a.getLocation());
        }
        for(RealAgent a: aSet.getActive()){
            positioning.add(a.getLocation());
        }

        return positioning;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Calculate agents' goals">

    public static LinkedList<Point> calculateTeamGoals(){
        LinkedList<RealAgent> pool = IdleSet.getInstance().getPool();
        LinkedList<RealAgent> active = ActiveSet.getInstance().getActive();

        LinkedList<Point> goals = new LinkedList<>();
        for(RealAgent a: pool){
            goals.add(a.getCurrentGoal());
        }
        for(RealAgent a: active){
            goals.add(a.getCurrentGoal());
        }

        return goals;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Set starting agent">

    public static class AgentFrontierPair {

        private RealAgent agent;
        private Frontier frontier;
        private double distance;

        public AgentFrontierPair(RealAgent a,Frontier f){
            agent = a;
            frontier = f;
            distance = agent.getLocation().distance(frontier.getCentre());
        }

        public RealAgent getAgent(){ return agent; }

        public Frontier getFrontier(){ return frontier; }

        public double getDistance(){ return distance; }

        public void setAgent(RealAgent a){ agent = a; }

        public void setFrontier(Frontier f){ frontier = f; }

        public void setDistance(double d){ distance = d; }

    }

    public static void setStartingAgent(RealAgent agent,Environment env){
        ExplorationController.calculateFrontiers(agent,env);
        PriorityQueue<Frontier> frontiers = agent.getFrontiers();
        LinkedList<RealAgent> team = IdleSet.getInstance().getPool();

        LinkedList<AgentFrontierPair> pairs = new LinkedList<>();
        for(Frontier f: frontiers) {
            Point currCenter = f.getCentre();
            try {
                for (RealAgent a : team) {
                    Point currLoc = a.getLocation();
                    double currDist = currLoc.distance(currCenter);
                    pairs.add(new AgentFrontierPair(a, f));
                }
            }
            catch (NullPointerException e){
                e.printStackTrace();
            }
        }

        AgentFrontierPair currPair = pairs.get(0);
        double currMin = currPair.getDistance();
        for(int i=1;i<pairs.size();i++){
            if(pairs.get(i).getDistance() < currMin){
                currMin = pairs.get(i).getDistance();
                currPair = pairs.get(i);
            }
        }

        if(!starterSelected) {
            currPair.getAgent().setStarter(true);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="MoveAgent">
    public static Point moveAgent(RealAgent agent,Frontier f){
        Point nextStep;
        Path goal = agent.calculatePath(agent.getLocation(),f.getCentre());
        agent.setPath(goal);

        // <editor-fold defaultstate="collapsed" desc="Handle path errors">
        if (agent.getPath() == null
                || agent.getPath().getPoints() == null
                || agent.getPath().getPoints().isEmpty()) {
            SimulationFramework.log("Path problems","errConsole");
            LinkedList<Point> outline = f.getPolygonOutline();
            boolean found = false;

            for(Point p : outline){
                SimulationFramework.log("Out. point: "+p,"errConsole");
                Path curr = agent.calculatePath(agent.getLocation(),p);
                agent.setPath(curr);
                if (!(agent.getPath() == null)
                        && !(agent.getPath().getPoints() == null)
                        && !agent.getPath().getPoints().isEmpty()){
                    found = true;
                }
            }

            if(!found) {
                nextStep = RandomWalk.takeStep(agent);
                agent.setEnvError(false);
                return nextStep;
            }

            SimulationFramework.log("Path problems SOLVED","errConsole");
        }
        // </editor-fold>

        if(agent.getPath() == null
                || agent.getPath().getPoints() == null
                || agent.getPath().getPoints().isEmpty()
                || agent.getPath().getPoints().get(0) == null){
            SimulationFramework.log(
                    "["+agent.getName()+"] start point is NULL at "+agent.getTimeElapsed(),
                    "errConsole"
            );
        }else {
            agent.setStartPoint(agent.getPath().getPoints().get(0));
        }
        agent.getPath().getPoints().remove(0);
        return agent.getNextPathPoint();
    }
    // </editor-fold>

    public static void updateFrontiers(RealAgent agent, Environment env){
        Integer time = agent.getTimeElapsed();
        SortedSet<Point> frontSet = Collections.synchronizedSortedSet(new TreeSet<>(
                (Point p1, Point p2) ->
                        p1.equals(p2) ? 0 :
                                ((p1.x - p2.x) > 0) ? 1 :
                                        (p1.x == p2.x) ?
                                                p1.y-p2.y : -1
        ));

        for(Frontier elem : calculateFrontiers(agent, env)){
            frontSet.add(elem.getCentre());
        }
        if(frontiers.containsKey(time)) frontiers.get(time).addAll(frontSet);
        else frontiers.put(time, frontSet);
    }

    public static List<Integer> sortFrontiersKeys(){
        List<Integer> keys = Collections.list(ExplorationController.frontiers.keys());
        Collections.sort(keys);
        return keys;
    }

    //<editor-fold defaultstate="collapsed" desc="Auxiliary method to compute frontiers set">

    public static Set<Point> getFrontiers(Integer key){
        return frontiers.get(key);
    }

    public static Set<Point> getModifiedFrontiers(Integer key, boolean newOrExp){

        int prevKey = getPreviousIndex(key);
        Set<Point> unexploredFSet = new HashSet<>(frontiers.get(key));

        if(prevKey > 0) unexploredFSet.retainAll(frontiers.get(prevKey));
        else return unexploredFSet;

        if(newOrExp){
            Set<Point> newFSet = new HashSet<>(frontiers.get(key));
            newFSet.removeAll(unexploredFSet);
            return newFSet;
        }
        else{
            if(key==1) return new HashSet<>();
            Set<Point> exploredFSet = new HashSet<>(frontiers.get(prevKey));
            exploredFSet.removeAll(unexploredFSet);
            return exploredFSet;
        }
    }

    private static void collapseFrontiers(Integer key){

    }

    // </editor-fold>

    public static int getFrontiersCount(Integer key){
        return frontiers.get(key).size();
    }

    public static int getModifiedFrontiersCount(Integer key, boolean newOrExp){

        int prevKey = getPreviousIndex(key);
        if(prevKey <= 0) return 0;
        Set<Point> currFSet = new HashSet<>(frontiers.get(key));
        Set<Point> prevFSet = new HashSet<>(frontiers.get(prevKey));
        Set<Point> unexploredFSet = new HashSet<>();

        for(Point p : currFSet) {
            if(prevFSet.contains(p))
                unexploredFSet.add(p);
            else {
                boolean newF = true;
                for (Point q : prevFSet) {
                    if (Math.abs(p.x - q.x) < Constants.DISTANCE_THRESHOLD
                            && Math.abs(p.y - q.y) < Constants.DISTANCE_THRESHOLD
                            && !currFSet.contains(q)) {
                                newF = false;
                    }
                }
                if(!newF)
                    unexploredFSet.add(p);
            }
        }

        return newOrExp ?
                currFSet.size() - unexploredFSet.size() :
                prevFSet.size() - unexploredFSet.size();
    }

    private static int getPreviousIndex(Integer key) {
        int keyIdx;
        List<Integer> keys = sortFrontiersKeys();

        keyIdx = keys.indexOf(key);
        if(keyIdx > 0) return keys.get(keyIdx - 1);

        return 0;

    }

    public static LinkedList<Double> computeDistances(LinkedList<Frontier> frontiers, RealAgent agent){

        LinkedList<Double> distances = new LinkedList<>();
        for(Frontier f: frontiers){
            distances.add(agent.calculatePath(agent.getLocation(), f.getCentre()).getLength());
        }
        return distances;
    }

    /*
    public static void log(RealAgent agent){

        Point goal = agent.getCurFrontier().getCentre();

        SimulationFramework.log(
                SimulationFramework.environmentCounter
                +"          "
                +agent.getName()
                +"          "
                +agent.getTimeElapsed()
                +"          "
                +"F:"
                +agent.getFrontiers().toString()
                +" L:"
                +"("+agent.getLocation().x+","+agent.getLocation().y+")"
                +" G:"
                +"("+goal.x+","+goal.y+")",
        //        +" SP:"
        //        +"("+agent.getPath().getStartPoint().x+","+agent.getPath().getStartPoint().y+")"
        //        +" GP:"
        //        +"("+agent.getPath().getGoalPoint().x+","+agent.getPath().getGoalPoint().y+")",
                "frontiersConsole"
        );
    }*/

}