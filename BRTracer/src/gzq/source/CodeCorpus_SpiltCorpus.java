package gzq.source;

import sourcecode.ast.FileParser;
import utils.Stem;
import utils.Stopword;
import utils.Utility;

import java.io.*;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * 代码语料库
 *
 * 类名-方法名表
 *
 */
public class CodeCorpus_SpiltCorpus {

    private static HashMap<String, String> ClassNameAndMethodNameTable = new HashMap<>();
    public static int spiltclass = 800;


    public CodeCorpus_SpiltCorpus() throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(Utility.workDir + "ClassAndMethodCorpus.txt"));
        String line;
        while (true) {
            line = reader.readLine();
            if (line == null)
                break;
            String values[] = line.split("\t");
            // 该类没有方法名
            if (values.length == 1) {
                continue;
            }
            // 提取完整类名,去除.java字符串
            String fileName = values[0].substring(0, values[0].lastIndexOf("."));
            // 保存类名-方法名表
            ClassNameAndMethodNameTable.put(fileName, values[1]);
        }
        reader.close();
    }

    /**
     * 创建指定源码文件的预料库
     *
     * @param file 源码文件名
     * @return
     */
    public static Corpus makeSingleCorpus(File file) {
        FileParser parser = new FileParser(file);
        //从解析器获取文件名和文件内容
        String fileName = parser.getPackageName();
        String[] content = parser.getContent();

        if (fileName.trim().equals(""))
            fileName = file.getName(); // 包名为空
        else
            fileName += "." + file.getName(); // 包名不为空

        /* 对 AspectJ 项目的偏移修改 */
        if (Utility.project.compareTo("AspectJ") == 0)
            fileName = file.getPath().substring(Utility.aspectj_filename_offset);

        fileName = fileName.substring(0, fileName.lastIndexOf("."));

        StringBuffer contentBuf = new StringBuffer();
        StringBuffer nameBuf = new StringBuffer();

        for (String word : content)
            if (!(Stopword.isKeyword(word) || Stopword.isEnglishStopword(word)))
                contentBuf.append(Stem.stem(word.toLowerCase()) + " ");

        for (String word :  parser.getClassNameAndMethodName())
            nameBuf.append(Stem.stem(word.toLowerCase() + " "));

        return new Corpus(fileName, file.getAbsolutePath(), contentBuf.toString() + nameBuf.toString());
    }

    public static void create() throws Exception {
        // 检测出所有源文件
        File[] files = Utility.detectSourceFiles(Utility.sourceFileDir, Utility.srcFileType);
        // 创建代码语料库 和 方法名 文件
        FileWriter writeCorpus = new FileWriter(Utility.outputFileDir + "CodeCorpus.txt");
        FileWriter writer = new FileWriter(Utility.outputFileDir + "MethodName.txt");
        int count = 0;
        TreeSet<String> nameSet = new TreeSet<>();
        for (File file : files) {
            // 创建指定源码的预料库
            Corpus corpus = makeSingleCorpus(file);
            if (corpus == null)
                continue;
            if (!nameSet.contains(corpus.getJavaFileFullClassName())) {
                String[] src = corpus.getContent().split(" ");
                // 方法数目
                Integer methodCount = 0;

                String tmpFileName = corpus.getJavaFileFullClassName();
                if (corpus.getJavaFileFullClassName().endsWith(".java"))
                    tmpFileName = tmpFileName.substring(0, tmpFileName.lastIndexOf("."));

                String names = ClassNameAndMethodNameTable.get(tmpFileName);
                while (methodCount == 0 || methodCount * spiltclass < src.length) {
                    StringBuffer content = new StringBuffer();
                    Integer i = methodCount * spiltclass;
                    while (true) {
                        if (i >= src.length || i >= (methodCount + 1) * spiltclass) {
                            break;
                        }
                        content.append(src[i] + " ");
                        i++;
                    }
                    content.append(names);

                    int tmp = count + methodCount;
                    if (corpus.getJavaFileFullClassName().endsWith(".java")) {
                        writer.write(tmp + "\t" + corpus.getJavaFileFullClassName() + "@" + methodCount + ".java" + Utility.lineSeparator);
                        writeCorpus.write(corpus.getJavaFileFullClassName() + "@" + methodCount + ".java" + "\t" + content.toString() + Utility.lineSeparator);
                    } else {
                        writer.write(tmp + "\t" + corpus.getJavaFileFullClassName() + ".java" + "@" + methodCount + ".java" + Utility.lineSeparator);
                        writeCorpus.write(corpus.getJavaFileFullClassName() + ".java" + "@" + methodCount + ".java" + "\t" + content.toString() + Utility.lineSeparator);
                    }
                    methodCount++;
                }
                writeCorpus.flush();
                writer.flush();
                nameSet.add(corpus.getJavaFileFullClassName());
                count += methodCount;
            }
        }
        Utility.sourceFileCount = count;
        writeCorpus.close();
        writer.close();
    }


}
