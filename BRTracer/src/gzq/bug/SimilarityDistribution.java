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

    /**
     * 获取修复表
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
     * 获取文件-ID 映射表
     *
     * @return
     * @throws IOException
     */
    public static Hashtable<String, Integer> getFileIdTable() throws IOException {
        Hashtable<String, Integer> idTable = new Hashtable<>();
        BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + "MethodName.txt"));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] values = line.split("\t");
            idTable.put(values[1].trim(), Integer.parseInt(values[0]));
        }
        return idTable;
    }

    public static void distribute() throws Exception {
        // 读取相似bug信息
        BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + "BugSimilarity.txt"));
        // 修复bug修复文件表 文件名-ID表
        Hashtable<Integer, TreeSet<String>> fixedTable = getFixedTable();
        Hashtable<String, Integer> idTable = getFileIdTable();

        FileWriter writer = new FileWriter(Utility.outputFileDir + "SimiScore.txt");
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
