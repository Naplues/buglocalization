package sourcecode;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.TreeSet;

import property.Property;

public class Indexer {
	private String workDir = Property.getInstance().getWorkDir()
			+ Property.getInstance().getSeparator();
	private String lineSparator = Property.getInstance().getLineSeparator();

	public static void main(String[] args) throws IOException {
		
	}

	public void index() throws IOException {
        // countTable: count how many times a word occurs in all files
		Hashtable<String, Integer> countTable = countDoc();
		Hashtable<String, Integer> idSet = new Hashtable<String, Integer>();
		int id = 0;
		FileWriter writerWord = new FileWriter(workDir + "Wordlist.txt");
		int wordCount = 0;
		for (String key : countTable.keySet()) {
			idSet.put(key, id);
			writerWord.write(key + "\t" + id + lineSparator);
			writerWord.flush();
			id++;
			wordCount++;
		}
		Property.getInstance().setWordCount(wordCount);
		writerWord.close();

        // IDC.txt tells how many time a word occurs in all files
		FileWriter writerDoc = new FileWriter(workDir + "IDC.txt");
		for (String key : countTable.keySet()) {
			writerDoc.write(key + "\t" + countTable.get(key) + lineSparator);
			writerDoc.flush();
		}
		writerDoc.close();

		BufferedReader reader = new BufferedReader(new FileReader(workDir
				+ "CodeCorpus.txt"));
		String line = null;
		FileWriter writer = new FileWriter(workDir + "TermInfo.txt");
		while ((line = reader.readLine()) != null) {
			String[] values = line.split("\t");
			String[] words = values[1].split(" ");
			int totalCount = 0;

			Hashtable<Integer, Integer> termTable = new Hashtable<Integer, Integer>();
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
			TreeSet<Integer> tmp = new TreeSet<Integer>();
			for (String word : words) {
				if (!word.trim().equals("")) {
					Integer termId = idSet.get(word);
					if (!tmp.contains(termId)) {
						tmp.add(termId);
						int termCount = termTable.get(termId);
                        // documentCount means how many times a word occurs in all files
						int documentCount = countTable.get(word);
						output.append(termId + ":" + termCount + " "
								+ documentCount + "\t");
					}
				}
			}
			writer.write(output.toString() + lineSparator);
			writer.flush();
		}
		writer.close();
	}

	public Hashtable<String, Integer> countDoc() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(workDir
				+ "CodeCorpus.txt"));
		String line = null;

		Hashtable<String, Integer> countTable = new Hashtable<String, Integer>();

		while ((line = reader.readLine()) != null) {
			String[] values = line.split("\t");
			String[] words = values[1].split(" ");
			TreeSet<String> wordSet = new TreeSet<String>();
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
}
