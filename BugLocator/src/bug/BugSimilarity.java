package bug;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import property.Property;

public class BugSimilarity
{
    private int wordCount = Property.getInstance().getBugTermCount();
    private int bugReportCount = Property.getInstance().getBugReportCount();
    private String workDir = Property.getInstance().getWorkDir() +
            Property.getInstance().getSeparator();

    public static void main(String[] args)
    {
        BugSimilarity sim = new BugSimilarity();
        try
        {
            sim.computeSimilarity();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void computeSimilarity()
            throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(this.workDir +
                "SortedId.txt"));
        String line = null;
        int[] idArr = new int[this.bugReportCount];
        int index = 0;
        while ((line = reader.readLine()) != null)
        {
            String idStr = line.substring(0, line.indexOf("\t"));
            idArr[(index++)] = Integer.parseInt(idStr);
        }
        Hashtable<Integer, float[]> vectors = getVector();

        FileWriter writer = new FileWriter(this.workDir + "BugSimilarity.txt");
        for (int i = 0; i < this.bugReportCount; i++)
        {
            int firstId = idArr[i];
            float[] firstVector = (float[])vectors.get(Integer.valueOf(firstId));
            String output = firstId + ";";
            for (int j = 0; j < i; j++)
            {
                int secondId = idArr[j];
                float[] secondVector = (float[])vectors.get(Integer.valueOf(secondId));
                float similarity = getCosineValue(firstVector,
                        secondVector);
                output = output + secondId + ":" + similarity + " ";
            }
            writer.write(output.trim() +
                    Property.getInstance().getLineSeparator());

            writer.flush();
        }
        writer.close();
    }

    private float getCosineValue(float[] firstVector, float[] secondVector)
    {
        float len1 = 0.0F;
        float len2 = 0.0F;
        float product = 0.0F;
        for (int i = 0; i < this.wordCount; i++)
        {
            len1 += firstVector[i] * firstVector[i];
            len2 += secondVector[i] * secondVector[i];
            product += firstVector[i] * secondVector[i];
        }
        return (float)(product / (Math.sqrt(len1) * Math.sqrt(len2)));
    }

    public Hashtable<Integer, float[]> getVector()
            throws IOException
    {
        Hashtable<Integer, float[]> vectors = new Hashtable();

        BufferedReader reader = new BufferedReader(new FileReader(this.workDir +
                "BugVector.txt"));
        String line = null;
        while ((line = reader.readLine()) != null)
        {
            String idStr = line.substring(0, line.indexOf("."));
            String vectorStr = line.substring(line.indexOf(";") + 1).trim();
            Integer id = Integer.valueOf(Integer.parseInt(idStr));
            float[] vector = getVector(vectorStr);
            vectors.put(id, vector);
        }
        return vectors;
    }

    private float[] getVector(String vectorStr)
    {
        float[] vector = new float[this.wordCount];
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
}
