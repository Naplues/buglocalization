package utils;

import gzq.source.CodeCorpus;
import gzq.source.Code;
import sourcecode.ast.FileParser;
import java.io.*;
import java.util.*;

/**
 * 功效类，定义了项目运行的各项配置信息
 * workDir         工作目录
 * bugFilePath     bugXML文件路径
 * sourceFileDir   源码根目录
 */
public class Utility {
    public static String separator = System.getProperty("file.separator"); // 文件分隔符
    public static String lineSeparator = System.getProperty("line.separator"); // 行分隔符

    public static String project = "AspectJ";
    private static String bugXMLFile = "AspectJBugRepository.xml";
    public static String workDir = "C:\\Users\\gzq\\Desktop\\BRTracer" + separator;
    public static String bugFilePath = workDir + project + separator + bugXMLFile;
    public static String sourceFileDir = workDir + project + "\\Source\\";
    public static String outputFileDir = workDir + project + separator + "Output" + separator;
    public static String srcFileType = "java";
    public static String outputFilePath = outputFileDir + "Result.txt";
    public static String metricDir = outputFileDir + "MAP" + separator;
    public static int originFileCount;
    public static int sourceFileCount;
    public static int sourceWordCount;
    public static int bugReportCount;
    public static int bugTermCount;
    public static int aspectj_filename_offset = sourceFileDir.length();

    public static float alpha = 0.3f;
    public static int B = 50;


    /**
     * 检测指定目录下指定类型的所有源码文件
     *
     * @param absoluteFilePath
     * @param fileType
     * @return 该目录下指定类型的所有文件
     */
    public static File[] detectSourceFiles(String absoluteFilePath, String fileType) {
        LinkedList<File> fileList = new LinkedList<>();  // 文件列表
        addFileToList(absoluteFilePath, fileType, fileList); // 将文件添加到列表中
        return fileList.toArray(new File[fileList.size()]);
    }

    /**
     * 将合适的文件添加到列表中
     *
     * @param absoluteFilePath
     * @param fileType
     * @param fileList
     */
    private static void addFileToList(String absoluteFilePath, String fileType, LinkedList<File> fileList) {
        // 获取指定目录下文件的列表
        File[] files = new File(absoluteFilePath).listFiles();
        // 当列表不为空时递归添加文件
        if (files != null) {
            for (File file : files) {
                if (!file.isDirectory()) {
                    if (fileType == null) {
                        fileList.add(file); // 添加所有文件
                    } else {
                        if (file.getName().endsWith(fileType)) {
                            fileList.addLast(file); // 添加指定类型的文件
                        }
                    }
                } else {
                    addFileToList(file.getAbsolutePath(), fileType, fileList);  //递归检测目录
                }
            }
        }
    }

    /**
     * 计算TF值
     *
     * @param freq
     * @param totalTermCount
     * @return
     */
    public static float getTfValue(int freq, int totalTermCount) {
        return (float) Math.log(freq) + 1;
    }

    /**
     * 计算IDF值
     *
     * @param docCount
     * @param totalCount
     * @return
     */
    public static float getIdfValue(double docCount, double totalCount) {
        return (float) Math.log(totalCount / docCount);
    }

    /**
     * 从Parser中解析类的全称限定类名
     *
     * @param file
     * @return
     */
    public static String getFileName(File file) {
        FileParser parser = new FileParser(file);
        // 文件名 文件内容
        String fileName = parser.getPackageName();
        // fileName = 包名+类名
        if (fileName.trim().equals(""))
            fileName = file.getName();
        else
            fileName += "." + file.getName();
        //AspectJ项目处理
        if (Utility.project.compareTo("AspectJ") == 0)
            fileName = file.getPath().substring(Utility.aspectj_filename_offset);
        fileName = fileName.substring(0, fileName.lastIndexOf(".")).replace("\\", ".");
        return fileName;
    }


    /**
     * 获取文件名-ID 映射表
     *
     * @return
     * @throws IOException
     */
    public static Hashtable<String, Integer> getFileIdTable(String fileName) throws IOException {
        Hashtable<String, Integer> idTable = new Hashtable<>();
        BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + fileName));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] values = line.split("\t");
            idTable.put(values[1].trim(), Integer.parseInt(values[0]));
        }
        return idTable;
    }

    /**
     * 获取 bug-fix 表
     *
     * @return
     * @throws IOException
     */
    public static Hashtable<Integer, TreeSet<String>> getFixedTable() throws IOException {
        Hashtable<Integer, TreeSet<String>> idTable = new Hashtable<>();
        BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + "FixLink.txt"));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] values = line.split("\t");
            Integer id = Integer.parseInt(values[0]); //bug编号
            // 修复表中没有改bugID编号新加入一个该编号的集合映射引用
            if (!idTable.containsKey(id))
                idTable.put(id, new TreeSet<>());
            idTable.get(id).add(values[1].trim());  //加入该ID关联的文件名
        }
        return idTable;
    }

    /**
     * 获取文件名
     *
     * @param fileName
     * @return
     * @throws IOException
     */
    public static Hashtable<Integer, String> getFileNameTable(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + fileName));
        String line;
        Hashtable<Integer, String> table = new Hashtable<>();
        while ((line = reader.readLine()) != null) {
            String[] values = line.split("\t");
            Integer idInteger = Integer.parseInt(values[0]);
            String nameString = values[1].trim();
            table.put(idInteger, nameString);
        }
        return table;
    }

    /**
     * 获取源码长度分数
     *
     * @param fileName
     * @return
     * @throws IOException
     */
    public static Hashtable<String, Double> getLenScore(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + fileName));
        Hashtable<String, Double> lenTable = new Hashtable<>();
        String line;
        while ((line = reader.readLine()) != null) {
            String[] values = line.split("\t");
            String name = values[0];//.substring(0, values[0].lastIndexOf("."));
            Double score = Double.parseDouble(values[1]);
            lenTable.put(name, score);
        }
        reader.close();
        return lenTable;
    }

    /**
     * 获取源码向量
     *
     * @param vectorStr
     * @return
     */
    public static float[] getVector(String vectorStr) {
        float[] vector = new float[Utility.sourceWordCount];
        if (vectorStr == null) return vector;
        String[] values = vectorStr.split(" ");

        for (String value : values) {
            String[] singleValues = value.split(":");
            if (singleValues.length != 1) {
                int index = Integer.parseInt(singleValues[0]);
                float sim = Float.parseFloat(singleValues[1]);
                vector[index] = sim;
            }
        }
        return vector;
    }

    /**
     * 获取导入表
     *
     * @param fileName
     * @return
     * @throws IOException
     */
    public static HashMap<String, String> getImportTable(String fileName) throws IOException {
        BufferedReader importReader = new BufferedReader(new FileReader(Utility.outputFileDir + fileName));
        HashMap<String, String> importTable = new HashMap<>();
        String line;
        while ((line = importReader.readLine()) != null) {
            String[] fields = line.split("\t");
            if (fields.length == 1) importTable.put(fields[0], null);
            else importTable.put(fields[0], fields[1]);
        }
        return importTable;
    }

    /**
     * 对向量进行归一化
     *
     * @param array
     * @return
     */
    public static float[] normalize(float[] array) {
        float max = Float.MIN_VALUE;
        float min = Float.MAX_VALUE;
        for (int i = 0; i < array.length; i++) {
            if (max < array[i])
                max = array[i];
            if (min > array[i])
                min = array[i];
        }
        float span = max - min;
        for (int i = 0; i < array.length; i++)
            array[i] = (array[i] - min) / span;
        return array;
    }

    /**
     * 将VSM和sim结合得到 带BRS的VSM
     *
     * @param vsmVector
     * @param simVector
     * @param f
     * @return
     */
    public static float[] combine(float[] vsmVector, float[] simVector, float f) {
        float[] results = new float[Utility.sourceFileCount];
        for (int i = 0; i < Utility.sourceFileCount; i++)
            results[i] = vsmVector[i] * (1 - f) + simVector[i] * f;
        return results;
    }

    public static Hashtable<String, Integer> getLOC(Integer TotalLOC) {
        Hashtable<String, Integer> table = new Hashtable<>();
        for (Code corpus : CodeCorpus.sourceCorpus) {
            TotalLOC += corpus.getLoc();
            table.put(corpus.getJavaFileFullClassName(), corpus.getLoc());
        }
        System.out.println("Total LOC: " + TotalLOC);
        return table;
    }

    /**
     * 中心化的值
     *
     * @param x
     * @param max
     * @param min
     * @param median
     * @return
     */
    public static Double getNormValue(Double x, Double max, Double min, Double median) {
        return B * (x - median) / (max - min);
    }

    /**
     * 计算代码行
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static Integer countLOC(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        Integer LoC = 0;
        while (reader.readLine() != null) LoC++;
        reader.close();
        return LoC;
    }

    /**
     * 计算单词-文件频度 DF 表
     * @return
     * @throws IOException
     *
     */
    public static Hashtable<String, Integer> countDF(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + fileName));
        Hashtable<String, Integer> DFTable = new Hashtable<>(); //文件-行数表
        String line;
        while ((line = reader.readLine()) != null) {
            String[] values = line.split("\t");
            String[] words = values[2].split(" "); //文件中的单词数组
            TreeSet<String> wordSet = new TreeSet<>();   //独特词单词集合
            for (String word : words) if (!word.trim().equals("") && !wordSet.contains(word)) wordSet.add(word);
            for (String word : wordSet) {
                if (DFTable.containsKey(word)) {
                    Integer count = DFTable.get(word);
                    count++;
                    DFTable.remove(word);
                    DFTable.put(word, count);
                } else DFTable.put(word, 1);
            }
        }
        return DFTable;
    }


    /**
     * 获取短文件名xxx.java
     *
     * @return
     * @throws IOException
     */
    public static HashMap<String, HashSet<String>> getShortNameSet() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + "ClassName.txt"));
        HashMap<String, HashSet<String>> nameSet = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null) {
            String[] fields = line.split("\t");
            String tmp = fields[1].substring(0, fields[1].lastIndexOf("."));
            String name = tmp.substring(tmp.lastIndexOf(".") + 1) + ".java";

            if (!nameSet.containsKey(name)) {
                HashSet<String> t = new HashSet<>();
                t.add(fields[1]);
                nameSet.put(name, t);
            }
        }
        return nameSet;
    }

    /**
     * 获取bug 包含的描述类名称集合
     *
     * @return
     * @throws IOException
     */
    public static HashMap<Integer, String> getBugNameSet() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + "DescriptionClassName.txt"));
        HashMap<Integer, String> bugNameSet = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null) {
            String[] fields = line.split("\t");
            if (fields.length < 2) continue;
            else bugNameSet.put(Integer.parseInt(fields[0]), fields[1]);
        }
        return bugNameSet;
    }
}
