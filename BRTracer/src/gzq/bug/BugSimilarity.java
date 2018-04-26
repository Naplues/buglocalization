package gzq.bug;

import utils.Utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Bug 相似度
 * @author gzq
 *
 */
public class BugSimilarity {
	/**
	 * 获取余弦值
	 * @param firstVector
	 * @param secondVector
	 * @return
	 */
	private static float getCosineValue(float[] firstVector, float[] secondVector) {
		float len1 = 0;
		float len2 = 0;
		float product = 0;
		for (int i = 0; i < Utility.bugTermCount; i++) {
			len1 += firstVector[i] * firstVector[i];
			len2 += secondVector[i] * secondVector[i];
			product += firstVector[i] * secondVector[i];
		}
		return (float) (product / (Math.sqrt(len1) * Math.sqrt(len2)));
	}

	/**
	 * 获取向量
	 * @return
	 * @throws IOException
	 */
	public static Hashtable<Integer, float[]> getVector() throws IOException {

		Hashtable<Integer, float[]> vectors = new Hashtable<>();

		BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + "BugVector.txt"));
		String line;
		while ((line = reader.readLine()) != null) {
			String idStr = line.substring(0, line.indexOf("."));
			String vectorStr = line.substring(line.indexOf(";") + 1).trim();
			Integer id = Integer.parseInt(idStr);
			float[] vector = getVector(vectorStr);
			vectors.put(id, vector);
		}
		return vectors;
	}

	private static float[] getVector(String vectorStr) {
		float[] vector = new float[Utility.bugTermCount];
		String[] values = vectorStr.split(" ");
		for (String value : values) {
			String[] singleValues = value.split(":");
			if (singleValues.length == 2) {
				int index = Integer.parseInt(singleValues[0]);
				float sim = Float.parseFloat(singleValues[1]);
				vector[index] = sim;
			}
		}
		return vector;
	}

	/**
	 * 计算相似度
	 * @throws IOException
	 */
	public static void computeSimilarity() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + "SortedId.txt"));
		String line;
		int[] idArr = new int[Utility.bugReportCount];
		int index = 0;
		while ((line = reader.readLine()) != null) {
			String idStr = line.substring(0, line.indexOf("\t"));
			idArr[index++] = Integer.parseInt(idStr);
		}

		Hashtable<Integer, float[]> vectors = getVector();

		FileWriter writer = new FileWriter(Utility.outputFileDir + "BugSimilarity.txt");

		for (int i = 0; i < Utility.bugReportCount; i++) {
			int firstId = idArr[i];
			float[] firstVector = vectors.get(firstId);
			String output = firstId + ";";
			for (int j = 0; j < i; j++) {
				int secondId = idArr[j];
				float[] secondVector = vectors.get(secondId);
				float similarity = getCosineValue(firstVector, secondVector);
				output += secondId + ":" + similarity + " ";
			}
			writer.write(output.trim() + Utility.lineSeparator);
			//System.out.println(i);
			writer.flush();
		}
		writer.close();
	}
}
