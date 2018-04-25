package sourcecode;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import property.Property;

public class CodeVectorCreator {
	private String workDir = Property.getInstance().getWorkDir()
			+ Property.getInstance().getSeparator();
	private String lineSparator=Property.getInstance().getLineSeparator();
	
	public int fileCount = Property.getInstance().getFileCount();
	public int codeTermCount = Property.getInstance().getWordCount();


	public static void main(String[] args) throws IOException {
		new CodeVectorCreator().create();
	}

	public void create() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(workDir
				+ "TermInfo.txt"));
		String line = null;
		FileWriter writer = new FileWriter(workDir + "CodeVector.txt");
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
			float[] vector = new float[codeTermCount];
			for (String str : termInfos) {
				String[] strs = str.split(":");
				Integer termId = Integer.parseInt(strs[0]);
				Integer termCount = Integer.parseInt(strs[1].substring(0,
						strs[1].indexOf(" ")));
				Integer documentCount = Integer.parseInt(strs[1]
						.substring(strs[1].indexOf(" ") + 1));

				float tf = this.getTfValue(termCount, totalTermCount);
				float idf = this.getIdfValue(documentCount, fileCount);
				vector[termId] = tf * idf;
			}
			double norm = 0.0f;
			for (int i = 0; i < vector.length; i++) {
				norm += vector[i] * vector[i];
			}
			norm = Math.sqrt(norm);

			StringBuffer buf = new StringBuffer();
			buf.append(name + ";");
			for (int i = 0; i < vector.length; i++) {
				if (vector[i] != 0.0f) {
					vector[i] = vector[i] / (float) norm;
					buf.append(i + ":" + vector[i] + " ");
				}
			}
			writer.write(buf.toString() + lineSparator);
			writer.flush();
		}
		writer.close();
	}

	private float getTfValue(int freq, int totalTermCount) {
		return (float)Math.log(freq)+1;
	}

	private float getIdfValue(double docCount, double totalCount) {
		return (float) Math.log(totalCount / docCount);
	}
}
