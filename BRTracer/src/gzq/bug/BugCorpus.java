package gzq.bug;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BugCorpus {

    /**
     * 提取bug中的单词 写入语料库
     * @param bug 对象
     * @param storeDir 存储目录
	 * @throws IOException
     */
	private static void writeCorpus(Bug bug, String storeDir) throws IOException {

		String content = bug.getBugSummary() + " " + bug.getBugDescription(); //bug文本
		String[] splitWords = Splitter.splitNatureLanguage(content);  //提取单词
		StringBuffer corpus = new StringBuffer();
		for (String word : splitWords) {
			word = Stem.stem(word.toLowerCase());  //提取词干并小写
			if (!Stopword.isEnglishStopword(word)) //过滤停用词
				corpus.append(word + " ");
		}
		FileWriter writer = new FileWriter(storeDir + bug.getBugId() + ".txt");
		writer.write(corpus.toString().trim());  //单词写入bug文件
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
	 * 提取类名
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

	public static void create() throws IOException {
        //提取bug列表
		ArrayList<Bug> list = BugCorpus.parseXML();
		String bugCorpusDir = Utility.outputFileDir + "BugCorpus" + Utility.separator; //语料库文件夹
		File file = new File(bugCorpusDir);
		if (!file.exists())
			file.mkdir();
		//生成语料库
		for (Bug bug : list)
			writeCorpus(bug, bugCorpusDir);

		FileWriter writer = new FileWriter(Utility.outputFileDir  + "SortedId.txt");  //按修复时间排序的bug列表
		FileWriter writerFix = new FileWriter(Utility.outputFileDir +  "FixLink.txt");  //修复link
		FileWriter writerClassName = new FileWriter(Utility.outputFileDir +  "DescriptionClassName.txt"); //bug中描述类名

		for (Bug bug : list) {
			writer.write(bug.getBugId() + "\t" + bug.getFixDate() + Utility.lineSeparator);
			writer.flush();
			for (String fixName : bug.set) {
				writerFix.write(bug.getBugId() + "\t" + fixName + Utility.lineSeparator);
				writerFix.flush();
			}
			writerClassName.write(bug.getBugId() + "\t" + extractClassName(bug.getBugDescription()) + Utility.lineSeparator);
		}
		writerClassName.close();
		writer.close();
		writerFix.close();
		Utility.bugReportCount = list.size();
		Utility.writeConfig("bugReportCount", list.size() + Utility.lineSeparator);  //写入配置
	}
}
