package evaluation;

import java.io.*;
import java.util.*;

import property.Property;
import sourcecode.CodeCorpusCreator_SpiltCorpus;
import sourcecode.LenScore_OriginClass;

public class Evaluation {
	private String workDir = Property.getInstance().getWorkDir()+Property.getInstance().getSeparator();
	private int fileCount = Property.getInstance().getFileCount();
	private int bugCount = Property.getInstance().getBugReportCount();
	private int originfilecount = Property.getInstance().getOriginFileCount();

	Integer a = CodeCorpusCreator_SpiltCorpus.spiltclass;
	Integer b = LenScore_OriginClass.B;
	private String outputFile = Property.getInstance().getOutputFile();
    private float alpha = 0.3f;
	private String lineSparator = Property.getInstance().getLineSeparator();

	Hashtable<String, Integer> idTable = null;
	Hashtable<Integer, String> nameTable = null;
	Hashtable<Integer, TreeSet<String>> fixTable = null;
	Hashtable<String, Double> lenTable = null;
	Hashtable<Integer, String> methodnameTable = null;
	Hashtable<String, Integer> methodidTable = null;
	Hashtable<String, Integer> LOCTable = null;
	HashMap<Integer, String> bugnametable = null;
	HashMap<String,HashSet<String>> shortnameset = null;
	LinkedList<HashMap<String,Integer>> groups = null;
	Iterator<HashMap<String, Integer>> itr = null;
	HashMap<String,Integer> tmp_group=null;
	Integer TotalLOC;

	public Evaluation() {
		try {
			idTable = getFileId();
			nameTable = getClassName();
			fixTable = getFixLinkTable();
			lenTable = this.getLenScore();
			methodnameTable = getMethodName();
			methodidTable = getMethodId();
			LOCTable = getLOC();
			shortnameset = getShortNameSet();
			bugnametable = getBugNameSet();
			groups = new LinkedList<HashMap<String, Integer>>();


		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		new Evaluation().evaluate();
	}

	public void evaluate() throws IOException {

		BufferedReader VSMReader = new BufferedReader(new FileReader(workDir
				+ "VSMScore.txt"));
		BufferedReader GraphReader = new BufferedReader(new FileReader(workDir
				+ "SimiScore.txt"));


		int count = 0;
		FileWriter writer = new FileWriter(outputFile);

		Integer top1 = 0;
		Integer top5 = 0;
		Integer top10 = 0;

		while (count < bugCount) {

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



			float[] finalscore = new float[originfilecount];
			int[] usedcount = new int[originfilecount];
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
			for( int i = 0; i<originfilecount; i++ ){
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


			for (int i = 0; i < originfilecount; i++) {
				finalscore[i] = finalscore[i] + groupScore[i];
			}


			Rank[] sort = this.sort(finalscore);

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
					writer.write(vsmId + "\t" + fileIdTable.get(rank.id) + "\t"
							+ i + "\t" + rank.rank + " " + lineSparator);

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
                if ((!fileIdTable.isEmpty())
                        && fileIdTable.containsKey(rank.id)) {
                }
            }

		}
		Float top1f = (float)top1/Property.getInstance().getBugReportCount();
		Float top5f = (float)top5/Property.getInstance().getBugReportCount();
		Float top10f = (float)top10/Property.getInstance().getBugReportCount();
		System.out.println("Top 1: " + top1f.toString());
		System.out.println("Top 5: " + top5f.toString());
		System.out.println("Top 10: " + top10f.toString());
        writer.close();
	}

	public Hashtable<String, Integer> getFileId() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(workDir
				+ "ClassName.txt"));
		String line = null;
		Hashtable<String, Integer> table = new Hashtable<String, Integer>();
		while ((line = reader.readLine()) != null) {
			String[] values = line.split("\t");
			Integer idInteger = Integer.parseInt(values[0]);
			String nameString = values[1].trim();
			table.put(nameString, idInteger);
		}
		return table;
	}

	public Hashtable<String, Integer> getMethodId() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(workDir
				+ "MethodName.txt"));
		String line = null;
		Hashtable<String, Integer> table = new Hashtable<String, Integer>();
		while ((line = reader.readLine()) != null) {
			String[] values = line.split("\t");
			Integer idInteger = Integer.parseInt(values[0]);
			String nameString = values[1].trim();
			table.put(nameString, idInteger);
		}
		return table;
	}

	public Hashtable<String, Integer> getLOC() throws IOException {
		TotalLOC = new Integer(0);
		BufferedReader reader = new BufferedReader(new FileReader(workDir
				+ "LOC.txt"));
		String line = null;
		Hashtable<String, Integer> table = new Hashtable<String, Integer>();
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

	public Hashtable<Integer, String> getMethodName() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(workDir
				+ "MethodName.txt"));
		String line = null;
		Hashtable<Integer, String> table = new Hashtable<Integer, String>();
		while ((line = reader.readLine()) != null) {
			String[] values = line.split("\t");
			Integer idInteger = Integer.parseInt(values[0]);
			String nameString = values[1].trim();
			table.put(idInteger, nameString);
		}
		return table;
	}

	public Hashtable<Integer, String> getClassName() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(workDir
				+ "ClassName.txt"));
		String line = null;
		Hashtable<Integer, String> table = new Hashtable<Integer, String>();
		while ((line = reader.readLine()) != null) {
			String[] values = line.split("\t");
			Integer idInteger = Integer.parseInt(values[0]);
			String nameString = values[1].trim();
			table.put(idInteger, nameString);
		}
		return table;
	}

	public Hashtable<Integer, TreeSet<String>> getFixLinkTable()
			throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(workDir
				+ "FixLink.txt"));
		String line = null;
		Hashtable<Integer, TreeSet<String>> table = new Hashtable<Integer, TreeSet<String>>();
		while ((line = reader.readLine()) != null) {
			String[] valueStrings = line.split("\t");
			Integer id = Integer.parseInt(valueStrings[0]);
			String fileName = valueStrings[1].trim();
			if (!table.containsKey(id)) {
				table.put(id, new TreeSet<String>());
			}
			table.get(id).add(fileName);
		}
		return table;
	}

	private Rank[] sort(float[] finalR) {
		Rank[] R = new Rank[finalR.length];
		for (int i = 0; i < R.length; i++) {
			Rank rank = new Rank();
			rank.rank = finalR[i];
			rank.id = i;
			R[i] = rank;
		}
		R = this.insertionSort(R);
		return R;
	}

	private Rank[] insertionSort(Rank[] R) {
		for (int i = 0; i < R.length; i++) {
			int maxIndex = i;
			for (int j = i; j < R.length; j++) {
				if (R[j].rank > R[maxIndex].rank) {
					maxIndex = j;
				}
			}
			Rank tmpRank = R[i];
			R[i] = R[maxIndex];
			R[maxIndex] = tmpRank;
		}
		return R;
	}


	public float[] combine(float[] vsmVector, float[] graphVector, float f) {
		float[] results = new float[fileCount];
		for (int i = 0; i < fileCount; i++) {
			results[i] = vsmVector[i] * (1 - f) + graphVector[i] * f;
		}
		return results;
	}

	private float[] normalize(float[] array) {
		float max = Float.MIN_VALUE;
		float min = Float.MAX_VALUE;
		for (int i = 0; i < array.length; i++) {
			if (max < array[i])
				max = array[i];
			if (min > array[i])
				min = array[i];
		}
		float span = max - min;
		for (int i = 0; i < array.length; i++) {
			array[i] = (array[i] - min) / span;
		}
		return array;
	}

	private float[] getVector(String vectorStr) {
		float[] vector = new float[fileCount];
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

	private Hashtable<String, Double> getLenScore() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(workDir
				+ "LengthScore.txt"));
		String line = null;
		Hashtable<String, Double> lenTable = new Hashtable<String, Double>();
		while ((line = reader.readLine()) != null) {
			String[] values = line.split("\t");
			String name = values[0];//.substring(0, values[0].lastIndexOf("."));
			Double score = Double.parseDouble(values[1]);
			lenTable.put(name, score);
		}
		reader.close();
		return lenTable;
	}


	public HashMap<String,HashSet<String>> getShortNameSet() throws IOException {
		BufferedReader namereader = new BufferedReader(new FileReader(workDir+"ClassName.txt"));
		HashMap<String, HashSet<String>> nameset = new HashMap<String, HashSet<String>>();
		String line;
		while ((line = namereader.readLine()) != null) {
			String[] fields = line.split("\t");
			String tmp = fields[1];

            String name;
            if(Property.getInstance().getProject().compareTo("aspectj")==0){
                //  For aspectj
                name = tmp.substring(tmp.lastIndexOf("/") + 1);
            }
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

	public HashMap<Integer,String> getBugNameSet() throws IOException {
		BufferedReader namereader = new BufferedReader(new FileReader(workDir+"DescriptionClassName.txt"));
		HashMap<Integer, String> bugnameset = new HashMap<Integer, String>();
		String line;
		while ((line = namereader.readLine()) != null) {
			String[] fields = line.split("\t");
			if (fields.length < 2) {
				continue;
			} else {
				bugnameset.put(Integer.parseInt(fields[0]), fields[1]);
			}
		}
		return bugnameset;

	}


	public float[] getRelativeScore(Integer bugid) throws IOException{
		float[] relativeScore = new float[originfilecount];
		//  clear
		for (int i = 0; i < relativeScore.length; i++) {
			relativeScore[i] = 0;
		}
		//  get all the names appear in BR
		String s = bugnametable.get(bugid);
		if(s==null){
			return relativeScore;
		}
		String[] f = s.split(" ");
		Set<String> nameset = new HashSet<String>();
		for (int i = 0; i < f.length; i++) {
			if (shortnameset.containsKey(f[i])) {
				nameset.addAll(shortnameset.get(f[i]));
			}
		}
		//  add the imported class
		HashMap<String,String> importTable = getImportTable();
		Set<String> fullnameset = idTable.keySet();
		Iterator<String> itr = nameset.iterator();
		Set<String> appendage = new HashSet<String>();
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
                    //@TODO: remember to switch
                    //  for aspectj
                    if(Property.getInstance().getProject().compareTo("aspectj")==0){
                        String[] namefields = tmpn.split("/");
                        int j=0;
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

	private HashMap<String,String> getImportTable() throws IOException{
		BufferedReader importReader = new BufferedReader(new FileReader(workDir + "Import.txt"));
		HashMap<String, String> importTable = new HashMap<String, String>();
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



}
