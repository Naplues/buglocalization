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
 * @author gzq
 */
public class ClassName {
    /**
     * 读取源文件创建对应的语料库
     *
     * @param file
     * @return
     */
    public static Corpus createCorpus(File file) {
        FileParser parser = new FileParser(file);
        // 文件名 文件内容
        String fileName = parser.getPackageName();
        String[] content = parser.getContent();
        // fileName = 包名+类名
        if (fileName.trim().equals(""))
            fileName = file.getName();
        else
            fileName += "." + file.getName();
        //AspectJ项目处理
        if (Utility.project.compareTo("AspectJ") == 0)
            fileName = file.getPath().substring(Utility.aspectj_filename_offset);
        fileName = fileName.substring(0, fileName.lastIndexOf(".")).replace("\\",".");

        StringBuffer contentBuf = new StringBuffer();
        StringBuffer nameBuf = new StringBuffer();
        for (String word : content)
            if (!(Stopword.isKeyword(word) || Stopword.isEnglishStopword(word)))
                contentBuf.append(Stem.stem(word.toLowerCase()) + " ");
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
        FileWriter writer = new FileWriter(Utility.outputFileDir + "ClassName.txt");
        FileWriter NameWriter = new FileWriter(Utility.outputFileDir + "ClassAndMethodCorpus.txt");
        int count = 0;
        TreeSet<String> nameSet = new TreeSet<>();  //名称集合
        for (File file : files) {
            Corpus corpus = createCorpus(file); //创建语料库
            if (corpus == null)
                continue;
            if (!nameSet.contains(corpus.getJavaFileFullClassName())) {
                writer.write(count + "\t" + corpus.getJavaFileFullClassName() + ".java" + Utility.lineSeparator);
                NameWriter.write(corpus.getJavaFileFullClassName() + ".java" + "\t" + corpus.getContent() + Utility.lineSeparator);
                writer.flush();
                NameWriter.flush();
                nameSet.add(corpus.getJavaFileFullClassName());
                count++;
            }
        }
        Utility.originFileCount = count; //原始文件数
        writer.close();
        NameWriter.close();
        Utility.writeConfig("originFileCount", Utility.originFileCount + Utility.lineSeparator);  //写入配置
    }
}
