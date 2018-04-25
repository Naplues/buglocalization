package sourcecode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import property.Property;
import sourcecode.ast.FileDetector;
import sourcecode.ast.FileParser;

public class LineofCode {
	private String fileName;
	private Integer loc;

    public void beginCount() throws Exception {
        FileDetector detector = new FileDetector("java");
        File[] files = detector.detect(Property.getInstance()
                .getSourceCodeDir());
        FileWriter writer = new FileWriter(Property.getInstance().getWorkDir()
				+ Property.getInstance().getSeparator() + "LOC.txt");

        for (File file : files) {
        	loc = count(file);
            if (fileName.endsWith(".java")) {
            	writer.write( fileName + "\t" + loc
    					+ Property.getInstance().getLineSeparator());
            } else {
            	writer.write( fileName + ".java" + "\t" + loc
    					+ Property.getInstance().getLineSeparator());
            }
            writer.flush();
        }
        writer.close();
    }
	
	public Integer count(File file) throws IOException{
		FileParser parser = new FileParser(file);

        fileName = parser.getPackageName();
        if (fileName.trim().equals("")) {
            fileName = file.getName();
        } else {
            fileName += "." + file.getName();
        }
        
        /* modification for AspectJ */
        if(Property.getInstance().getProject().compareTo("aspectj")==0){
            fileName = file.getPath();
            fileName = fileName.substring(Property.getInstance().getOffset());
        }
        /* ************************** */
        
        fileName = fileName.substring(0, fileName.lastIndexOf("."));
		BufferedReader reader = new BufferedReader(new FileReader(file));
		Integer LoC=0;
		String tmp;
		while(true){
			tmp = reader.readLine();
			if(tmp==null)
				break;
			LoC++;
		}
		reader.close();
		return LoC;
	}
}
