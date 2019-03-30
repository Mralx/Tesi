package exploration.graph;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class GraphEdit {

    public static void main(String args[]) {

        String oldFileName = "C:\\Users\\Alex\\Desktop\\Tesi\\Documenti\\Marco Catteneo\\DataAnalysis\\Reserve\\prova.txt";
        String newFileName = "C:\\Users\\Alex\\Desktop\\Tesi\\Documenti\\Marco Catteneo\\DataAnalysis\\Reserve\\tmpprova.txt";
        String agents = "ABCD";

        //GraphStats.degree(System.getProperty("user.dir")+"/logs/frontA.txt",
        //        System.getProperty("user.dir")+"/logs/stats.txt");
        //editFile(oldFileName, newFileName);
        //editPng(60,1, newFileName);
        //editCallFrontiersFile();
        //for(int i=0; i<agents.length(); i++){
        //    editAgentFrontPng(agents.charAt(i),9);
        //    editAgentFrontFile(agents.charAt(i));
        //}
        shortestPathGraph('A',20);
    }

    private static void editAgentFrontPng(char agentName, int n) {
        BufferedImage bi;
        String filename = System.getProperty("user.dir") + "/logs/front" + agentName +".txt";
        try {
            bi = ImageIO.read(new File(System.getProperty("user.dir") + "/agentFrontiers.png"));
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            int x=0, y=0;
            //print agent positions from time 1 to time n-1
            line = br.readLine();
            while(line!= null) {
                line = line.substring(5);
                int timeElapsed = Integer.parseInt(line.substring(0, line.indexOf(" ")));
                line = line.substring(line.indexOf('[')+1, line.indexOf(']'));
                x = Integer.parseInt(line.substring(0,line.indexOf(',')));
                y = Integer.parseInt(line.substring(line.indexOf(',')+1));

                if(timeElapsed == n)
                    break;

                //check if near another marked position
                boolean dirty = false;
                for(int x_i=-7; x_i < 8; x_i++){
                    for(int y_i=-7; y_i < 8; y_i++){
                        if(bi.getRGB(x+x_i,y+y_i)==Color.RED.getRGB())
                            dirty = true;
                    }
                }

                //if near another position, don't print it. Used to avoid overlap
                if(!dirty){
                    for(int x_i=-1; x_i < 2; x_i++){
                        for(int y_i=-1; y_i < 2; y_i++){
                            bi.setRGB(x+x_i, y+y_i,Color.RED.getRGB());
                        }
                    }
                }
                line = br.readLine();
            }

            //mark differently the actual position of the robot, i.e. at time n
            for(int x_i=-1; x_i < 2; x_i++){
                for(int y_i=-1; y_i < 2; y_i++){
                    bi.setRGB(x+x_i, y+y_i,Color.BLUE.getRGB());
                }
            }

            //print frontiers at time n
            line = br.readLine();
            line = line.substring(5);   //same as above
            line = line.substring(line.indexOf(']')+1);
            line = line.substring(line.indexOf('('),line.indexOf(']')-1);
            while(line.contains("(")){
                line = line.substring(line.indexOf("("));
                x = Integer.parseInt(line.substring(line.indexOf('(')+1,line.indexOf(',')));
                y = Integer.parseInt(line.substring(line.indexOf(',')+1,line.indexOf(')')));
                for(int x_i=-1; x_i < 2; x_i++)
                    for (int y_i = -1; y_i < 2; y_i++)
                        bi.setRGB(x + x_i, y + y_i, Color.GREEN.getRGB());
                line = line.substring(line.indexOf(")"));
            }

            File file = new File(System.getProperty("user.dir")+"/agentFrontiers.png");
            ImageIO.write(bi, "png",file);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private static void editAgentFrontFile(char agentName){
        String filename = System.getProperty("user.dir") + "/logs/agentFrontiers" + agentName +"2.txt";
        FileWriter fw;
        BufferedWriter bw;

        try {
            File file;
            file = new File(System.getProperty("user.dir") + "/logs/tmp_agentFrontiers" + agentName +"2.txt");
            fw = new FileWriter(file.getAbsoluteFile(), true);

            BufferedReader br = new BufferedReader(new FileReader(filename));
            bw = new BufferedWriter(fw);
            String line, next_line;
            line = br.readLine();
            while (line != null) {
                next_line = br.readLine();
                while(next_line != null &&
                        line.substring(line.indexOf("]")+1).
                        equals(next_line.substring(next_line.indexOf("]")+1))){
                    next_line = br.readLine();
                }
                bw.write(line);
                bw.newLine();
                line = next_line;
            }

            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void editFile(String oldFileName, String newFileName){
        FileWriter fw;
        BufferedWriter bw;

        try {
            File file;
            file = new File(newFileName);
            fw = new FileWriter(file.getAbsoluteFile(), true);

            BufferedReader br = new BufferedReader(new FileReader(oldFileName));
            bw = new BufferedWriter(fw);
            String line;
            while ((line = br.readLine()) != null) {
                //line = line.substring(line.indexOf("]]")+2);
                //line = line.substring(line.indexOf("]]")+2);
                bw.write(line.substring(line.indexOf("[[")+1, line.indexOf("]]")) + "]");
                bw.newLine();
            }

            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void editPng(int n, int env, String filename){
        BufferedImage bi = null;
        try{
            bi = ImageIO.read(new File(System.getProperty("user.dir")+"/environments/Tesi/env_"+env+".png"));
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            int x, y;
            for(int i=0; i<n; i++){
                line=br.readLine();
                if(line == null)
                    break;
                while(line.contains("[")){
                    if(line.contains("$"))
                        break;
                    x = Integer.parseInt(line.substring(line.indexOf('[')+1,line.indexOf(',')));
                    y = Integer.parseInt(line.substring(line.indexOf(',')+1,line.indexOf(']')));
                    line = line.substring(line.indexOf(']')+1);
                    if(line.contains("["))
                        line = line.substring(line.indexOf('['));

                    for(int x_i=-1; x_i < 2; x_i++){
                        for(int y_i=-1; y_i < 2; y_i++){
                            if(i==n-1)
                                bi.setRGB(x+x_i, y+y_i,Color.GREEN.getRGB());
                            else
                                bi.setRGB(x+x_i, y+y_i,Color.RED.getRGB());
                        }
                    }
                }
            }

            File file = new File(System.getProperty("user.dir")+"/image.png");
            ImageIO.write(bi, "png",file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void editCallFrontiersFile(){
        String oldFN = System.getProperty("user.dir")+"/logs/callFrontiers.txt";
        String newFN = System.getProperty("user.dir")+"/logs/tmp-callFrontiers.txt";

        FileWriter fw;
        BufferedWriter bw;

        try {
            File file;
            file = new File(newFN);
            fw = new FileWriter(file.getAbsoluteFile(), true);

            BufferedReader br = new BufferedReader(new FileReader(oldFN));
            bw = new BufferedWriter(fw);
            String line1, line2, c_line1, c_line2;
            line1 = br.readLine();
            while ((line2 = br.readLine()) != null) {
                c_line1 = line1.substring(line1.indexOf(" "));
                c_line2 = line2.substring(line2.indexOf(" "));
                if(!c_line1.equals(c_line2)){
                    bw.write(line1);
                    bw.newLine();
                    line1 = line2;
                }
            }

            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void shortestPathGraph(char agentName, int n){

        String filename = System.getProperty("user.dir")+"/logs/front"+agentName+".txt";
        String newFilename = System.getProperty("user.dir")+"/logs/spg "+agentName+".txt";
        Builder builder = new Builder();

        try {
            File file = new File(newFilename);
            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedReader br = new BufferedReader(new FileReader(filename));
            BufferedWriter bw = new BufferedWriter(fw);
            String line  = br.readLine();
            int i = 2;

            while (line != null && i<n) {
                line = br.readLine();
                //legge una linea, parsa il nodo, lo aggiunge al grafo
                builder.parseLine(line);
                i++;
            }

            ExplorationGraph graph = builder.getGraph();
            for(SimpleNode node : graph.getNodeMap().keySet()){
                bw.write(graph.getNode(node).toString());
                bw.newLine();
            }

            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}


