package gzq;

import gzq.bug.*;
import gzq.source.*;
import gzq.evaluation.*;


public class Main {
    public static void main(String[] args) throws Exception{

        // 处理 source code
        System.out.println("1. Creating origin code corpus...");
        CodeCorpus.run();
        System.out.println("Finish");

        //处理 bug report
        System.out.println("2. Creating bug corpus and vectors and similarities...");
        BugCorpus.run();
        System.out.println("Finish");

        //计算相似度
        Similarity.computeSimilarity();

        System.out.println("3. Evaluating...");
        Evaluation.evaluate();
        System.out.println("Finish");

        Evaluation.getTopK(1);
        Evaluation.getTopK(5);
        Evaluation.getTopK(10);

        Evaluation.getMRR();
        //Evaluation.getMAP();


    }
}
