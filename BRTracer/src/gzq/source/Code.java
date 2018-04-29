package gzq.source;

import java.util.Hashtable;

/**
 * 语料库
 * 全称限定类名
 * 路径
 * 内容
 * 标识符
 * 代码行
 * 长度分数
 * 单词-文档 频度表, 所有文件的语料库共享一份
 * 分段后每个段的索引表
 *
 * @author gzq
 *
 */
public class Code {

	private String javaFileFullClassName;
	private String javaFilePath;
	private String content;
	private String identifiers;
	private Integer loc;
	private double lengthScore;
	public static Hashtable<String, Integer> DFTable;
	public static Hashtable<String, Integer> methodIndexTable = new Hashtable<>();

	public Code(String javaFileFullClassName, String javaFilePath, String content, String identifers) {
		this.javaFileFullClassName = javaFileFullClassName;
		this.javaFilePath = javaFilePath;
		this.content = content;
		this.identifiers = identifers;
	}

	public Code(String javaFileFullClassName, String javaFilePath, String content, String identifers, Integer loc) {
		this.javaFileFullClassName = javaFileFullClassName;
		this.javaFilePath = javaFilePath;
		this.content = content;
		this.identifiers = identifers;
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

	public String getIdentifiers() {
		return identifiers;
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


	public void setIdentifiers(String identifiers) {
		this.identifiers = identifiers;
	}
}
