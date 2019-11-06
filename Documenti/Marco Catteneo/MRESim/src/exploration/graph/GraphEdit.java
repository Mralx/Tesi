package exploration.graph;

import config.Constants;
import exploration.SimulationFramework;
import gui.MainGUI;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.List;

public class GraphEdit {

    public static void main(String args[]) {
        /*
        int env = 6;
        //GraphHandler.test();
        drawDiscretizedGraph(env);
        drawDiscretizedGraphMetrics("Closeness", env);
        drawDiscretizedGraphMetrics("Betweenness", env);
        String agents = "ABCDEFGHI";
        char agentName;
        int teamSize = 5;
        String expAlgorithm = "ProactiveReserve";
        Builder builder = new Builder();
        String dataFile, spgFile, spgStatsFile, graphFile, gStatsFile, statsImage;
        String vgFile, vgStatsFile, vgMergedFile = null, vgMergedStatsFile = null;

        /*
        try {
            for (int n = 65; n<= 65 ; n++){
            //while (env <= 5) {
                while (teamSize <= 5) {
                    for (int i = 0; i < teamSize-1; i++) {
                        agentName = agents.charAt(i);
                        dataFile = System.getProperty("user.dir") + "/logs/" + expAlgorithm + "/data/"
                                + env + "/front" + agentName + "_" + teamSize + ".txt";
                        spgFile = System.getProperty("user.dir") + "/logs/" + expAlgorithm + "/spg/"
                                + env + "/" + agentName + "_" + teamSize + " " + n + ".txt";
                        spgStatsFile = System.getProperty("user.dir") + "/logs/" + expAlgorithm + "/spg/"
                                + env + "/" + agentName + "_" + teamSize + " " + n + " stats.txt";
                        vgFile = System.getProperty("user.dir") + "/logs/" + expAlgorithm + "/vg/"
                                + env + "/" + agentName + "_" + teamSize + " " + n + "test.txt";
                        vgStatsFile = System.getProperty("user.dir") + "/logs/" + expAlgorithm + "/vg/"
                                + env + "/" + agentName + "_" + teamSize + " " + n + " stats test.txt";
                        vgMergedFile = System.getProperty("user.dir") + "/logs/" + expAlgorithm + "/vg/"
                                + env + "/" + teamSize + " " + n + "merged.txt";
                        vgMergedStatsFile = System.getProperty("user.dir") + "/logs/" + expAlgorithm + "/vg/"
                                + env + "/" + teamSize + " " + n + " stats merged.txt";
                        graphFile = System.getProperty("user.dir") + "/logs/" + expAlgorithm + "/g/"
                                + env + "/" + agentName + "_" + teamSize + " " + n + ".txt";
                        gStatsFile = System.getProperty("user.dir") + "/logs/" + expAlgorithm + "/g/"
                                + env + "/" + agentName + "_" + teamSize + " " + n + " stats.txt";
                        statsImage = System.getProperty("user.dir") + "/logs/" + expAlgorithm + "/g/"
                                + env + "/" + agentName + "_" + teamSize + " " + n + ".png";


                        graphs(dataFile, vgFile, graphFile, vgStatsFile, gStatsFile, n, builder);
                        //editAgentFrontPng(statsImage,vgMergedStatsFile,env);
                        //drawFrontiersPng(env,
                        //        System.getProperty("user.dir") + "\\logs\\ProactiveReserve\\spg\\1\\" + agentName + "_5 "+n+".txt",
                        //        System.getProperty("user.dir") + "\\logs\\ProactiveReserve\\spg\\1\\" + agentName + "_5 "+n+" graph.png");
                        //        editPng(env,System.getProperty("user.dir")+"\\logs\\ProactiveReserve\\spg\\"+env+"\\" + agentName + "_5 "+n+".txt",
                        //                System.getProperty("user.dir") + "\\logs\\ProactiveReserve\\spg\\"+env+"\\" + agentName + "_5 "+n+" graph 0.png");

                        if(i==teamSize-2){
                            logTestGraph(vgFile,vgStatsFile,builder.getMergedGraph());
                            drawFrontiersPng(env,
                                    System.getProperty("user.dir") + "\\logs\\ProactiveReserve\\vg\\1\\graph.txt",
                                    System.getProperty("user.dir") + "\\logs\\ProactiveReserve\\vg\\1\\graph.png");
                        }



                    }
                        mergedPathG(vgMergedFile, vgMergedStatsFile, builder);
                        System.out.println("Completed team size " + teamSize + " for env " + env);
                        teamSize++;
                        builder = new Builder();
                }

                teamSize = 5;
                System.out.println("Completed env " + env);
                //env++;
            //}

            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        */
    }

    //disegna le metriche
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
            //while(!line.equals("Betweenness centrality:"))
                line = br.readLine();
            line = br.readLine();

            //while(!line.substring(0,9).equals("Betweenne")) {
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
                val = Double.parseDouble(line);
                //val = (val-300)/1200*5;   // use for betweenness
                val = val*1000;           // use for closeness
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
                    case 0:
                        color = Color.PINK.getRGB();
                        break;
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
                    case 6:
                        color = Color.CYAN.getRGB();
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

    private static void drawDiscretizedGraph(int n) {
        BufferedImage bi;
        String imageFilename = System.getProperty("user.dir") + "/logs/Discretization/"+n+" nodes test "+
                Constants.MIN_DISTANCE+" d"+Constants.DISCRETIZATION_STEP+".png";
        try {
            String statsFile = System.getProperty("user.dir") + "/logs/Discretization/" + n + " stats test "+
                    Constants.MIN_DISTANCE+" d"+Constants.DISCRETIZATION_STEP+".txt";
            bi = ImageIO.read(new File(System.getProperty("user.dir") + "/environments/Tesi/env_"+n+".png"));
            BufferedReader br = new BufferedReader(new FileReader(statsFile));
            String line;
            int x, y;

            line = br.readLine();
            while(!line.contains("["))
                line = br.readLine();

            while(line.contains("[")) {
                line = line.substring(line.indexOf('[') + 1, line.indexOf(']'));
                x = Integer.parseInt(line.substring(0, line.indexOf(',')));
                y = Integer.parseInt(line.substring(line.indexOf(',') + 1));

                for (int x_i = -1; x_i < 2; x_i++)
                    for (int y_i = -1; y_i < 2; y_i++)
                        bi.setRGB(x + x_i, y + y_i, Color.GREEN.getRGB());
                line = br.readLine();
            }
            File file = new File(imageFilename);
            ImageIO.write(bi, "png",file);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private static void drawDiscretizedGraphMetrics(String metric, int n) {
        BufferedImage bi;
        String imageFilename = System.getProperty("user.dir") + "/logs/Discretization/"+n+" "+metric+".png";
        try {
            String statsFile = System.getProperty("user.dir") + "/logs/Discretization/" + n + " stats test "+
                    Constants.MIN_DISTANCE+" d"+Constants.DISCRETIZATION_STEP+".txt";
            bi = ImageIO.read(new File(System.getProperty("user.dir") + "/environments/Tesi/env_"+n+".png"));
            BufferedReader br = new BufferedReader(new FileReader(statsFile));
            String line, line1;
            int x, y;
            Double val;

            //print agent positions from time 1 to time n-1
            line = br.readLine();
            while(!line.equals(metric+" centrality:"))
                line = br.readLine();
            line = br.readLine();

            while(line.contains("[")) {
                System.out.println("Parsed "+line);
                int color;
                line1 = line.substring(line.indexOf('[') + 1, line.indexOf(']'));
                x = Integer.parseInt(line1.substring(0, line1.indexOf(',')));
                y = Integer.parseInt(line1.substring(line1.indexOf(',') + 1));
                line = line.substring(line.indexOf(']') + 4);
                System.out.println("Parsed point "+x+","+y);
                line = line.replace(',','.');
                val = Double.parseDouble(line);
                if(metric.equals("Closeness"))
                    val = val*1000;           // use for closeness
                else
                    val = val*6/176827;
                System.out.println("Parsed value "+val);

                switch (val.intValue()){
                    case 0:
                        color = new Color(250,218,221).getRGB();
                        break;
                    case 1:
                        color = new Color(255,192,203).getRGB();
                        break;
                    case 2:
                        color = new Color(231,84,128).getRGB();
                        break;
                    case 3:
                        color = new Color(255,0,0).getRGB();
                        break;
                    case 4:
                        color = new Color(200,8,21).getRGB();
                        break;
                    case 5:
                        color = new Color(199,21,255).getRGB();
                        break;
                    case 6:
                        color = new Color(153,17,153).getRGB();
                        break;
                    default: color=Color.BLUE.getRGB();
                }

                int inf_limit=-1, sup_limit=2;
                for(int x_i=inf_limit; x_i < sup_limit; x_i++){
                    for(int y_i=inf_limit; y_i < sup_limit; y_i++){
                        bi.setRGB(x+x_i, y+y_i,color);
                    }
                }

                System.out.println("Image updated");
                line = br.readLine();
            }

            File file = new File(imageFilename);
            ImageIO.write(bi, "png",file);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    /*inutilizzato
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
     */

    //disegna il grafo completo, in verde le frontiere, rosso i nodi e in nero le frontiere disconnesse
    private static void editPng(int env, String filename, String destFilename){
        BufferedImage bi;
        try{
            bi = ImageIO.read(new File(System.getProperty("user.dir")+"/environments/Tesi/env_"+env+".png"));
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line=br.readLine();
            int x, y;
            while(line!=null) {
                boolean isF = line.contains("f");
                boolean isE = line.contains("{}");
                line = line.substring(line.indexOf('[') + 1, line.indexOf(']'));
                x = Integer.parseInt(line.substring(0, line.indexOf(',')));
                y = Integer.parseInt(line.substring(line.indexOf(',') + 1));
                for (int x_i = -1; x_i < 2; x_i++) {
                    for (int y_i = -1; y_i < 2; y_i++) {
                        if (isF)
                            bi.setRGB(x + x_i, y + y_i, Color.GREEN.getRGB());
                        else
                            bi.setRGB(x + x_i, y + y_i, Color.RED.getRGB());
                        if (isE)
                            bi.setRGB(x + x_i, y + y_i, Color.BLACK.getRGB());
                    }
                }
            line= br.readLine();
            }
            File file = new File(destFilename);
            ImageIO.write(bi, "png",file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //disegna il grafo completo, in verde le frontiere, rosso i nodi
    private static void drawFrontiersPng(int env, String filename, String destFilename){
        BufferedImage bi;
        try{
            bi = ImageIO.read(new File(System.getProperty("user.dir")+"/environments/Tesi/env_"+env+".png"));
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line=br.readLine();
            int x, y;
            while(line!=null) {
                boolean isF = line.contains("f");
                line = line.substring(line.indexOf('[') + 1, line.indexOf(']'));
                x = Integer.parseInt(line.substring(0, line.indexOf(',')));
                y = Integer.parseInt(line.substring(line.indexOf(',') + 1));
                for (int x_i = -1; x_i < 2; x_i++) {
                    for (int y_i = -1; y_i < 2; y_i++) {
                        if (isF)
                            bi.setRGB(x + x_i, y + y_i, Color.GREEN.getRGB());
                        else
                            bi.setRGB(x + x_i, y + y_i, Color.RED.getRGB());
                    }
                }
                line= br.readLine();
            }
            File file = new File(destFilename);
            ImageIO.write(bi, "png",file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void graphs(String filename, String spg, String g,
                               String spgStats, String gStats, int n, Builder builder){

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

            //pathG(g,gStats,builder);
            //shortestPathG(spg,spgStats,builder);
            visibilityPathG(spg,spgStats,builder);

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
            System.out.println("Retrieved g");
            logGraphStats(statsFile, fw, bw, graph);
            System.out.println("Stats computed for g");

        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void visibilityPathG(String vgFile, String statsFile, Builder builder) {
        System.out.println("Starting vg");
        try {
            File file = new File(vgFile);
            FileWriter fw = new FileWriter(file.getAbsoluteFile(), false);
            BufferedWriter bw = new BufferedWriter(fw);

            ExplorationGraph graph = builder.getVisibilityGraph();
            System.out.println("Retrieved vg");
            logGraphStats(statsFile, fw, bw, graph);
            System.out.println("Stats computed for vg");

        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void mergedPathG(String vgMergedFile, String mergedStatsFile, Builder builder) {
        System.out.println("Starting merged graph");
        try {
            File file = new File(vgMergedFile);
            FileWriter fw = new FileWriter(file.getAbsoluteFile(), false);
            BufferedWriter bw = new BufferedWriter(fw);

            VisibilityGraph graph = builder.getMergedGraph();
            System.out.println("Retrieved merged graph");
            logGraphStats(mergedStatsFile, fw, bw, graph);
            System.out.println("Stats computed for merged graph");

        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void logTestGraph(String vgFile, String statsFile, VisibilityGraph graph) {
        try {
            File file = new File(vgFile);
            FileWriter fw = new FileWriter(file.getAbsoluteFile(), false);
            BufferedWriter bw = new BufferedWriter(fw);

            logGraphStats(statsFile, fw, bw, graph);
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

        bw.close();
        fw.close();

        GraphStats stats = new GraphStats();
        stats.logStats(graph,statsFile);

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
    }


}


