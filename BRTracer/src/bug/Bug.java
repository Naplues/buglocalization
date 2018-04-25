package bug;

import java.util.TreeSet;
/**
 * Bug 实体类
 * @author gzq
 *
 */
public class Bug {

	String bugId; //bug编号
	String openDate; //开放日期
	String fixDate; //修复日期
	String bugSummary; //标题
	String bugDescription; //描述
	TreeSet<String> set = new TreeSet<String>();  //修复集

	public String getBugId() {
		return bugId;
	}

	public void setBugId(String bugId) {
		this.bugId = bugId;
	}

	public String getOpenDate() {
		return openDate;
	}

	public void setOpenDate(String openDate) {
		this.openDate = openDate;
	}

	public String getFixDate() {
		return fixDate;
	}

	public void setFixDate(String fixDate) {
		this.fixDate = fixDate;
	}

	public String getBugSummary() {
		return bugSummary;
	}

	public void setBugSummary(String bugSummary) {
		this.bugSummary = bugSummary;
	}

	public String getBugDescription() {
		return bugDescription;
	}

	public void setBugDescription(String bugDescription) {
		this.bugDescription = bugDescription;
	}

	public TreeSet<String> getSet() {
		return set;
	}

	public void addFixedFile(String fileName) {
		this.set.add(fileName);
	}

}
