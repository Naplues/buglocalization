package property;

public class Property
{
    public final String bugFilePath;
    public final String sourceCodeDir;
    private final String workDir;
    private int fileCount;
    private int wordCount;
    private int bugReportCount;
    private int bugTermCount;
    private final float alpha;
    private final String outputFile;

    public int getBugTermCount()
    {
        return this.bugTermCount;
    }

    public void setBugTermCount(int bugTermCount)
    {
        this.bugTermCount = bugTermCount;
    }

    public int getBugReportCount()
    {
        return this.bugReportCount;
    }

    public void setBugReportCount(int bugReportCount)
    {
        this.bugReportCount = bugReportCount;
    }

    public int getFileCount()
    {
        return this.fileCount;
    }

    public void setFileCount(int fileCount)
    {
        this.fileCount = fileCount;
    }

    public int getWordCount()
    {
        return this.wordCount;
    }

    public void setWordCount(int wordCount)
    {
        this.wordCount = wordCount;
    }

    private final String separator = System.getProperty("file.separator");
    private final String lineSeparator = System.getProperty("line.separator");

    public String getLineSeparator()
    {
        return this.lineSeparator;
    }

    public String getWorkDir()
    {
        return this.workDir;
    }

    private static Property p = null;

    public static void createInstance(String bugFilePath, String sourceCodeDir, String workDir, float alpha, String outputFile)
    {
        if (p == null) {
            p = new Property(bugFilePath, sourceCodeDir, workDir, alpha, outputFile);
        }
    }

    public static Property getInstance()
    {
        return p;
    }

    private Property(String bugFilePath, String sourceCodeDir, String workDir, float alpha, String outputFile)
    {
        this.bugFilePath = bugFilePath;
        this.sourceCodeDir = sourceCodeDir;
        this.workDir = workDir;
        this.alpha = alpha;
        this.outputFile = outputFile;
    }

    public float getAlpha()
    {
        return this.alpha;
    }

    public String getOutputFile()
    {
        return this.outputFile;
    }

    public String getBugFilePath()
    {
        return this.bugFilePath;
    }

    public String getSourceCodeDir()
    {
        return this.sourceCodeDir;
    }

    public String getSeparator()
    {
        return this.separator;
    }
}
