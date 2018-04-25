package sourcecode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.TreeSet;
import property.Property;
import sourcecode.ast.Corpus;
import sourcecode.ast.FileDetector;
import sourcecode.ast.FileParser;
import utils.Splitter;
import utils.Stem;
import utils.Stopword;

public class CodeCorpusCreator_SpiltCorpus {
	
	private HashMap<String, String> ClassNameAndMethodNameTable;
	public static int spiltclass = 800;

    public CodeCorpusCreator_SpiltCorpus() throws IOException, ParseException {

        ClassNameAndMethodNameTable = new HashMap<String, String>();
        BufferedReader reader =new BufferedReader(
                new FileReader(Property.getInstance().getWorkDir()+Property.getInstance().getSeparator()
                        +"ClassAndMethodCorpus.txt") );
        String line;
        while(true){
            line = reader.readLine();
            if(line == null)
                break;
            String values[] = line.split("\t");
            if(values.length == 1){
                continue;
            }
            String fileName = values[0].substring(0, values[0].lastIndexOf("."));
            ClassNameAndMethodNameTable.put(fileName, values[1]);
        }
        reader.close();

    }

    public void create() throws Exception {
        FileDetector detector = new FileDetector("java");
        File[] files = detector.detect(Property.getInstance()
                .getSourceCodeDir());
        CodeCorpusCreator_SpiltCorpus corpusCreator = new CodeCorpusCreator_SpiltCorpus();

        FileWriter writeCorpus = new FileWriter(Property.getInstance()
                .getWorkDir()
                + Property.getInstance().getSeparator() + "CodeCorpus.txt");
        FileWriter writer = new FileWriter(Property.getInstance().getWorkDir()
				+ Property.getInstance().getSeparator() + "MethodName.txt");
        int count = 0;

        TreeSet<String> nameSet = new TreeSet<String>();
        for (File file : files) {
            Corpus corpus = corpusCreator.create(file);
            if (corpus == null)
                continue;
            
            if (!nameSet.contains(corpus.getJavaFileFullClassName())) {
            	
            	String srccontent = corpus.getContent();
            	String[] src = srccontent.split(" ");
            	Integer methodCount = 0;
            	
            	String tmpFileName = corpus.getJavaFileFullClassName();
            	if(corpus.getJavaFileFullClassName().endsWith(".java")){
            		tmpFileName = tmpFileName.substring(0, tmpFileName.lastIndexOf("."));
            	}
            	String names = ClassNameAndMethodNameTable.get(tmpFileName);
            	while(methodCount==0||methodCount*spiltclass<src.length){
            		StringBuffer content = new StringBuffer();
            		Integer i=methodCount*spiltclass;
            		while(true){
            			if(i>=src.length||i>=(methodCount+1)*spiltclass){
            				break;
            			}
            			content.append(src[i]+" ");
            			i++;
            		}
            		content.append(names);


            		int tmp = count+methodCount;
            		if (corpus.getJavaFileFullClassName().endsWith(".java")) {
            			writer.write( tmp + "\t"
    							+ corpus.getJavaFileFullClassName()+"@"+methodCount+".java"
    							+ Property.getInstance().getLineSeparator());
            			writeCorpus.write(corpus.getJavaFileFullClassName()+"@"+methodCount+".java" + "\t"
            					+ content.toString()
            					+ Property.getInstance().getLineSeparator());
            		} else {
            			writer.write( tmp + "\t"
    							+ corpus.getJavaFileFullClassName()+".java"+"@"+methodCount+".java"
    							+ Property.getInstance().getLineSeparator());
            			writeCorpus.write(corpus.getJavaFileFullClassName()+".java"+"@"+methodCount+".java" + "\t"
            					+ content.toString()
            					+ Property.getInstance().getLineSeparator());
            		}
                
            		methodCount++;
            	}
                writeCorpus.flush();
                writer.flush();
                nameSet.add(corpus.getJavaFileFullClassName());
                count+=methodCount;
            }

        }
        Property.getInstance().setFileCount(count);
        writeCorpus.close();
        writer.close();
    }

    public Corpus create(File file) {
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
	    corpus.setContent(sourceCodeContent + names);
	    return corpus;
    }
}
