package gzq.source;

import gzq.bug.*;
import utils.Utility;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

public class Similarity {

	public static String CodeVectorFileName = "CodeVector.txt";
	public static String MethodNameFileName = "MethodName.txt";
	//保存
	public static String VSMScoreFileName = "VSMScore.txt";


	/**
	 * 获取DF 文档频率表word-DF
	 * @return
	 * @throws IOException
	 */
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

	/**
	 * 获取单词索引
	 * @return
	 * @throws IOException
	 */
	private static Hashtable<String, Integer> getWordIndex() throws IOException {
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

	/**
	 * 获取源文件索引
	 * @return
	 * @throws IOException
	 */
	private static Hashtable<String, Integer> getSourceFileIndex() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + MethodNameFileName));
		String line;
		Hashtable<String, Integer> table = new Hashtable<>();
		while ((line = reader.readLine()) != null) {
			String[] values = line.split("\t");
			table.put(values[1].trim(), Integer.parseInt(values[0]));
		}
		return table;
	}

	/**
	 * 计算相似度
	 * @param bugVector
	 * @return 源码文件与指定bug相似的数组
	 * @throws IOException
	 */
	private static float[] computeOneSimilarity(float[] bugVector, Hashtable<String, Integer> fileIndexTable) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + CodeVectorFileName));
		float[] simValues = new float[Utility.sourceFileCount];  //相似度数组: 每个源码文件与该bug的相似度
		String line;
		while ((line = reader.readLine()) != null) {
			String[] values = line.split(";");
			String name = values[0];   //类名
			Integer fileIndex = fileIndexTable.get(name);  //类名索引
			if (fileIndex == null) System.out.println(name);
			float[] codeVector;  //大小为代码中词的个数
			if (values.length != 1) codeVector = Utility.getVector(values[1]);
			else codeVector = Utility.getVector(null);
			float sim = 0.0f;
			for (int i = 0; i < codeVector.length; i++) sim += bugVector[i] * codeVector[i];
			simValues[fileIndex] = sim;
		}
		return simValues;
	}

	/**
	 * 计算相似度,返回相似度值
	 * @throws IOException
	 */
	public static float[][] computeSimilarity() throws Exception {
		Hashtable<String, Integer> fileIndexTable = getSourceFileIndex(); //源码文件索引
		Hashtable<String, Integer> wordIndexTable = getWordIndex();       //源码单词索引
		Hashtable<String, Integer> DFTable = getIDCTable();               //源码单词DF表

		FileWriter writer = new FileWriter(Utility.outputFileDir + VSMScoreFileName);
		List<Bug> bugs = BugCorpus.getBugs();  //所有bug信息
        float[][] allBugSimValues = new float[bugs.size()][];
        int bugIndex = 0;
		for (Bug bug : bugs) {  //对每个bug进行处理
			Integer bugId = Integer.parseInt(bug.getBugId());
			BufferedReader readerBug = new BufferedReader(new FileReader(Utility.outputFileDir + "BugCorpus" + Utility.separator + bugId + ".txt"));
			String line = readerBug.readLine();
			String[] words = line.split(" ");  //bug报告中的单词
			Hashtable<String, Integer> wordTable = new Hashtable<>();  //存储单词对应的词频
			for (String word : words) {
				if (!word.trim().equals("")) {
					if (wordTable.containsKey(word)) {
						Integer count = wordTable.get(word);
						count++;
						wordTable.remove(word);
						wordTable.put(word, count);
					} else wordTable.put(word, 1);
				}
			}
			int totalTermCount = 0;   //总单词数
			for (String word : wordTable.keySet()) if (wordIndexTable.get(word) != null) totalTermCount += wordTable.get(word);

			float[] bugVector = new float[Utility.sourceWordCount];  //bug向量, 长度为源码单词的长度
			// TF-IDF值
			for (String word : wordTable.keySet()) {
				Integer index = wordIndexTable.get(word);
				if (index != null)
					bugVector[index] = Utility.getTfValue(wordTable.get(word), totalTermCount) * Utility.getIdfValue(DFTable.get(word), Utility.sourceFileCount);
			}
			double norm = 0.0f;
			for (int i = 0; i < bugVector.length; i++) norm += bugVector[i] * bugVector[i];
			norm = Math.sqrt(norm);
			for (int i = 0; i < bugVector.length; i++) bugVector[i] = bugVector[i] / (float) norm;
			// 归一化处理，计算相似度
			float[] simValues = computeOneSimilarity(bugVector, fileIndexTable);
            allBugSimValues[bugIndex++] = simValues;
			StringBuffer buf = new StringBuffer();
			buf.append(bugId + ";");
			for (int i = 0; i < simValues.length; i++) if (simValues[i] != 0.0f) buf.append(i + ":" + simValues[i] + " ");
			writer.write(buf.toString().trim() + Utility.lineSeparator);
			writer.flush();
		}
		writer.close();
        return allBugSimValues;
	}
}
