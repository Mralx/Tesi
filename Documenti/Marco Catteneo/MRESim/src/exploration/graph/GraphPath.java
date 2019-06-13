package exploration.graph;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class GraphPath{

    private SimpleNode startingNode;
    private SimpleNode arrivalNode;
    private List<SimpleNode> nodes;
    private double length;
    private int numberOfNodes;

    GraphPath() {
        this.startingNode = null;
        this.arrivalNode = null;
        this.nodes = new LinkedList<>();
        this.length = (double) 0;
        this.numberOfNodes = 0;
    }

    GraphPath(GraphPath path){
        if(path.getNodes()!=null) {
            setNodes(path.getNodes());
            this.length = path.getLength();
        }
        else
            new GraphPath();
    }

    public List<SimpleNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<SimpleNode> nodes) {
        if(nodes==null)
            return;

        this.nodes = new LinkedList<>();
        this.nodes.addAll(nodes);
        this.numberOfNodes = this.nodes.size();
        this.startingNode = nodes.get(0);
        this.arrivalNode = nodes.get(numberOfNodes-1);
        this.length = (double) 0;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    SimpleNode getStartingNode() {
        return startingNode;
    }

    void setStartingNode(SimpleNode startingNode, double distance) {
        this.startingNode = startingNode;
        this.nodes.add(0,startingNode);
        this.numberOfNodes += 1;
        this.length += distance;
        if(numberOfNodes==1)
            this.arrivalNode = startingNode;
    }

    public SimpleNode getArrivalNode() {
        return arrivalNode;
    }

    public void setArrivalNode(SimpleNode arrivalNode, double distance) {
        this.arrivalNode = arrivalNode;
        this.nodes.add(arrivalNode);
        this.numberOfNodes += 1;
        this.length += distance;
        if(numberOfNodes==1)
            this.startingNode = arrivalNode;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    boolean addNode(SimpleNode node, double distance){
        if(this.startingNode==null)
            this.startingNode = node;
        this.arrivalNode = node;
        this.numberOfNodes += 1;
        this.length += distance;
        return this.nodes.add(node);
    }

    void removeNode(SimpleNode node, double distance){
        if(!this.nodes.contains(node))
            return;

        this.nodes.remove(node);
        this.numberOfNodes -= 1;
        this.length -= distance;
        if(numberOfNodes==0){
            startingNode = null;
            arrivalNode = null;
        }
        else{
            if(node.equals(startingNode))
                startingNode = nodes.get(0);
            if(node.equals(arrivalNode))
                arrivalNode = nodes.get(nodes.size()-1);
        }
    }

    public boolean contains(SimpleNode entryNode) {
        return nodes.contains(entryNode);
    }

    /**
     * Provides the inverse path of the one in input. This means that the starting node and the arrival node are
     * inverted and so are all the intermediate nodes. This works only under the assumption that the path refers to an
     * undirected graph.
     *
     * @param path path to invert
     * @return a path from the arrival node to the starting node of the path in input
     */
    GraphPath invertPath(GraphPath path){
        GraphPath invertedPath = new GraphPath();
        LinkedList<SimpleNode> invertedNodes = new LinkedList<>();
        for(SimpleNode node : path.getNodes()){
            invertedNodes.addLast(node);
        }
        invertedPath.setNodes(invertedNodes);
        invertedPath.setLength(path.getLength());
        return invertedPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphPath path = (GraphPath) o;
        return Objects.equals(nodes, path.nodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodes);
    }

    @Override
    public String toString() {
        String string = startingNode.toString();
        int idx = numberOfNodes-1;
        if(idx>0) {
            List<SimpleNode> headlessNodes = getNodes();
            headlessNodes.remove(0);
            for (SimpleNode node : headlessNodes) {
                string = string.concat(" -> " + node.toString());
            }
        }

        return string.concat("      "+length+" ||| ");

    }

}
