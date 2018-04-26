package sourcecode.ast;

import java.io.File;
import java.util.LinkedList;

public class FileDetector
{
    private LinkedList<File> fileList = new LinkedList();
    private String fileType = null;

    public FileDetector() {}

    public FileDetector(String fileType)
    {
        this.fileType = fileType;
    }

    private File[] listFiles(String absoluteFilePath)
    {
        File dir = new File(absoluteFilePath);
        return dir.listFiles();
    }

    public File[] detect(String absoluteFilePath)
    {
        File[] files = listFiles(absoluteFilePath);
        if (files != null) {
            classifyFileAndDirectory(files);
        }
        return (File[])this.fileList.toArray(new File[this.fileList.size()]);
    }

    private void classifyFileAndDirectory(File[] files)
    {
        File[] arrayOfFile;
        int j = (arrayOfFile = files).length;
        for (int i = 0; i < j; i++)
        {
            File file = arrayOfFile[i];
            if (file.isDirectory()) {
                detect(file.getAbsolutePath());
            } else {
                addFile(file);
            }
        }
    }

    private void addFile(File file)
    {
        if (this.fileType == null) {
            this.fileList.add(file);
        } else {
            addFileBySuffix(file);
        }
    }

    private void addFileBySuffix(File file)
    {
        if (file.getName().endsWith(this.fileType)) {
            this.fileList.addLast(file);
        }
    }
}
