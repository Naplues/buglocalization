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

	public Corpus(String javaFileFullClassName, String javaFilePath, String content) {
		this.javaFileFullClassName = javaFileFullClassName;
		this.javaFilePath = javaFilePath;
		this.content = content;
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
}
