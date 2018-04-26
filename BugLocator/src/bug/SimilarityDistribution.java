package bug;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;
import property.Property;

public class SimilarityDistribution
{
    private int fileCount = Property.getInstance().getFileCount();
    private String workDir = Property.getInstance().getWorkDir() +
            Property.getInstance().getSeparator();

    public static void main(String[] args)
    {
        SimilarityDistribution graph = new SimilarityDistribution();
        try
        {
            graph.distribute();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void distribute()
            throws Exception, IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(this.workDir +
                "BugSimilarity.txt"));
        String line = null;
        Hashtable<Integer, TreeSet<String>> fixedTable = getFixedTable();
        Hashtable<String, Integer> idTable = getFileIdTable();

        FileWriter writer = new FileWriter(this.workDir +
                "SimiScore.txt");
        while ((line = reader.readLine()) != null)
        {
            float[] similarValues = new float[this.fileCount];
            String idStr = line.substring(0, line.indexOf(";"));
            String vectorStr = line.substring(line.indexOf(";") + 1).trim();
            Integer id = Integer.valueOf(Integer.parseInt(idStr));
            String[] values = vectorStr.split(" ");
            String[] arrayOfString1;
            int j = (arrayOfString1 = values).length;
            for (int i = 0; i < j; i++)
            {
                String value = arrayOfString1[i];
                String[] singleValues = value.split(":");
                if (singleValues.length == 2)
                {
                    Integer simBugId = Integer.valueOf(Integer.parseInt(singleValues[0]));
                    float sim = Float.parseFloat(singleValues[1]);
                    TreeSet<String> fileSet = (TreeSet)fixedTable.get(simBugId);
                    if (fileSet == null) {
                        System.out.println(simBugId);
                    }
                    Iterator<String> fileSetIt = fileSet.iterator();
                    int size = fileSet.size();
                    float singleValue = sim / size;
                    while (fileSetIt.hasNext())
                    {
                        String name = (String)fileSetIt.next();
                        Integer fileId = (Integer)idTable.get(name);
                        if (fileId == null) {
                            System.err.println(name);
                        }
                        similarValues[fileId.intValue()] += singleValue;
                    }
                }
            }
            String output = id + ";";
            for (int i = 0; i < this.fileCount; i++) {
                if (similarValues[i] != 0.0F) {
                    output = output + i + ":" + similarValues[i] + " ";
                }
            }
            writer.write(output.trim() + Property.getInstance().getLineSeparator());
            writer.flush();
        }
        writer.close();
    }

    public Hashtable<Integer, TreeSet<String>> getFixedTable()
            throws IOException
    {
        Hashtable<Integer, TreeSet<String>> idTable = new Hashtable();

        BufferedReader reader = new BufferedReader(new FileReader(this.workDir +
                "FixLink.txt"));
        String line = null;
        while ((line = reader.readLine()) != null)
        {
            String[] values = line.split("\t");
            Integer id = Integer.valueOf(Integer.parseInt(values[0]));
            String name = values[1].trim();
            if (!idTable.containsKey(id)) {
                idTable.put(id, new TreeSet());
            }
            ((TreeSet)idTable.get(id)).add(name);
        }
        return idTable;
    }

    public Hashtable<String, Integer> getFileIdTable()
            throws IOException
    {
        Hashtable<String, Integer> idTable = new Hashtable();

        BufferedReader reader = new BufferedReader(new FileReader(this.workDir +
                "ClassName.txt"));
        String line = null;
        while ((line = reader.readLine()) != null)
        {
            String[] values = line.split("\t");
            Integer id = Integer.valueOf(Integer.parseInt(values[0]));
            String name = values[1].trim();
            idTable.put(name, id);
        }
        return idTable;
    }
}
