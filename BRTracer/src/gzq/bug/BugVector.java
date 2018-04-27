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
	//本过程读取的数据文件列表
    public static String BugCorpusFolderName = "BugCorpus";
	//本过程保存的数据文件列表
    public static String BugTermListFileName = "BugTermList.txt";
    public static String BugVectorFileName = "BugVector.txt";

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
		list.addEntry(new WVTDocumentInfo(Utility.outputFileDir + BugCorpusFolderName + Utility.separator, "txt", "", "english", 0));
		WVTWordList wordList = wvt.createWordList(list, config);
		wordList.pruneByFrequency(1, Integer.MAX_VALUE);

		int termCount = wordList.getNumWords();
		wordList.storePlain(new FileWriter(Utility.outputFileDir + BugTermListFileName));
		FileWriter outFile = new FileWriter(Utility.outputFileDir + BugVectorFileName);
		WordVectorWriter wvw = new WordVectorWriter(outFile, true);
		config.setConfigurationRule(WVTConfiguration.STEP_OUTPUT, new WVTConfigurationFact(wvw));
		config.setConfigurationRule(WVTConfiguration.STEP_VECTOR_CREATION, new WVTConfigurationFact(new TFIDF()));
		wvt.createVectors(list, config, wordList);
		wvw.close();
		outFile.close();
		Utility.bugTermCount = termCount;
		Utility.writeConfig("bugTermCount",termCount + Utility.lineSeparator);
		System.out.println("   Generating " + termCount + " Bug Terms");
	}
}
