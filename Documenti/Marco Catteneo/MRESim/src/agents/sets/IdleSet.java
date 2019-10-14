package agents.sets;

import agents.RealAgent;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by marco on 18/05/2017.
 */
public class IdleSet {
    private static IdleSet is;
    private static HashSet<RealAgent> pool;

    // <editor-fold defaultstate="collapsed" desc="Constructor and getInstance method">
    public IdleSet(){}

    public synchronized static IdleSet getInstance() {
        if(is == null){
            is = new IdleSet();
            pool = new HashSet<>();
        }
        return is;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters and setters">

    public boolean isIdle(RealAgent a){
        return pool.contains(a);
    }

    public HashSet<RealAgent> getPool(){ return pool; }

    public void setPool(HashSet<RealAgent> p){ pool = p; }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Adders and removers">
    public HashSet<RealAgent> addPoolAgent(RealAgent a){
        if(!pool.contains(a))
            pool.add(a);
        return pool;
    }

    public HashSet<RealAgent> removePoolAgent(RealAgent a){
        pool.remove(a);
        return pool;
    }
    // </editor-fold>
}
