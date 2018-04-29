package gzq.source;

import sourcecode.ast.FileParser;
import utils.Stem;
import utils.Stopword;
import utils.Utility;

import java.io.*;
import java.util.*;

/**
 * 原始代码语料库
 *
 * 提取源码文件的类名和其中的方法名
 * 输出文件：
 * ClassName.txt                原始类文件和其索引
 * ClassAndMethodCorpus.txt     原始类文件和其中包含的类名和方法名称
 * CodeCorpus_OriginClass.txt   原始类文件语料库
 * Import.txt                   该类导入的类的信息
 *
 */
public class CodeCorpus {

	//本过程保存的数据文件列表
    public static String ClassNameFileName = "ClassName.txt";
    public static String MethodNameFileName = "MethodName.txt";
    public static String CodeCorpusFileName = "CodeCorpus.txt";
	public static String CodeCorpus_OriginClassFileName = "CodeCorpus_OriginClass.txt";
	public static String ImportFileName = "Import.txt";
	public static String ClassAndMethodCorpusFileName = "ClassAndMethodCorpus.txt";
	public static String TermInfo_OriginClassFileName = "TermInfo_OriginClass.txt";
	public static String LengthScoreFileName = "LengthScore.txt";
	public static String WordListFileName = "Wordlist.txt";
	public static String TermInfoFileName = "TermInfo.txt";
	public static String CodeVectorFileName = "CodeVector.txt";
    public static String VSMScoreFileName = "VSMScore.txt";

    private static HashMap<String, String> ClassNameAndMethodNameTable = new HashMap<>();
    public static int spiltclass = 800;  //分段大小800个单词
	public static List<Code> sourceCorpus = new ArrayList<>();

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
		Hashtable<String, Integer> DFTable = Utility.countDF(CodeCorpus_OriginClassFileName); //原始文件的单词-文档频度表
		Hashtable<String, Integer> indexSet = new Hashtable<>(); //单词-索引表映射，根据DF表创建
		int index = 0;
		for (String key : DFTable.keySet()) indexSet.put(key, index++);
		index(indexSet, DFTable, CodeCorpus_OriginClassFileName, TermInfo_OriginClassFileName);
	}

	/**
	 * 为分段后的代码单词建立索引
	 * @throws IOException
	 */
	public static void indexSplitCode() throws IOException {
        Code.DFTable =  countDF(CodeCorpusFileName);
		Hashtable<String, Integer> indexSet = new Hashtable<>();  //为源码单词建立索引
		int index = 0;  //索引从0开始
		FileWriter writerWord = new FileWriter(Utility.outputFileDir + WordListFileName);
		for (String key : Code.DFTable.keySet()) {
			writerWord.write(key + "\t" + index + Utility.lineSeparator);
			indexSet.put(key, index++);
			writerWord.flush();
		}
		Utility.sourceWordCount = index;
		writerWord.close();
		// 源码单词索引建立完毕
		index(indexSet, Code.DFTable, CodeCorpusFileName, TermInfoFileName);
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
	 * 获取长度分数：exp(x)/(1+exp(x))
	 * @param len
	 * @return
	 */
	public static double getLenScore(double len) {
		return (Math.exp(len) / (1 + Math.exp(len)));
	}

	/**
	 * 计算长度分数：根据文件包含的单词数来计算
	 * @throws IOException
	 */
	public static void computeLengthScore() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + TermInfo_OriginClassFileName));
		double max = Double.MIN_VALUE;  //最大单词数初始化为最小值，逐渐更新
		double min = Double.MAX_VALUE;  //最小单词数初始化为最大值，逐渐更新
		Integer[] lens = new Integer[Utility.originFileCount];  //长度数组,存储每个文件的长度
		int i = 0;
		Hashtable<String, Integer> lensTable = new Hashtable<>();  //文件-单词数 列表
		int count = 0; //内容不为空的文件
		String line;
		while ((line = reader.readLine()) != null) {
			String[] values = line.split(";");
			String name = values[0].substring(0, values[0].indexOf("\t"));  //类名
			Integer len = Integer.parseInt(values[0].substring(values[0].indexOf("\t") + 1)); //单词个数
			lensTable.put(name, len);
			lens[i++] = len;
			if (len != 0) count++;     //更新非空文件数目
			if (len > max) max = len;  //更新最大单词数
			if (len < min) min = len;  //更新最小单词数
		}
		double low = min;
		double high = max;
		Collections.sort(Arrays.asList(lens));  // 对长度数组进行排序
		double median;   //计算中位值
		if( lens.length%2==0 ) median = (lens[lens.length/2]+lens[lens.length/2+1])/2;
		else median = lens[lens.length/2+1];

		int n = 0;
		FileWriter writer = new FileWriter(Utility.outputFileDir + LengthScoreFileName);
		for (String key : lensTable.keySet()) {
			double len = (double)lensTable.get(key);
			double score = 0.0;
			double nor = Utility.getNormValue(len, max, min, median); //归一化的值
			if (len != 0) {
				if (len >= low && len <= high) {  //范围内的值，取逻辑回归值
					score = getLenScore(nor);
					n++;
				} else if (len < low) score = 0.5;  //小值
				else score = 1.0;  //大值
			} else score = 0.0;  //长度为0的值
			writer.write(key + "\t" + score + "\r\n");
			writer.flush();
		}
		writer.close();
	}

	/**
	 * 为一个源文件建立语料库
	 * @param file
	 * @param writeImport
	 * @return
	 * @throws IOException
	 */
	public static Code makeSingleCorpus(File file, FileWriter writeImport) throws IOException {
		FileParser parser = new FileParser(file);
        String fileName = Utility.getFileName(file);  //该文件的全称限定类名

        writeImport.write(fileName + "\t");
		parser.getImport(writeImport);
		writeImport.write(Utility.lineSeparator);

		StringBuffer contentBuf = new StringBuffer();
		StringBuffer nameBuf = new StringBuffer();
        //去除关键字、停用词、提取词干、转换为小写
        String[] content = parser.getContent();
		for (String word : content)
			if (!(Stopword.isKeyword(word) || Stopword.isEnglishStopword(word)))
				contentBuf.append(Stem.stem(word.toLowerCase()) + " ");
		for (String word : parser.getClassNameAndMethodName())
			nameBuf.append(Stem.stem(word.toLowerCase()) + " ");
		String contents = contentBuf.toString() + " " + nameBuf.toString();
		return new Code(fileName, file.getAbsolutePath(), contents, nameBuf.toString(),Utility.countLOC(file));
	}

    /**
     * 创建原始代码语料库和导入信息
     * @throws Exception
     */
    public static void createOriginCodeCorpus() throws Exception {
        File[] files = Utility.detectSourceFiles(Utility.sourceFileDir, Utility.srcFileType);
        FileWriter writer = new FileWriter(Utility.outputFileDir + ClassNameFileName);
        FileWriter writeCorpus = new FileWriter(Utility.outputFileDir + CodeCorpus_OriginClassFileName);
	    FileWriter writeImport = new FileWriter(Utility.outputFileDir + ImportFileName);
        FileWriter NameWriter = new FileWriter(Utility.outputFileDir + ClassAndMethodCorpusFileName);
	    int index = 0;
        TreeSet<String> nameSet = new TreeSet<>();

        for (File file : files) {
	        Code corpus = makeSingleCorpus(file, writeImport); //根据源文件创建语料库
			sourceCorpus.add(corpus);  //源码语料库
	        if (!nameSet.contains(corpus.getJavaFileFullClassName())) {
                writer.write(index + "\t" + corpus.getJavaFileFullClassName() + ".java" + "\t" + corpus.getLoc() + Utility.lineSeparator);
	            writeCorpus.write(index + "\t" + corpus.getJavaFileFullClassName() + ".java" + "\t" + corpus.getContent() + Utility.lineSeparator);
                NameWriter.write(corpus.getJavaFileFullClassName() + ".java" + "\t" + corpus.getIdentifiers() + Utility.lineSeparator);
                writer.flush();
                writeCorpus.flush();
                NameWriter.flush();
		        nameSet.add(corpus.getJavaFileFullClassName());
		        index++;
	        }
        }
        writeCorpus.close();
	    writeImport.close();
        NameWriter.close();
        Utility.originFileCount = index; //原始文件数
        System.out.println("   Generated " + index + " Origin Class Corpus");
    }

    /**
     * 初始化
     * @throws IOException
     */
    public static void init() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + ClassAndMethodCorpusFileName));
        String line;
        while (true) {
            line = reader.readLine();
            if (line == null) break;
            String values[] = line.split("\t");
            // 该类没有方法名
            if (values.length == 1) continue;
            // 提取完整类名,去除.java字符串
            String fileName = values[0].substring(0, values[0].lastIndexOf("."));
            // 保存类名-方法名表
            ClassNameAndMethodNameTable.put(fileName, values[1]);
        }
        reader.close();
    }
    /**
     * 创建指定源码文件的预料库
     *
     * @param file 源码文件名
     * @return
     */
    public static Code makeSingleSplitCorpus(File file) {
        FileParser parser = new FileParser(file);
        String fileName = Utility.getFileName(file);
        String[] content = parser.getContent();

        StringBuffer contentBuf = new StringBuffer();
        StringBuffer nameBuf = new StringBuffer();

        for (String word : content)
            if (!(Stopword.isKeyword(word) || Stopword.isEnglishStopword(word)))
                contentBuf.append(Stem.stem(word.toLowerCase()) + " ");
        for (String word :  parser.getClassNameAndMethodName())
            nameBuf.append(Stem.stem(word.toLowerCase() + " "));
        return new Code(fileName, file.getAbsolutePath(), contentBuf.toString() + nameBuf.toString(),"");
    }

    /**
     * 创建代码语料库和方法名
     * @throws Exception
     */
    public static void createSplitCodeCorpus() throws Exception {
        init();  //初始化
        // 检测出所有源文件
        File[] files = Utility.detectSourceFiles(Utility.sourceFileDir, Utility.srcFileType);
        // 创建代码语料库 和 方法名 文件
        FileWriter writeCorpus = new FileWriter(Utility.outputFileDir + CodeCorpusFileName);
        FileWriter writer = new FileWriter(Utility.outputFileDir + MethodNameFileName);
        int count = 0;  //
        TreeSet<String> nameSet = new TreeSet<>();
        for (File file : files) {
            // 创建指定源码的预料库
            Code corpus = makeSingleSplitCorpus(file);
            if (corpus == null) continue; //该语料库为null
            if (!nameSet.contains(corpus.getJavaFileFullClassName())) {
                String[] src = corpus.getContent().split(" ");     //源码中的单词数
                Integer methodCount = 0;  // 方法数目
                String tmpFileName = corpus.getJavaFileFullClassName();  //源码全城限定类名,没有.java
                String names = ClassNameAndMethodNameTable.get(tmpFileName);
                while (methodCount == 0 || methodCount * spiltclass < src.length) {
                    StringBuffer content = new StringBuffer();
                    Integer i = methodCount * spiltclass; //新段起始位置
                    while (true) { //超出全文总长或者 到达该段总长，跳出
                        if (i >= src.length || i >= (methodCount + 1) * spiltclass) break;
                        content.append(src[i] + " ");
                        i++;
                    }
                    content.append(names);  // 添加源码中的方法名
                    int tmp = count + methodCount;
                    writer.write(tmp + "\t" + tmpFileName + ".java" + "@" + methodCount + ".java" + Utility.lineSeparator);
                    Code.methodIndexTable.put(tmpFileName + ".java" + "@" + methodCount + ".java", tmp);
                    writeCorpus.write(tmp+"\t"+ tmpFileName + ".java" + "@" + methodCount + ".java" + "\t" + content.toString() + Utility.lineSeparator);
                    methodCount++;
                }
                writeCorpus.flush();
                writer.flush();
                nameSet.add(tmpFileName);
                count += methodCount; //累计该文件的段数
            }
        }
        Utility.sourceFileCount = count;  //分段后的源码文件数目
        writeCorpus.close();
        writer.close();
    }

    public static void run() throws Exception{
		createOriginCodeCorpus(); //建立原始文件语料库
		createSplitCodeCorpus();  //建立分段后代码语料库
		indexOriginCode();        //建立原始文件索引
		indexSplitCode();         //建立分段后代码索引
    	computeLengthScore();     //计算长度分数
		makeCodeVector();         //制作源码向量
	}
}
