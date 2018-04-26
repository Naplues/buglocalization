package gzq.source;

import utils.Utility;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.TreeSet;

public class Indexer {

    public static Hashtable<String, Integer> countDoc() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + "CodeCorpus.txt"));
        String line;
        Hashtable<String, Integer> countTable = new Hashtable<String, Integer>();

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
        // countTable: count how many times a word occurs in all files
		Hashtable<String, Integer> countTable = countDoc();
		Hashtable<String, Integer> idSet = new Hashtable<>();
		int id = 0;
		FileWriter writerWord = new FileWriter(Utility.outputFileDir + "Wordlist.txt");
		int wordCount = 0;
		for (String key : countTable.keySet()) {
			idSet.put(key, id);
			writerWord.write(key + "\t" + id + Utility.lineSeparator);
			writerWord.flush();
			id++;
			wordCount++;
		}
		Utility.sourceWordCount = wordCount;
		writerWord.close();

        // IDC.txt tells how many time a word occurs in all files
		FileWriter writerDoc = new FileWriter(Utility.outputFileDir + "IDC.txt");
		for (String key : countTable.keySet()) {
			writerDoc.write(key + "\t" + countTable.get(key) + Utility.lineSeparator);
			writerDoc.flush();
		}
		writerDoc.close();

		BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + "CodeCorpus.txt"));
		String line;
		FileWriter writer = new FileWriter(Utility.outputFileDir + "TermInfo.txt");
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
                        // documentCount means how many times a word occurs in all files
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
