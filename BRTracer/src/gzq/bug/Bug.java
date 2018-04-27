package gzq.bug;

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
 */
public class Bug {

    String bugId;
    String openDate;
    String fixDate;
    String bugSummary;
    String bugDescription;
    TreeSet<String> set = new TreeSet<>();

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

}
