package main;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import miningChanges.CorpusCreation;
import miningChanges.ProduceChangeLevelResults;
import miningChanges.ProduceFileLevelResults;
import preprocess.ExtractCommits;
import utils.FileToLines;

public class Main {
	
	public static HashMap<String,String> settings;
	public static String task = "";
	public static String repoDir = "";
	public static String workingLoc = "";
	public static String bugReport = "";
	public static String changeOracle = "";
	public static String sourceDir = "";
	public static double lambda = 5;
	public static double belta1 = 0.1;
	public static double belta2 = 0.2;
	
	public static void startTask() throws Exception {
		if (task.equals("indexHunks")) {
			ExtractCommits.indexHunks();
		} else if (task.equals("corpusCreation")) {
			// Create the corpus change logs, hunks, and create the code like term corpus
			CorpusCreation.createCorpus();
		} else if (task.equals("produceChangeResults")) {
			ProduceChangeLevelResults rank = new ProduceChangeLevelResults();
			rank.getFinalResults();
		} else if (task.equals("produceFileResults")) {
			ProduceFileLevelResults rank = new ProduceFileLevelResults();
			rank.getFinalResults();
		} else if (task.equals("all")) {
			ExtractCommits.indexHunks();
			System.out.println("Finish Indexing Files");
			CorpusCreation.createCorpus();
			System.out.println("Finish Creating Corpus");
			ProduceChangeLevelResults rank1 = new ProduceChangeLevelResults();
			rank1.getFinalResults();
			System.out.println("Finish Creating Change Level Results");
			ProduceFileLevelResults rank2 = new ProduceFileLevelResults();
			rank2.getFinalResults();
			System.out.println("Finish Creating File Level Results");
		}
	}
	
	public static void loadConfigure(String filename) throws Exception {
		File file = new File(filename);
		settings = new HashMap<String,String>();
        if (!file.exists()) {
			System.out.println("Configuration File Not Found!");
			return;
		} 
		else {
			List<String> lines = FileToLines.fileToLines(filename);
			for (String line : lines) {
				if (!line.startsWith("#") && line.contains("=")) {
					settings.put(line.split("=")[0].trim(), line.split("=")[1].trim());
				}
			}
		}

		if (settings.containsKey("task"))
			task = settings.get("task");
		if (settings.containsKey("repoDir"))
			repoDir = settings.get("repoDir");
		if (settings.containsKey("workingLoc"))
			workingLoc = settings.get("workingLoc");
		if (settings.containsKey("bugReport"))
			bugReport = settings.get("bugReport");
		if (settings.containsKey("changeOracle"))
			changeOracle = settings.get("changeOracle");
		if (settings.containsKey("lambda"))
			lambda = Double.parseDouble(settings.get("lambda"));
		if (settings.containsKey("belta1"))
			belta1 = Double.parseDouble(settings.get("belta1"));
		if (settings.containsKey("belta2"))
			belta2 = Double.parseDouble(settings.get("belta2"));
		if (settings.containsKey("sourceDir"))
			sourceDir = settings.get("sourceDir");
		if (task.equals("") || repoDir.equals("") || workingLoc.equals("") || bugReport.equals("") || changeOracle.equals("")) {
			System.out.println("Missing Required Configuration");
			return;
		} else {
			startTask();
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		if (args.length > 0) {
			loadConfigure(args[0]);
		} else {
			System.out.println("Using default configuration file");
			loadConfigure("./config_local.txt");
		}
		
	}
}
