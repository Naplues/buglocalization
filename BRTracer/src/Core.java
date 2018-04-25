
import evaluation.Evaluation;
import sourcecode.*;
import bug.BugCorpusCreator;
import bug.BugSimilarity;
import bug.BugVector;
import bug.SimilarityDistribution;

public class Core {

	public void process() {


		try{
			System.out.println("get all class name...");
			new ClassName().create();
		} catch( Exception ex ){
			ex.printStackTrace();
		}

        // ------------bug-------------------------
		try {
			System.out.println("create bug corpus...");
			new BugCorpusCreator().create();

		} catch (Exception ex) {

		}


		try {
			System.out.println("create bug vector...");
			new BugVector().create();
		} catch (Exception ex) {

		}
		try {
			System.out.println("compute bug similarity...");
			new BugSimilarity().computeSimilarity();
		} catch (Exception ex) {

		}



		try {
			System.out.println("create code corpus...");
			new CodeCorpusCreator_SpiltCorpus().create();
		} catch (Exception ex) {
			ex.printStackTrace();
		}



        try {
            System.out.println("create code corpus origin...");
            new CodeCorpusCreator_OriginClass().create();
        } catch (Exception ex) {

        }


		try {
			System.out.println("compute SimiScore...");
			new SimilarityDistribution().distribute();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// --------------------sourcecode---------------

		try {
			System.out.println("create index...");
			new Indexer().index();
		} catch (Exception ex) {

		}

        try {
            System.out.println("create index origin...");
            new Indexer_OriginClass().index();
        } catch (Exception ex) {

        }


		try {
			System.out.println("create vector...");
			new CodeVectorCreator().create();
		} catch (Exception ex) {

		}
		try {
			System.out.println("compute VSMScore...");
			new Similarity().compute();
		} catch (Exception ex) {

		}
		try {
			System.out.println("compute LengthScore...");
			new LenScore_OriginClass().computeLenScore();
		} catch (Exception ex) {

		}
		//----------------evaluation--------

		try {
			System.out.println("count LoC...");
			new LineofCode().beginCount();
		}
		catch (Exception ex){
			ex.printStackTrace();
		}

		try {
			System.out.println("evaluate...");
			new Evaluation().evaluate();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		System.out.println("finished");
	}
}
