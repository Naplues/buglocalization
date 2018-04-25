package property;

/**
 * 属性类
 * 存储程序的基本信息
 * 
 * @author gzq
 *
 */
public class Property {

	public final String bugFilePath; // bug文件路径
	public final String sourceCodeDir; // 源代码目录
	private final String workDir; // 工作目录
	private int fileCount; // 文件计数
	private int wordCount; // 单词计数
	private int bugReportCount; // bug报告计数
	private int bugTermCount; // bug单词计数
	private final float alpha; // alpha值
	private final String outputFile; // 输出文件

	// newly added
	private int originfilecount; // 原始文件计数
	private String project; // 项目
	private int aspectj_filename_offset; // aspectj文件名偏移

	private final String separator = System.getProperty("file.separator"); // 文件分隔符
	private final String lineSeparator = System.getProperty("line.separator"); // 行分隔符

	private static Property p = null; // 静态属性对象, 对外使用

	/**
	 * 获取属性实例
	 * @return
	 */
	public static Property getInstance() {
		return p;
	}

	/**
	 * 创建属性实例
	 * 
	 * @param bugFilePath
	 * @param sourceCodeDir
	 * @param workDir
	 * @param alpha
	 * @param outputFile
	 * @param poj
	 * @param offset
	 */
	public static void createInstance(String bugFilePath, String sourceCodeDir, String workDir, float alpha,
			String outputFile, String poj, int offset) {
		if (p == null)
			p = new Property(bugFilePath, sourceCodeDir, workDir, alpha, outputFile, poj, offset);
	}

	/**
	 * 构造函数
	 * 
	 * @param bugFilePath
	 * @param sourceCodeDir
	 * @param workDir
	 * @param alpha
	 * @param outputFile
	 * @param poj
	 * @param offset
	 */
	private Property(String bugFilePath, String sourceCodeDir, String workDir, float alpha, String outputFile,
			String poj, int offset) {
		this.bugFilePath = bugFilePath;
		this.sourceCodeDir = sourceCodeDir;
		this.workDir = workDir;
		this.alpha = alpha;
		this.outputFile = outputFile;
		this.project = poj;
		this.aspectj_filename_offset = offset;
	}

	public String getProject() {
		return project;
	}

	public int getOffset() {
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

	// new
	public void setOriginFileCount(int n) {
		this.originfilecount = n;
	}

	// new
	public int getOriginFileCount() {
		return originfilecount;
	}

	public String getLineSeparator() {
		return lineSeparator;
	}

	public String getWorkDir() {
		return workDir;
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

	public String getSeparator() {
		return separator;
	}

}
