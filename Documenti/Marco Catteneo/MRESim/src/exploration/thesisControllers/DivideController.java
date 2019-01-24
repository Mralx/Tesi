package exploration.thesisControllers;

import agents.RealAgent;
import agents.sets.FollowerSet;
import agents.sets.LeaderSet;
import environment.Frontier;
import exploration.SimulationFramework;

import java.awt.*;
import java.util.LinkedList;

/**
 * Created by marco on 28/07/2017.
 */
@SuppressWarnings("Duplicates")
public class DivideController {

    // <editor-fold defaultstate="collapsed" desc="Variables">
    private static DivideController dc;
    private static Frontier splitFrontier;
    private static int runNum;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public DivideController(){}
    // /editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Get instance">
    public static synchronized DivideController getInstance(){
        if(dc == null){
            dc = new DivideController();
            splitFrontier = null;
            runNum = 0;
        }
        return dc;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters and setters">
    public static Frontier getSplitFrontier() {
        return splitFrontier;
    }

    public static void setSplitFrontier(Frontier splitFrontier) {
        DivideController.splitFrontier = splitFrontier;
    }

    public static int getRunNum() {
        return runNum;
    }

    public static void setRunNum(int runNum) {
        DivideController.runNum = runNum;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Utils">
    public static Frontier checkSplit(LinkedList<Frontier> frontiers,Frontier selected,RealAgent leader){
        Point pos = leader.getLocation();
        double min = 1000000000;
        Frontier closer = null;
        for(Frontier f: frontiers){
            if(pos.distance(f.getCentre()) < min && !f.isClose(selected)){
                closer = f;
                min = pos.distance(f.getCentre());
            }
        }
        return closer;
    }

    public static void handleSplit(RealAgent newLeader){
        FollowerSet fSet = FollowerSet.getInstance();
        LeaderSet lSet = LeaderSet.getInstance();
        LinkedList<RealAgent> followers = fSet.getFollowers();

        //Remove new leader from follower set and add it to leader one
        int teamSize = followers.size();
        if(teamSize > 0) {
            fSet.removeFollower(newLeader);
            lSet.addLeader(newLeader);
        }
        newLeader.setFirstCall(true);
        //SimulationFramework.log("["+newLeader.getName()+"] NEW LEADER","personalConsole");

        //Set new leader as leader for half the follower group
        int newTeamSize = teamSize/2;
        for(int i=1; i<newTeamSize; i++){
            followers.get(i).setBuddy(newLeader);
        }
    }
    // </editor-fold>
}
