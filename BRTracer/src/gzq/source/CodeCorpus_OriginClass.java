package gzq.source;

import sourcecode.ast.FileParser;
import utils.Stem;
import utils.Stopword;
import utils.Utility;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeSet;

public class CodeCorpus_OriginClass {

	//本过程保存的数据文件列表
	public static String CodeCorpus_OriginClassFileName = "CodeCorpus_OriginClass.txt";
	public static String ImportFileName = "Import.txt";

	/**
	 *
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

        String[] content = parser.getContent();
		for (String word : content)
			if (!(Stopword.isKeyword(word) || Stopword.isEnglishStopword(word)))
				contentBuf.append(Stem.stem(word.toLowerCase()) + " ");
		for (String word : parser.getClassNameAndMethodName())
			nameBuf.append(Stem.stem(word.toLowerCase()) + " ");
		return new Corpus(fileName, file.getAbsolutePath(), contentBuf.toString() + " " + nameBuf.toString());
	}

    /**
     * 创建原始代码语料库和导入信息
     * @throws Exception
     */
    public static void create() throws Exception {
        File[] files = Utility.detectSourceFiles(Utility.sourceFileDir, Utility.srcFileType);
        FileWriter writeCorpus = new FileWriter(Utility.outputFileDir + CodeCorpus_OriginClassFileName);
	    FileWriter writeImport = new FileWriter(Utility.outputFileDir + ImportFileName);
        TreeSet<String> nameSet = new TreeSet<>();
        for (File file : files) {
	        Corpus corpus = makeSingleCorpus(file, writeImport);
	        if (corpus == null)
		        continue;
	        if (!nameSet.contains(corpus.getJavaFileFullClassName())) {
	            writeCorpus.write(corpus.getJavaFileFullClassName() + ".java" + "\t" + corpus.getContent() + Utility.lineSeparator);
		        writeCorpus.flush();
		        nameSet.add(corpus.getJavaFileFullClassName());
	        }
        }
        writeCorpus.close();
	    writeImport.close();
    }
}
