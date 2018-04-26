package gzq.source;

import property.Property;
import utils.Utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.TreeSet;

public class Indexer_OriginClass {
    /**
     *
     * @return
     * @throws IOException
     */
    public static Hashtable<String, Integer> countDoc() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + "CodeCorpus_OriginClass.txt"));
        // 计数表
        Hashtable<String, Integer> countTable = new Hashtable<>();
        String line;
        while ((line = reader.readLine()) != null) {
            String[] values = line.split("\t");
            String[] words = values[1].split(" ");
            TreeSet<String> wordSet = new TreeSet<>();
            for (String word : words) {
                if (!word.trim().equals("") && !wordSet.contains(word)) {
                    wordSet.add(word);
                }
            }
            for (String word : wordSet) {
                if (countTable.containsKey(word)) {
                    Integer count = countTable.get(word);
                    count++;
                    countTable.remove(word);
                    countTable.put(word, count);
                } else {
                    countTable.put(word, 1);
                }
            }
        }
        return countTable;
    }

    public static void index() throws IOException {
        Hashtable<String, Integer> countTable = countDoc();
        Hashtable<String, Integer> idSet = new Hashtable<>();
        int id = 0;
        for (String key : countTable.keySet()) {
            idSet.put(key, id);
            id++;
        }

        BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + "CodeCorpus_OriginClass.txt"));
        FileWriter writer = new FileWriter(Utility.outputFileDir + "TermInfo_OriginClass.txt");
        String line;
        while ((line = reader.readLine()) != null) {
            String[] values = line.split("\t");
            String[] words = values[1].split(" ");
            int totalCount = 0;

            Hashtable<Integer, Integer> termTable = new Hashtable<>();
            for (String word : words) {
                if (!word.trim().equals("")) {
                    totalCount++;
                    Integer termId = idSet.get(word);
                    if (termTable.containsKey(termId)) {
                        Integer count = termTable.get(termId);
                        count++;
                        termTable.remove(termId);
                        termTable.put(termId, count);
                    } else {
                        termTable.put(termId, 1);
                    }
                }
            }
            StringBuffer output = new StringBuffer();
            output.append(values[0] + "\t" + totalCount + ";");
            TreeSet<Integer> tmp = new TreeSet<>();
            for (String word : words) {
                if (!word.trim().equals("")) {
                    Integer termId = idSet.get(word);
                    if (!tmp.contains(termId)) {
                        tmp.add(termId);
                        int termCount = termTable.get(termId);
                        int documentCount = countTable.get(word);
                        output.append(termId + ":" + termCount + " " + documentCount + "\t");
                    }
                }
            }
            writer.write(output.toString() + Utility.lineSeparator);
            writer.flush();
        }
        writer.close();
    }
}
