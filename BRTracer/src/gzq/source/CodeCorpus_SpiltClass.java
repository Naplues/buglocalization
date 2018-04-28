package gzq.source;

import sourcecode.ast.FileParser;
import utils.Stem;
import utils.Stopword;
import utils.Utility;

import java.io.*;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * 代码分段后的语料库
 *
 * 类名-方法名表
 *
 */
public class CodeCorpus_SpiltClass {
    //本过程读取的数据文件列表
    public static String ClassAndMethodCorpusFileName = "ClassAndMethodCorpus.txt";
    //本过程保存的数据文件列表
    public static String CodeCorpusFileName = "CodeCorpus.txt";
    public static String MethodNameFileName = "MethodName.txt";

    private static HashMap<String, String> ClassNameAndMethodNameTable = new HashMap<>();
    public static int spiltclass = 800;  //分段大小800个单词

    /**
     * 初始化
     * @throws IOException
     */
    public static void init() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + ClassAndMethodCorpusFileName));
        String line;
        while (true) {
            line = reader.readLine();
            if (line == null) break;
            String values[] = line.split("\t");
            // 该类没有方法名
            if (values.length == 1) continue;
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
        String fileName = Utility.getFileName(file);
        String[] content = parser.getContent();

        StringBuffer contentBuf = new StringBuffer();
        StringBuffer nameBuf = new StringBuffer();

        for (String word : content)
            if (!(Stopword.isKeyword(word) || Stopword.isEnglishStopword(word)))
                contentBuf.append(Stem.stem(word.toLowerCase()) + " ");
        for (String word :  parser.getClassNameAndMethodName())
            nameBuf.append(Stem.stem(word.toLowerCase() + " "));
        return new Corpus(fileName, file.getAbsolutePath(), contentBuf.toString() + nameBuf.toString(),"");
    }

    /**
     * 创建代码语料库和方法名
     * @throws Exception
     */
    public static void create() throws Exception {
        init();  //初始化
        // 检测出所有源文件
        File[] files = Utility.detectSourceFiles(Utility.sourceFileDir, Utility.srcFileType);
        // 创建代码语料库 和 方法名 文件
        FileWriter writeCorpus = new FileWriter(Utility.outputFileDir + CodeCorpusFileName);
        FileWriter writer = new FileWriter(Utility.outputFileDir + MethodNameFileName);
        int count = 0;  //
        TreeSet<String> nameSet = new TreeSet<>();
        for (File file : files) {
            // 创建指定源码的预料库
            Corpus corpus = makeSingleCorpus(file);
            if (corpus == null) continue; //该语料库为null
            if (!nameSet.contains(corpus.getJavaFileFullClassName())) {
                String[] src = corpus.getContent().split(" ");     //源码中的单词数
                Integer methodCount = 0;  // 方法数目
                String tmpFileName = corpus.getJavaFileFullClassName();  //源码全城限定类名,没有.java
                String names = ClassNameAndMethodNameTable.get(tmpFileName);
                while (methodCount == 0 || methodCount * spiltclass < src.length) {
                    StringBuffer content = new StringBuffer();
                    Integer i = methodCount * spiltclass; //新段起始位置
                    while (true) { //超出全文总长或者 到达该段总长，跳出
                        if (i >= src.length || i >= (methodCount + 1) * spiltclass) break;
                        content.append(src[i] + " ");
                        i++;
                    }
                    content.append(names);  // 添加源码中的方法名
                    int tmp = count + methodCount;
                    writer.write(tmp + "\t" + tmpFileName + ".java" + "@" + methodCount + ".java" + Utility.lineSeparator);
                    writeCorpus.write(tmp+"\t"+ tmpFileName + ".java" + "@" + methodCount + ".java" + "\t" + content.toString() + Utility.lineSeparator);
                    methodCount++;
                }
                writeCorpus.flush();
                writer.flush();
                nameSet.add(tmpFileName);
                count += methodCount; //累计该文件的段数
            }
        }
        Utility.sourceFileCount = count;  //分段后的源码文件数目
        writeCorpus.close();
        writer.close();
    }
}
