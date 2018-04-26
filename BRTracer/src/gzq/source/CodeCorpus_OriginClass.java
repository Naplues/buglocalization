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

	public static Corpus makeSingleCorpus(File file, FileWriter writeImport) throws IOException {

		FileParser parser = new FileParser(file);

		String fileName = parser.getPackageName();
		String[] content = parser.getContent();

		if (fileName.trim().equals(""))
			fileName = file.getName();
		else
			fileName += "." + file.getName();


		/* modification for AspectJ */
		if(Utility.project.compareTo("AspectJ")==0)
			fileName = file.getPath().substring(Utility.aspectj_filename_offset);

		writeImport.write(fileName + "\t");
		parser.getImport(writeImport);
		writeImport.write(Utility.lineSeparator);

		fileName = fileName.substring(0, fileName.lastIndexOf("."));

		StringBuffer contentBuf = new StringBuffer();
		StringBuffer nameBuf = new StringBuffer();

		for (String word : content)
			if (!(Stopword.isKeyword(word) || Stopword.isEnglishStopword(word)))
				contentBuf.append(Stem.stem(word.toLowerCase()) + " ");

		for (String word : parser.getClassNameAndMethodName())
			nameBuf.append(Stem.stem(word.toLowerCase()) + " ");

		return new Corpus(fileName, file.getAbsolutePath(), contentBuf.toString() + " " + nameBuf.toString());
	}

    public static void create() throws Exception {
        File[] files = Utility.detectSourceFiles(Utility.sourceFileDir, Utility.srcFileType);

        FileWriter writeCorpus = new FileWriter(Utility.outputFileDir + "CodeCorpus_OriginClass.txt");
	    FileWriter writeImport = new FileWriter(Utility.outputFileDir + "Import.txt");

        TreeSet<String> nameSet = new TreeSet<>();
        for (File file : files) {
	        Corpus corpus = makeSingleCorpus(file, writeImport);
	        if (corpus == null)
		        continue;
	        if (!nameSet.contains(corpus.getJavaFileFullClassName())) {
		        if (corpus.getJavaFileFullClassName().endsWith(".java"))
			        writeCorpus.write(corpus.getJavaFileFullClassName() + "\t" + corpus.getContent() + Utility.lineSeparator);
		        else
			        writeCorpus.write(corpus.getJavaFileFullClassName() + ".java" + "\t" + corpus.getContent() + Utility.lineSeparator);
		        writeCorpus.flush();
		        nameSet.add(corpus.getJavaFileFullClassName());
	        }
        }
        writeCorpus.close();
	    writeImport.close();
    }
}
