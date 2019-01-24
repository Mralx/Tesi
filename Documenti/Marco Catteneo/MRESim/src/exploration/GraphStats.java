package exploration;

import java.io.*;
import java.util.ArrayList;

class GraphStats {

    static void degree(String src_file, String dest_file){

        int min_d=100, max_d=0, d=0;
        double avg_d=0;
        ArrayList<Integer> degrees = new ArrayList<>();
        File file;
        FileWriter fw;
        BufferedWriter bw;
        BufferedReader br;

        try{
            file = new File(dest_file);
            fw =  new FileWriter(file.getAbsoluteFile(),true);
            br = new BufferedReader(new FileReader(src_file));
            bw = new BufferedWriter(fw);

            String line= br.readLine();
            while(line!=null){
                line = line.substring(line.indexOf('f'),line.indexOf('d'));
                line = line.substring(line.indexOf('('),line.lastIndexOf(")"));
                while(line.contains("(")){
                    d+=1;
                    line = line.substring(line.indexOf("(")+1);
                }
                //list of degrees at each time elapsed
                degrees.add(d);
                //average degree through incremental mean
                avg_d = (d+degrees.size()*avg_d)/(degrees.size()+1);
                //update min and max
                if(d<min_d)
                    min_d = d;
                if(d>max_d)
                    max_d = d;

                d = 0;
                line = br.readLine();
            }

            bw.write("Degree stats: total count = "+degrees.size()+
                    ", min = "+min_d+
                    ", max = "+max_d+
                    ", average = "+avg_d);
            bw.newLine();
            bw.write(degrees.toString());
            bw.newLine();
            bw.close();
            fw.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
