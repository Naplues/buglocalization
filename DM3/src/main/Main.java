package main;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.util.Random;


/**
 * Created by naplues on 5/23/2018.
 */
public class Main {

    public static Instances filter(Instances data) throws Exception{
        data.setClassIndex(data.numAttributes() - 1); //设置最后一个属性为类别属性
        String[] options = Utils.splitOptions("-R 1");
        Remove remove = new Remove();
        //remove.setOptions(options);
        remove.setInputFormat(data);
        return Filter.useFilter(data, remove);
    }

    public static void runMethod(String file) throws Exception {
        Instances data = ConverterUtils.DataSource.read("data/" + file);
        data = filter(data);
        Evaluation evaluation = new Evaluation(data);
        J48 j48 = new J48();
        j48.buildClassifier(data);
        System.out.println(j48);

        evaluation.crossValidateModel(j48, data, 10, new Random(1));

        System.out.println(evaluation.toSummaryString(false));
        System.out.println(evaluation.toClassDetailsString());
        System.out.println(evaluation.toMatrixString());
    }

    public static void main(String[] args) throws Exception{
        String[] files = {"breast-w.arff", "colic.arff", "credit-a.arff", "credit-g.arff", "diabetes.arff",
                "hepatitis.arff", "mozilla4.arff", "pc1.arff", "pc5.arff", "waveform-5000.arff"};

        for(String file : files) {
            runMethod(file);
        }

    }
}
