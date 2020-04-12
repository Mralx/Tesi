package exploration.graph;

import org.w3c.dom.css.CSSImportRule;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

public class GraphDrawer {

    private static void drawDiscretizedGraph(boolean edge) {
        BufferedImage bi;
        String imageFilename = System.getProperty("user.dir") + "/logs/disegni/d3v.png";
        boolean isFrontier = false;
        boolean draw = true;
        try {
            String statsFile = System.getProperty("user.dir") + "/logs/disegni/d3v.txt";
            bi = ImageIO.read(new File(System.getProperty("user.dir") + "/environments/Tesi/env_3.png"));
            BufferedReader br = new BufferedReader(new FileReader(statsFile));
            String line;
            int x, y;

            line = br.readLine();
            while(line!=null){
                isFrontier = line.charAt(0) == 'f';

                String[] split1 = line.split(";");
                if(split1.length!=2){
                    System.out.println(Arrays.toString(split1));
                    System.exit(-1);
                }
                String[] split2 = split1[1].split(" ");

                x = Integer.parseInt(split1[0].substring(split1[0].indexOf('[')+1, split1[0].indexOf(',')));
                y = Integer.parseInt(split1[0].substring(split1[0].indexOf(',')+1,split1[0].indexOf(']')));

                for (int x_i = -2; x_i < 3; x_i++)
                    for (int y_i = -2; y_i < 3; y_i++)
                        if(bi.getRGB(x + x_i, y + y_i)!=Color.WHITE.getRGB())
                            draw = false;

                if (draw)
                    for (int x_i = -2; x_i < 3; x_i++)
                        for (int y_i = -2; y_i < 3; y_i++)
                            if(isFrontier)
                                bi.setRGB(x + x_i, y + y_i, Color.RED.getRGB());
                            else
                                bi.setRGB(x + x_i, y + y_i, Color.GREEN.getRGB());

                draw = true;

                if (edge){
                Point start = new Point(x,y);
                    for(int i=0;i<split2.length;i++){
                        int x1 = Integer.parseInt(split2[i].substring(split2[i].indexOf('[')+1, split2[i].indexOf(',')));
                        int y1 = Integer.parseInt(split2[i].substring(split2[i].indexOf(',')+1,split2[i].indexOf(']')));
                        drawLine(start, new Point(x1,y1), bi);
                    }
                }

                line = br.readLine();

            }
            File file = new File(imageFilename);
            ImageIO.write(bi, "png",file);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void topologicalGraph() throws IOException {
        //GraphHandler.test(3);
        //parseTopoGraph(3);
        drawDiscretizedGraph(false);
    }

    private static void parseTopoGraph(int n) throws IOException {
        String statsFile = System.getProperty("user.dir") + "/logs/disegni/" + n + " deg v.txt";
        String writeFile = System.getProperty("user.dir") + "/logs/disegni/d3v.txt";
        FileReader fr = new FileReader(statsFile);
        BufferedReader br = new BufferedReader(fr);
        FileWriter fw = new FileWriter(writeFile, true);

        String line = br.readLine();
        String sub1;
        while(line!=null){
            line = line.substring(3);
            if(line.charAt(0)=='(')
                line = "f ["+line.substring(1);
            line = line.replace('(','[');
            line = line.replace(')',']');
            //line = line.substring(line.indexOf('['));
            sub1 = line.substring(line.indexOf(']')+1,line.indexOf("[["));
            line = line.replace(sub1, ";");
            line = line.replace(", "," ");
            line = line.replace("[[","[");
            line = line.replace("]]","]");
            fw.write(line+" ;\n");
            line = br.readLine();
        }
        fw.close();
        br.close();
        fr.close();
    }

    public static LinkedList<Point> pointsAlongSegment(Point p1, Point p2) {
        LinkedList<Point> pts = new LinkedList<Point>();
        int x, y;
        double angle = Math.atan2(p2.y-p1.y, p2.x-p1.x);

        for(int i=0; i<=p1.distance(p2); i++) {
            x = p1.x + (int)(i * Math.cos(angle));
            y = p1.y + (int)(i * Math.sin(angle));
            if(!pts.contains(new Point(x,y)))
                pts.add(new Point(x,y));
        }

        if(!pts.contains(p2))
            pts.add(p2);

        return pts;
    }

    public static void drawLine(Point start, Point end, BufferedImage image) {
        for(Point p : pointsAlongSegment(start, end))
            setPixel(p.x, p.y, image);
    }

    public static void setPixel(int row, int column, BufferedImage image) {
        if ((row < 0) || (column < 0)) return;
        try{
            image.setRGB(row, column, Color.black.getRGB());
        }
        catch(ArrayIndexOutOfBoundsException e) {
            System.out.println("Error: pixel out of image bounds (" + row + ", " + column + ")");
        }
    }

    public static void drawMetrics() throws IOException {
        HashMap<Point,Double> clos = new HashMap<>();
        HashMap<Point,Double> bet = new HashMap<>();

        String statsFile = System.getProperty("user.dir") + "/logs/disegni/vis stats.txt";
        FileReader fr = new FileReader(statsFile);
        BufferedReader br = new BufferedReader(fr);

        String line = br.readLine();
        int x, y;
        while (!line.contains("Bet")){
            line = br.readLine();
            String[] split = line.split(" ");
            x = Integer.parseInt(split[0].substring(1,split[0].indexOf(',')));
            y = Integer.parseInt(split[0].substring(split[0].indexOf(',')+1,split[0].indexOf(']')));
            clos.put(new Point(x,y),Double.parseDouble(split[1].replace(',','.')));
            line = br.readLine();
        }
        line = br.readLine();
        while (line!=null){
            String[] split = line.split(" ");
            x = Integer.parseInt(split[0].substring(1,split[0].indexOf(',')));
            y = Integer.parseInt(split[0].substring(split[0].indexOf(',')+1,split[0].indexOf(']')));
            bet.put(new Point(x,y),Double.parseDouble(split[1].replace(',','.')));
            line = br.readLine();
        }

        Double min = clos.values().stream().min(Double::compareTo).get();
        Double max = clos.values().stream().max(Double::compareTo).get();
        double delta = max-min;
        HashMap<Point, Double> norm_clos = new HashMap<>();
        HashMap<Point, Double> norm_bet = new HashMap<>();
        double norm_value;
        for(Point p : clos.keySet()){
            norm_value = (clos.get(p)-min)/delta;
            if(norm_value>0.75)
                norm_clos.put(p,norm_value);
        }

        Double minb = bet.values().stream().min(Double::compareTo).get();
        Double maxb = bet.values().stream().max(Double::compareTo).get();
        delta = maxb-minb;
        for(Point p : bet.keySet()){
            norm_value = (bet.get(p)-minb)/delta;
            if(norm_value>0.5)
                norm_bet.put(p,norm_value);
        }

        BufferedImage bi;
        String imageFilenameC = System.getProperty("user.dir") + "/logs/disegni/d3v c.png";
        String imageFilenameB = System.getProperty("user.dir") + "/logs/disegni/d3v b.png";
        bi = ImageIO.read(new File(System.getProperty("user.dir") + "/logs/disegni/d3v.png"));
        for(Point point : norm_clos.keySet()){

            boolean draw = true;
            for (int x_i = -2; x_i < 3; x_i++)
                for (int y_i = -2; y_i < 3; y_i++)
                    if(bi.getRGB(point.x + x_i, point.y + y_i)==Color.BLACK.getRGB())
                        draw = false;

            if (draw)
                for (int x_i = -2; x_i < 3; x_i++)
                    for (int y_i = -2; y_i < 3; y_i++){
                        if(norm_clos.get(point) == 1)
                            bi.setRGB(point.x + x_i, point.y + y_i, Color.BLUE.getRGB());
                        else
                            bi.setRGB(point.x + x_i, point.y + y_i, Color.CYAN.getRGB());
                    }
        }

        File closFile = new File(imageFilenameC);
        ImageIO.write(bi,"png",closFile);

        bi = ImageIO.read(new File(System.getProperty("user.dir") + "/logs/disegni/d3v.png"));
        for(Point point : norm_bet.keySet()){

            boolean draw = true;
            for (int x_i = -2; x_i < 3; x_i++)
                for (int y_i = -2; y_i < 3; y_i++)
                    if(bi.getRGB(point.x + x_i, point.y + y_i)==Color.BLACK.getRGB())
                        draw = false;

            if (draw)
                for (int x_i = -2; x_i < 3; x_i++)
                    for (int y_i = -2; y_i < 3; y_i++){
                        if(norm_bet.get(point) == 1)
                            bi.setRGB(point.x + x_i, point.y + y_i, Color.ORANGE.getRGB());
                        else
                            bi.setRGB(point.x + x_i, point.y + y_i, Color.YELLOW.getRGB());
                    }
        }

        File betFile = new File(imageFilenameB);
        ImageIO.write(bi,"png",betFile);

    }

    public static void main(String[] args) throws IOException {
        //drawDiscretizedGraph();
        //topologicalGraph();
        drawMetrics();
    }
}
