package gzq.evaluation;

import gzq.bug.Bug;
import gzq.bug.BugCorpus;
import utils.Utility;
import gzq.source.*;

import java.io.*;
import java.util.*;

public class Evaluation {
    //本过程读取的数据文件列表
    public static String ClassNameFileName = "ClassName.txt";
    public static String MethodNameFileName = "MethodName.txt";
    public static String VSMScoreFileName = "VSMScore.txt";
    public static String SimiScoreFileName = "SimiScore.txt";
    public static String LengthScoreFileName = "LengthScore.txt";
    public static String ImportFileName = "Import.txt";

    public static Integer a = CodeCorpus.spiltclass;
    public static Integer b = Utility.B;

    public static Hashtable<String, Integer> idTable = null;
    public static Hashtable<Integer, String> nameTable = null;

    public static Hashtable<String, Integer> methodIdTable = null;
    public static Hashtable<Integer, String> methodNameTable = null;

    public static Hashtable<Integer, TreeSet<String>> fixTable = null;
    public static Hashtable<String, Double> lenTable = null;

    public static Hashtable<String, Integer> LOCTable = null;
    public static HashMap<Integer, String> bugNameTable = null;
    public static HashMap<String, HashSet<String>> shortNameSet = null;
    public static LinkedList<HashMap<String, Integer>> groups = null;
    public static HashMap<String, Integer> tmp_group = null;
    public static Integer TotalLOC = new Integer(0);

    public static void init() throws IOException {
        idTable = Utility.getFileIdTable("ClassName.txt");
        nameTable = Utility.getFileNameTable(ClassNameFileName);

        methodIdTable = Utility.getFileIdTable("MethodName.txt");
        methodNameTable = Utility.getFileNameTable(MethodNameFileName);

        fixTable = Utility.getFixedTable();
        lenTable = Utility.getLenScore(LengthScoreFileName);

        LOCTable = Utility.getLOC(TotalLOC);
        shortNameSet = Utility.getShortNameSet();
        bugNameTable = Utility.getBugNameSet();
        groups = new LinkedList<>();
    }

    /**
     * 获取相关分数
     *
     * @param bugid
     * @return
     * @throws IOException
     */
    public static float[] getRelativeScore(Integer bugid) throws Exception {
        float[] relativeScore = new float[Utility.originFileCount];  //原始文件相关数组260
        for (int i = 0; i < relativeScore.length; i++)
            relativeScore[i] = 0;

        String s = bugNameTable.get(bugid);  //该bug中包含的描述性类名
        if (s == null) return relativeScore;  //没有相关类名，返回0

        String[] f = s.split(" ");  //获取类名数组
        Set<String> nameSet = new HashSet<>();
        for (int i = 0; i < f.length; i++)
            if (shortNameSet.containsKey(f[i]))
                nameSet.addAll(shortNameSet.get(f[i]));  //

        //  add the imported class
        HashMap<String, String> importTable = Utility.getImportTable(ImportFileName);
        Set<String> fullNameSet = idTable.keySet();  //类名集合
        Iterator<String> itr = nameSet.iterator();
        Set<String> appendage = new HashSet<>();
        while (itr.hasNext()) {
            String n = itr.next();
            String ims = importTable.get(n);
            if (ims == null) {
                continue;
            }
            String[] imclasses = ims.split(" ");
            for (int i = 0; i < imclasses.length; i++) {
                Iterator<String> all_itr = fullNameSet.iterator();
                while (all_itr.hasNext()) {
                    String tmpn = all_itr.next();
                    String backupname = tmpn;
                    if (Utility.project.compareTo("AspectJ") == 0) {
                        String[] namefields = tmpn.split("/");
                        int j;
                        for (j = 0; j < namefields.length; j++) {
                            if (namefields[j].compareTo("org") == 0) {
                                break;
                            }
                        }
                        tmpn = "org";
                        for (j = j + 1; j < namefields.length; j++) {
                            tmpn = tmpn + "." + namefields[j];
                        }
                    }
                    // end for aspectj
                    if (tmpn.contains(imclasses[i])) {
                        Integer l1 = tmpn.split("\\.").length;
                        Integer l2 = imclasses[i].split("\\.").length;
                        if (l1 - l2 <= 2) {
                            appendage.add(backupname);
                        }
                    }
                }
            }
        }
        //Calculate scores
        itr = appendage.iterator();
        while (itr.hasNext()) {
            Integer id = idTable.get(itr.next());
            relativeScore[id] = 0.2f;
        }

        itr = nameSet.iterator();
        while (itr.hasNext()) {
            Integer id = idTable.get(itr.next());
            relativeScore[id] = 0.5f;
        }
        return relativeScore;
    }


    public static void evaluate() throws Exception {
        init(); //初始化
        BufferedReader VSMReader = new BufferedReader(new FileReader(Utility.outputFileDir + VSMScoreFileName));
        BufferedReader SimReader = new BufferedReader(new FileReader(Utility.outputFileDir + SimiScoreFileName));
        List<Bug> bugs = BugCorpus.getBugs();
        int count = 0;
        FileWriter writer = new FileWriter(Utility.outputFilePath);  //输出结果文件
        while (count < bugs.size()) {
            String vsmLine = VSMReader.readLine();
            Integer vsmId = Integer.parseInt(bugs.get(count++).getBugId());       //bug ID
            float[] vsmVector = Utility.getVector(vsmLine.substring(vsmLine.indexOf(";") + 1)); //bug VSM向量

            tmp_group = null; //Map<String, Integer>

            // 对VSM进行修正 rVSM
            for (String key : lenTable.keySet()) {
                Double score = lenTable.get(key);  //长度分数
                Integer i = 0;
                while (true) {
                    String name = key + "@" + i.toString() + ".java"; //分段名
                    Integer id = methodIdTable.get(name);             //分段名索引
                    if (id == null) break;
                    vsmVector[id] = vsmVector[id] * score.floatValue();  //修正VSM Vector TF-IDF * LEN
                    i++;
                }
            }
            vsmVector = Utility.normalize(vsmVector);  //归一化

            String simiLine = SimReader.readLine();  //相似bug信息
            Integer graphId = Integer.parseInt(simiLine.substring(0, simiLine.indexOf(";")));  //相似bug ID
            float[] simiVector = Utility.getVector(simiLine.substring(simiLine.indexOf(";") + 1)); //相似bug 相似度,长度为单词数目
            simiVector = Utility.normalize(simiVector);  //对相似向量进行归一化

            float[] finalR = Utility.combine(vsmVector, simiVector, Utility.alpha);  //最终的向量

            float[] finalScore = new float[Utility.originFileCount];  //最终分数向量

            HashMap<Integer, ArrayList<Float>> scores = new HashMap<>();
            for (int counter = 0; counter < finalR.length; counter++) {
                String name = methodNameTable.get(counter);
                name = name.substring(0, name.indexOf('@'));
                Integer id = idTable.get(name);
                if (id == null) {
                    System.err.println(name);
                    continue;
                }
                /* 自动确定代表源文件的文件数 */
                if (scores.containsKey(id)) {
                    ArrayList<Float> t = scores.get(id);
                    t.add(finalR[counter]);
                } else {
                    ArrayList<Float> t = new ArrayList<>();
                    t.add(finalR[counter]);
                    scores.put(id, t);
                }
            }
            for (int i = 0; i < Utility.originFileCount; i++) {
                ArrayList<Float> t = scores.get(i);
                try {
                    Collections.sort(t, Collections.reverseOrder());
                } catch (Exception e) {
                    System.out.println(i);
                    continue;
                }
                finalScore[i] = t.get(0);
            }

            float[] groupScore = getRelativeScore(vsmId);  //获取相对分数
            for (int i = 0; i < Utility.originFileCount; i++) finalScore[i] = finalScore[i] + groupScore[i];
            Rank[] sort = sort(finalScore);  //排序后的分数数组

            Iterator<String> fileIt = fixTable.get(vsmId).iterator();    //修复该bug的文件迭代器
            Hashtable<Integer, String> fileIdTable = new Hashtable<>();  //文件-索引表
            while (fileIt.hasNext()) {
                String fileName = fileIt.next();         //某个修复文件
                Integer fileId = idTable.get(fileName);  //该修复文件的索引
                //未在语料库中找到该文件索引或者没有该文件名
                if (fileId == null || fileName == null) {
                    //System.out.println("null pointer" + fileName);
                    continue;
                }
                fileIdTable.put(fileId, fileName);
            }

            for (int i = 0; i < sort.length; i++) {
                Rank rank = sort[i];  //260个源文件
                if ((!fileIdTable.isEmpty()) && fileIdTable.containsKey(rank.id)) {
                    writer.write(vsmId + "\t" + fileIdTable.get(rank.id) + "\t" + i + "\t" + rank.rank + " " + Utility.lineSeparator);
                    writer.flush();
                    //break;
                }
            }
        }//while end
        writer.close();
    }


    private static Rank[] sort(float[] finalR) {
        Rank[] R = new Rank[finalR.length];
        for (int i = 0; i < R.length; i++) {
            R[i] = new Rank();
            R[i].rank = finalR[i];
            R[i].id = i;
        }
        R = insertionSort(R);
        return R;
    }

    private static Rank[] insertionSort(Rank[] R) {
        for (int i = 0; i < R.length; i++) {
            int maxIndex = i;
            for (int j = i; j < R.length; j++)
                if (R[j].rank > R[maxIndex].rank)
                    maxIndex = j;
            Rank tmpRank = R[i];
            R[i] = R[maxIndex];
            R[maxIndex] = tmpRank;
        }
        return R;
    }


    /**
     * VSM方法进行排序
     */
    public static void rankByVSM() throws Exception{
        float[][] allBugSimValues = Similarity.computeSimilarity();
        List<Bug> bugs = BugCorpus.getBugs();
        for(int i=0;i<allBugSimValues.length;i++){  //对每个相似度进行排序
            Rank[] rank = sort(allBugSimValues[i]);
            System.out.println( bugs.get(i).getBugId() + ":" + rank[0].id);
            System.out.println();
        }
    }

    /**
     * 获取Top k 准确率
     *
     * @param k
     */
    public static void getTopK(int k) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFilePath));
        String line;
        int sum = 0;
        Set<Integer> hasCountBugs = new HashSet<>();
        while ((line = reader.readLine()) != null) {
            String[] value = line.split("\t");
            if(hasCountBugs.contains(Integer.parseInt(value[0])))
                continue;
            hasCountBugs.add(Integer.parseInt(value[0]));
            if (Integer.parseInt(value[2]) < k)
                sum++;
        }

        System.out.println("Top" + k + ": " + (float)sum / hasCountBugs.size());
    }

    public static void getMRR() throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFilePath));
        String line;
        float sum = 0;
        Set<Integer> hasCountBugs = new HashSet<>();
        while ((line = reader.readLine()) != null) {
            String[] value = line.split("\t");
            if(hasCountBugs.contains(Integer.parseInt(value[0])))
                continue;
            hasCountBugs.add(Integer.parseInt(value[0]));
            sum += 1/(Integer.parseInt(value[2]) + 1);
        }
        sum/= hasCountBugs.size();
        System.out.println("MRR: " + sum);
    }

    public static void getMAP() throws IOException{

    }
}
