package gzq.bug;

import java.util.HashMap;
import java.util.TreeSet;

/**
 * Bug 实体类
 *
 * @author gzq
 *         bug编号
 *         开放日期
 *         修复日期
 *         综述
 *         描述
 *         修复集
 *         bug向量
 */
public class Bug {

    private String bugId;
    private String openDate;
    private String fixDate;
    private String bugSummary;
    private String bugDescription;
    private TreeSet<String> set = new TreeSet<>();
    private HashMap<Integer, Float> preSimValues = new HashMap<>();      //该bug之前的相似的bug
    private float[] similarValues;
    private float[] vector;

    public Bug(String bugId, String openDate, String fixDate) {
        this.bugId = bugId;
        this.openDate = openDate;
        this.fixDate = fixDate;
    }

    public String getBugId() {
        return bugId;
    }

    public String getBugSummary() {
        return bugSummary;
    }

    public void setBugSummary(String bugSummary) { this.bugSummary = bugSummary; }

    public String getBugDescription() {
        return bugDescription;
    }

    public void setBugDescription(String bugDescription) { this.bugDescription = bugDescription; }

    public TreeSet<String> getSet() {
        return set;
    }

    public void addFixedFile(String fileName) {
        this.set.add(fileName);
    }

    public HashMap<Integer, Float> getPreSimValues() {
        return preSimValues;
    }

    public void setPreSimValues(HashMap<Integer, Float> preSimValues) {
        this.preSimValues = preSimValues;
    }

    public float[] getSimilarValues() {
        return similarValues;
    }

    public void setSimilarValues(float[] similarValues) {
        this.similarValues = similarValues;
    }
}
