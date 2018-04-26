package gzq.evaluation;

import utils.Utility;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

public class MAP {

	public static String[][] result = null;

	public static File[] getResultFile() {
		return new File(Utility.outputFileDir + Utility.metricDir).listFiles();
	}

	public static Integer[] sort(ArrayList<Integer> list) {
		Integer[] ranks = list.toArray(new Integer[list.size()]);
		for (int i = 0; i < ranks.length; i++) {
			int minIndex = i;
			for (int j = i + 1; j < ranks.length; j++)
				if (ranks[j] < ranks[minIndex])
					minIndex = j;
			int tmp = ranks[i];
			ranks[i] = ranks[minIndex];
			ranks[minIndex] = tmp;
		}
		return ranks;
	}


	public static void statistics() throws Exception {
		File[] files = getResultFile();
		result = new String[files.length + 1][2];
		result[0][0] = "factor";
		result[0][1] = "MAP";
		int i = 1;
		for (File file : files) {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			float ap = 0.0f;
			Hashtable<Integer, ArrayList<Integer>> rankTable = new Hashtable<>();
			String line;
			while ((line = reader.readLine()) != null) {
				String[] values = line.split("\t");
				Integer id = Integer.parseInt(values[0]);
				int rank = Integer.parseInt(values[2]);
				if (!rankTable.containsKey(id)) {
					rankTable.put(id, new ArrayList<>());
				}
				rankTable.get(id).add(rank);
			}
			Iterator<Integer> rankTableIt = rankTable.keySet().iterator();
			TreeSet<Integer> filterSet = new TreeSet<>();
			while (rankTableIt.hasNext()) {
				Integer id = rankTableIt.next();
				ArrayList<Integer> rankList = rankTable.get(id);
				Integer[] ranks = sort(rankList);
				if (!filterSet.contains(id)) {

					filterSet.add(id);
				}
				float p = 0.0f;
				for (int j = 0; j < ranks.length; j++) {
					p += (j + 1.0f) / (ranks[j] + 1.0f);
				}
				float sap = p / ranks.length;
				ap += sap;
				System.out.println(id + "," + sap);
			}
			float map = ap / Utility.bugReportCount;
			String factor = file.getName().replace(".csv", "").replace("result_", "");
			result[i][0] = factor;
			result[i][1] = map + "";
			i++;
		}
		FileWriter writer = new FileWriter(Utility.outputFileDir + Utility.metricDir + "map.csv");
		for (int k = 0; k < result.length; k++) {
			String output = "";
			for (int j = 0; j < result[k].length; j++)
				output += result[k][j] + ",";
			writer.write(output + "\r\n");
			writer.flush();
		}
		writer.close();
	}
}
