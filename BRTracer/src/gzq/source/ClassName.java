package gzq.source;

import sourcecode.ast.FileParser;
import utils.Stem;
import utils.Stopword;
import utils.Utility;

import java.io.File;
import java.io.FileWriter;
import java.util.TreeSet;

/**
 * 类名称
 * 提取源码文件的类名和其中的方法名
 * 输出文件：
 * ClassName.txt            原始类文件和其索引
 * ClassAndMethodCorpus.txt 原始类文件和其中包含的类名和方法名称
 *
 * @author gzq
 */
public class ClassName {

    //本过程保存的数据文件列表
    public static String ClassNameFileName = "ClassName.txt";
    public static String ClassAndMethodCorpusFileName = "ClassAndMethodCorpus.txt";

    /**
     * 读取源文件创建对应的原始文件语料库
     *
     * @param file
     * @return
     */
    public static Corpus createCorpus(File file) {
        FileParser parser = new FileParser(file);
        String fileName = Utility.getFileName(file);  //该文件的全称限定类名
        StringBuffer nameBuf = new StringBuffer();
        //去除关键字、停用词、提取词干、转换为小写
        for (String word : parser.getClassNameAndMethodName())
            nameBuf.append(Stem.stem(word.toLowerCase()) + " ");
        return new Corpus(fileName, file.getAbsolutePath(), nameBuf.toString());
    }

    /**
     * 创建类名文件
     *
     * @throws Exception
     */
    public static void create() throws Exception {
        // 检测出所有的源文件
        File[] files = Utility.detectSourceFiles(Utility.sourceFileDir, Utility.srcFileType);
        FileWriter writer = new FileWriter(Utility.outputFileDir + ClassNameFileName);
        FileWriter NameWriter = new FileWriter(Utility.outputFileDir + ClassAndMethodCorpusFileName);
        int index = 0;
        TreeSet<String> classNameSet = new TreeSet<>();  //类文件名称集合
        for (File file : files) {
            Corpus corpus = createCorpus(file); //根据源文件创建语料库
            if (!classNameSet.contains(corpus.getJavaFileFullClassName())) {
                writer.write(index + "\t" + corpus.getJavaFileFullClassName() + ".java" + Utility.lineSeparator);
                NameWriter.write(corpus.getJavaFileFullClassName() + ".java" + "\t" + corpus.getContent() + Utility.lineSeparator);
                writer.flush();
                NameWriter.flush();
                classNameSet.add(corpus.getJavaFileFullClassName());
                index++;
            }
        }
        writer.close();
        NameWriter.close();
        Utility.originFileCount = index; //原始文件数
        Utility.writeConfig("originFileCount", Utility.originFileCount + Utility.lineSeparator);  //写入配置
        System.out.println("   Generated " + index + " Origin ClassNames");
    }
}
