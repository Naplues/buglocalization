package gzq;

import gzq.bug.*;
import gzq.source.*;
import gzq.evaluation.*;


public class Main {
    public static void main(String[] args) throws Exception{

        // 处理 source code
        System.out.println("1. Creating origin code corpus...");
        CodeCorpus_OriginClass.run();
        System.out.println("Finish");

        System.out.println("2. Creating split code corpus...");
        CodeCorpus_SpiltClass.create();
        System.out.println("Finish");

        System.out.println("3. Creating origin and split index...");
        Indexer.createIndex();
        System.out.println("Finish");

        //处理 bug report
        System.out.println("4. Creating bug corpus and vectors and similarities...");
        BugCorpus.run();
        System.out.println("Finish");

        System.out.println("5. Evaluating...");
        Evaluation.evaluate();
        System.out.println("Finish");

        Evaluation.getTopK(1);
        Evaluation.getTopK(5);
        Evaluation.getTopK(10);
/*
        Evaluation.getMRR();
        Evaluation.getMAP();*/


    }
}
