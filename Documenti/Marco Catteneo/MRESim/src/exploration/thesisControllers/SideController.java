package exploration.thesisControllers;

import agents.RealAgent;
import environment.Frontier;
import exploration.SimulationFramework;

import java.awt.*;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

/**
 * Created by marco on 06/03/2018.
 */
public class SideController {

    // <editor-fold defaultstate="collapsed" desc="Variables">
    private static SideController sc;
    private static LinkedList<ExplorationController.AgentFrontierPair> leftFrontiers;
    private static LinkedList<ExplorationController.AgentFrontierPair> rightFrontiers;
    private static Semaphore sem;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constructor and get instance">
    public SideController(){}

    public static SideController getInstance(){
        if(sc == null){
            sc = new SideController();
            leftFrontiers = new LinkedList<>();
            rightFrontiers = new LinkedList<>();
            sem = new Semaphore(1);
        }

        return sc;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters and setters">
    public static LinkedList<ExplorationController.AgentFrontierPair> getLeftFrontiers() {
        return leftFrontiers;
    }

    public static void setLeftFrontiers(LinkedList<ExplorationController.AgentFrontierPair> leftFrontiers) {
        SideController.leftFrontiers = leftFrontiers;
    }

    public static LinkedList<ExplorationController.AgentFrontierPair> getRightFrontiers() {
        return rightFrontiers;
    }

    public static void setRightFrontiers(LinkedList<ExplorationController.AgentFrontierPair> rightFrontiers) {
        SideController.rightFrontiers = rightFrontiers;
    }

    public static Semaphore getSem() {
        return sem;
    }

    public static void setSem(Semaphore sem) {
        SideController.sem = sem;
    }

    public static LinkedList<ExplorationController.AgentFrontierPair> getNotAssignedLeftFrontiers(){
        LinkedList<ExplorationController.AgentFrontierPair> leftNotAssigned = new LinkedList<>();
        for(ExplorationController.AgentFrontierPair agentFrontierPair
                : leftFrontiers){
            if(!agentFrontierPair.getFrontier().isAssigned()){
                leftNotAssigned.add(agentFrontierPair);
            }
        }
        return leftNotAssigned;
    }

    public static LinkedList<ExplorationController.AgentFrontierPair> getNotAssignedRightFrontiers(){
        LinkedList<ExplorationController.AgentFrontierPair> rightNotAssigned = new LinkedList<>();
        for(ExplorationController.AgentFrontierPair agentFrontierPair
                : rightFrontiers){
            if(!agentFrontierPair.getFrontier().isAssigned()){
                rightNotAssigned.add(agentFrontierPair);
            }
        }
        return rightNotAssigned;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Adders and removers">
    public void addLeftFrontier(RealAgent agent, Frontier frontier){
        ExplorationController.AgentFrontierPair agentFrontierPair =
                new ExplorationController.AgentFrontierPair(
                        agent,
                        frontier
                );
        leftFrontiers.add(agentFrontierPair);
    }

    public void addRightFrontier(RealAgent agent, Frontier frontier){
        ExplorationController.AgentFrontierPair agentFrontierPair =
                new ExplorationController.AgentFrontierPair(
                        agent,
                        frontier
                );
        rightFrontiers.add(agentFrontierPair);
    }

    public void removeLeftFrontier(RealAgent agent, Frontier frontier){
        for(ExplorationController.AgentFrontierPair rightPair : rightFrontiers){
            if(frontier.isClose(rightPair.getFrontier())){
                rightPair.getFrontier().setAssigned(true);
            }
        }
    }

    public void removeRightFrontier(RealAgent agent, Frontier frontier){
        for(ExplorationController.AgentFrontierPair leftPair : leftFrontiers){
            if(frontier.isClose(leftPair.getFrontier())){
                leftPair.getFrontier().setAssigned(true);
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Compute angle">
    public static double computeAngle(RealAgent agent, Point pos,Point prevPos, Point frontPos){
        Vector directionVector = new Vector(
                prevPos,
                pos
        );
        Vector frontierVector = new Vector(
                pos,
                frontPos
        );

        double cos =
                directionVector.computeScalarProduct(frontierVector)
                / (directionVector.computeNorm()*frontierVector.computeNorm());

        double acos = Math.acos(cos);

        if(Double.isNaN(acos)){
            return 0;
        }
        return Math.toDegrees(acos);
    }

    private static class Vector {
        private Point pointA;
        private Point pointB;
        private double[] vectorCoordinates = new double[2];

        public Vector(Point pointA, Point pointB){
            this.pointA = pointA;
            this.pointB = pointB;
            vectorCoordinates[0] = pointB.getX() - pointA.getX();
            vectorCoordinates[1] = pointB.getY() - pointA.getY();
        }

        public Point getPointA() {
            return pointA;
        }

        public void setPointA(Point pointA) {
            this.pointA = pointA;
        }

        public Point getPointB() {
            return pointB;
        }

        public void setPointB(Point pointB) {
            this.pointB = pointB;
        }

        public double[] getVectorCoordinates() {
            return vectorCoordinates;
        }

        public void setVectorCoordinates(double[] vectorCoordinates) {
            this.vectorCoordinates = vectorCoordinates;
        }

        public double computeScalarProduct(Vector vector){
            double leftProduct = this.vectorCoordinates[0]*vector.getVectorCoordinates()[0];
            double rightProduct = this.vectorCoordinates[1]*vector.getVectorCoordinates()[1];

            return leftProduct + rightProduct;
        }

        public double computeNorm(){
            return
                    Math.sqrt(Math.pow(this.vectorCoordinates[0],2) + Math.pow(this.vectorCoordinates[1],2));
        }

        @Override
        public String toString() {
            return "Vector: "+pointA.toString()+" "+pointB.toString()+" ("+vectorCoordinates[0]+","+vectorCoordinates[1]+")";
        }
    }
    // </editor-fold>

}
