package gzq.source;

import utils.Utility;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

public class Similarity {
	public static Hashtable<String, Integer> fileIdTable = null;

	private static float[] computeSimilarity(float[] bugVector) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + "CodeVector.txt"));
		String line;
		float[] simValues = new float[Utility.sourceFileCount];
		while ((line = reader.readLine()) != null) {
			String[] values = line.split(";");
			String name = values[0];
			Integer fileId = fileIdTable.get(name);
			if (fileId == null)
				System.out.println(name);
			float[] codeVector;
			if (values.length != 1)
				codeVector = getVector(values[1]);
			else
				codeVector = getVector(null);
			float sim = 0.0f;
			for (int i = 0; i < codeVector.length; i++)
				sim += bugVector[i] * codeVector[i];
			simValues[fileId] = sim;
		}
		return simValues;
	}

	private static float[] getVector(String vecStr) {
		float[] vector = new float[Utility.sourceWordCount];
		if (vecStr == null)
			return vector;
		String[] values = vecStr.split(" ");
		for (String str : values) {
			Integer id = Integer.parseInt(str.substring(0, str.indexOf(":")));
			float w = Float.parseFloat(str.substring(str.indexOf(":") + 1));
			vector[id] = w;
		}
		return vector;
	}

	private static Hashtable<String, Integer> getWordId() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + "Wordlist.txt"));
		String line;
		Hashtable<String, Integer> wordIdTable = new Hashtable<>();
		while ((line = reader.readLine()) != null) {
			String[] values = line.split("\t");
			wordIdTable.put(values[0], Integer.parseInt(values[1]));
		}
		reader.close();
		return wordIdTable;
	}

	private static Hashtable<String, Integer> getIDCTable() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + "IDC.txt"));
		String line;
		Hashtable<String, Integer> idcTable = new Hashtable<>();
		while ((line = reader.readLine()) != null) {
			String[] values = line.split("\t");
			idcTable.put(values[0], Integer.parseInt(values[1]));
		}
		reader.close();
		return idcTable;
	}

	private static Hashtable<String, Integer> getFileId() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + "MethodName.txt"));
		String line;
		Hashtable<String, Integer> table = new Hashtable<>();
		while ((line = reader.readLine()) != null) {
			String[] values = line.split("\t");
			Integer idInteger = Integer.parseInt(values[0]);
			String nameString = values[1].trim();
			table.put(nameString, idInteger);
		}
		return table;
	}


	public static void compute() throws IOException {
		fileIdTable = getFileId();
		Hashtable<String, Integer> wordIdTable = getWordId();
		Hashtable<String, Integer> idcTable = getIDCTable();

		FileWriter writer = new FileWriter(Utility.outputFileDir + "VSMScore.txt");
		BufferedReader readerId = new BufferedReader(new FileReader(Utility.outputFileDir + "SortedId.txt"));
		String idLine;
		while ((idLine = readerId.readLine()) != null) {
			Integer bugId = Integer.parseInt(idLine.substring(0,
					idLine.indexOf("\t")));
			BufferedReader readerBug = new BufferedReader(new FileReader(Utility.outputFileDir + "BugCorpus" + Utility.separator + bugId + ".txt"));
			String line = readerBug.readLine();
			String[] words = line.split(" ");

			Hashtable<String, Integer> wordTable = new Hashtable<>();
			for (String word : words) {
				if (!word.trim().equals("")) {
					if (wordTable.containsKey(word)) {
						Integer count = wordTable.get(word);
						count++;
						wordTable.remove(word);
						wordTable.put(word, count);
					} else {
						wordTable.put(word, 1);
					}
				}
			}
			int totalTermCount = 0;
			for (String word : wordTable.keySet()) {
				Integer id = wordIdTable.get(word);
				if (id != null)
					totalTermCount += wordTable.get(word);
			}
			float[] bugVector = new float[Utility.sourceWordCount];

			for (String word : wordTable.keySet()) {
				Integer id = wordIdTable.get(word);
				if (id != null) {
					Integer idc = idcTable.get(word);
					Integer count = wordTable.get(word);
					// TF-IDF值
					bugVector[id] = Utility.getTfValue(count, totalTermCount) * Utility.getIdfValue(idc, Utility.sourceFileCount);
				}
			}
			double norm = 0.0f;
			for (int i = 0; i < bugVector.length; i++) {
				norm += bugVector[i] * bugVector[i];
			}
			norm = Math.sqrt(norm);
			for (int i = 0; i < bugVector.length; i++) {
				bugVector[i] = bugVector[i] / (float) norm;
			}

			float[] simValues = computeSimilarity(bugVector);

			StringBuffer buf = new StringBuffer();
			buf.append(bugId + ";");
			for (int i = 0; i < simValues.length; i++)
				if (simValues[i] != 0.0f)
					buf.append(i + ":" + simValues[i] + " ");
			writer.write(buf.toString().trim() + Utility.lineSeparator);
			writer.flush();
		}
		writer.close();
	}
}
