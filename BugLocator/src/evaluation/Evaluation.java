package evaluation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;
import property.Property;

public class Evaluation
{
    private String workDir = Property.getInstance().getWorkDir() +
            Property.getInstance().getSeparator();
    private String outputFile = Property.getInstance().getOutputFile();
    private int fileCount = Property.getInstance().getFileCount();
    private int bugCount = Property.getInstance().getBugReportCount();
    private float alpha = Property.getInstance().getAlpha();
    private String lineSparator = Property.getInstance().getLineSeparator();
    Hashtable<String, Integer> idTable = null;
    Hashtable<Integer, TreeSet<String>> fixTable = null;
    Hashtable<String, Double> lenTable = null;

    public Evaluation()
    {
        try
        {
            this.idTable = getFileId();
            this.fixTable = getFixLinkTable();
            this.lenTable = getLenScore();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
            throws IOException
    {
        new Evaluation().evaluate();
    }

    public void evaluate()
            throws IOException
    {
        BufferedReader VSMReader = new BufferedReader(new FileReader(this.workDir +
                "VSMScore.txt"));
        BufferedReader GraphReader = new BufferedReader(new FileReader(this.workDir +
                "SimiScore.txt"));

        int count = 0;
        FileWriter writer = new FileWriter(this.outputFile);
        Rank[] sort;
        int i;
        for (; count < this.bugCount; i < sort.length)
        {
            count++;
            String vsmLine = VSMReader.readLine();
            String vsmIdStr = vsmLine.substring(0, vsmLine.indexOf(";"));
            Integer vsmId = Integer.valueOf(Integer.parseInt(vsmIdStr));
            String vsmVectorStr = vsmLine.substring(vsmLine.indexOf(";") + 1);
            float[] vsmVector = getVector(vsmVectorStr);
            for (String key : this.lenTable.keySet())
            {
                Integer id = (Integer)this.idTable.get(key);
                Double score = (Double)this.lenTable.get(key);
                vsmVector[id.intValue()] *= score.floatValue();
            }
            vsmVector = normalize(vsmVector);
            String graphLine = GraphReader.readLine();
            String graphIdStr = graphLine.substring(0, graphLine.indexOf(";"));
            Integer graphId = Integer.valueOf(Integer.parseInt(graphIdStr));
            String graphVectorStr = graphLine
                    .substring(graphLine.indexOf(";") + 1);
            float[] graphVector = getVector(graphVectorStr);
            graphVector = normalize(graphVector);

            float[] finalR = combine(vsmVector, graphVector, this.alpha);
            sort = sort(finalR);

            TreeSet<String> fileSet = (TreeSet)this.fixTable.get(vsmId);
            Iterator<String> fileIt = fileSet.iterator();
            Hashtable<Integer, String> fileIdTable = new Hashtable();
            while (fileIt.hasNext())
            {
                String fileName = (String)fileIt.next();
                Integer fileId = (Integer)this.idTable.get(fileName);
                fileIdTable.put(fileId, fileName);
            }
            i = 0; continue;
            Rank rank = sort[i];
            if ((!fileIdTable.isEmpty()) &&
                    (fileIdTable.containsKey(Integer.valueOf(rank.id))))
            {
                writer.write(vsmId + "," + (String)fileIdTable.get(Integer.valueOf(rank.id)) + "," +
                        i + "," + rank.rank + this.lineSparator);
                writer.flush();
            }
            i++;
        }
        writer.close();
    }

    public Hashtable<String, Integer> getFileId()
            throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(this.workDir +
                "ClassName.txt"));
        String line = null;
        Hashtable<String, Integer> table = new Hashtable();
        while ((line = reader.readLine()) != null)
        {
            String[] values = line.split("\t");
            Integer idInteger = Integer.valueOf(Integer.parseInt(values[0]));
            String nameString = values[1].trim();
            table.put(nameString, idInteger);
        }
        return table;
    }

    public Hashtable<Integer, String> getFile()
            throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(this.workDir +
                "ClassName.txt"));
        String line = null;
        Hashtable<Integer, String> table = new Hashtable();
        while ((line = reader.readLine()) != null)
        {
            String[] values = line.split("\t");
            Integer idInteger = Integer.valueOf(Integer.parseInt(values[0]));
            String nameString = values[1].trim();
            table.put(idInteger, nameString);
        }
        return table;
    }

    public Hashtable<Integer, TreeSet<String>> getFixLinkTable()
            throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(this.workDir +
                "FixLink.txt"));
        String line = null;
        Hashtable<Integer, TreeSet<String>> table = new Hashtable();
        while ((line = reader.readLine()) != null)
        {
            String[] valueStrings = line.split("\t");
            Integer id = Integer.valueOf(Integer.parseInt(valueStrings[0]));
            String fileName = valueStrings[1].trim();
            if (!table.containsKey(id)) {
                table.put(id, new TreeSet());
            }
            ((TreeSet)table.get(id)).add(fileName);
        }
        return table;
    }

    private Rank[] sort(float[] finalR)
    {
        Rank[] R = new Rank[finalR.length];
        for (int i = 0; i < R.length; i++)
        {
            Rank rank = new Rank();
            rank.rank = finalR[i];
            rank.id = i;
            R[i] = rank;
        }
        R = insertionSort(R);
        return R;
    }

    private Rank[] insertionSort(Rank[] R)
    {
        for (int i = 0; i < R.length; i++)
        {
            int maxIndex = i;
            for (int j = i; j < R.length; j++) {
                if (R[j].rank > R[maxIndex].rank) {
                    maxIndex = j;
                }
            }
            Rank tmpRank = R[i];
            R[i] = R[maxIndex];
            R[maxIndex] = tmpRank;
        }
        return R;
    }

    public float[] combine(float[] vsmVector, float[] graphVector, float f)
    {
        float[] results = new float[this.fileCount];
        for (int i = 0; i < this.fileCount; i++) {
            results[i] = (vsmVector[i] * (1.0F - f) + graphVector[i] * f);
        }
        return results;
    }

    private float[] normalize(float[] array)
    {
        float max = Float.MIN_VALUE;
        float min = Float.MAX_VALUE;
        for (int i = 0; i < array.length; i++)
        {
            if (max < array[i]) {
                max = array[i];
            }
            if (min > array[i]) {
                min = array[i];
            }
        }
        float span = max - min;
        for (int i = 0; i < array.length; i++) {
            array[i] = ((array[i] - min) / span);
        }
        return array;
    }

    private float[] getVector(String vectorStr)
    {
        float[] vector = new float[this.fileCount];
        String[] values = vectorStr.split(" ");
        String[] arrayOfString1;
        int j = (arrayOfString1 = values).length;
        for (int i = 0; i < j; i++)
        {
            String value = arrayOfString1[i];
            String[] singleValues = value.split(":");
            if (singleValues.length == 2)
            {
                int index = Integer.parseInt(singleValues[0]);

                float sim = Float.parseFloat(singleValues[1]);
                vector[index] = sim;
            }
        }
        return vector;
    }

    private Hashtable<String, Double> getLenScore()
            throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(this.workDir +
                "LengthScore.txt"));
        String line = null;
        Hashtable<String, Double> lenTable = new Hashtable();
        while ((line = reader.readLine()) != null)
        {
            String[] values = line.split("\t");
            String name = values[0];
            Double score = Double.valueOf(Double.parseDouble(values[1]));
            lenTable.put(name, score);
        }
        reader.close();
        return lenTable;
    }
}
