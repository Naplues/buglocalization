package gzq;

import gzq.bug.*;
import gzq.source.*;
import gzq.evaluation.*;


public class Main {
    public static void main(String[] args) throws Exception{

        System.out.println("1. Getting all class names...");
        ClassName.create();
        System.out.println("Finish");

        System.out.println("2. Creating bug corpus...");
        BugCorpus.create();
        System.out.println("Finish");

        System.out.println("3. Creating bug vector...");
        BugVector.create();
        System.out.println("Finish");

        System.out.println("4. Computing bug similarity...");
        BugSimilarity.computeSimilarity();
        System.out.println("Finish");
/*
        System.out.println("5. Creating code corpus...");
        CodeCorpus_SpiltCorpus.create();
        System.out.println("Finish");

        System.out.println("6. Creating code corpus origin...");
        CodeCorpus_OriginClass.create();
        System.out.println("Finish");

        System.out.println("7. Computing SimiScore...");
        SimilarityDistribution.distribute();
        System.out.println("Finish");

        System.out.println("8. Creating index...");
        Indexer.index();
        System.out.println("Finish");

        System.out.println("9. Creating index origin...");
        Indexer_OriginClass.index();
        System.out.println("Finish");

        System.out.println("10. Creating vector...");
        CodeVector.create();
        System.out.println("Finish");

        System.out.println("11. Computing VSMScore...");
        Similarity.compute();
        System.out.println("Finish");

        System.out.println("12. Computing LengthScore...");
        LenScore_OriginClass.computeLenScore();
        System.out.println("Finish");

        System.out.println("13. Counting LoC...");
        LineOfCode.beginCount();
        System.out.println("Finish");

        System.out.println("14. Evaluate...");
        Evaluation.evaluate();
        System.out.println("Finish");

        //MAP.statistics();
        System.out.println("Finish");

*/
    }
}
