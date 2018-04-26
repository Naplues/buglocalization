package gzq.source;

import sourcecode.ast.FileParser;
import utils.Utility;

import java.io.*;

public class LineOfCode {
	private static String fileName;
	private static Integer loc;

    public static Integer count(File file) throws IOException{
        FileParser parser = new FileParser(file);
        fileName = parser.getPackageName();
        if (fileName.trim().equals(""))
            fileName = file.getName();
        else
            fileName += "." + file.getName();

        /* modification for AspectJ */
        if(Utility.project.compareTo("AspectJ")==0)
            fileName = file.getPath().substring(Utility.aspectj_filename_offset);

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

    public static void beginCount() throws Exception {
        FileWriter writer = new FileWriter(Utility.outputFileDir + "LOC.txt");
        File[] files = Utility.detectSourceFiles(Utility.sourceFileDir, Utility.srcFileType);
        for (File file : files) {
        	loc = count(file);
            if (fileName.endsWith(".java"))
            	writer.write( fileName + "\t" + loc + Utility.lineSeparator);
            else
            	writer.write( fileName + ".java" + "\t" + loc + Utility.lineSeparator);
            writer.flush();
        }
        writer.close();
    }
}
