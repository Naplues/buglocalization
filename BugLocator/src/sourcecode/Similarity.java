package sourcecode;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import property.Property;

public class Similarity
{
    Hashtable<String, Integer> fileIdTable = null;
    private String workDir = Property.getInstance().getWorkDir() +
            Property.getInstance().getSeparator();
    private String lineSparator = Property.getInstance().getLineSeparator();
    public int fileCount = Property.getInstance().getFileCount();
    public int codeTermCount = Property.getInstance().getWordCount();

    public Similarity()
            throws IOException
    {}

    public void compute()
            throws IOException
    {
        this.fileIdTable = getFileId();
        Hashtable<String, Integer> wordIdTable = getWordId();
        Hashtable<String, Integer> idcTable = getIDCTable();

        FileWriter writer = new FileWriter(this.workDir + "VSMScore.txt");
        BufferedReader readerId = new BufferedReader(new FileReader(this.workDir +
                "SortedId.txt"));
        String idLine = null;
        while ((idLine = readerId.readLine()) != null)
        {
            Integer bugId = Integer.valueOf(Integer.parseInt(idLine.substring(0,
                    idLine.indexOf("\t"))));
            BufferedReader readerBug = new BufferedReader(new FileReader(
                    this.workDir + "BugCorpus" +
                            Property.getInstance().getSeparator() + bugId +
                            ".txt"));
            String line = readerBug.readLine();

            String[] words = line.split(" ");

            Hashtable<String, Integer> wordTable = new Hashtable();
            String[] arrayOfString1;
            int j = (arrayOfString1 = words).length;
            for (int i = 0; i < j; i++)
            {
                String word = arrayOfString1[i];
                if (!word.trim().equals("")) {
                    if (wordTable.containsKey(word))
                    {
                        Integer count = (Integer)wordTable.get(word);
                        count = Integer.valueOf(count.intValue() + 1);
                        wordTable.remove(word);
                        wordTable.put(word, count);
                    }
                    else
                    {
                        wordTable.put(word, Integer.valueOf(1));
                    }
                }
            }
            int totalTermCount = 0;
            for (String word : wordTable.keySet())
            {
                id = (Integer)wordIdTable.get(word);
                if (id != null) {
                    totalTermCount += ((Integer)wordTable.get(word)).intValue();
                }
            }
            float[] bugVector = new float[this.codeTermCount];
            for (Object id = wordTable.keySet().iterator(); ((Iterator)id).hasNext();)
            {
                String word = (String)((Iterator)id).next();
                Integer id = (Integer)wordIdTable.get(word);
                if (id != null)
                {
                    Integer idc = (Integer)idcTable.get(word);
                    Integer count = (Integer)wordTable.get(word);
                    float tf = getTfValue(count.intValue(), totalTermCount);
                    float idf = getIdfValue(idc.intValue(), this.fileCount);
                    bugVector[id.intValue()] = (tf * idf);
                }
            }
            double norm = 0.0D;
            for (int i = 0; i < bugVector.length; i++) {
                norm += bugVector[i] * bugVector[i];
            }
            norm = Math.sqrt(norm);
            for (int i = 0; i < bugVector.length; i++) {
                bugVector[i] /= (float)norm;
            }
            float[] simValues = computeSimilarity(bugVector);

            StringBuffer buf = new StringBuffer();
            buf.append(bugId + ";");
            for (int i = 0; i < simValues.length; i++) {
                if (simValues[i] != 0.0F) {
                    buf.append(i + ":" + simValues[i] + " ");
                }
            }
            writer.write(buf.toString().trim() + this.lineSparator);
            writer.flush();
        }
        writer.close();
    }

    private float[] computeSimilarity(float[] bugVector)
            throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(this.workDir +
                "CodeVector.txt"));
        String line = null;
        float[] simValues = new float[this.fileCount];
        while ((line = reader.readLine()) != null)
        {
            String[] values = line.split(";");
            String name = values[0];
            Integer fileId = (Integer)this.fileIdTable.get(name);
            if (fileId == null) {
                System.out.println(name);
            }
            float[] codeVector = (float[])null;
            if (values.length != 1) {
                codeVector = getVector(values[1]);
            } else {
                codeVector = getVector(null);
            }
            float sim = 0.0F;
            for (int i = 0; i < codeVector.length; i++) {
                sim += bugVector[i] * codeVector[i];
            }
            simValues[fileId.intValue()] = sim;
        }
        return simValues;
    }

    private float[] getVector(String vecStr)
    {
        float[] vector = new float[this.codeTermCount];
        if (vecStr == null) {
            return vector;
        }
        String[] values = vecStr.split(" ");
        String[] arrayOfString1;
        int j = (arrayOfString1 = values).length;
        for (int i = 0; i < j; i++)
        {
            String str = arrayOfString1[i];
            Integer id = Integer.valueOf(Integer.parseInt(str.substring(0, str.indexOf(":"))));
            float w = Float.parseFloat(str.substring(str.indexOf(":") + 1));
            vector[id.intValue()] = w;
        }
        return vector;
    }

    private float getTfValue(int freq, int totalTermCount)
    {
        return (float)Math.log(freq) + 1.0F;
    }

    private float getIdfValue(double docCount, double totalCount)
    {
        return (float)Math.log(totalCount / docCount);
    }

    private Hashtable<String, Integer> getWordId()
            throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(this.workDir +
                "Wordlist.txt"));
        String line = null;
        Hashtable<String, Integer> wordIdTable = new Hashtable();
        while ((line = reader.readLine()) != null)
        {
            String[] values = line.split("\t");
            wordIdTable.put(values[0], Integer.valueOf(Integer.parseInt(values[1])));
        }
        reader.close();
        return wordIdTable;
    }

    private Hashtable<String, Integer> getIDCTable()
            throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(this.workDir +
                "IDC.txt"));
        String line = null;
        Hashtable<String, Integer> idcTable = new Hashtable();
        while ((line = reader.readLine()) != null)
        {
            String[] values = line.split("\t");
            idcTable.put(values[0], Integer.valueOf(Integer.parseInt(values[1])));
        }
        reader.close();
        return idcTable;
    }

    private Hashtable<String, Integer> getFileId()
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
}
