package sourcecode.ast;

public class Corpus {
	private String javaFileFullClassName;
	private String javaFilePath;
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
	private String content;
}
