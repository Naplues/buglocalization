package gzq.source;

import utils.Utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CodeVector {

	public static void create() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + "TermInfo.txt"));
		FileWriter writer = new FileWriter(Utility.outputFileDir + "CodeVector.txt");
		String line;
		while ((line = reader.readLine()) != null) {
			String[] values = line.split(";");

			String name = values[0].substring(0, values[0].indexOf("\t"));
			if (values.length == 1) {
				System.out.println(name + ";");
				continue;
			}
			Integer totalTermCount = Integer.parseInt(values[0]
					.substring(values[0].indexOf("\t") + 1));
			String[] termInfos = values[1].split("\t");
			float[] vector = new float[Utility.sourceWordCount];
			for (String str : termInfos) {
				String[] strs = str.split(":");
				Integer termId = Integer.parseInt(strs[0]);
				Integer termCount = Integer.parseInt(strs[1].substring(0, strs[1].indexOf(" ")));
				Integer documentCount = Integer.parseInt(strs[1].substring(strs[1].indexOf(" ") + 1));

				float tf = Utility.getTfValue(termCount, totalTermCount);
				float idf = Utility.getIdfValue(documentCount, Utility.sourceFileCount);
				vector[termId] = tf * idf;
			}
			double norm = 0.0f;
			for (int i = 0; i < vector.length; i++) {
				norm += vector[i] * vector[i];
			}
			norm = Math.sqrt(norm);

			StringBuffer buf = new StringBuffer();
			buf.append(name + ";");
			for (int i = 0; i < vector.length; i++)
				if (vector[i] != 0.0f) {
					vector[i] = vector[i] / (float) norm;
					buf.append(i + ":" + vector[i] + " ");
				}
			writer.write(buf.toString() + Utility.lineSeparator);
			writer.flush();
		}
		writer.close();
	}
}
