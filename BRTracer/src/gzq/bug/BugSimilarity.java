package gzq.bug;

import utils.Utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Bug 相似度
 *
 * @author gzq
 */
public class BugSimilarity {
    //本过程读取的数据文件列表
    public static String BugVectorFileName = "BugVector.txt";
    public static String SortedIdFileName = "SortedId.txt";
    //本过程保存的数据文件列表
    public static String BugSimilarityFileName = "BugSimilarity.txt";

    /**
     * 获取余弦值
     *
     * @param firstVector
     * @param secondVector
     * @return
     */
    private static float getCosineValue(float[] firstVector, float[] secondVector) {
        float len1 = 0, len2 = 0, product = 0;
        for (int i = 0; i < Utility.bugTermCount; i++) {
            product += firstVector[i] * secondVector[i];
            len1 += firstVector[i] * firstVector[i];
            len2 += secondVector[i] * secondVector[i];
        }
        return (float) (product / (Math.sqrt(len1) * Math.sqrt(len2)));
    }

    /**
     * 构建长度为bugTerm的bug向量
     * map: bugID-vector
     * @return
     * @throws IOException
     */
    public static Hashtable<Integer, float[]> getBugVector() throws IOException {
        Hashtable<Integer, float[]> vectors = new Hashtable<>();
        BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + BugVectorFileName));
        String line;
        while ((line = reader.readLine()) != null) {
            Integer id = Integer.parseInt(line.substring(0, line.indexOf(".")));  //Bug ID
            float[] vector = new float[Utility.bugTermCount];
            String[] values = line.substring(line.indexOf(";") + 1).trim().split(" "); //索引-值 数组
            for (String value : values) {
                String[] singleValues = value.split(":");  //索引-值
                if (singleValues.length == 2)                    //正常值, 在相应索引位置上写入值
                    vector[Integer.parseInt(singleValues[0])] = Float.parseFloat(singleValues[1]);
            }
            vectors.put(id, vector);
        }
        return vectors;
    }

    /**
     * 计算相似度
     *
     * @throws IOException
     */
    public static void computeBugSimilarity() throws IOException {
        // 按照修复时间排序的bugID列表
        BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + SortedIdFileName));
        //bug报告数量大小的数组, 地址-bugID映射
        int[] idArr = new int[Utility.bugReportCount];
        String line;
        for (int index = 0; (line = reader.readLine()) != null; )
            idArr[index++] = Integer.parseInt(line.substring(0, line.indexOf("\t")));
        //获取bug向量数组
        Hashtable<Integer, float[]> vectors = getBugVector();

        // 从最早的bug开始计算相似度
        FileWriter writer = new FileWriter(Utility.outputFileDir + BugSimilarityFileName);
        for (int i = 0; i < idArr.length; i++) {
            int firstId = idArr[i];                             //目标bugID
            float[] firstVector = vectors.get(firstId);         //目标bug的向量
            String output = firstId + ";";
            for (int j = 0; j < i; j++) {
                int secondId = idArr[j];                        //备选bugID
                float[] secondVector = vectors.get(secondId);   //备选bug向量
                output += secondId + ":" + getCosineValue(firstVector, secondVector) + " ";
            }
            writer.write(output.trim() + Utility.lineSeparator);
            writer.flush();
        }
        writer.close();
    }
}
