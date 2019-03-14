package exploration.graph;

import java.awt.*;

import static config.Constants.DISTANCE_THRESHOLD;

public class SimpleNode extends Point {

    public SimpleNode(int x, int y) {
        super(x, y);
    }

    SimpleNode(Node node){
        super(node.x, node.y);
    }

    @Override
    public String toString() {
        return new String("["+this.x+","+this.y+"]");
    }

    public boolean equals(SimpleNode node) {
        if(node.x-DISTANCE_THRESHOLD <= this.x && this.x <= node.x+DISTANCE_THRESHOLD)
            return node.y-DISTANCE_THRESHOLD <= this.y && this.y <= node.y+DISTANCE_THRESHOLD;
        return false;
    }
}
