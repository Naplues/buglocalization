package bug;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import property.Property;

public class SimilarityDistribution {
	private int fileCount = Property.getInstance().getFileCount();
	private String workDir = Property.getInstance().getWorkDir()
			+ Property.getInstance().getSeparator();

	public static void main(String[] args) {
		SimilarityDistribution graph = new SimilarityDistribution();
		try {
			graph.distribute();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void distribute() throws Exception, IOException {
		BufferedReader reader = new BufferedReader(new FileReader(workDir
				+ "BugSimilarity.txt"));
		String line = null;
		Hashtable<Integer, TreeSet<String>> fixedTable = this.getFixedTable();
		Hashtable<String, Integer> idTable = getFileIdTable();

		FileWriter writer = new FileWriter(workDir
				+ "SimiScore.txt");

		while ((line = reader.readLine()) != null) {
			float[] similarValues = new float[fileCount];
			String idStr = line.substring(0, line.indexOf(";"));
			String vectorStr = line.substring(line.indexOf(";") + 1).trim();
			Integer id = Integer.parseInt(idStr);
			String[] values = vectorStr.split(" ");

			for (String value : values) {
				String[] singleValues = value.split(":");
				if (singleValues.length == 2) {
					Integer simBugId = Integer.parseInt(singleValues[0]);
					float sim = Float.parseFloat(singleValues[1]);
					TreeSet<String> fileSet = fixedTable.get(simBugId);
					if (fileSet == null) {
						System.out.println(simBugId);
					}
					Iterator<String> fileSetIt = fileSet.iterator();
					int size = fileSet.size();
					float singleValue = sim / size;
					while (fileSetIt.hasNext()) {
						String name = fileSetIt.next();
						//change: add the singleValue to each segment belonged to that class
						Integer counter = 0;
						while(true){
							Integer fileId = idTable.get(name+"@"+counter+".java");
							if (fileId == null) {
								if( counter==0 )
									System.err.println(name);
								break;
							}
							similarValues[fileId] += singleValue;
							counter++;
						}

					}
				}
			}
			String output = id + ";";
			for (int i = 0; i < fileCount; i++) {
				if (similarValues[i] != 0) {
					output += i + ":" + similarValues[i] + " ";
				}
			}
			writer.write(output.trim() + Property.getInstance().getLineSeparator());
			writer.flush();
		}
		writer.close();
	}

	public Hashtable<Integer, TreeSet<String>> getFixedTable()
			throws IOException {
		Hashtable<Integer, TreeSet<String>> idTable = new Hashtable<Integer, TreeSet<String>>();

		BufferedReader reader = new BufferedReader(new FileReader(workDir
				+ "FixLink.txt"));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] values = line.split("\t");
			Integer id = Integer.parseInt(values[0]);
			String name = values[1].trim();
			if (!idTable.containsKey(id)) {
				idTable.put(id, new TreeSet<String>());
			}
			idTable.get(id).add(name);
		}
		return idTable;
	}

	public Hashtable<String, Integer> getFileIdTable() throws IOException {
		Hashtable<String, Integer> idTable = new Hashtable<String, Integer>();

		BufferedReader reader = new BufferedReader(new FileReader(workDir
				+ "MethodName.txt"));
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] values = line.split("\t");
			Integer id = Integer.parseInt(values[0]);
			String name = values[1].trim();
			idTable.put(name, id);
		}
		return idTable;
	}

}
