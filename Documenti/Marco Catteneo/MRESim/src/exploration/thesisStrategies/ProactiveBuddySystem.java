package exploration.thesisStrategies;

import agents.RealAgent;
import agents.sets.ActiveSet;
import agents.sets.FollowerSet;
import agents.sets.IdleSet;
import agents.sets.LeaderSet;
import config.Constants;
import environment.Environment;
import environment.Frontier;
import exploration.thesisControllers.BuddyController;
import exploration.thesisControllers.ExplorationController;

import java.awt.*;
import java.util.LinkedList;

/**
 * Created by marco on 24/02/2018.
 */
@SuppressWarnings("Duplicates")
public class ProactiveBuddySystem {

    private static IdleSet idleSet;
    private static ActiveSet activeSet;
    private static LeaderSet leaderSet;
    private static FollowerSet followerSet;

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
        leaderSet = LeaderSet.getInstance();
        followerSet = FollowerSet.getInstance();
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="TIME = 0: set strategy support sets">
        if(agent.getTimeElapsed() == 0){
            /*
            SimulationFramework.log(
                    "["+agent.getName()+"] is buddy of "+agent.getBuddy().getName()+" (leader = "+agent.isLeader()+")",
                    "personalConsole"
            );
            */
            idleSet.addPoolAgent(agent);
            if(agent.isLeader()) {
                leaderSet.addLeader(agent);
            }else{
                followerSet.addFollower(agent);
            }

        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="TIME = 1 move starting agents">
        else if (agent.getTimeElapsed() == 1){
            try{
                BuddyController.getInstance().getSem().acquire();
                ExplorationController.setStartingAgent(agent, env);
            }catch(InterruptedException ie){
                ie.printStackTrace();
            }finally {
                BuddyController.getInstance().getSem().release();
            }

            if(agent.getStarter() || agent.getBuddy().getStarter()){
                /*
                SimulationFramework.log(
                        "["+agent.getName()+"] Starting at "+agent.getTimeElapsed(),
                        "personalConsole"
                );
                */
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

        //Call goal function
        Point goal = null;
        if(LeaderSet.getInstance().isLeader(agent)){
            goal = leaderGoalFunction(agent,frontiers,teamPositioning,teamGoals);
        }else if(FollowerSet.getInstance().isFollower(agent)){
            Point splittingGoal = splittingFunction(agent);
            if(splittingGoal != null){
                agent.setFirstCall(true);
                goal = splittingGoal;
            }else {
                goal = followerGoalFunction(agent, frontiers, teamPositioning, teamGoals);
            }
        }

        //Logging of frontiers and nextStep
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

        if(LeaderSet.getInstance().isLeader(agent)) {
            try {
                BuddyController.getInstance().getSem().acquire();
                LinkedList<Frontier> freeCall = BuddyController.getInstance().getNotAssignedFrontiers();

                if (!freeCall.isEmpty()) {
                    Frontier f = BuddyController.getInstance().chooseBestReservePair(agent, freeCall);
                    if (f != null) {
                        agent.setFirstCall(true);
                        agent.getBuddy().setFirstCall(true);
                        BuddyController.getInstance().setAssignedFrontier(f);

                        IdleSet.getInstance().removePoolAgent(agent);
                        IdleSet.getInstance().removePoolAgent(agent.getBuddy());
                        ActiveSet.getInstance().addActiveAgent(agent);
                        ActiveSet.getInstance().addActiveAgent(agent.getBuddy());

                        activationGoal = ExplorationController.moveAgent(agent, f);
                        agent.setCurFrontier(f);
                    }
                }
            } catch (InterruptedException e) {
                //Do something
            } finally {
                BuddyController.getInstance().getSem().release();
            }
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

        //Remove closer frontier
        frontiers.remove(closer);

        //Calculate second closer frontier
        Frontier splitFront = BuddyController.getInstance().checkSplit(frontiers,closer,agent);

        //Call buddy
        if(splitFront != null) {
            BuddyController.getInstance().addFollowerFrontier(splitFront, agent.getBuddy());
        }

        //Call reserve couples
        BuddyController.getInstance().addCallFrontiers(frontiers);

        //Move agent
        Point goal = ExplorationController.moveAgent(agent,closer);
        agent.setCurFrontier(closer);
        return goal;

    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="GAMMA: goal function for follower agents">
    /**
     * Calculate goal location for leader agents
     * @param agent
     * @param frontiers
     * @param teamPositioning
     * @param teamGoals
     * @return leaderGoal
     */
    private static Point followerGoalFunction(RealAgent agent,LinkedList<Frontier> frontiers,LinkedList<Point> teamPositioning,LinkedList<Point> teamGoals){
        if(agent.getBuddy().getCurFrontier() != null) {
            return ExplorationController.moveAgent(agent, agent.getBuddy().getCurFrontier());
        }else{
            return agent.getLocation();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="PHI: splitting function">
    private static Point splittingFunction(RealAgent agent){
        Frontier splitFront = BuddyController.getInstance().getFollowerFrontier(agent);
        if(splitFront != null && agent.getTimeElapsed() > Constants.SPLIT_TIME){
            Point goal = ExplorationController.moveAgent(agent,splitFront);
            agent.setCurFrontier(splitFront);
            BuddyController.handleSplit(agent);
            return goal;
        }
        return null;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="LAMBDA: proactivity function">
    private static Point proactivityFunction(RealAgent agent, Environment env){
        //Compute the barycenter of the polygon formed by all active agents
        LinkedList<RealAgent> activeAgents = activeSet.getInstance().getActive();
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

            /*
            if(ExplorationController.closeToObstacle(barycenter,env)){
                SimulationFramework.log(
                        "["+agent.getName()+"] proactively assigned to an obstacle at time "+agent.getTimeElapsed(),
                        "personalConsole"
                );
            }else{
                SimulationFramework.log(
                        "["+agent.getName()+"] proactively assigned to: "+barycenter.toString()+"at time "+agent.getTimeElapsed(),
                        "personalConsole"
                );
            }
            */

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
            /*
            SimulationFramework.log(
                    "["+agent.getName()+"] proactively assigned to its location at time "+agent.getTimeElapsed(),
                    "personalConsole"
            );
            */
        }

        //Use barycenter as proactivity goal
        return barycenter;
    }
    // </editor-fold>

}
