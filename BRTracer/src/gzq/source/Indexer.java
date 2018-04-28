package gzq.source;

import utils.Utility;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.TreeSet;

public class Indexer {
	//本过程读取的数据文件列表
    public static String CodeCorpus_OriginClassFileName = "CodeCorpus_OriginClass.txt";
	public static String CodeCorpusFileName = "CodeCorpus.txt";

	//本过程保存的数据文件列表
	public static String WordListFileName = "Wordlist.txt";
    public static String TermInfo_OriginClassFileName = "TermInfo_OriginClass.txt";
	public static String TermInfoFileName = "TermInfo.txt";
	public static String IDCFileName = "IDC.txt";
	public static String CodeVectorFileName = "CodeVector.txt";

	/**
	 * 计算单词-文件频度 DF 表
	 * @return
	 * @throws IOException
	 */
    public static Hashtable<String, Integer> countDF(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + fileName));
        Hashtable<String, Integer> DFTable = new Hashtable<>(); //文件-行数表
		String line;
        while ((line = reader.readLine()) != null) {
            String[] values = line.split("\t");
            String[] words = values[2].split(" "); //文件中的单词数组
            TreeSet<String> wordSet = new TreeSet<>();   //独特词单词集合
            for (String word : words) if (!word.trim().equals("") && !wordSet.contains(word)) wordSet.add(word);
            for (String word : wordSet) {
                if (DFTable.containsKey(word)) {
                    Integer count = DFTable.get(word);
                    count++;
                    DFTable.remove(word);
                    DFTable.put(word, count);
                } else DFTable.put(word, 1);
            }
        }
        return DFTable;
    }

    /**
     * 索引建立方法
     * @param indexSet
     * @param DFTable
     * @param codeCorpus
     * @param termInfo
     * @throws IOException
     */
    public static void index(Hashtable<String, Integer> indexSet, Hashtable<String, Integer> DFTable, String codeCorpus, String termInfo) throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + codeCorpus));
        FileWriter writer = new FileWriter(Utility.outputFileDir + termInfo);
        String line;
        while ((line = reader.readLine()) != null) {
            String[] values = line.split("\t");
            String[] words = values[2].split(" ");  //文件中单词数组
            int totalCount = 0;  //单词总数

            Hashtable<Integer, Integer> termTable = new Hashtable<>();  //词频表 index-count 表
            for (String word : words) {
                if (!word.trim().equals("")) {
                    totalCount++;
                    Integer termIndex = indexSet.get(word);   //单词索引
                    if (termTable.containsKey(termIndex)) {
                        Integer count = termTable.get(termIndex);
                        count++;
                        termTable.remove(termIndex);
                        termTable.put(termIndex, count);
                    } else termTable.put(termIndex, 1);
                }
            }
            StringBuffer output = new StringBuffer();
            output.append(values[1] + "\t" + totalCount + ";");
            TreeSet<Integer> tmp = new TreeSet<>();
            for (String word : words) {
                if (!word.trim().equals("")) {
                    Integer termIndex = indexSet.get(word);  //单词索引
                    if (!tmp.contains(termIndex)) {
                        tmp.add(termIndex);
                        int termCount = termTable.get(termIndex);  //在该文档中的词频TF
                        int DFCount = DFTable.get(word);           //DF
                        output.append(termIndex + ":" + termCount + " " + DFCount + "\t");
                        // 文件单词数目;单词索引: TF DF
                    }
                }
            }
            writer.write(output.toString() + Utility.lineSeparator);
            writer.flush();
        }//while end
        writer.close();
    }

    /**
     * 为原始代码单词建立索引
     * @throws IOException
     */
    public static void indexOriginCode() throws IOException {
        Hashtable<String, Integer> DFTable = Indexer.countDF(CodeCorpus_OriginClassFileName);
        Hashtable<String, Integer> indexSet = new Hashtable<>();
        int id = 0;
        for (String key : DFTable.keySet()) {
            indexSet.put(key, id);
            id++;
        }
        index(indexSet, DFTable, CodeCorpus_OriginClassFileName, TermInfo_OriginClassFileName);
    }

    /**
     * 为分段后的代码单词建立索引
     * @throws IOException
     */
	public static void indexSplitCode() throws IOException {
		Hashtable<String, Integer> DFTable = countDF(CodeCorpusFileName);
		Hashtable<String, Integer> indexSet = new Hashtable<>();  //为源码单词建立索引
		int index = 0;  //索引从0开始
		FileWriter writerWord = new FileWriter(Utility.outputFileDir + WordListFileName);
		for (String key : DFTable.keySet()) {
			indexSet.put(key, index);
			writerWord.write(key + "\t" + index + Utility.lineSeparator);
			writerWord.flush();
			index++;
		}
		Utility.sourceWordCount = index;
		writerWord.close();
		// 源码单词索引建立完毕

        // IDC.txt 保存DFTable的内容
		FileWriter writerDoc = new FileWriter(Utility.outputFileDir + IDCFileName );
		for (String key : DFTable.keySet()) {
			writerDoc.write(key + "\t" + DFTable.get(key) + Utility.lineSeparator);
			writerDoc.flush();
		}
		writerDoc.close();
		// 保存完毕
        index(indexSet, DFTable, CodeCorpusFileName, TermInfoFileName);
	}


    /**
     * 制作代码向量
     * @throws IOException
     */
    public static void makeCodeVector() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + TermInfoFileName));
        FileWriter writer = new FileWriter(Utility.outputFileDir + CodeVectorFileName);
        String line;
        while ((line = reader.readLine()) != null) {
            String[] values = line.split(";");

            String name = values[0].substring(0, values[0].indexOf("\t"));  //类名
            if (values.length == 1) {
                System.out.println(name + ";");
                continue;
            }
            // 文件单词数目;单词索引: TF DF
            Integer totalTermCount = Integer.parseInt(values[0].substring(values[0].indexOf("\t") + 1));
            String[] termInfos = values[1].split("\t");
            float[] vector = new float[Utility.sourceWordCount];  //文件向量，长度为源码单词数
            for (String str : termInfos) {    //每个单词
                String[] strs = str.split(":");
                Integer termIndex = Integer.parseInt(strs[0]);  //单词索引
                Integer termCount = Integer.parseInt(strs[1].substring(0, strs[1].indexOf(" "))); //TF
                Integer DFCount = Integer.parseInt(strs[1].substring(strs[1].indexOf(" ") + 1));  //DF

                float tf = Utility.getTfValue(termCount, totalTermCount);
                float idf = Utility.getIdfValue(DFCount, Utility.sourceFileCount);
                vector[termIndex] = tf * idf;
            }

            double norm = 0.0f;
            for (int i = 0; i < vector.length; i++) norm += vector[i] * vector[i];
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


    /**
     * 建立单词索引
     * @throws IOException
     */
	public static void createIndex() throws IOException{
        indexSplitCode();
        indexOriginCode();
        makeCodeVector();
    }
}
