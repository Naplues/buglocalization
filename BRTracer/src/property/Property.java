package property;
public class Property {
	public final String bugFilePath;
	public final String sourceCodeDir;
	private final String workDir;
	private int fileCount;
	private int wordCount;
	private int bugReportCount;
	private int bugTermCount;
	private final float alpha;
	private final String outputFile;
	
	//newly added
	private int originfilecount;
    private String project;
    private int aspectj_filename_offset;

    public String getProject(){
        return project;
    }

    public int getOffset(){
        return aspectj_filename_offset;
    }

	public int getBugTermCount() {
		return bugTermCount;
	}

	public void setBugTermCount(int bugTermCount) {
		this.bugTermCount = bugTermCount;
	}

	public int getBugReportCount() {
		return bugReportCount;
	}

	public void setBugReportCount(int bugReportCount) {
		this.bugReportCount = bugReportCount;
	}

	public int getFileCount() {
		return fileCount;
	}

	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}

	public int getWordCount() {
		return wordCount;
	}

	public void setWordCount(int wordCount) {
		this.wordCount = wordCount;
	}
	
	//new 
	public void setOriginFileCount(int n){
		this.originfilecount = n;
	}
	
	//new
	public int getOriginFileCount(){
		return originfilecount;
	}

	private final String separator=System.getProperty("file.separator");
	private final String lineSeparator=System.getProperty("line.separator");
	public String getLineSeparator() {
		return lineSeparator;
	}

	public String getWorkDir() {
		return workDir;
	}

	private static Property p = null;

	public static void createInstance(String bugFilePath, String sourceCodeDir,String workDir,float alpha,String outputFile, String poj, int offset) {
		if (p == null)
			p = new Property(bugFilePath, sourceCodeDir,workDir,alpha,outputFile,poj, offset);
	}

	public static Property getInstance() {
		return p;
	}

	private Property(String bugFilePath, String sourceCodeDir,String workDir,float alpha,String outputFile, String poj, int offset) {
		this.bugFilePath = bugFilePath;
		this.sourceCodeDir = sourceCodeDir;
		this.workDir=workDir;
		this.alpha=alpha;
		this.outputFile=outputFile;
        this.project = poj;
        this.aspectj_filename_offset = offset;
	}

	public float getAlpha() {
		return alpha;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public String getBugFilePath() {
		return bugFilePath;
	}

	public String getSourceCodeDir() {
		return sourceCodeDir;
	}
	public String getSeparator(){
		return separator;
	}

}
