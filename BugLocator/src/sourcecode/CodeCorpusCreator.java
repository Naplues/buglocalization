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

public class CodeCorpusCreator
{
    public CodeCorpusCreator()
            throws IOException, ParseException
    {}

    public void create()
            throws Exception
    {
        FileDetector detector = new FileDetector("java");
        File[] files = detector.detect(
                Property.getInstance().getSourceCodeDir());
        CodeCorpusCreator corpusCreator = new CodeCorpusCreator();

        FileWriter writeCorpus = new FileWriter(
                Property.getInstance().getWorkDir() +
                        Property.getInstance().getSeparator() + "CodeCorpus.txt");
        FileWriter writer = new FileWriter(Property.getInstance().getWorkDir() +
                Property.getInstance().getSeparator() + "ClassName.txt");
        int count = 0;

        TreeSet<String> nameSet = new TreeSet();
        File[] arrayOfFile1;
        int j = (arrayOfFile1 = files).length;
        for (int i = 0; i < j; i++)
        {
            File file = arrayOfFile1[i];
            Corpus corpus = corpusCreator.create(file);
            if (corpus != null) {
                if (!nameSet.contains(corpus.getJavaFileFullClassName()))
                {
                    if (corpus.getJavaFileFullClassName().endsWith(".java"))
                    {
                        writer.write(count + "\t" +
                                corpus.getJavaFileFullClassName() +
                                Property.getInstance().getLineSeparator());
                        writeCorpus.write(corpus.getJavaFileFullClassName() + "\t" +
                                corpus.getContent() +
                                Property.getInstance().getLineSeparator());
                    }
                    else
                    {
                        writer.write(count + "\t" +
                                corpus.getJavaFileFullClassName() + ".java" +
                                Property.getInstance().getLineSeparator());
                        writeCorpus.write(corpus.getJavaFileFullClassName() + ".java" + "\t" +
                                corpus.getContent() +
                                Property.getInstance().getLineSeparator());
                    }
                    writer.flush();
                    writeCorpus.flush();
                    writer.flush();
                    nameSet.add(corpus.getJavaFileFullClassName());
                    count++;
                }
            }
        }
        Property.getInstance().setFileCount(count);
        writeCorpus.close();
        writer.close();
    }

    public Corpus create(File file)
    {
        FileParser parser = new FileParser(file);

        String fileName = parser.getPackageName();
        if (fileName.trim().equals("")) {
            fileName = file.getName();
        } else {
            fileName = fileName + "." + file.getName();
        }
        fileName = fileName.substring(0, fileName.lastIndexOf("."));
        String[] content = parser.getContent();
        StringBuffer contentBuf = new StringBuffer();
        String[] arrayOfString1;
        int j = (arrayOfString1 = content).length;
        for (int i = 0; i < j; i++)
        {
            String word = arrayOfString1[i];
            stemWord = Stem.stem(word.toLowerCase());
            if ((!Stopword.isKeyword(word)) && (!Stopword.isEnglishStopword(word)))
            {
                contentBuf.append(stemWord);
                contentBuf.append(" ");
            }
        }
        String sourceCodeContent = contentBuf.toString();

        String[] classNameAndMethodName = parser.getClassNameAndMethodName();
        StringBuffer nameBuf = new StringBuffer();
        String[] arrayOfString2;
        String str1 = (arrayOfString2 = classNameAndMethodName).length;
        for (String stemWord = 0; stemWord < str1; stemWord++)
        {
            String word = arrayOfString2[stemWord];
            stemWord = Stem.stem(word.toLowerCase());
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
