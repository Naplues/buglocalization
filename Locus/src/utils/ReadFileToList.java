package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.List;

public class ReadFileToList {
	public static void readFiles(String file, List<String> lines){
		try{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			while(line!=null){
				lines.add(line);
				line = reader.readLine();
			}
			reader.close();
		}catch(Exception e ){
			System.out.println("when reading file " + file +  " come across exeception.");
		}
	}
	
	public static void readString(String str, List<String> lines){
		try{
			BufferedReader reader = new BufferedReader(new StringReader(str));
			String line = reader.readLine();
			while(line!=null){
				lines.add(line);
				line = reader.readLine();
			}
			reader.close();
		}catch(Exception e ){
			System.out.println("when reading String " + str +  " come across exeception.");
		}
	}
	
	public static String readFiles(String file){
		try{
			StringBuffer buffer = new StringBuffer();
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			while(line!=null){
				buffer.append(line+ "\n");
				line = reader.readLine();
			}
			reader.close();
			return buffer.toString();
		}catch(Exception e ){
			e.printStackTrace();
			System.out.println("when reading file " + file +  " come across exeception.");
			return null;
		}
	}
	
}
