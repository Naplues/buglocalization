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
 * 提取源码文件的类和其中的方法
 * @author gzq
 */
public class ClassName {

    /**
     * 创建类名文件
     *
     * @throws Exception
     */
    public static void create() throws Exception {
        // 检测出所有的源文件
        File[] files = Utility.detectSourceFiles(Utility.sourceFileDir, Utility.srcFileType);

        FileWriter writer = new FileWriter(Utility.outputFileDir + "ClassName.txt");
        FileWriter NameWriter = new FileWriter(Utility.outputFileDir + "ClassAndMethodCorpus.txt");

        int count = 0;

        TreeSet<String> nameSet = new TreeSet<>();
        for (File file : files) {
            //创建语料库
            Corpus corpus = createCorpus(file);

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
     * 读取源文件创建对应的语料库
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
        if (Utility.project.compareTo("AspectJ") == 0) {
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

    public static void main(String[] args) throws Exception {
        System.out.println("Getting all class names...");
        ClassName.create();
        System.out.println("Finish");
    }
}
