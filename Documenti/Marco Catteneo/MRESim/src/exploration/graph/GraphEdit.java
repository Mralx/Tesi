package exploration.graph;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GraphEdit {

    public static void main(String args[]) {

        int env = 4;
        char agentName = 'A';
        int teamSize = 6;
        String expAlgorithm = "ProactiveReserve";
        int n = 35;

        try {
            while (env <= 4) {
                while (teamSize <= 6) {
                    String dataFile = System.getProperty("user.dir") + "/logs/" + expAlgorithm + "/data/"
                            + env + "/front" + agentName + "_" + teamSize + ".txt";
                    String spgFile = System.getProperty("user.dir") + "/logs/" + expAlgorithm + "/spg/"
                            + env + "/" + agentName + "_" + teamSize + " " + n + ".txt";
                    String spgStatsFile = System.getProperty("user.dir") + "/logs/" + expAlgorithm + "/spg/"
                            + env + "/" + agentName + "_" + teamSize + " " + n + " stats.txt";
                    String graphFile = System.getProperty("user.dir") + "/logs/" + expAlgorithm + "/g/"
                            + env + "/" + agentName + "_" + teamSize + " " + n + ".txt";
                    String gStatsFile = System.getProperty("user.dir") + "/logs/" + expAlgorithm + "/g/"
                            + env + "/" + agentName + "_" + teamSize + " " + n + " stats.txt";
                    String statsImage = System.getProperty("user.dir") + "/logs/" + expAlgorithm + "/g/"
                            + env + "/" + agentName + "_" + teamSize + " " + n + ".png";


                    graphs(dataFile, spgFile, graphFile, spgStatsFile, gStatsFile, n);
                    editAgentFrontPng(statsImage,gStatsFile,env);

                    System.out.println("Completed team size " + teamSize + " for env " + env);
                    teamSize++;
                }
                teamSize = 5;
                System.out.println("Completed env " + env);
                env++;
            }
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }

    }

    private static void editAgentFrontPng(String imageFilename, String statsFilename,int env) {
        BufferedImage bi;
        try {
            bi = ImageIO.read(new File(System.getProperty("user.dir") + "/logs/ProactiveReserve/g/"+env+"/env_"+env+".png"));
            BufferedReader br = new BufferedReader(new FileReader(statsFilename));
            String line, line1;
            int x, y;
            Double val;

            //print agent positions from time 1 to time n-1
            line = br.readLine();
            while(!line.equals("Closeness centrality:"))
                line = br.readLine();
            line = br.readLine();

            while(!line.substring(0,9).equals("Closeness")) {
                System.out.println("Parsed "+line);
                boolean front = false;
                int color;
                if(line.contains("[")) {
                    line1 = line.substring(line.indexOf('[') + 1, line.indexOf(']'));
                    x = Integer.parseInt(line1.substring(0, line1.indexOf(',')));
                    y = Integer.parseInt(line1.substring(line1.indexOf(',') + 1));
                    line = line.substring(line.indexOf(']') + 4);
                }
                else{
                    front = true;
                    line1 = line.substring(line.indexOf('(') + 1, line.indexOf(')'));
                    x = Integer.parseInt(line1.substring(0, line1.indexOf(',')));
                    y = Integer.parseInt(line1.substring(line1.indexOf(',') + 1));
                    line = line.substring(line.indexOf(')') + 4);
                }
                System.out.println("Parsed point "+x+","+y+" and front is "+front);
                line = line.replace(',','.');
                val = Double.parseDouble(line)*1000;
                System.out.println("Parsed value "+val);

                /*
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
                */
                switch (val.intValue()){
                    case 1:
                        color = Color.YELLOW.getRGB();
                        break;
                    case 2:
                        color = Color.ORANGE.getRGB();
                        break;
                    case 3:
                        color = Color.RED.getRGB();
                        break;
                    case 4:
                        color = Color.MAGENTA.getRGB();
                        break;
                    case 5:
                        color = Color.BLUE.getRGB();
                        break;
                    default: color=Color.LIGHT_GRAY.getRGB();
                }

                int inf_limit=-1, sup_limit=2;
                for(int x_i=inf_limit; x_i < sup_limit; x_i++){
                    for(int y_i=inf_limit; y_i < sup_limit; y_i++){
                        bi.setRGB(x+x_i, y+y_i,color);
                    }
                }
                if(front)
                    bi.setRGB(x,y,Color.BLACK.getRGB());
                System.out.println("Image updated");
                line = br.readLine();
            }

            /*
            //mark differently the actual position of the robot, i.e. at time n
            for(int x_i=-1; x_i < 2; x_i++){
                for(int y_i=-1; y_i < 2; y_i++){
                    x_i *= val;
                    y_i *= val;
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
            */
            File file = new File(imageFilename);
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

    private static void graphs(String filename, String spg, String g,
                               String spgStats, String gStats, int n){

        Builder builder = new Builder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line  = br.readLine();
            int i = 2;

            while (line != null && i<n) {
                line = br.readLine();
                //legge una linea, parsa il nodo, lo aggiunge al grafo
                builder.parseLine(line);
                i++;
            }

            pathG(g,gStats,builder);
            //shortestPathG(spg,spgStats,builder);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void shortestPathG(String spgFile, String statsFile, Builder builder) {
        System.out.println("Starting spg");
        try {
            File file = new File(spgFile);
            FileWriter fw = new FileWriter(file.getAbsoluteFile(), false);
            BufferedWriter bw = new BufferedWriter(fw);

            ExplorationGraph graph = builder.getShortestPathGraph();
            System.out.println("Retrieved spg");
            logGraphStats(statsFile, fw, bw, graph);
            System.out.println("Stats computed for spg");

        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void pathG(String graphFile, String statsFile, Builder builder) {
        //System.out.println("Starting g");
        try {
            File file = new File(graphFile);
            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);

            ExplorationGraph graph = builder.getGraph();
            //System.out.println("Retrieved g");
            logGraphStats(statsFile, fw, bw, graph);
            //System.out.println("Stats computed for g");

        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void logGraphStats(String statsFile, FileWriter fw, BufferedWriter bw, ExplorationGraph graph) throws IOException {
        for(SimpleNode node : graph.getNodeMap().keySet()){
            bw.write(graph.getNode(node).toString());
            bw.newLine();
        }

        GraphStats stats = new GraphStats();
        stats.logStats(graph,statsFile);

        bw.close();
        fw.close();
    }

    private static void testPath(){
        Node a,b,c,d,e;
        a = new Node(0,0);
        b = new Node(2,0);
        c = new Node(2,2);
        d = new Node(0,2);
        e = new Node(3,1);
        a.addAdjacent(new SimpleNode(b),1);
        a.addAdjacent(new SimpleNode(d),1);

        b.addAdjacent(new SimpleNode(a),1);
        b.addAdjacent(new SimpleNode(c),1);
        b.addAdjacent(new SimpleNode(e),1);

        c.addAdjacent(new SimpleNode(b),1);
        c.addAdjacent(new SimpleNode(e),1);
        c.addAdjacent(new SimpleNode(d),1);


        d.addAdjacent(new SimpleNode(a),1);
        d.addAdjacent(new SimpleNode(c),1);

        e.addAdjacent(new SimpleNode(b),1);
        e.addAdjacent(new SimpleNode(c),1);

        ExplorationGraph graph = new ExplorationGraph();
        Map<SimpleNode, Node> nodeMap = new HashMap<>();
        nodeMap.put(new SimpleNode(a),a);
        nodeMap.put(new SimpleNode(b),b);
        nodeMap.put(new SimpleNode(c),c);
        nodeMap.put(new SimpleNode(d),d);
        nodeMap.put(new SimpleNode(e),e);
        graph.setNodeMap(nodeMap);

        /*
        for(SimpleNode node : graph.getNodeMap().keySet())
            for(SimpleNode arr : graph.getNodeMap().keySet()){
                System.out.println("Path from "+node.toString()+" to "+arr.toString());
                System.out.println(graph.getMultiplePaths(node,arr).toString());
            }
        */
        GraphStats stat = new GraphStats();
        Map<SimpleNode,Double> bet = stat.betweennessCentrality(graph);
        System.out.println(bet.toString());
        System.out.println(stat.spgMatrix.toString());
    }


}


