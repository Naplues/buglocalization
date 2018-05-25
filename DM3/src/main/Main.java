package main;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.Bagging;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.io.File;
import java.util.Random;


/**
 * Created by naplues on 5/23/2018.
 */
public class Main {

    /**
     * 数据过滤
     * @param data
     * @return
     * @throws Exception
     */
    public static Instances filter(Instances data) throws Exception {
        data.setClassIndex(data.numAttributes() - 1); //设置最后一个属性为类别属性
        String[] options = Utils.splitOptions("-R 1");
        Remove remove = new Remove();
        //remove.setOptions(options);
        remove.setInputFormat(data);
        return Filter.useFilter(data, remove);
    }

    /**
     * 单个数据集上运行单个方法，用10折交叉验证评价该方法
     *
     * @param classifier
     * @param file
     * @throws Exception
     */
    public static void runMethod(Classifier classifier, String methodName, String file) throws Exception {
        Instances data = ConverterUtils.DataSource.read(file);
        data = filter(data);
        Evaluation evaluation = new Evaluation(data);
        // 10折交叉验证
        evaluation.crossValidateModel(classifier, data, 10, new Random(1));
        // AUC 和 Accuracy
        double avgAUC = .0, avgAccuracy = .0;
        for (int i = 0; i < data.numClasses(); i++) {
            avgAUC += (evaluation.areaUnderROC(i) - avgAUC) / (i + 1);
            avgAccuracy += evaluation.precision(i) * (evaluation.numTruePositives(i) + evaluation.numFalseNegatives(i)) / evaluation.numInstances();
        }
        System.out.println(methodName + ", " + avgAccuracy + ", " + avgAUC);
        // 输出更加详细的评估信息
        // System.out.println(evaluation.toSummaryString(false));
        // System.out.println(evaluation.toClassDetailsString());
        // System.out.println(evaluation.toMatrixString());
    }

    public static void main(String[] args) throws Exception {
        File files = new File("data");
        for (File file : files.listFiles()) {
            System.out.println("Data set: " + file);

            // J48, Naive Bayes, SVM, Neural Network, kNN(3)
            runMethod(new J48(), "J48", file.getPath());
            runMethod(new NaiveBayes(), "Naive Bayes", file.getPath());
            runMethod(new SMO(), "SVM", file.getPath());
            runMethod(new MultilayerPerceptron(), "Neural Network", file.getPath());
            runMethod(new IBk(3), "kNN(3)", file.getPath());
            System.out.println();

            // ensemble version
            Classifier classifier = new Bagging();
            ((Bagging) classifier).setClassifier(new J48());
            runMethod(classifier, "Ensemble J48", file.getPath());
            ((Bagging) classifier).setClassifier(new NaiveBayes());
            runMethod(classifier, "Ensemble Naive Bayes", file.getPath());
            ((Bagging) classifier).setClassifier(new SMO());
            runMethod(classifier, "Ensemble SVM", file.getPath());
            ((Bagging) classifier).setClassifier(new MultilayerPerceptron());
            runMethod(classifier, "Ensemble Neural Network", file.getPath());
            ((Bagging) classifier).setClassifier(new IBk(3));
            runMethod(classifier, "Ensemble kNN(3)", file.getPath());
            System.out.println();

        }
    }
}
