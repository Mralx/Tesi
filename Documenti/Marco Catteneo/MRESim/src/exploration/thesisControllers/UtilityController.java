package exploration.thesisControllers;

import agents.RealAgent;
import environment.Frontier;
import exploration.thesisControllers.ExplorationController.AgentFrontierPair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

/**
 * Created by marco on 02/04/2018.
 */
public class UtilityController {
    // <editor-fold defaultstate="collapsed" desc="Variables">
    private static UtilityController bc;
    private static ArrayList<AgentFrontierPair> assignedFrontiers;
    private static Semaphore sem;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    public UtilityController(){}
    // /editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Get instance">
    public static synchronized UtilityController getInstance(){
        if(bc == null){
            bc = new UtilityController();
            assignedFrontiers = new ArrayList<>();
            sem = new Semaphore(1);
        }
        return bc;
    }
    // /editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters and setters">

    public static ArrayList<AgentFrontierPair> getAssignedFrontiers() {
        return assignedFrontiers;
    }

    public static void setAssignedFrontiers(ArrayList<AgentFrontierPair> assignedFrontiers) {
        UtilityController.assignedFrontiers = assignedFrontiers;
    }

    public static Semaphore getSem() {
        return sem;
    }

    public static void setSem(Semaphore sem) {
        UtilityController.sem = sem;
    }

    public static boolean isAssigned(Frontier frontier, RealAgent agent){
        boolean assigned = false;
        for(AgentFrontierPair assignedFrontier : assignedFrontiers){
            if(frontier.isClose(assignedFrontier.getFrontier()) &&
                    assignedFrontier.getAgent().getID() != agent.getID()){
                assigned = true;
            }
        }
        return assigned;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Adders and removers">
    public static void addAssignedFrontier(AgentFrontierPair frontier){
        assignedFrontiers.add(frontier);
    }
    // </editor-fold>


}
