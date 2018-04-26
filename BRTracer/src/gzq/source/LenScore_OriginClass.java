package gzq.source;

import utils.Utility;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;

public class LenScore_OriginClass {

    public static int B = 50;

    public static Double getNormValue(Double x, Double max, Double min, Double median) {
        return B *  (x - median) /  (max - min);
    }

    public static double getLenScore(double len) {
        return (Math.exp(len) / (1 + Math.exp(len)));
    }

    public static void computeLenScore() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + "TermInfo_OriginClass.txt"));
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        Integer[] lens = new Integer[Utility.originFileCount];
        int i = 0;
        Hashtable<String, Integer> lensTable = new Hashtable<String, Integer>();
        int count = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            String[] values = line.split(";");
            String name = values[0].substring(0, values[0].indexOf("\t"));
            Integer len = Integer.parseInt(values[0].substring(values[0]
                    .indexOf("\t") + 1));
            lensTable.put(name, len);
            lens[i++] = len;
            if (len != 0)
                count++;
            if (len > max) {
                max = len;
            }
            if (len < min) {
            	min = len;
            }
        }
        double low = min;
        double high = max;
        Collections.sort(Arrays.asList(lens));
        double median;
        if( lens.length%2==0 )
        	median = (lens[lens.length/2]+lens[lens.length/2+1])/2;
        else
        	median = lens[lens.length/2+1];
        int n = 0;
        FileWriter writer = new FileWriter(Utility.outputFileDir + "LengthScore.txt");
        for (String key : lensTable.keySet()) {
            double len = (double)lensTable.get(key);
            double score = 0.0;
            double nor = getNormValue(len, max, min, median);
            if (len != 0) {
                if (len >= low && len <= high) {
                    score = getLenScore(nor);
                    n++;
                } else if (len < low) {
                    score = 0.5;
                } else {
                    score = 1.0;
                }
            } else {
                score = 0.0;
            }
            writer.write(key + "\t" + score + "\r\n");
            writer.flush();
        }
        writer.close();
    }
}
