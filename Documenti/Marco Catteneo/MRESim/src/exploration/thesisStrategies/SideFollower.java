package exploration.thesisStrategies;

import agents.RealAgent;
import agents.sets.FollowerSet;
import agents.sets.LeaderSet;
import environment.Environment;
import environment.Frontier;
import exploration.thesisControllers.ExplorationController;
import exploration.thesisControllers.SideController;

import java.awt.*;
import java.util.LinkedList;

/**
 * Created by marco on 04/03/2018.
 */
@SuppressWarnings("Duplicates")
public class SideFollower {

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
        leaderSet = LeaderSet.getInstance();
        followerSet = FollowerSet.getInstance();
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="TIME = 0: set strategy support sets">
        if(agent.getTimeElapsed() == 0){
            /*
            if(agent.getBuddies().size() == 2) {
                SimulationFramework.log(
                        "[" + agent.getName() + "] Is buddy of: " + agent.getBuddies().get(0).getName()
                                + " and of:" + agent.getBuddies().get(1).getName() + "( leader = " + agent.isLeader()+" )",
                        "personalConsole"
                );
            }else if(agent.getBuddies().size() == 1){
                SimulationFramework.log(
                        "[" + agent.getName() + "] Is buddy of: " + agent.getBuddies().get(0).getName()+"( leader = " + agent.isLeader()+" )",
                        "personalConsole"
                );
            }else{
                SimulationFramework.log(
                        "[" + agent.getName() + "] Is alone ( leader = " + agent.isLeader()+" )",
                        "personalConsole"
                );
            }
            */
            if(agent.isLeader()) {
                leaderSet.addLeader(agent);
            }else{
                followerSet.addFollower(agent);
            }

        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="TIME = 1 move starting agents">
        else if (agent.getTimeElapsed() == 1){
            if(agent.isLeader()){
                //SimulationFramework.log("["+agent.getName()+"] Starting","personalConsole");

                Point goal = rePlan(agent,env);
                //agent.setFirstCall(true);
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
        Point goal;
        if(LeaderSet.getInstance().isLeader(agent)){

            goal = leaderGoalFunction(agent,frontiers,teamPositioning,teamGoals);

        }else if(FollowerSet.getInstance().isFollower(agent)){

            goal = followerGoalFunction(agent, frontiers, teamPositioning, teamGoals);

        }else{

            goal = agent.getLocation();

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
     * @param agent
     * @param frontiers
     * @param teamPositioning
     * @param teamGoals
     * @return leaderGoal
     */
    private static Point leaderGoalFunction(
            RealAgent agent,
            LinkedList<Frontier> frontiers,
            LinkedList<Point> teamPositioning,
            LinkedList<Point> teamGoals){
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

        //Calculate straight frontier
        Point pos = agent.getLocation();
        Point prevPos;
        if(agent.getPath().getStartPoint() != null){
            prevPos = agent.getStartPoint();
        }else{
            prevPos = new Point(
                    agent.getPrevX(),
                    agent.getPrevY()
            );
        }
        Frontier straight = null;
        Frontier left = null;
        Frontier right = null;

        final int LEFT_REFERENCE = 90;
        final int RIGHT_REFERENCE = 270;

        double straightMin = 1000000000;
        double leftMin = 1000000000;
        double rightMin = 1000000000;
        double angle;
        for(Frontier f: frontiers) {
            angle =
                    SideController.computeAngle(
                            agent,
                            pos,
                            prevPos,
                            f.getCentre()
                    );
            /*
            SimulationFramework.log(
                    "["+agent.getName()+"] Frontier "+f.toString()+" Angle: "+angle+" at "+agent.getTimeElapsed(),
                    "personalConsole"
            );
            */

            if(angle < straightMin){
                straight = f;
                straightMin = angle;
            }

            if(Math.abs(LEFT_REFERENCE-angle) < leftMin){
                left = f;
                leftMin = Math.abs(LEFT_REFERENCE-angle);
            }

            if(Math.abs(RIGHT_REFERENCE-angle) < rightMin){
                right = f;
                rightMin = Math.abs(RIGHT_REFERENCE-angle);
            }
        }
        /*
        SimulationFramework.log(
                "["+agent.getName()+"] Frontier: "+straight.toString()+" chosen at "+agent.getTimeElapsed(),
                "personalConsole"
        );
        */

        //Call left agent
        if(left != null){
            /*
            SimulationFramework.log(
                    "["+agent.getName()+"] Frontier: "+left.toString()+" assigned at left at "+agent.getTimeElapsed(),
                    "personalConsole"
            );
            */
            SideController.getInstance().addLeftFrontier(agent,left);
        }

        //Call right agent
        if(right != null){
            /*
            SimulationFramework.log(
                    "["+agent.getName()+"] Frontier: "+right.toString()+" assigned at right at "+agent.getTimeElapsed(),
                    "personalConsole"
            );
            */
            SideController.getInstance().addRightFrontier(agent,right);
        }

        //Move agent
        Point goal = ExplorationController.moveAgent(agent,straight);
        agent.setCurFrontier(straight);
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
        RealAgent leader = null;
        for(RealAgent buddy : agent.getBuddies()){
            if(LeaderSet.getInstance().isLeader(buddy)){
                leader = buddy;
            }
        }
        if(leader == null){
            return  agent.getLocation();
        }

        LinkedList<ExplorationController.AgentFrontierPair> sideFrontiers;
        if(agent.getFollowerType().equals(RealAgent.LEFT_FOLLOWER)){
            sideFrontiers = SideController.getInstance().getNotAssignedLeftFrontiers();
        }else{
            sideFrontiers = SideController.getInstance().getNotAssignedRightFrontiers();
        }

        /*
        SimulationFramework.log(
                "["+agent.getName()+"] considering following frontiers at "+agent.getTimeElapsed(),
                "personalConsole"
        );
        for(ExplorationController.AgentFrontierPair agentFrontierPair
                : sideFrontiers){
            SimulationFramework.log(
                    "["+agent.getName()+"]"+agentFrontierPair.getAgent().toString()+" "+agentFrontierPair.getFrontier().toString(),
                    "personalConsole"
            );
        }
        */
        //Collections.reverse(leftFrontiers);
        for(ExplorationController.AgentFrontierPair agentFrontierPair
                : sideFrontiers){
            if(agentFrontierPair.getAgent().getID() == leader.getID()){
                SideController.getInstance().removeLeftFrontier(agent,agentFrontierPair.getFrontier());
                SideController.getInstance().removeRightFrontier(agent,agentFrontierPair.getFrontier());
                /*
                SimulationFramework.log(
                        "["+agent.getName()+"] taking "+agentFrontierPair.getFrontier().toString()+" at "+agent.getTimeElapsed(),
                        "personalConsole"
                );
                */
                return ExplorationController.moveAgent(agent, agentFrontierPair.getFrontier());
            }
        }

        return agent.getLocation();
    }
    // </editor-fold>

}
