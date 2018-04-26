package gzq.evaluation;

import utils.Utility;
import gzq.source.CodeCorpus_SpiltCorpus;
import gzq.source.LenScore_OriginClass;
import java.io.*;
import java.util.*;

public class Evaluation {

	public static Integer a = CodeCorpus_SpiltCorpus.spiltclass;
	public static Integer b = LenScore_OriginClass.B;
    public static float alpha = 0.3f;

	public static Hashtable<String, Integer> idTable = null;
	public static Hashtable<Integer, String> nameTable = null;
	public static Hashtable<Integer, TreeSet<String>> fixTable = null;
	public static Hashtable<String, Double> lenTable = null;
	public static Hashtable<Integer, String> methodnameTable = null;
	public static Hashtable<String, Integer> methodidTable = null;
	public static Hashtable<String, Integer> LOCTable = null;
	public static HashMap<Integer, String> bugnametable = null;
	public static HashMap<String,HashSet<String>> shortnameset = null;
	public static LinkedList<HashMap<String,Integer>> groups = null;
	public static Iterator<HashMap<String, Integer>> itr = null;
	public static HashMap<String,Integer> tmp_group=null;
	public static Integer TotalLOC;

	public static void init() throws IOException {
		idTable = getFileId();
		nameTable = getClassName();
		fixTable = getFixLinkTable();
		lenTable = getLenScore();
		methodnameTable = getMethodName();
		methodidTable = getMethodId();
		LOCTable = getLOC();
		shortnameset = getShortNameSet();
		bugnametable = getBugNameSet();
		groups = new LinkedList<>();
	}

	public static Hashtable<String, Integer> getFileId() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + "ClassName.txt"));
		String line;
		Hashtable<String, Integer> table = new Hashtable<>();
		while ((line = reader.readLine()) != null) {
			String[] values = line.split("\t");
			Integer idInteger = Integer.parseInt(values[0]);
			String nameString = values[1].trim();
			table.put(nameString, idInteger);
		}
		return table;
	}

	public static Hashtable<String, Integer> getMethodId() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + "MethodName.txt"));
		String line = null;
		Hashtable<String, Integer> table = new Hashtable<>();
		while ((line = reader.readLine()) != null) {
			String[] values = line.split("\t");
			Integer idInteger = Integer.parseInt(values[0]);
			String nameString = values[1].trim();
			table.put(nameString, idInteger);
		}
		return table;
	}

	public static Hashtable<String, Integer> getLOC() throws IOException {
		TotalLOC = new Integer(0);
		BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + "LOC.txt"));
		String line;
		Hashtable<String, Integer> table = new Hashtable<>();
		while ((line = reader.readLine()) != null) {
			String[] values = line.split("\t");
			Integer loc = Integer.parseInt(values[1]);
			TotalLOC+=loc;
			String nameString = values[0].trim();
			table.put(nameString, loc);
		}
		System.out.println("Total LOC: "+TotalLOC);
		return table;
	}

	public static Hashtable<Integer, String> getMethodName() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + "MethodName.txt"));
		String line = null;
		Hashtable<Integer, String> table = new Hashtable<>();
		while ((line = reader.readLine()) != null) {
			String[] values = line.split("\t");
			Integer idInteger = Integer.parseInt(values[0]);
			String nameString = values[1].trim();
			table.put(idInteger, nameString);
		}
		return table;
	}

	public static Hashtable<Integer, String> getClassName() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + "ClassName.txt"));
		String line;
		Hashtable<Integer, String> table = new Hashtable<>();
		while ((line = reader.readLine()) != null) {
			String[] values = line.split("\t");
			Integer idInteger = Integer.parseInt(values[0]);
			String nameString = values[1].trim();
			table.put(idInteger, nameString);
		}
		return table;
	}

	public static Hashtable<Integer, TreeSet<String>> getFixLinkTable() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + "FixLink.txt"));
		String line;
		Hashtable<Integer, TreeSet<String>> table = new Hashtable<>();
		while ((line = reader.readLine()) != null) {
			String[] valueStrings = line.split("\t");
			Integer id = Integer.parseInt(valueStrings[0]);
			String fileName = valueStrings[1].trim();
			if (!table.containsKey(id)) {
				table.put(id, new TreeSet<>());
			}
			table.get(id).add(fileName);
		}
		return table;
	}

	private static Rank[] sort(float[] finalR) {
		Rank[] R = new Rank[finalR.length];
		for (int i = 0; i < R.length; i++) {
			Rank rank = new Rank();
			rank.rank = finalR[i];
			rank.id = i;
			R[i] = rank;
		}
		R = insertionSort(R);
		return R;
	}

	private static Rank[] insertionSort(Rank[] R) {
		for (int i = 0; i < R.length; i++) {
			int maxIndex = i;
			for (int j = i; j < R.length; j++)
				if (R[j].rank > R[maxIndex].rank)
					maxIndex = j;
			Rank tmpRank = R[i];
			R[i] = R[maxIndex];
			R[maxIndex] = tmpRank;
		}
		return R;
	}

	public static float[] combine(float[] vsmVector, float[] graphVector, float f) {
		float[] results = new float[Utility.sourceFileCount];
		for (int i = 0; i < Utility.sourceFileCount; i++)
			results[i] = vsmVector[i] * (1 - f) + graphVector[i] * f;
		return results;
	}

	private static float[] normalize(float[] array) {
		float max = Float.MIN_VALUE;
		float min = Float.MAX_VALUE;
		for (int i = 0; i < array.length; i++) {
			if (max < array[i])
				max = array[i];
			if (min > array[i])
				min = array[i];
		}
		float span = max - min;
		for (int i = 0; i < array.length; i++)
			array[i] = (array[i] - min) / span;
		return array;
	}

	private static float[] getVector(String vectorStr) {
		float[] vector = new float[Utility.sourceFileCount];
		String[] values = vectorStr.split(" ");
		for (String value : values) {
			String[] singleValues = value.split(":");
			if (singleValues.length == 2) {
				int index = Integer.parseInt(singleValues[0]);
				float sim = Float.parseFloat(singleValues[1]);
				vector[index] = sim;
			}
		}
		return vector;
	}

	private static Hashtable<String, Double> getLenScore() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(Utility.outputFileDir + "LengthScore.txt"));
		Hashtable<String, Double> lenTable = new Hashtable<>();
		String line;
		while ((line = reader.readLine()) != null) {
			String[] values = line.split("\t");
			String name = values[0];//.substring(0, values[0].lastIndexOf("."));
			Double score = Double.parseDouble(values[1]);
			lenTable.put(name, score);
		}
		reader.close();
		return lenTable;
	}

	public static HashMap<String,HashSet<String>> getShortNameSet() throws IOException {
		BufferedReader namereader = new BufferedReader(new FileReader(Utility.outputFileDir + "ClassName.txt"));
		HashMap<String, HashSet<String>> nameset = new HashMap<>();
		String line;
		while ((line = namereader.readLine()) != null) {
			String[] fields = line.split("\t");
			String tmp = fields[1];

            String name;
            if(Utility.project.compareTo("AspectJ")==0)
                name = tmp.substring(tmp.lastIndexOf("/") + 1);
            else{
                //  For swt and eclipse
                tmp = tmp.substring(0, tmp.lastIndexOf("."));
                name = tmp.substring(tmp.lastIndexOf(".") + 1) + ".java";
            }

			if (nameset.containsKey(name)) {
				HashSet<String> t = nameset.get(name);
				t.add(fields[1]);
			} else {
				HashSet<String> t = new HashSet<String>();
				t.add(fields[1]);
				nameset.put(name, t);
			}
		}
		return nameset;
	}

	public static HashMap<Integer,String> getBugNameSet() throws IOException {
		BufferedReader namereader = new BufferedReader(new FileReader(Utility.outputFileDir + "DescriptionClassName.txt"));
		HashMap<Integer, String> bugnameset = new HashMap<>();
		String line;
		while ((line = namereader.readLine()) != null) {
			String[] fields = line.split("\t");
			if (fields.length < 2)
				continue;
			else
				bugnameset.put(Integer.parseInt(fields[0]), fields[1]);
		}
		return bugnameset;
	}

	/**
	 * 获取相关分数
	 * @param bugid
	 * @return
	 * @throws IOException
	 */
	public static float[] getRelativeScore(Integer bugid) throws IOException{
		float[] relativeScore = new float[Utility.originFileCount];
		for (int i = 0; i < relativeScore.length; i++)
			relativeScore[i] = 0;
		//  get all the names appear in BR
		String s = bugnametable.get(bugid);
		if(s==null)
			return relativeScore;

		String[] f = s.split(" ");
		Set<String> nameset = new HashSet<>();
		for (int i = 0; i < f.length; i++)
			if (shortnameset.containsKey(f[i]))
				nameset.addAll(shortnameset.get(f[i]));

		//  add the imported class
		HashMap<String,String> importTable = getImportTable();
		Set<String> fullnameset = idTable.keySet();
		Iterator<String> itr = nameset.iterator();
		Set<String> appendage = new HashSet<>();
		while (itr.hasNext()) {
			String n = itr.next();
			String ims = importTable.get(n);
			if (ims == null) {
				continue;
			}
			String[] imclasses = ims.split(" ");
			for (int i = 0; i < imclasses.length; i++) {
				Iterator<String> all_itr = fullnameset.iterator();
				while (all_itr.hasNext()) {
					String tmpn = all_itr.next();
                    String backupname = tmpn;
                    if(Utility.project.compareTo("AspectJ")==0){
                        String[] namefields = tmpn.split("/");
                        int j;
                        for (j = 0; j < namefields.length; j++) {
                            if (namefields[j].compareTo("org") == 0) {
                                break;
                            }
                        }
                        tmpn="org";
                        for (j = j + 1; j < namefields.length; j++) {
                            tmpn = tmpn + "." + namefields[j];
                        }
                    }
                    // end for aspectj
                    if (tmpn.contains(imclasses[i])) {
						Integer l1 = tmpn.split("\\.").length;
						Integer l2 = imclasses[i].split("\\.").length;
						if (l1 - l2 <= 2) {
							appendage.add(backupname);
						}
					}
                }
			}
		}
		//Calculate scores
        itr = appendage.iterator();
        while (itr.hasNext()) {
            Integer id = idTable.get(itr.next());
            relativeScore[id] = 0.2f;
        }

		itr = nameset.iterator();
		while (itr.hasNext()) {
			Integer id = idTable.get(itr.next());
			relativeScore[id] = 0.5f;
		}
        return relativeScore;
	}

	private static HashMap<String,String> getImportTable() throws IOException{
		BufferedReader importReader = new BufferedReader(new FileReader(Utility.outputFileDir + "Import.txt"));
		HashMap<String, String> importTable = new HashMap<>();
		String line;
		while ((line = importReader.readLine()) != null) {
			String[] fields = line.split("\t");
			if (fields.length == 1) {
				importTable.put(fields[0], null);
			} else {
				importTable.put(fields[0], fields[1]);
			}
		}
		return importTable;
	}

	public static void evaluate() throws IOException {
		init(); //初始化
		BufferedReader VSMReader = new BufferedReader(new FileReader(Utility.outputFileDir + "VSMScore.txt"));
		BufferedReader GraphReader = new BufferedReader(new FileReader(Utility.outputFileDir + "SimiScore.txt"));

		int count = 0;
		FileWriter writer = new FileWriter(Utility.outputFilePath);

		Integer top1 = 0;
		Integer top5 = 0;
		Integer top10 = 0;

		while (count < Utility.bugReportCount) {
			count++;
			String vsmLine = VSMReader.readLine();
			String vsmIdStr = vsmLine.substring(0, vsmLine.indexOf(";"));
			Integer vsmId = Integer.parseInt(vsmIdStr);
			String vsmVectorStr = vsmLine.substring(vsmLine.indexOf(";") + 1);
			float[] vsmVector = getVector(vsmVectorStr);

			tmp_group = null;
			float[] groupScore = getRelativeScore(vsmId);

			for (String key : lenTable.keySet()) {
				Double score = lenTable.get(key);
				Integer i=0;
				while(true){
					String name = key + "@" + i.toString() + ".java";
					Integer id = methodidTable.get(name);
					if( id == null ){
						break;
					}
					vsmVector[id] = vsmVector[id] * score.floatValue();
					i++;
				}
			}
			vsmVector = normalize(vsmVector);
			String graphLine = GraphReader.readLine();
			String graphIdStr = graphLine.substring(0, graphLine.indexOf(";"));
			Integer graphId = Integer.parseInt(graphIdStr);
			String graphVectorStr = graphLine
					.substring(graphLine.indexOf(";") + 1);
			float[] graphVector = getVector(graphVectorStr);
			graphVector = normalize(graphVector);

			float[] finalR = combine(vsmVector, graphVector, alpha);

			float[] finalscore = new float[Utility.originFileCount];
			int[] usedcount = new int[Utility.originFileCount];
			HashMap<Integer,ArrayList<Float>> scores = new HashMap<Integer,ArrayList<Float>>();
			for( int counter = 0; counter < finalR.length; counter++ ){
				String name = methodnameTable.get(counter);
				name = name.substring(0, name.indexOf('@'));
				Integer id = idTable.get(name);
				if( id==null ){
					System.err.println(name);
					Console console = System.console();
					String delay_input = console.readLine();
					continue;
				}
				/* automatically determine num of file to represent the origin file */
				if(scores.containsKey(id)){
					ArrayList<Float> t = scores.get(id);
					t.add(finalR[counter]);
				}
				else{
					ArrayList<Float> t = new ArrayList<Float>();
					t.add(finalR[counter]);
					scores.put(id, t);
				}
			}
			for( int i = 0; i<Utility.originFileCount; i++ ){
				ArrayList<Float> t = scores.get(i);
				try{
					Collections.sort(t, Collections.reverseOrder());
				}
				catch(Exception e){
					System.out.println(i);
					continue;
				}
				finalscore[i] = t.get(0);
			}

			for (int i = 0; i < Utility.originFileCount; i++) {
				finalscore[i] = finalscore[i] + groupScore[i];
			}

			Rank[] sort = sort(finalscore);

			TreeSet<String> fileSet = fixTable.get(vsmId);
			Iterator<String> fileIt = fileSet.iterator();
			Hashtable<Integer, String> fileIdTable = new Hashtable<Integer, String>();
			while (fileIt.hasNext()) {
				String fileName = fileIt.next();
				Integer fileId = idTable.get(fileName);
				if(fileId==null || fileName==null){
					System.out.println("null pointer");
					System.out.println(fileName);
					continue;
				}
				fileIdTable.put(fileId, fileName);
			}
			Integer tmploc = 0;
			for (int i = 0; i < sort.length; i++) {
				Rank rank = sort[i];
				if ((!fileIdTable.isEmpty())
						&& fileIdTable.containsKey(rank.id)) {
					String filename = nameTable.get(rank.id);
					tmploc += LOCTable.get(filename);
					Float percent = (float)tmploc/(float)TotalLOC;
					writer.write(vsmId + "\t" + fileIdTable.get(rank.id) + "\t" + i + "\t" + rank.rank + " " + Utility.lineSeparator);
					// Calculate TNRF
					if(i==0){
						top1++;
					}
					if(i<5){
						top5++;
					}
					if(i<10){
						top10++;
					}
					writer.flush();
					break;
				}
				else{
					String filename = nameTable.get(rank.id);
					tmploc += LOCTable.get(filename);
				}
			}
			for (int i = 0; i < sort.length; i++) {
				Rank rank = sort[i];
				if ((!fileIdTable.isEmpty()) && fileIdTable.containsKey(rank.id)) { }
			}
		}
		System.out.println("Top 1: " + (float)top1/Utility.bugReportCount);
		System.out.println("Top 5: " + (float)top5/Utility.bugReportCount);
		System.out.println("Top 10: " + (float)top10/Utility.bugReportCount);
		writer.close();
	}
}
