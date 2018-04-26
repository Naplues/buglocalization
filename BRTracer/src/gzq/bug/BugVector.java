package gzq.bug;

import edu.udo.cs.wvtool.config.WVTConfigException;
import edu.udo.cs.wvtool.config.WVTConfiguration;
import edu.udo.cs.wvtool.config.WVTConfigurationFact;
import edu.udo.cs.wvtool.config.WVTConfigurationRule;
import edu.udo.cs.wvtool.generic.output.WordVectorWriter;
import edu.udo.cs.wvtool.generic.stemmer.LovinsStemmerWrapper;
import edu.udo.cs.wvtool.generic.stemmer.PorterStemmerWrapper;
import edu.udo.cs.wvtool.generic.stemmer.WVTStemmer;
import edu.udo.cs.wvtool.generic.vectorcreation.TFIDF;
import edu.udo.cs.wvtool.main.WVTDocumentInfo;
import edu.udo.cs.wvtool.main.WVTFileInputList;
import edu.udo.cs.wvtool.main.WVTool;
import edu.udo.cs.wvtool.util.WVToolException;
import edu.udo.cs.wvtool.wordlist.WVTWordList;
import utils.Utility;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Bug 向量
 * @author gzq
 *
 */
public class BugVector {

	private static String HOME_FOLDER = Utility.outputFileDir;
	private static String BUG_CORPUS_FOLDER = "BugCorpus" + Utility.separator;
	
	public static void create() throws WVToolException, IOException {

		WVTool wvt = new WVTool(false);
		WVTConfiguration config = new WVTConfiguration();
		final WVTStemmer porterStemmer = new PorterStemmerWrapper();
		config.setConfigurationRule(WVTConfiguration.STEP_STEMMER, new WVTConfigurationRule() {
			public Object getMatchingComponent(WVTDocumentInfo d) throws WVTConfigException {
				return porterStemmer;
			}
		});
		WVTStemmer stemmer = new LovinsStemmerWrapper();
		config.setConfigurationRule(WVTConfiguration.STEP_STEMMER, new WVTConfigurationFact(stemmer));
		WVTFileInputList list = new WVTFileInputList(1);

		list.addEntry(new WVTDocumentInfo(HOME_FOLDER + BUG_CORPUS_FOLDER, "txt", "", "english", 0));

		WVTWordList wordList = wvt.createWordList(list, config);

		wordList.pruneByFrequency(1, Integer.MAX_VALUE);

		int termCount = wordList.getNumWords();
		Utility.bugTermCount = termCount;
		wordList.storePlain(new FileWriter(HOME_FOLDER + "BugTermList.txt"));

		FileWriter outFile = new FileWriter(HOME_FOLDER + "BugVector.txt");

		WordVectorWriter wvw = new WordVectorWriter(outFile, true);

		config.setConfigurationRule(WVTConfiguration.STEP_OUTPUT, new WVTConfigurationFact(wvw));
		config.setConfigurationRule(WVTConfiguration.STEP_VECTOR_CREATION, new WVTConfigurationFact(new TFIDF()));

		wvt.createVectors(list, config, wordList);
		wvw.close();
		outFile.close();
	}
}
