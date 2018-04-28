package gzq.bug;

import edu.udo.cs.wvtool.config.WVTConfigException;
import edu.udo.cs.wvtool.config.WVTConfiguration;
import edu.udo.cs.wvtool.config.WVTConfigurationFact;
import edu.udo.cs.wvtool.config.WVTConfigurationRule;
import edu.udo.cs.wvtool.generic.output.WordVectorWriter;
import edu.udo.cs.wvtool.generic.stemmer.LovinsStemmerWrapper;
import edu.udo.cs.wvtool.generic.stemmer.PorterStemmerWrapper;
import edu.udo.cs.wvtool.generic.stemmer.WVTStemmer;
import edu.udo.cs.wvtool.generic.vectorcreation.TFIDF;
import edu.udo.cs.wvtool.main.WVTDocumentInfo;
import edu.udo.cs.wvtool.main.WVTFileInputList;
import edu.udo.cs.wvtool.main.WVTool;
import edu.udo.cs.wvtool.util.WVToolException;
import edu.udo.cs.wvtool.wordlist.WVTWordList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.Splitter;
import utils.Stem;
import utils.Stopword;
import utils.Utility;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bug语料库
 * 从XML文件中提取Bug信息制作语料库
 * 输出文件：
 * BugCorpus              Bug列表目录
 * SortedId               排序后的Bug列表
 * FixLink                Bug相关的文件
 * DescriptionClassName   Bug中包含的类名
 *
 */
public class BugCorpus {
	//本过程保存的数据文件列表
	public static String BugCorpusFolderName = "BugCorpus";
	public static String FixLinkFileName = "FixLink.txt";
	public static String DescriptionClassNameFileName = "DescriptionClassName.txt";
	public static String BugTermListFileName = "BugTermList.txt";
	public static String BugVectorFileName = "BugVector.txt";
	public static String BugSimilarityFileName = "BugSimilarity.txt";
    /**
     * 提取bug中的单词 写入语料库
     * @param bug 对象
     * @param storeDir 存储目录
	 * @throws IOException
     */
	private static void writeCorpus(Bug bug, String storeDir) throws IOException {
		String content = bug.getBugSummary() + " " + bug.getBugDescription(); //bug文本
		String[] splitWords = Splitter.splitNatureLanguage(content);         //从文本中提取单词
		StringBuffer corpus = new StringBuffer();
		for (String word : splitWords) {
			word = Stem.stem(word.toLowerCase());  //提取词干并小写
			if (!Stopword.isEnglishStopword(word)) //过滤停用词
				corpus.append(word + " ");
		}
		FileWriter writer = new FileWriter(storeDir + bug.getBugId() + ".txt");
		writer.write(corpus.toString().trim());   //单词写入bug文件
		writer.flush();
		writer.close();
	}

	/**
	 * 从XML中提取Bug信息
	 * @return
	 */
	private static ArrayList<Bug> parseXML() {
		ArrayList<Bug> list = new ArrayList<>();
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
			InputStream is = new FileInputStream(Utility.bugFilePath);
			Document doc = domBuilder.parse(is);
			Element root = doc.getDocumentElement();
			NodeList bugRepository = root.getChildNodes();
			if (bugRepository != null) {
				for (int i = 0; i < bugRepository.getLength(); i++) {
					Node bugNode = bugRepository.item(i);
					if (bugNode.getNodeType() == Node.ELEMENT_NODE) {
						String bugId = bugNode.getAttributes().getNamedItem("id").getNodeValue();
						String openDate = bugNode.getAttributes().getNamedItem("opendate").getNodeValue();
						String fixDate = bugNode.getAttributes().getNamedItem("fixdate").getNodeValue();

						Bug bug = new Bug(bugId, openDate, fixDate);
						// 提取每个bug
						for (Node node = bugNode.getFirstChild(); node != null; node = node.getNextSibling()) {
							if (node.getNodeType() == Node.ELEMENT_NODE) {
								if (node.getNodeName().equals("buginformation")) {
									NodeList _l = node.getChildNodes();
									for (int j = 0; j < _l.getLength(); j++) {
										Node _n = _l.item(j);
										if (_n.getNodeName().equals("summary"))
											bug.setBugSummary(_n.getTextContent());

										if (_n.getNodeName().equals("description"))
											bug.setBugDescription(_n.getTextContent());
									}
								}
								if (node.getNodeName().equals("fixedFiles")) {
									NodeList _l = node.getChildNodes();
									for (int j = 0; j < _l.getLength(); j++) {
										Node _n = _l.item(j);
										if (_n.getNodeName().equals("file"))
											bug.addFixedFile(_n.getTextContent());
									}
								}
							}
						}
						list.add(bug);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return list;
	}

    /**
     * 获取按修复时间顺序排列的所有Bug信息
     * @return
     */
	public static ArrayList<Bug> getBugs(){
	    return parseXML();
    }

	/**
	 * 提取bug中包含的类名
	 * @param content
	 * @return
	 */
	public static String extractClassName(String content) {
		String pattern = "[a-zA-Z_][a-zA-Z0-9_\\-]*\\.java";
		StringBuffer res = new StringBuffer();
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(content);
		while (m.find())
			res.append(m.group(0) + " ");
		return res.toString();
	}

	public static void makeBugCorpus() throws IOException {
        //提取bug列表
		ArrayList<Bug> list = BugCorpus.parseXML();
		String bugCorpusDir = Utility.outputFileDir + BugCorpusFolderName + Utility.separator; //语料库文件夹
		File file = new File(bugCorpusDir);
		if (!file.exists()) file.mkdir();
		for (Bug bug : list) writeCorpus(bug, bugCorpusDir);  //生成语料库
        //bug-fix文件 bug-附带的类名文件
		FileWriter writerFix = new FileWriter(Utility.outputFileDir +  FixLinkFileName);  //修复link
		FileWriter writerClassName = new FileWriter(Utility.outputFileDir +  DescriptionClassNameFileName); //bug中描述类名

		for (Bug bug : list) {
			for (String fixName : bug.set) {
				writerFix.write(bug.getBugId() + "\t" + fixName.replace("/",".") + Utility.lineSeparator);
				writerFix.flush();
			}
			writerClassName.write(bug.getBugId() + "\t" + extractClassName(bug.getBugDescription()) + Utility.lineSeparator);
		}
		writerClassName.close();
		writerFix.close();
		Utility.bugReportCount = list.size();
		Utility.writeConfig("bugReportCount", list.size() + Utility.lineSeparator);  //写入配置
        System.out.println("   Generating " + list.size() + " Bug Reports");
	}

	/**
	 * 制作bug向量
	 * @throws WVToolException
	 * @throws IOException
	 */
	public static void makeBugVector() throws WVToolException, IOException {
		WVTool wvt = new WVTool(false);
		WVTConfiguration config = new WVTConfiguration();
		final WVTStemmer wvtStemmer = new PorterStemmerWrapper();
		config.setConfigurationRule(WVTConfiguration.STEP_STEMMER, new WVTConfigurationRule() {
			public Object getMatchingComponent(WVTDocumentInfo d) throws WVTConfigException {
				return wvtStemmer;
			}
		});
		WVTStemmer stemmer = new LovinsStemmerWrapper();
		config.setConfigurationRule(WVTConfiguration.STEP_STEMMER, new WVTConfigurationFact(stemmer));
		WVTFileInputList list = new WVTFileInputList(1);
		list.addEntry(new WVTDocumentInfo(Utility.outputFileDir + BugCorpusFolderName + Utility.separator, "txt", "", "english", 0));
		WVTWordList wordList = wvt.createWordList(list, config);
		wordList.pruneByFrequency(1, Integer.MAX_VALUE);

		int termCount = wordList.getNumWords();
		wordList.storePlain(new FileWriter(Utility.outputFileDir + BugTermListFileName));
		FileWriter outFile = new FileWriter(Utility.outputFileDir + BugVectorFileName);
		WordVectorWriter wvw = new WordVectorWriter(outFile, true);
		config.setConfigurationRule(WVTConfiguration.STEP_OUTPUT, new WVTConfigurationFact(wvw));
		config.setConfigurationRule(WVTConfiguration.STEP_VECTOR_CREATION, new WVTConfigurationFact(new TFIDF()));
		wvt.createVectors(list, config, wordList);
		wvw.close();
		outFile.close();
		Utility.bugTermCount = termCount;
		Utility.writeConfig("bugTermCount",termCount + Utility.lineSeparator);
		System.out.println("   Generating " + termCount + " Bug Terms");
	}


	/**
	 * 获取余弦值
	 *
	 * @param firstVector
	 * @param secondVector
	 * @return
	 */
	private static float getCosineValue(float[] firstVector, float[] secondVector) {
		float len1 = 0, len2 = 0, product = 0;
		for (int i = 0; i < Utility.bugTermCount; i++) {
			product += firstVector[i] * secondVector[i];
			len1 += firstVector[i] * firstVector[i];
			len2 += secondVector[i] * secondVector[i];
		}
		return (float) (product / (Math.sqrt(len1) * Math.sqrt(len2)));
	}

	/**
	 * 构建长度为bugTerm的bug向量
	 * map: bugID-vector
	 * @return
	 * @throws IOException
	 */
	public static Hashtable<Integer, float[]> getBugVector() throws IOException {
		Hashtable<Integer, float[]> vectors = new Hashtable<>();
		BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + BugVectorFileName));
		String line;
		while ((line = reader.readLine()) != null) {
			Integer id = Integer.parseInt(line.substring(0, line.indexOf(".")));  //Bug ID
			float[] vector = new float[Utility.bugTermCount];
			String[] values = line.substring(line.indexOf(";") + 1).trim().split(" "); //索引-值 数组
			for (String value : values) {
				String[] singleValues = value.split(":");  //索引-值
				if (singleValues.length == 2)                    //正常值, 在相应索引位置上写入值
					vector[Integer.parseInt(singleValues[0])] = Float.parseFloat(singleValues[1]);
			}
			vectors.put(id, vector);
		}
		return vectors;
	}
	/**
	 * 计算相似度
	 *
	 * @throws IOException
	 */
	public static void computeBugSimilarity() throws IOException {
		// 按照修复时间排序的bugID列表
		List<Bug> bugs = BugCorpus.getBugs();
		//bug报告数量大小的数组, 地址-bugID映射
		int[] idArr = new int[Utility.bugReportCount];
		String line;
		for (int index = 0; index < bugs.size(); index++ )
			idArr[index] = Integer.parseInt(bugs.get(index).getBugId());
		//获取bug向量数组
		Hashtable<Integer, float[]> vectors = getBugVector();

		// 从最早的bug开始计算相似度
		FileWriter writer = new FileWriter(Utility.outputFileDir + BugSimilarityFileName);
		for (int i = 0; i < idArr.length; i++) {
			int firstId = idArr[i];                             //目标bugID
			float[] firstVector = vectors.get(firstId);         //目标bug的向量
			String output = firstId + ";";
			for (int j = 0; j < i; j++) {
				int secondId = idArr[j];                        //备选bugID
				float[] secondVector = vectors.get(secondId);   //备选bug向量
				output += secondId + ":" + getCosineValue(firstVector, secondVector) + " ";
			}
			writer.write(output.trim() + Utility.lineSeparator);
			writer.flush();
		}
		writer.close();
	}


	/**
	 * 运行bug处理方法
	 */
	public static void run() throws Exception{
		makeBugCorpus();           //制作bug语料库
		makeBugVector();           //生成bug向量
		computeBugSimilarity();    //计算bug相似度
	}

}
