package bug;

import java.util.TreeSet;

public class Bug
{
    String bugId;
    String openDate;
    String fixDate;
    String bugSummary;
    String bugDescription;
    TreeSet<String> set = new TreeSet();

    public String getBugId()
    {
        return this.bugId;
    }

    public void setBugId(String bugId)
    {
        this.bugId = bugId;
    }

    public String getOpenDate()
    {
        return this.openDate;
    }

    public void setOpenDate(String openDate)
    {
        this.openDate = openDate;
    }

    public String getFixDate()
    {
        return this.fixDate;
    }

    public void setFixDate(String fixDate)
    {
        this.fixDate = fixDate;
    }

    public String getBugSummary()
    {
        return this.bugSummary;
    }

    public void setBugSummary(String bugSummary)
    {
        this.bugSummary = bugSummary;
    }

    public String getBugDescription()
    {
        return this.bugDescription;
    }

    public void setBugDescription(String bugDescription)
    {
        this.bugDescription = bugDescription;
    }

    public TreeSet<String> getSet()
    {
        return this.set;
    }

    public void addFixedFile(String fileName)
    {
        this.set.add(fileName);
    }
}
