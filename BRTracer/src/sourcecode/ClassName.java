package sourcecode;

import property.Property;
import sourcecode.ast.Corpus;
import sourcecode.ast.FileDetector;
import sourcecode.ast.FileParser;
import utils.Stem;
import utils.Stopword;
import java.io.*;
import java.util.TreeSet;

/**
 * 类名称
 * 
 * @author gzq
 *
 */
public class ClassName {

	/**
	 * 创建类名文件
	 * 
	 * @throws Exception
	 */
	public void create() throws Exception {
		// java文件检测器
		FileDetector detector = new FileDetector("java");
		File[] files = detector.detect(Property.getInstance().getSourceCodeDir());

		// 语料库创建
		ClassName corpusCreator = new ClassName();

		FileWriter writer = new FileWriter(
				Property.getInstance().getWorkDir() + Property.getInstance().getSeparator() + "ClassName.txt");
		FileWriter NameWriter = new FileWriter(Property.getInstance().getWorkDir()
				+ Property.getInstance().getSeparator() + "ClassAndMethodCorpus.txt");

		int count = 0;

		TreeSet<String> nameSet = new TreeSet<String>();
		for (File file : files) {
			Corpus corpus = corpusCreator.createCorpus(file);

			if (corpus == null)
				continue;
			if (!nameSet.contains(corpus.getJavaFileFullClassName())) {
				if (corpus.getJavaFileFullClassName().endsWith(".java")) {
					writer.write(count + "\t" + corpus.getJavaFileFullClassName()
							+ Property.getInstance().getLineSeparator());
					NameWriter.write(corpus.getJavaFileFullClassName() + "\t" + corpus.getContent()
							+ Property.getInstance().getLineSeparator());
				} else {
					writer.write(count + "\t" + corpus.getJavaFileFullClassName() + ".java"
							+ Property.getInstance().getLineSeparator());
					NameWriter.write(corpus.getJavaFileFullClassName() + ".java" + "\t" + corpus.getContent()
							+ Property.getInstance().getLineSeparator());

				}
				writer.flush();
				NameWriter.flush();
				nameSet.add(corpus.getJavaFileFullClassName());
				count++;
			}

		}
		Property.getInstance().setOriginFileCount(count);
		writer.close();
		NameWriter.close();
	}

	/**
	 * 读取文件创建语料库
	 * 
	 * @param file
	 * @return
	 */
	public Corpus createCorpus(File file) {
		FileParser parser = new FileParser(file);

		String fileName = parser.getPackageName();
		if (fileName.trim().equals("")) {
			fileName = file.getName();
		} else {
			fileName += "." + file.getName();
		}

		/* modification for AspectJ */
		if (Property.getInstance().getProject().compareTo("aspectj") == 0) {
			fileName = file.getPath();
			fileName = fileName.substring(Property.getInstance().getOffset());
		}
		/* ************************** */

		fileName = fileName.substring(0, fileName.lastIndexOf("."));

		String[] content = parser.getContent();
		StringBuffer contentBuf = new StringBuffer();
		for (String word : content) {
			String stemWord = Stem.stem(word.toLowerCase());
			if (!(Stopword.isKeyword(word) || Stopword.isEnglishStopword(word))) {

				contentBuf.append(stemWord);
				contentBuf.append(" ");
			}
		}

		String[] classNameAndMethodName = parser.getClassNameAndMethodName();
		StringBuffer nameBuf = new StringBuffer();

		for (String word : classNameAndMethodName) {
			String stemWord = Stem.stem(word.toLowerCase());
			nameBuf.append(stemWord);
			nameBuf.append(" ");
		}

		String names = nameBuf.toString();
		Corpus corpus = new Corpus();
		corpus.setJavaFilePath(file.getAbsolutePath());
		corpus.setJavaFileFullClassName(fileName);
		corpus.setContent(names);
		return corpus;
	}


	public static void main(String[] agrs) throws Exception {
		Property.createInstance("C:\\Users\\gzq\\Desktop\\BRTracer\\Dataset\\SWTBugRepository.xml",
				"C:\\Users\\gzq\\Desktop\\BRTracer\\Dataset\\swt-3.1", "C:\\Users\\gzq\\Desktop\\BRTracer\\tmp", 0.2f, "C:\\Users\\gzq\\Desktop\\BRTracer\\Output", "swt", 1);
		
		new ClassName().create();
		System.out.println("Finish");
	}
}
