package utils;

import java.io.File;
import java.util.LinkedList;

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

    public static String project = "Eclipse";
    public static String bugXMLFile = "EclipseBugRepository.xml";
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

    public static int aspectj_filename_offset = detectSourceFiles(sourceFileDir, srcFileType).length;

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
}
