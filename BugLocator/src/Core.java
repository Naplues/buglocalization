import bug.BugCorpusCreator;
import bug.BugSimilarity;
import bug.BugVector;
import bug.SimilarityDistribution;
import evaluation.Evaluation;
import java.io.PrintStream;
import sourcecode.CodeCorpusCreator;
import sourcecode.CodeVectorCreator;
import sourcecode.Indexer;
import sourcecode.LenScore;
import sourcecode.Similarity;

public class Core
{
    public void process()
    {
        try
        {
            System.out.println("create bug corpus...");
            new BugCorpusCreator().create();
        }
        catch (Exception localException1) {}
        try
        {
            System.out.println("create bug vector...");
            new BugVector().create();
        }
        catch (Exception localException2) {}
        try
        {
            System.out.println("compute bug similarity...");
            new BugSimilarity().computeSimilarity();
        }
        catch (Exception localException3) {}
        try
        {
            System.out.println("create code corpus...");
            new CodeCorpusCreator().create();
        }
        catch (Exception localException4) {}
        try
        {
            System.out.println("compute SimiScore...");
            new SimilarityDistribution().distribute();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        try
        {
            System.out.println("create index...");
            new Indexer().index();
        }
        catch (Exception localException5) {}
        try
        {
            System.out.println("create vector...");
            new CodeVectorCreator().create();
        }
        catch (Exception localException6) {}
        try
        {
            System.out.println("compute VSMScore...");
            new Similarity().compute();
        }
        catch (Exception localException7) {}
        try
        {
            System.out.println("compute LengthScore...");
            new LenScore().computeLenScore();
        }
        catch (Exception localException8) {}
        try
        {
            System.out.println("evaluate...");
            new Evaluation().evaluate();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        System.out.println("finished");
    }
}
