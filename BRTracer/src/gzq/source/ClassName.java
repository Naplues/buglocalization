package gzq.source;

import sourcecode.ast.Corpus;
import sourcecode.ast.FileParser;
import utils.Stem;
import utils.Stopword;
import utils.Utility;

import java.io.File;
import java.io.FileWriter;
import java.util.TreeSet;

/**
 * 类名称
 *
 * @author gzq
 */
public class ClassName {

    /**
     * 创建类名文件
     *
     * @throws Exception
     */
    public static void create() throws Exception {
        // java文件检测器
        File[] files = Utility.detectSourceFiles(Utility.sourceFileDir, Utility.srcFileType);

        // 语料库创建
        ClassName corpusCreator = new ClassName();

        FileWriter writer = new FileWriter(Utility.workDir + Utility.separator + "ClassName.txt");
        FileWriter NameWriter = new FileWriter(Utility.workDir + Utility.separator + "ClassAndMethodCorpus.txt");

        int count = 0;

        TreeSet<String> nameSet = new TreeSet<String>();
        for (File file : files) {
            Corpus corpus = corpusCreator.createCorpus(file);

            if (corpus == null)
                continue;
            if (!nameSet.contains(corpus.getJavaFileFullClassName())) {
                if (corpus.getJavaFileFullClassName().endsWith(".java")) {
                    writer.write(count + "\t" + corpus.getJavaFileFullClassName() + Utility.lineSeparator);
                    NameWriter.write(corpus.getJavaFileFullClassName() + "\t" + corpus.getContent() + Utility.lineSeparator);
                } else {
                    writer.write(count + "\t" + corpus.getJavaFileFullClassName() + ".java" + Utility.lineSeparator);
                    NameWriter.write(corpus.getJavaFileFullClassName() + ".java" + "\t" + corpus.getContent() + Utility.lineSeparator);
                }
                writer.flush();
                NameWriter.flush();
                nameSet.add(corpus.getJavaFileFullClassName());
                count++;
            }
        }
        Utility.originFileCount = count;
        writer.close();
        NameWriter.close();
    }

    /**
     * 读取文件创建语料库
     *
     * @param file
     * @return
     */
    public static Corpus createCorpus(File file) {
        FileParser parser = new FileParser(file);

        String fileName = parser.getPackageName();
        if (fileName.trim().equals("")) {
            fileName = file.getName();
        } else {
            fileName += "." + file.getName();
        }

        /* modification for AspectJ */
        if (Utility.project.compareTo("aspectj") == 0) {
            fileName = file.getPath();
            fileName = fileName.substring(Utility.aspectj_filename_offset);
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

        ClassName.create();
        System.out.println("Finish");
    }
}
