package exploration.graph;

public class Edge extends SimpleEdge {

    private SimpleNode starter;

    public Edge(SimpleNode adjacent, SimpleNode starter) {
        super(adjacent);
        this.starter = starter;
    }

    public Edge(SimpleNode adjacent, int distance, SimpleNode starter) {
        super(adjacent, distance);
        this.starter = starter;
    }

    public SimpleNode getStarter() {
        return starter;
    }
}
