package sourcecode.ast;

import java.io.File;
import java.util.LinkedList;

/**
 * 文件检测器
 * 
 * @author gzq
 *
 */
public class FileDetector {

	private LinkedList<File> fileList = new LinkedList<File>(); // 文件列表
	private String fileType = null; // 文件类型

	public FileDetector() {
	}

	/**
	 * 实例化指定文件类型的检测器
	 * 
	 * @param fileType
	 */
	public FileDetector(String fileType) {
		this.fileType = fileType;
	}

	/**
	 * 检测指定目录下指定类型的所有文件
	 * 
	 * @param absoluteFilePath
	 * @return
	 */
	public File[] detect(String absoluteFilePath) {
		// 获取指定目录下文件的列表
		File[] files = new File(absoluteFilePath).listFiles();
		// 当列表不为空时递归添加文件
		if (files != null) {
			for (File file : files) {
				if (!file.isDirectory()) {
					if (fileType == null) {
						fileList.add(file); // 添加所有文件
					} else {
						if (file.getName().endsWith(fileType)) {
							fileList.addLast(file); // 添加指定类型的文件
						}
					}
				} else {
					detect(file.getAbsolutePath());  //递归检测目录
				}
			}
		}
		
		return fileList.toArray(new File[fileList.size()]);
	}
}
