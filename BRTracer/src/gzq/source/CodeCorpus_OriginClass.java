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
public class CodeCorpus_OriginClass {

	//本过程保存的数据文件列表
    public static String ClassNameFileName = "ClassName.txt";
	public static String CodeCorpus_OriginClassFileName = "CodeCorpus_OriginClass.txt";
	public static String ImportFileName = "Import.txt";
	public static String ClassAndMethodCorpusFileName = "ClassAndMethodCorpus.txt";
	public static String LOCFileName = "LOC.txt";
	public static String TermInfo_OriginClassFileName = "TermInfo_OriginClass.txt";
	public static String LengthScoreFileName = "LengthScore.txt";
	public static int B = 50;
	public static List<Corpus> sourceCorpus = new ArrayList<>();

	/**
	 * 计算代码行
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static Integer countLOC(File file) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		Integer LoC=0;
		String tmp;
		while(true){
			tmp = reader.readLine();
			if(tmp==null) break;
			LoC++;
		}
		reader.close();
		return LoC;
	}

	/**
	 * 中心化的值
	 * @param x
	 * @param max
	 * @param min
	 * @param median
	 * @return
	 */
	public static Double getNormValue(Double x, Double max, Double min, Double median) {
		return B *  (x - median) /  (max - min);
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
			double nor = getNormValue(len, max, min, median); //归一化的值
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
	public static Corpus makeSingleCorpus(File file, FileWriter writeImport) throws IOException {
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
		return new Corpus(fileName, file.getAbsolutePath(), contentBuf.toString() + " " + nameBuf.toString(), nameBuf.toString(),countLOC(file));
	}

	/**
	 * 获取源码语料库
	 * @return
	 */
	public static List<Corpus> getSourceCorpus(){return sourceCorpus;}

    /**
     * 创建原始代码语料库和导入信息
     * @throws Exception
     */
    public static void create() throws Exception {
        File[] files = Utility.detectSourceFiles(Utility.sourceFileDir, Utility.srcFileType);
        FileWriter writer = new FileWriter(Utility.outputFileDir + ClassNameFileName);
        FileWriter writeCorpus = new FileWriter(Utility.outputFileDir + CodeCorpus_OriginClassFileName);
	    FileWriter writeImport = new FileWriter(Utility.outputFileDir + ImportFileName);
        FileWriter NameWriter = new FileWriter(Utility.outputFileDir + ClassAndMethodCorpusFileName);
		FileWriter writerLOC = new FileWriter(Utility.outputFileDir + LOCFileName);
	    int index = 0;
        TreeSet<String> nameSet = new TreeSet<>();

        for (File file : files) {
	        Corpus corpus = makeSingleCorpus(file, writeImport); //根据源文件创建语料库
			sourceCorpus.add(corpus);  //源码语料库
	        if (!nameSet.contains(corpus.getJavaFileFullClassName())) {
                writer.write(index + "\t" + corpus.getJavaFileFullClassName() + ".java" + "\t" + corpus.getLoc() + Utility.lineSeparator);
	            writeCorpus.write(index + "\t" + corpus.getJavaFileFullClassName() + ".java" + "\t" + corpus.getContent() + Utility.lineSeparator);
                NameWriter.write(corpus.getJavaFileFullClassName() + ".java" + "\t" + corpus.getIdentifers() + Utility.lineSeparator);
				writerLOC.write( corpus.getJavaFileFullClassName() + ".java" + "\t" + corpus.getLoc() + Utility.lineSeparator);
                writer.flush();
                writeCorpus.flush();
                NameWriter.flush();
				writerLOC.flush();
		        nameSet.add(corpus.getJavaFileFullClassName());
		        index++;
	        }
        }
        writeCorpus.close();
	    writeImport.close();
        NameWriter.close();
        writerLOC.close();
        Utility.originFileCount = index; //原始文件数
        Utility.writeConfig("originFileCount", Utility.originFileCount + Utility.lineSeparator);  //写入配置
        System.out.println("   Generated " + index + " Origin Class Corpus");
    }

    public static void run() throws Exception{
		create();
    	computeLengthScore();
	}
}
