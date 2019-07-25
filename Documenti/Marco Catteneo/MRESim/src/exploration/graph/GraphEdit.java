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

        int env = 1;
        String agents = "ABCDEFGHI";
        char agentName;
        int teamSize = 5;
        String expAlgorithm = "ProactiveReserve";

        try {
            for (int n = 65; n<= 65 ; n++){
            //while (env <= 5) {
                while (teamSize <= 5) {
                    for (int i = 0; i < teamSize-1; i++) {
                        agentName = agents.charAt(i);
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


                        //graphs(dataFile, spgFile, graphFile, spgStatsFile, gStatsFile, n);
                        editAgentFrontPng(statsImage,gStatsFile,env);
                        //drawFrontiersPng(env,
                        //        System.getProperty("user.dir") + "\\logs\\ProactiveReserve\\g\\1\\" + agentName + "_5 "+n+".txt",
                        //        System.getProperty("user.dir") + "\\logs\\ProactiveReserve\\g\\1\\" + agentName + "_5 "+n+" graph.png");
                        //        editPng(env,System.getProperty("user.dir")+"\\logs\\ProactiveReserve\\g\\"+env+"\\" + agentName + "_5 "+n+".txt",
                        //                System.getProperty("user.dir") + "\\logs\\ProactiveReserve\\g\\"+env+"\\" + agentName + "_5 "+n+" graph.png");

                    }
                        System.out.println("Completed team size " + teamSize + " for env " + env);
                        teamSize++;
                }

                teamSize = 5;
                System.out.println("Completed env " + env);
                //env++;
            //}

            }
        } catch (NullPointerException e) {
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
            //visibilityPathG(spg,spgStats,builder);

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


