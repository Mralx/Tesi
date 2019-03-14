package exploration.graph;

class SimpleEdge {

    private SimpleNode adjacent;
    private double distance;

    public SimpleEdge(SimpleNode adjacent) {
        this.adjacent = adjacent;
    }

    public SimpleEdge(SimpleNode adjacent, double distance) {
        this.adjacent = adjacent;
        this.distance = distance;
    }

    public SimpleNode getAdjacent() {
        return adjacent;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "("+adjacent.x+","+adjacent.y+")"+
                ","+distance;
    }
}
