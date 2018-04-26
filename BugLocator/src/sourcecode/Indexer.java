package sourcecode;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;
import property.Property;

public class Indexer
{
    private String workDir = Property.getInstance().getWorkDir() +
            Property.getInstance().getSeparator();
    private String lineSparator = Property.getInstance().getLineSeparator();

    public static void main(String[] args)
            throws IOException
    {}

    public void index()
            throws IOException
    {
        Hashtable<String, Integer> countTable = countDoc();
        Hashtable<String, Integer> idSet = new Hashtable();
        int id = 0;
        FileWriter writerWord = new FileWriter(this.workDir + "Wordlist.txt");
        int wordCount = 0;
        for (String key : countTable.keySet())
        {
            idSet.put(key, Integer.valueOf(id));
            writerWord.write(key + "\t" + id + this.lineSparator);
            writerWord.flush();
            id++;
            wordCount++;
        }
        Property.getInstance().setWordCount(wordCount);
        writerWord.close();

        FileWriter writerDoc = new FileWriter(this.workDir + "IDC.txt");
        for (String key : countTable.keySet())
        {
            writerDoc.write(key + "\t" + countTable.get(key) + this.lineSparator);
            writerDoc.flush();
        }
        writerDoc.close();

        BufferedReader reader = new BufferedReader(new FileReader(this.workDir +
                "CodeCorpus.txt"));
        String line = null;
        FileWriter writer = new FileWriter(this.workDir + "TermInfo.txt");
        while ((line = reader.readLine()) != null)
        {
            String[] values = line.split("\t");
            String[] words = values[1].split(" ");
            int totalCount = 0;

            Hashtable<Integer, Integer> termTable = new Hashtable();
            String[] arrayOfString1;
            int j = (arrayOfString1 = words).length;
            Integer count;
            for (int i = 0; i < j; i++)
            {
                String word = arrayOfString1[i];
                if (!word.trim().equals(""))
                {
                    totalCount++;
                    termId = (Integer)idSet.get(word);
                    if (termTable.containsKey(termId))
                    {
                        count = (Integer)termTable.get(termId);
                        count = Integer.valueOf(count.intValue() + 1);
                        termTable.remove(termId);
                        termTable.put(termId, count);
                    }
                    else
                    {
                        termTable.put(termId, Integer.valueOf(1));
                    }
                }
            }
            StringBuffer output = new StringBuffer();
            output.append(values[0] + "\t" + totalCount + ";");
            Object tmp = new TreeSet();
            Integer termId = (count = words).length;
            for (Integer localInteger1 = 0; localInteger1 < termId; localInteger1++)
            {
                String word = count[localInteger1];
                if (!word.trim().equals(""))
                {
                    Integer termId = (Integer)idSet.get(word);
                    if (!((TreeSet)tmp).contains(termId))
                    {
                        ((TreeSet)tmp).add(termId);
                        int termCount = ((Integer)termTable.get(termId)).intValue();
                        int documentCount = ((Integer)countTable.get(word)).intValue();
                        output.append(termId + ":" + termCount + " " +
                                documentCount + "\t");
                    }
                }
            }
            writer.write(output.toString() + this.lineSparator);
            writer.flush();
        }
        writer.close();
    }

    public Hashtable<String, Integer> countDoc()
            throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(this.workDir +
                "CodeCorpus.txt"));
        String line = null;

        Hashtable<String, Integer> countTable = new Hashtable();
        Iterator localIterator;
        for (; (line = reader.readLine()) != null; localIterator.hasNext())
        {
            String[] values = line.split("\t");
            String[] words = values[1].split(" ");
            TreeSet<String> wordSet = new TreeSet();
            String[] arrayOfString1;
            int j = (arrayOfString1 = words).length;
            for (int i = 0; i < j; i++)
            {
                String word = arrayOfString1[i];
                if ((!word.trim().equals("")) && (!wordSet.contains(word))) {
                    wordSet.add(word);
                }
            }
            localIterator = wordSet.iterator(); continue;String word = (String)localIterator.next();
            if (countTable.containsKey(word))
            {
                Integer count = (Integer)countTable.get(word);
                count = Integer.valueOf(count.intValue() + 1);
                countTable.remove(word);
                countTable.put(word, count);
            }
            else
            {
                countTable.put(word, Integer.valueOf(1));
            }
        }
        return countTable;
    }
}
