package exploration.thesisStrategies;

import agents.RealAgent;
import agents.sets.ActiveSet;
import agents.sets.IdleSet;
import config.Constants;
import config.SimulatorConfig;
import environment.Environment;
import environment.Frontier;
import exploration.SimulationFramework;
import exploration.graph.GraphHandler;
import exploration.graph.SimpleNode;
import exploration.thesisControllers.ExplorationController;
import exploration.thesisControllers.ReserveController;

import java.awt.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by marco on 24/02/2018.
 */
@SuppressWarnings("Duplicates")
public class ProactiveReserve {

    private static IdleSet idleSet;
    private static ActiveSet activeSet;

    // <editor-fold defaultstate="collapsed" desc="TakeStep">
    /**
     * Handles timing and agents' roles during exploration
     * @param agent
     * @param env
     * @return nextStep
     */
    public static Point takeStep(RealAgent agent, Environment env){
        Point nextStep = agent.getLocation();

        // <editor-fold defaultstate="collapsed" desc="Get strategy support sets">
        idleSet = IdleSet.getInstance();
        activeSet = ActiveSet.getInstance();
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="TIME = 0: set strategy support sets">
        if(agent.getTimeElapsed() == 0){
            idleSet.addPoolAgent(agent);
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="TIME = 1 move starting agents">
        else if (agent.getTimeElapsed() == 1){
            ExplorationController.setStartingAgent(agent,env);
            if(agent.getStarter()){
                Point goal = rePlan(agent,env);

                idleSet.removePoolAgent(agent);
                activeSet.addActiveAgent(agent);

                nextStep = goal;
            }
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="TIME > 1: move remaining agents">
        else if(agent.getTimeElapsed() > 1) {
            if(activeSet.isActive(agent)) {
                if(agent.getFirstCall()){
                    agent.setFirstCall(false);
                }
                Point goal = rePlan(agent,env);
                nextStep = goal;
            }else{
                Point activationGoal = activationFunction(agent);
                if(activationGoal != null){
                    idleSet.removePoolAgent(agent);
                    activeSet.addActiveAgent(agent);
                    nextStep = activationGoal;
                }else{
                    Point proactivityGoal = proactivityFunction(agent,env);
                    nextStep = proactivityGoal;
                }
            }
        }
        // </editor-fold>

        return nextStep;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Re-plan">
    /**
     * Updates model exploration sets and calls model functions
     * @param agent
     * @param env
     * @return currentGoal
     */
    private static Point rePlan(RealAgent agent,Environment env){
        //F set
        LinkedList<Frontier> frontiers = ExplorationController.calculateFrontiers(agent,env);

        //P set
        LinkedList<Point> teamPositioning = ExplorationController.calculateTeamPositioning();

        //G set
        LinkedList<Point> teamGoals = ExplorationController.calculateTeamGoals();

        //Call appropriate goal function
        Point goal = leaderGoalFunction(agent,frontiers,teamPositioning,teamGoals);

        try {
            ExplorationController.updateFrontiers(agent, env);
        }
        catch (NullPointerException e){
            System.out.println("No frontier assigned yet!");
        }

        return goal;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ALPHA: Activation function">
    private static Point activationFunction(RealAgent agent){
        agent.getStats().setTimeSinceLastPlan(0);
        Point activationGoal = null;

        try{
            ReserveController.getInstance().getSem().acquire();
            LinkedList<Frontier> freeCall = ReserveController.getInstance().getNotAssignedFrontiers();

            if(!freeCall.isEmpty()){
                Frontier f = ReserveController.getInstance().chooseBestReservePair(agent,freeCall);
                if(f!=null){
                    agent.setFirstCall(true);
                    ReserveController.getInstance().setAssignedFrontier(f);

                    IdleSet.getInstance().removePoolAgent(agent);
                    ActiveSet.getInstance().addActiveAgent(agent);

                    activationGoal = ExplorationController.moveAgent(agent,f);
                }
            }
        }catch(InterruptedException e){
            //Do something
        }finally{
            ReserveController.getInstance().getSem().release();
        }

        return activationGoal;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="BETA: goal function for leader agents">
    /**
     * Calculate goal location for leader agents
     * @param agent
     * @param frontiers
     * @param teamPositioning
     * @param teamGoals
     * @return leaderGoal
     */
    private static Point leaderGoalFunction(RealAgent agent,LinkedList<Frontier> frontiers,LinkedList<Point> teamPositioning,LinkedList<Point> teamGoals){
        agent.getStats().setTimeSinceLastPlan(0);

        //Check agent's frontiers
        if(agent.getFrontiers().isEmpty()){
            agent.setMissionComplete(true);
            agent.setPathToBaseStation();
            return agent.getNextPathPoint();
        }

        //Check clean frontiers
        if(frontiers.isEmpty()){
            agent.setMissionComplete(true);
            agent.setPathToBaseStation();
            return agent.getNextPathPoint();
        }

        //Calculate closer frontier
        Point pos = agent.getLocation();
        double min = 1000000000;
        Frontier closer = null;
        for(Frontier f: frontiers){
            if(pos.distance(f.getCentre()) < min){
                closer = f;
                min = pos.distance(f.getCentre());
            }
        }

        //Call reserve agents
        frontiers.remove(closer);
        ReserveController.getInstance().addCallFrontiers(frontiers);

        //Move agent
        Point goal = ExplorationController.moveAgent(agent,closer);
        return goal;

    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="LAMBDA: proactivity function">
    private static Point proactivityFunction(RealAgent agent, Environment env){

        //Compute the barycenter of the polygon formed by all active agents
        HashSet<RealAgent> activeAgents = activeSet.getInstance().getActive();

        //<editor-fold defaultstate="collapsed" desc="Original code">
        /*
        Point barycenter;

        if(activeAgents.size() > Constants.MIN_CLUSTER_SIZE) {
            double xSum = 0;
            double ySum = 0;
            for (RealAgent activeAgent : activeAgents) {
                xSum = xSum + activeAgent.getLocation().getX();
                ySum = ySum + activeAgent.getLocation().getY();
            }
            barycenter = new Point(
                    (int) (xSum / activeAgents.size()),
                    (int) (ySum / activeAgents.size())
            );

            LinkedList<Point> barycenterList = new LinkedList<>();
            barycenterList.add(barycenter);
            Frontier barycenterFrontier = new Frontier(
                    agent.getX(),
                    agent.getY(),
                    barycenterList
            );
            barycenter = ExplorationController.moveAgent(agent,barycenterFrontier);

        }else{
            barycenter = agent.getLocation();
        }

        //Use barycenter as proactivity goal
        return barycenter;
         */
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Test code with metrics">

        Point barycenter = null; //used for logging and comparison
        Point metricBarycenter = null;

        //used for logging and comparison
        if(activeAgents.size() > Constants.MIN_CLUSTER_SIZE) {
            double xSum = 0;
            double ySum = 0;
            for (RealAgent activeAgent : activeAgents) {
                xSum = xSum + activeAgent.getLocation().getX();
                ySum = ySum + activeAgent.getLocation().getY();
            }
            barycenter = new Point(
                    (int) (xSum / activeAgents.size()),
                    (int) (ySum / activeAgents.size())
            );

        }

        if(activeAgents.size() > Constants.MIN_CLUSTER_SIZE) {
            double xSum = 0;
            double ySum = 0;
            Map<SimpleNode, Double> nodes = null;
            try{
                GraphHandler.getSemaphore().acquire();
                nodes = GraphHandler.highestCNodes();
            }catch(InterruptedException ie){
                ie.printStackTrace();
            }finally {
                GraphHandler.getSemaphore().release();
            }

            double statSum = nodes.values().stream().mapToDouble(Double::doubleValue).sum();
            for (SimpleNode n : nodes.keySet()) {
                xSum = xSum + n.x * nodes.get(n);
                ySum = ySum + n.y * nodes.get(n);
            }
            metricBarycenter = new Point(
                    (int) (xSum / statSum),
                    (int) (ySum / statSum)
            );
        }
        if(metricBarycenter!=null && !metricBarycenter.equals(new Point(0, 0))) {
            LinkedList<Point> barycenterList = new LinkedList<>();
            barycenterList.add(metricBarycenter);
            Frontier barycenterFrontier = new Frontier(
                    agent.getX(),
                    agent.getY(),
                    barycenterList
            );

            metricBarycenter = ExplorationController.moveAgent(agent, barycenterFrontier);
        }else
            metricBarycenter = agent.getLocation();

        if(barycenter!=null && metricBarycenter!=null)
            SimulationFramework.log(env +"    "+(idleSet.getPool().size()+activeAgents.size())+"    "+
                            "Time: " + agent.getTimeElapsed() + " Barycenter: " + barycenter.toString() +
                            " Metric barycenter: " + metricBarycenter.toString() + " Agent: " + agent.getLocation().toString() +
                            "Idle Set size: " + idleSet.getPool().size(),
                    "Barycenter log");

        //Use barycenter as proactivity goal
        return metricBarycenter;

        //</editor-fold>

    }
    // </editor-fold>

}
