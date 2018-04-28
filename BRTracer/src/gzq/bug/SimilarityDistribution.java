package gzq.bug;

import utils.Utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * 相似度分布
 */
public class SimilarityDistribution {
    //本过程读取的数据文件列表
    public static String MethodNameFileName = "MethodName.txt";
    public static String BugSimilarityFileName = "BugSimilarity.txt";

    //本过程保存的数据文件列表
    public static String SimiScoreFileName = "SimiScore.txt";


    /**
     * 分布
     * @throws Exception
     */
    public static void distribute() throws Exception {
        // 读取相似bug信息
        BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + BugSimilarityFileName));
        // 修复bug修复文件表 文件名-ID表
        Hashtable<Integer, TreeSet<String>> fixedTable = Utility.getFixedTable();
        Hashtable<String, Integer> idTable = Utility.getFileIdTable(MethodNameFileName);  //获取分段后文件名-ID 映射表

        FileWriter writer = new FileWriter(Utility.outputFileDir + SimiScoreFileName);
        String line;
        while ((line = reader.readLine()) != null) {
            float[] similarValues = new float[Utility.sourceFileCount];
            // 当前bug编号  该与bug相比的之前的bug的编号及相似度数组
            Integer id = Integer.parseInt(line.substring(0, line.indexOf(";")));
            String[] values = line.substring(line.indexOf(";") + 1).trim().split(" ");
            for (String value : values) {
                String[] singleValues = value.split(":");
                // 正常情况下 bug编号和相似度均存在
                if (singleValues.length == 2) {
                    Integer simBugId = Integer.parseInt(singleValues[0]);
                    float sim = Float.parseFloat(singleValues[1]);
                    // 修复该bug的文件集合
                    TreeSet<String> fileSet = fixedTable.get(simBugId);
                    if (fileSet == null)
                        System.out.println(simBugId);

                    for (Iterator<String> fileSetIt = fileSet.iterator(); fileSetIt.hasNext(); ) {
                        //取出文件集合中的某一个文件
                        String name = fileSetIt.next();
                        //change: add the singleValue to each segment belonged to that class
                        Integer counter = 0;
                        for (; true; counter++) {
                            Integer fileId = idTable.get(name + "@" + counter + ".java");
                            if (fileId == null) {
                                if (counter == 0)
                                    System.err.println(name);
                                break;
                            }
                            similarValues[fileId] += sim / fileSet.size();
                        }
                    }
                }
            }
            String output = id + ";";
            for (int i = 0; i < Utility.sourceFileCount; i++)
                if (similarValues[i] != 0)
                    output += i + ":" + similarValues[i] + " ";
            writer.write(output.trim() + Utility.lineSeparator);
            writer.flush();
        }
        writer.close();
    }
}
