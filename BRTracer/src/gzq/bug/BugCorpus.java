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
import java.util.*;
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
    //本过程读取的数据文件列表
    public static String MethodNameFileName = "MethodName.txt";
	//本过程保存的数据文件列表
	public static String BugCorpusFolderName = "BugCorpus";
	public static String FixLinkFileName = "FixLink.txt";
	public static String DescriptionClassNameFileName = "DescriptionClassName.txt";
	public static String BugTermListFileName = "BugTermList.txt";
	public static String BugVectorFileName = "BugVector.txt";
	public static String BugSimilarityFileName = "BugSimilarity.txt";

    //本过程保存的数据文件列表
    public static String SimiScoreFileName = "SimiScore.txt";
    /**
     * 提取bug中的单词 写入语料库 OK
     * @param bug 对象
     * @param storeDir 存储目录
	 * @throws IOException
     */
	private static void writeCorpus(Bug bug, String storeDir) throws IOException {
		String content = bug.getBugSummary() + " " + bug.getBugDescription(); //bug内容文本
		String[] splitWords = Splitter.splitNatureLanguage(content);          //从文本中提取的单词
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
	 * 从XML中提取Bug信息 OK
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
											bug.addFixedFile(_n.getTextContent().replace("/", "."));
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
	 * 提取bug stack-trace中包含的类名 OK
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

	/**
	 * 制作bug语料库
	 * @throws IOException
	 */
	public static void makeBugCorpus() throws IOException {
        //提取bug列表
		ArrayList<Bug> bugs = BugCorpus.parseXML();
		String bugCorpusDir = Utility.outputFileDir + BugCorpusFolderName + Utility.separator; //语料库文件夹
		File file = new File(bugCorpusDir);
		if (!file.exists()) file.mkdir(); //文件夹不存在则新建该文件夹
		for (Bug bug : bugs) writeCorpus(bug, bugCorpusDir);  //为每份bug报告生成语料库
        //bug-fix文件 bug-附带的类名文件
		FileWriter writerFix = new FileWriter(Utility.outputFileDir +  FixLinkFileName);  //修复link
		FileWriter writerClassName = new FileWriter(Utility.outputFileDir +  DescriptionClassNameFileName); //bug中描述类名

		for (Bug bug : bugs) {
			for (String fixName : bug.getSet()) {
				writerFix.write(bug.getBugId() + "\t" + fixName + Utility.lineSeparator);
				writerFix.flush();
			}
			writerClassName.write(bug.getBugId() + "\t" + extractClassName(bug.getBugDescription()) + Utility.lineSeparator);
		}
        writerFix.close();
		writerClassName.close();
		Utility.bugReportCount = bugs.size();
		Utility.writeConfig("bugReportCount", bugs.size() + Utility.lineSeparator);  //写入配置
        System.out.println("   Generating " + bugs.size() + " Bug Reports");
	}

	/////////////////////////////////////生成Bug向量，长度为///////////////////////////////////////////////
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

		int termCount = wordList.getNumWords();  //bug单词数目
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

	//////////////////////////////////////计算Bug之间的相似度////////////////////////////////////////////////
	/**
	 * 构建长度为bugTerm的bug向量
	 * map: bugID-vector bugID和其对应的向量
	 * @return
	 * @throws IOException
	 */
	public static Hashtable<Integer, float[]> getBugVector() throws IOException {
		Hashtable<Integer, float[]> vectors = new Hashtable<>();
		BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + BugVectorFileName));
		String line;
		while ((line = reader.readLine()) != null) {
			Integer id = Integer.parseInt(line.substring(0, line.indexOf(".")));  //Bug ID
			float[] vector = new float[Utility.bugTermCount];                     //Bug向量
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
	 * 计算bug向量余弦值，向量长度为bugTermCount
	 *
	 * @param firstVector
	 * @param secondVector
	 * @return
	 */
	private static float getCosineValue(float[] firstVector, float[] secondVector) {
		float len1 = 0, len2 = 0, product = 0;
		for (int i = 0; i < firstVector.length; i++) {
			product += firstVector[i] * secondVector[i];
			len1 += firstVector[i] * firstVector[i];
			len2 += secondVector[i] * secondVector[i];
		}
		return (float) (product / (Math.sqrt(len1) * Math.sqrt(len2)));
	}

	/**
	 * 计算相似度
	 *
	 * @throws IOException
	 */
	public static List<Bug> computeBugSimilarity() throws Exception {
		// 按照修复时间排序的bugID列表
		List<Bug> bugs = BugCorpus.parseXML();
		//bug报告数量大小的数组, index-bugID映射
		int[] idArr = new int[bugs.size()];
		for (int index = 0; index < bugs.size(); index++ )
			idArr[index] = Integer.parseInt(bugs.get(index).getBugId());
		//获取bug向量数组
		Hashtable<Integer, float[]> vectors = getBugVector();

		// 从最早的bug开始计算相似度
		FileWriter writer = new FileWriter(Utility.outputFileDir + BugSimilarityFileName);
		for (int i = 0; i < idArr.length; i++) {
            HashMap<Integer, Float> preSimValues = new HashMap<>();      //该bug之前的相似的bug数组,大小为i
			int firstId = idArr[i];                             //目标bugID
			float[] firstVector = vectors.get(firstId);         //目标bug的向量
			String output = firstId + ";";
			for (int j = 0; j < i; j++) {
				int secondId = idArr[j];                        //备选bugID
				float[] secondVector = vectors.get(secondId);   //备选bug向量
                float sim = getCosineValue(firstVector, secondVector);
				output += secondId + ":" + sim + " ";
				preSimValues.put(secondId, sim);
			}
            bugs.get(i).setPreSimValues(preSimValues);
			writer.write(output.trim() + Utility.lineSeparator);
			writer.flush();
		}
		writer.close();
		return bugs;
	}

    /////////////////////////////////////////////////////////////////////////

    /**
     * 分布
     * @throws Exception
     */
    public static List<Bug> distribute() throws Exception {
        // 修复bug修复文件表 文件名-ID表
        Hashtable<Integer, TreeSet<String>> fixedTable = Utility.getFixedTable();
        Hashtable<String, Integer> idTable = Utility.getFileIdTable(MethodNameFileName);  //获取分段后文件名-ID 映射表
        FileWriter writer = new FileWriter(Utility.outputFileDir + SimiScoreFileName);

        List<Bug> bugs = BugCorpus.computeBugSimilarity();
        for (Bug bug : bugs) {
            float[] similarValues = new float[Utility.sourceFileCount];  //计算多少相似度
            // 当前bug编号  该与bug相比的之前的bug的编号及相似度数组
            Integer id = Integer.parseInt(bug.getBugId());
            HashMap<Integer, Float> preSimValues = bug.getPreSimValues();

            for(Integer simBugId: preSimValues.keySet()){
                Float sim = preSimValues.get(simBugId);
                // 修复该bug的文件集合
                TreeSet<String> fileSet = fixedTable.get(simBugId);
                if (fileSet == null) System.out.println(simBugId);  //没有修复该bug

                for (Iterator<String> fileSetIt = fileSet.iterator(); fileSetIt.hasNext(); ) {
                    //取出文件集合中的某一个文件
                    String name = fileSetIt.next();
                    //change: add the singleValue to each segment belonged to that class
                    Integer counter = 0;
                    for (; idTable.get(name + "@" + counter + ".java")!=null; counter++) {
                        Integer fileId = idTable.get(name + "@" + counter + ".java");
                        similarValues[fileId] += sim / fileSet.size(); //与该bug的相似度/修复该bug的文件数
                    }
                }
            }
            String output = id + ";";
            for (int i = 0; i < Utility.sourceFileCount; i++)
                if (similarValues[i] != 0)
                    output += i + ":" + similarValues[i] + " ";
            writer.write(output.trim() + Utility.lineSeparator);
            writer.flush();
            bug.setSimilarValues(similarValues); //设置该bug的通过历史bug获得的与文件的相似度分数
        }
        writer.close();
        return bugs;
    }

    /**
     * 获取按修复时间顺序排列的所有Bug信息
     * @return
     */
    public static List<Bug> getBugs() throws Exception{
        return distribute();
    }

	/**
	 * 运行bug处理方法
	 */
	public static void run() throws Exception{
		makeBugCorpus();           //制作bug语料库
		makeBugVector();           //生成bug向量
		computeBugSimilarity();    //计算bug之间相似度
        distribute();              //根据bug之间的相似度来计算与该bug有关的源文件列表
	}
}
