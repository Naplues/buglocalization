package sourcecode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.TreeSet;
import property.Property;
import sourcecode.ast.Corpus;
import sourcecode.ast.FileDetector;
import sourcecode.ast.FileParser;
import utils.Stem;
import utils.Stopword;

public class CodeCorpusCreator_OriginClass {

    public CodeCorpusCreator_OriginClass() throws IOException, ParseException {

    }

    public void create() throws Exception {
        FileDetector detector = new FileDetector("java");
        File[] files = detector.detect(Property.getInstance()
                .getSourceCodeDir());
        CodeCorpusCreator_OriginClass corpusCreator = new CodeCorpusCreator_OriginClass();

        FileWriter writeCorpus = new FileWriter(Property.getInstance()
                .getWorkDir()
                + Property.getInstance().getSeparator() + "CodeCorpus_OriginClass.txt");
	    FileWriter writeImport = new FileWriter(Property.getInstance().getWorkDir() + Property.getInstance().getSeparator() + "Import.txt");

        TreeSet<String> nameSet = new TreeSet<String>();
        for (File file : files) {
	        Corpus corpus = corpusCreator.create(file, writeImport);

	        if (corpus == null)
		        continue;
	        if (!nameSet.contains(corpus.getJavaFileFullClassName())) {


		        if (corpus.getJavaFileFullClassName().endsWith(".java")) {
			        writeCorpus.write(corpus.getJavaFileFullClassName() + "\t"
					        + corpus.getContent()
					        + Property.getInstance().getLineSeparator());
		        } else {
			        writeCorpus.write(corpus.getJavaFileFullClassName() + ".java" + "\t"
					        + corpus.getContent()
					        + Property.getInstance().getLineSeparator());
		        }
		        writeCorpus.flush();
		        nameSet.add(corpus.getJavaFileFullClassName());
	        }

        }
        writeCorpus.close();
	    writeImport.close();
    }

    public Corpus create(File file, FileWriter writeImport) throws IOException {

	    FileParser parser = new FileParser(file);

	    String fileName = parser.getPackageName();
	    if (fileName.trim().equals("")) {
		    fileName = file.getName();
	    } else {
		    fileName += "." + file.getName();
	    }
        
        /* modification for AspectJ */
        if(Property.getInstance().getProject().compareTo("aspectj")==0){
            fileName = file.getPath();
            fileName = fileName.substring(Property.getInstance().getOffset());
        }
	    /* ************************** */

	    writeImport.write(fileName + "\t");
	    parser.getImport(writeImport);
	    writeImport.write(Property.getInstance().getLineSeparator());


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
	    String sourceCodeContent = contentBuf.toString();

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
	    corpus.setContent(sourceCodeContent + " " + names);
	    return corpus;
    }
}
