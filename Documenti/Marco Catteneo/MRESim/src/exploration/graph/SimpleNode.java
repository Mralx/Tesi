package exploration.graph;

import java.awt.*;

import static config.Constants.DISTANCE_THRESHOLD;

public class SimpleNode extends Point {

    //Values used to mark frontier nodes. Default is false
    private boolean isFrontier = false;

    public SimpleNode(int x, int y) {
        super(x, y);
    }

    SimpleNode(SimpleNode node){
        super(node.x, node.y);
        if(node.isFrontier())
            this.isFrontier = true;
    }

    SimpleNode(SimpleNode node, boolean isFrontier){
        super(node.x, node.y);
        setFrontier(isFrontier);
    }

    public boolean isFrontier() {
        return isFrontier;
    }

    public void setFrontier(boolean f){
        isFrontier = f;
    }

    @Override
    public String toString() {
        if(!isFrontier)
            return "["+this.x+","+this.y+"]";
        else
            return "("+this.x+","+this.y+")";

    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
