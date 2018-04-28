package gzq.source;

/**
 * 语料库实体
 *
 * java文件全称限定类名
 * 文件路径
 * 内容
 *
 * @author gzq
 *
 */
public class Corpus {

	private String javaFileFullClassName;
	private String javaFilePath;
	private String content;
	private String identifers;
	private Integer loc;
	private double lengthScore;  //长度分数

	public Corpus(String javaFileFullClassName, String javaFilePath, String content, String identifers) {
		this.javaFileFullClassName = javaFileFullClassName;
		this.javaFilePath = javaFilePath;
		this.content = content;
		this.identifers = identifers;
	}

	public Corpus(String javaFileFullClassName, String javaFilePath, String content, String identifers, Integer loc) {
		this.javaFileFullClassName = javaFileFullClassName;
		this.javaFilePath = javaFilePath;
		this.content = content;
		this.identifers = identifers;
		this.loc = loc;
	}

	public String getJavaFileFullClassName() {
		return javaFileFullClassName;
	}

	public void setJavaFileFullClassName(String javaFileFullClassName) {
		this.javaFileFullClassName = javaFileFullClassName;
	}

	public String getJavaFilePath() {
		return javaFilePath;
	}

	public void setJavaFilePath(String javaFilePath) {
		this.javaFilePath = javaFilePath;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getIdentifers() {
		return identifers;
	}

	public Integer getLoc() {
		return loc;
	}

	public void setLoc(Integer loc) {
		this.loc = loc;
	}

	public double getLengthScore() {
		return lengthScore;
	}

	public void setLengthScore(double lengthScore) {
		this.lengthScore = lengthScore;
	}
}
