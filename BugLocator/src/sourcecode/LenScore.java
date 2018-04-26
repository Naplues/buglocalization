package sourcecode;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import property.Property;

public class LenScore
{
    private String workDir = Property.getInstance().getWorkDir() +
            Property.getInstance().getSeparator();
    private int fileCount = Property.getInstance().getFileCount();

    public static void main(String[] args)
    {
        LenScore score = new LenScore();
        try
        {
            score.computeLenScore();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void computeLenScore()
            throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(this.workDir +
                "TermInfo.txt"));
        String line = null;
        int max = Integer.MIN_VALUE;
        int[] lens = new int[this.fileCount];
        int i = 0;
        Hashtable<String, Integer> lensTable = new Hashtable();
        int count = 0;
        while ((line = reader.readLine()) != null)
        {
            String[] values = line.split(";");
            String name = values[0].substring(0, values[0].indexOf("\t"));
            Integer len = Integer.valueOf(Integer.parseInt(values[0].substring(
                    values[0].indexOf("\t") + 1)));
            lensTable.put(name, len);
            lens[(i++)] = len.intValue();
            if (len.intValue() != 0) {
                count++;
            }
            if (len.intValue() > max) {
                max = len.intValue();
            }
        }
        int sum = 0;
        for (int j = 0; j < lens.length; j++) {
            sum += lens[j];
        }
        double average = sum / count;
        double squareDevi = 0.0D;
        Hashtable<Integer, Integer> statTable = new Hashtable();
        for (int j = 0; j < lens.length; j++) {
            if (lens[j] != 0)
            {
                int index = lens[j] / 10;
                if (statTable.containsKey(Integer.valueOf(index)))
                {
                    int l = ((Integer)statTable.get(Integer.valueOf(index))).intValue();
                    l++;
                    statTable.remove(Integer.valueOf(index));
                    statTable.put(Integer.valueOf(index), Integer.valueOf(l));
                }
                else
                {
                    statTable.put(Integer.valueOf(index), Integer.valueOf(1));
                }
            }
        }
        for (int j = 0; j < lens.length; j++) {
            if (lens[j] != 0) {
                squareDevi += (lens[j] - average) * (lens[j] - average);
            }
        }
        double standardDevi = Math.sqrt(squareDevi / count);
        double low = average - 3.0D * standardDevi;
        double high = average + 3.0D * standardDevi;

        int min = 0;
        if (low > 0.0D) {
            min = (int)low;
        }
        int n = 0;
        FileWriter writer = new FileWriter(this.workDir + "LengthScore.txt");
        int count1 = 0;
        for (String key : lensTable.keySet())
        {
            int len = ((Integer)lensTable.get(key)).intValue();
            double score = 0.0D;
            double nor = getNormValue(len, high, min);
            if (len != 0)
            {
                if ((len > low) && (len < high))
                {
                    score = getLenScore(nor);
                    n++;
                }
                else if (len < low)
                {
                    score = 0.5D;
                }
                else
                {
                    score = 1.0D;
                }
            }
            else {
                score = 0.0D;
            }
            if (nor > 6.0D) {
                nor = 6.0D;
            }
            if (score < 0.5D) {
                score = 0.5D;
            }
            if (score < 0.9D) {
                count1++;
            }
            writer.write(key + "\t" + score +
                    Property.getInstance().getLineSeparator());
            writer.flush();
        }
        writer.close();
    }

    private float getNormValue(int x, double max, double min)
    {
        return 6.0F * (float)(x - min) / (float)(max - min);
    }

    public double getLenScore(double len)
    {
        return Math.exp(len) / (1.0D + Math.exp(len));
    }
}
