package exploration.graph;

import java.awt.*;

import static config.Constants.DISTANCE_THRESHOLD;

public class SimpleNode extends Point {

    //Values used to mark frontier nodes. Default is false
    private boolean isFrontier = false;

    SimpleNode(int x, int y) {
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
        return "["+this.x+","+this.y+"]";
    }

    public boolean equals(SimpleNode node) {
        if(node.x-DISTANCE_THRESHOLD <= this.x && this.x <= node.x+DISTANCE_THRESHOLD)
            return node.y-DISTANCE_THRESHOLD <= this.y && this.y <= node.y+DISTANCE_THRESHOLD;
        return false;
    }
}
