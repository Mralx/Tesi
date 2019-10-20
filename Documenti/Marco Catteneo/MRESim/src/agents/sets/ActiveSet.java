package agents.sets;

import agents.RealAgent;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by marco on 18/05/2017.
 */
public class ActiveSet {
    private static ActiveSet as;
    private static HashSet<RealAgent> active;

    // <editor-fold defaultstate="collapsed" desc="Constructor and getInstance method">
    public ActiveSet(){}

    public synchronized  static ActiveSet getInstance() {
        if(as == null){
            as = new ActiveSet();
            active = new HashSet<>();
        }
        return as;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters and setters">

    public boolean isActive(RealAgent a){
        return active.contains(a);
    }

    public HashSet<RealAgent> getActive(){ return active; }

    public void setActive(HashSet<RealAgent> a){ active = a; }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Adders and removers">
    public HashSet<RealAgent> addActiveAgent(RealAgent a){
        active.add(a);
        return active;
    }

    public HashSet<RealAgent> removeActiveAgent(RealAgent a){
        active.remove(a);
        return active;
    }

    public void reset(){
        active = new HashSet<>();
    }
    // </editor-fold>
}
