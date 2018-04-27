package utils;

import sourcecode.ast.FileParser;

import java.io.*;
import java.util.*;

/**
 * 功效类，定义了项目运行的各项配置信息
 * workDir         工作目录
 * bugFilePath     bugXML文件路径
 * sourceFileDir   源码根目录
 *
 */
public class Utility {
    public static  String separator = System.getProperty("file.separator"); // 文件分隔符
    public static  String lineSeparator = System.getProperty("line.separator"); // 行分隔符

    public static String project = "Swt";
    private static String bugXMLFile = "SWTBugRepository.xml";
    public static String workDir = "C:\\Users\\gzq\\Desktop\\BRTracer" + separator;
    public static String bugFilePath = workDir + project + separator + bugXMLFile;
    public static String sourceFileDir = workDir + project + "\\Source\\";
    public static String outputFileDir = workDir + project + separator + "Output" + separator;
    public static String srcFileType = "java";
    public static String outputFilePath = outputFileDir + "Result.txt";
    public static String metricDir = outputFileDir + "MAP" + separator;
    public static int sourceFileCount;
    public static int sourceWordCount;
    public static int bugReportCount;
    public static int bugTermCount;
    public static float alpha = 0.3f;

    public static int originFileCount;

    public static int aspectj_filename_offset = sourceFileDir.length();

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
     * @param absoluteFilePath
     * @param fileType
     * @param fileList
     */
    private static void addFileToList(String absoluteFilePath, String fileType, LinkedList<File> fileList){
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
     * @param freq
     * @param totalTermCount
     * @return
     */
    public static float getTfValue(int freq, int totalTermCount) {
        return (float) Math.log(freq) + 1;
    }

    /**
     * 计算IDF值
     * @param docCount
     * @param totalCount
     * @return
     */
    public static float getIdfValue(double docCount, double totalCount) {
        return (float) Math.log(totalCount / docCount);
    }

    /**
     * 从Parser中解析类的全称限定类名
     * @param file
     * @return
     */
    public static String getFileName(File file){
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
        fileName = fileName.substring(0, fileName.lastIndexOf(".")).replace("\\",".");
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
     * 获源码取向量
     * @param vectorStr
     * @return
     */
    public static float[] getVector(String vectorStr) {
        float[] vector = new float[Utility.sourceFileCount];
        String[] values = vectorStr.split(" ");
        for (String value : values) {
            String[] singleValues = value.split(":");
            if (singleValues.length == 2) {
                int index = Integer.parseInt(singleValues[0]);
                float sim = Float.parseFloat(singleValues[1]);
                vector[index] = sim;
            }
        }
        return vector;
    }

    /**
     * 获取导入表
     * @param fileName
     * @return
     * @throws IOException
     */
    public static HashMap<String,String> getImportTable(String fileName) throws IOException{
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
     * 写入配置
     * @param name
     * @param value
     */
    public static void writeConfig(String name, String value) throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + "Config.txt"));
        List lines = new ArrayList();
        String line;
        while ((line = reader.readLine()) != null)
            lines.add(line.toString());

        FileWriter configWriter = new FileWriter(Utility.outputFileDir + "Config.txt");
        for(int i = 0;i<lines.size();i++)
            configWriter.write(lines.get(i).toString() + lineSeparator);
        configWriter.write(name + ":" + value + lineSeparator);
        configWriter.close();
    }
}
