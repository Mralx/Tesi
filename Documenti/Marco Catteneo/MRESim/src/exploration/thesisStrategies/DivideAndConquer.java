package exploration.thesisStrategies;

import agents.RealAgent;
import agents.sets.FollowerSet;
import agents.sets.LeaderSet;
import config.Constants;
import environment.Environment;
import environment.Frontier;
import exploration.thesisControllers.DivideController;
import exploration.thesisControllers.ExplorationController;

import java.awt.*;
import java.util.LinkedList;

/**
 * Created by marco on 28/07/2017.
 */
@SuppressWarnings("Duplicates")
public class DivideAndConquer {

    // <editor-fold defaultstate="collapsed" desc="TakeStep">
    /**
     * Handles timing and agents' roles during exploration
     *
     * @param agent
     * @param env
     * @return nextStep
     */
    public static Point takeStep(RealAgent agent, Environment env) {
        Point nextStep = agent.getLocation();

        // <editor-fold defaultstate="collapsed" desc="Get strategy support sets">
        LeaderSet leaderSet = LeaderSet.getInstance();
        FollowerSet followerSet = FollowerSet.getInstance();
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="TIME = 0: set strategy support sets">
        if(agent.getTimeElapsed() == 0){
            if(agent.isLeader()) {
                leaderSet.addLeader(agent);
            }else{
                followerSet.addFollower(agent);
            }
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="TIME = 1 move starting agents">
        else if (agent.getTimeElapsed() == 1){
            if(leaderSet.isLeader(agent)){
                Point goal = rePlan(agent,env);
                nextStep = goal;
            }
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="TIME > 1: move remaining agents">
        else if(agent.getTimeElapsed() > 1) {
            Point goal = rePlan(agent,env);
            nextStep = goal;
        }
        // </editor-fold>

        return nextStep;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Re-plan">
    /**
     * Updates model exploration sets and calls model functions
     *
     * @param agent
     * @param env
     * @return currentGoal
     */
    private static Point rePlan(RealAgent agent, Environment env) {
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
            agent.setFirstCall(false);
        }else if(FollowerSet.getInstance().isFollower(agent)){
            Point splittingGoal = splittingFunction(agent);
            if(splittingGoal != null){
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

    // <editor-fold defaultstate="collapsed" desc="BETA: goal function for leader agents">
    /**
     * Calculate goal location for leader agents
     *
     * @param agent
     * @param frontiers
     * @param teamPositioning
     * @param teamGoals
     * @return leaderGoal
     */
    private static Point leaderGoalFunction(RealAgent agent, LinkedList<Frontier> frontiers, LinkedList<Point> teamPositioning, LinkedList<Point> teamGoals) {
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
        Frontier splitFront = DivideController.getInstance().checkSplit(frontiers,closer,agent);

        //Split new group
        if(splitFront != null) {
            DivideController.getInstance().setSplitFrontier(splitFront);
        }

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
        Frontier splitFront = DivideController.getInstance().getSplitFrontier();
        if(splitFront != null && agent.getTimeElapsed() > Constants.SPLIT_TIME &&
                FollowerSet.getInstance().getFollowers().get(0).getID() == agent.getID()){

            DivideController.setSplitFrontier(null);
            Point goal = ExplorationController.moveAgent(agent,splitFront);
            agent.setCurFrontier(splitFront);
            DivideController.handleSplit(agent);
            return goal;

        }
        return null;
    }
    // </editor-fold>

}
