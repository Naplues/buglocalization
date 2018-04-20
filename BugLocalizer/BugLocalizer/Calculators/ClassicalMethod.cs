using System;
using System.IO;
using System.Collections.Generic;
using System.Linq;

using CenterSpace.NMath.Core;
using CenterSpace.NMath.Matrix;

using BugLocalization.Helpers;
using BugLocalizer.Models;

namespace BugLocalizer.Calculators
{

    /// <summary>
    /// 传统IR方法实现
    /// </summary>
    public class ClassicalMethod : Method
    {


        // IDF 字典
        internal static readonly MyDoubleDictionary IdfDictionary = new MyDoubleDictionary();
        // 词频字典
        internal static readonly Dictionary<string, MyDoubleDictionary> TfDictionary = new Dictionary<string, MyDoubleDictionary>();
        // TF-IDF 词典
        internal static readonly Dictionary<string, MyDoubleDictionary> TfIdfDictionary = new Dictionary<string, MyDoubleDictionary>();



        #region Init for VSM SIM LSI
        /// <summary>
        /// 对VSM SIM LSI 方法进行初始化
        /// </summary>
        public static void InitializeForVsmSimLsi()
        {
            // compute tf and idf
            foreach (var fileAndItsWords in CodeFilesWithContent)
            {
                MyDoubleDictionary fileTfDictionary = new MyDoubleDictionary();

                // for each word in the file add 1 to the count
                foreach (string word in fileAndItsWords.Value)
                {
                    fileTfDictionary.Add(word);
                }

                // save tf result for the file
                TfDictionary.Add(fileAndItsWords.Key, fileTfDictionary);

                // for each DISTINCT word found in the file increase the idf by 1. At this point idf holds document frequency
                foreach (var wordAndItsCount in fileTfDictionary)
                {
                    IdfDictionary.Add(wordAndItsCount.Key);
                }
            }

            // change df to idf
            int totalNumberOfDocuments = CodeFilesWithContent.Count;
            foreach (var wordAndItsDocumentCount in IdfDictionary.ToList()) // to list so that we can change the dictionary
            {
                IdfDictionary[wordAndItsDocumentCount.Key] = Math.Log10(totalNumberOfDocuments / wordAndItsDocumentCount.Value);
            }

            // update tfidf for each file
            foreach (var sourceFileWithTfDictionary in TfDictionary)
            {
                MyDoubleDictionary fileTfIdfDictionary = new MyDoubleDictionary();
                foreach (var wordWithTfCount in sourceFileWithTfDictionary.Value)
                {
                    fileTfIdfDictionary.Add(wordWithTfCount.Key, wordWithTfCount.Value * IdfDictionary[wordWithTfCount.Key]);
                }
                TfIdfDictionary.Add(sourceFileWithTfDictionary.Key, fileTfIdfDictionary);
            }
        }

        #endregion



        #region VSM

        /// <summary>
        /// 计算 VSM 方法
        /// </summary>
        /// <param name="outputFolderPath"></param>
        /// <param name="bugName"></param>
        /// <param name="queryText"></param>
        public static void ComputeVsm(string outputFolderPath, string bugName, List<string> queryText)
        {
            Utility.Status("Creating VSM: " + bugName);

            // 创建查询的TF-IDF字典
            MyDoubleDictionary queryTfIdfDictionary = new MyDoubleDictionary();
            queryText.ForEach(queryTfIdfDictionary.Add);

            // 最大频度
            double maxFrequency = queryTfIdfDictionary.Max(x => x.Value);

            // 计算TF-IDF
            foreach (var queryWordWithTf in queryTfIdfDictionary.ToList())
            {
                queryTfIdfDictionary[queryWordWithTf.Key] = IdfDictionary.ContainsKey(queryWordWithTf.Key)
                    ? (queryWordWithTf.Value / maxFrequency) * IdfDictionary[queryWordWithTf.Key]
                    : 0;
            }

            // 计算相似度
            MyDoubleDictionary similarityDictionary = new MyDoubleDictionary();
            CosineSimilarityCalculator cosineSimilarityCalculator = new CosineSimilarityCalculator(queryTfIdfDictionary);

            // compute similarity of fileText with each _codeFiles
            foreach (var codeFileWithTfIdfDictionary in TfIdfDictionary)
            {
                double cosineSimilarityWithUseCase = cosineSimilarityCalculator.GetSimilarity(codeFileWithTfIdfDictionary.Value);
                similarityDictionary.Add(codeFileWithTfIdfDictionary.Key, cosineSimilarityWithUseCase);
            }

            // 将文档向量降序写入文件ZXing\001\Results\Jen.txt
            WriteDocumentVectorToFileOrderedDescending(outputFolderPath + VsmFileName, similarityDictionary);

            Utility.Status("Completed VSM: " + bugName);
        }

        #endregion



        #region LSI

        internal static Dictionary<int, DoubleMatrix> _uk;
        internal static Dictionary<int, DoubleMatrix> _sk;
        internal static Dictionary<int, DoubleMatrix> _vkTranspose;

        public static void DoSvd()
        {
            Utility.Status("Creating SVD");

            // create the matrix
            int totalNumberOfSourceFiles = TfDictionary.Count;
            int totalDistinctTermsInAllSourceFiles = IdfDictionary.Count;
            Dictionary<string, int> allSourceFilesWithIndex = TfDictionary.Keys.Select((x, index) => new { Name = x, Index = index }).ToDictionary(x => x.Name, x => x.Index);
            Dictionary<string, int> allSourceWordsWithIndex = IdfDictionary.Keys.Select((x, index) => new { Name = x, Index = index }).ToDictionary(x => x.Name, x => x.Index);

            double[,] sourceMatrix = new double[totalDistinctTermsInAllSourceFiles, totalNumberOfSourceFiles]; // row, col row is word col docs

            foreach (var fileNameWithTfDictionary in TfDictionary)
            {
                int fileIndex = allSourceFilesWithIndex[fileNameWithTfDictionary.Key];
                foreach (var fileWordWithTf in fileNameWithTfDictionary.Value)
                {
                    sourceMatrix[allSourceWordsWithIndex[fileWordWithTf.Key], fileIndex] = fileWordWithTf.Value;
                }
            }

            // create matrix
            DoubleMatrix generalMatrix = new DoubleMatrix(sourceMatrix);

            // singular value decomposition
            var svd = new DoubleSVDecomp(generalMatrix);

            _uk = new Dictionary<int, DoubleMatrix>();
            _sk = new Dictionary<int, DoubleMatrix>();
            _vkTranspose = new Dictionary<int, DoubleMatrix>();

            Utility.LsiKs.Where(x => x <= svd.Cols).ToList().ForEach(k =>
            {
                Utility.Status("Creating k matrix of size " + k);

                DoubleMatrix U = svd.LeftVectors;
                DoubleMatrix V = svd.RightVectors;
                DoubleMatrix VH = NMathFunctions.Transpose(svd.RightVectors);

                _uk.Add(k, new DoubleMatrix(U.Rows, k, Utility.ToOne(U.ToArray()), StorageType.RowMajor));
                _sk.Add(k, new DoubleMatrix(k, k, Utility.ToOne(U.ToArray()), StorageType.RowMajor));
                _vkTranspose.Add(k, new DoubleMatrix(k, VH.Cols, Utility.ToOne(U.ToArray()), StorageType.RowMajor));
            });
        }

        /// <summary>
        /// 计算 LSI 方法
        /// </summary>
        /// <param name="outputFolderPath"></param>
        /// <param name="bugName"></param>
        /// <param name="queryText"></param>
        public static void ComputeLsi(string outputFolderPath, string bugName, List<string> queryText)
        {
            Utility.Status("Creating LSI: " + bugName);

            int totalDistinctTermsInAllSourceFiles = IdfDictionary.Count;

            Dictionary<string, int> allSourceFilesWithIndex = TfDictionary.Keys.Select((x, index) => new { Name = x, Index = index }).ToDictionary(x => x.Name, x => x.Index);
            Dictionary<string, int> allSourceWordsWithIndex = IdfDictionary.Keys.Select((x, index) => new { Name = x, Index = index }).ToDictionary(x => x.Name, x => x.Index);

            // create one for query as well
            double[,] queryMatrixTranspose = new double[1, totalDistinctTermsInAllSourceFiles];
            queryText.ForEach(queryWord =>
            {
                if (allSourceWordsWithIndex.ContainsKey(queryWord))
                    queryMatrixTranspose[0, allSourceWordsWithIndex[queryWord]] = queryMatrixTranspose[0, allSourceWordsWithIndex[queryWord]] + 1;
            });

            var ks = _uk.Keys.Where(x => !File.Exists(outputFolderPath + LsiOutputFolderName + x + ".txt")).ToList();

            foreach (var k in ks)
            {
                Utility.Status("Creating LSI for " + bugName + " where k=" + k);
                var uk = _uk[k];
                var sk = _sk[k];
                var vkTranspose = _vkTranspose[k];

                DoubleMatrix q = new DoubleMatrix(queryMatrixTranspose);
                //qv = q * uk * sk.I
                DoubleMatrix qv = NMathFunctions.Product(q, uk);
                qv = NMathFunctions.Product(qv, NMathFunctions.Inverse(sk));
                List<double> qDoubles = qv.Row(0).ToArray().ToList();

                var similarityList = allSourceFilesWithIndex.Select(doc => new KeyValuePair<string, double>(doc.Key, GetSimilarity(qDoubles, vkTranspose.Col(doc.Value).ToArray().ToList())));
                File.WriteAllLines(outputFolderPath + LsiOutputFolderName + k + ".txt", similarityList.OrderByDescending(x => x.Value).Select(x => x.Key + " " + x.Value.ToString("##.00000")));
            }

            Utility.Status("Completed LSI: " + bugName);
        }

        private static double GetSimilarity(IReadOnlyList<double> a1, IReadOnlyList<double> a2)
        {
            double dotProduct = 0;
            double aSum = 0, bSum = 0;

            for (int i = 0; i < a1.Count; i++)
            {
                dotProduct += a1[i] * a2[i];
                aSum += Math.Pow(a1[i], 2);
                bSum += Math.Pow(a2[i], 2);
            }

            return dotProduct / (Math.Sqrt(aSum) * Math.Sqrt(bSum));
        }

        #endregion



        #region JEN

        /// <summary>
        /// 计算 Jensen-Shannon 方法
        /// </summary>
        /// <param name="outputFolderPath">输出文件夹,各个bug文件夹</param>
        /// <param name="bugName">bug名称</param>
        /// <param name="queryText">查询文本</param>
        public static void ComputeJen(string outputFolderPath, string bugName, List<string> queryText)
        {
            Utility.Status("Computing JEN: " + bugName);

            /// 为源代码创建向量
            // 源码和查询中出现的单词, 单词库大小的向量
            List<string> allUniqueWordsInSourceAndQuery = IdfDictionary.Keys.Union(queryText).Distinct().ToList();
            // 总单词数
            int allUniqueWordsInSourceAndQueryCount = allUniqueWordsInSourceAndQuery.Count;
            //源码向量字典
            Dictionary<string, double[]> sourceVectors = new Dictionary<string, double[]>();
            TfDictionary.ToList().ForEach(fileWithTfCount =>
            {
                MyDoubleDictionary tfDictionary = fileWithTfCount.Value;
                // 某源码中的总单词数
                int totalWordsInFile = CodeFilesWithContent[fileWithTfCount.Key].Count;
                // 单个源码文件向量,存放Pd=f(w, d)/Td
                double[] vector = new double[allUniqueWordsInSourceAndQueryCount];
                int counter = 0;
                allUniqueWordsInSourceAndQuery.ForEach(uniqueWord =>
                {
                    vector[counter] = tfDictionary.ContainsKey(uniqueWord)
                        ? tfDictionary[uniqueWord] / totalWordsInFile
                        : 0;
                    counter++;
                });

                sourceVectors.Add(fileWithTfCount.Key, vector);
            });

            // 为查询创建向量
            double[] queryVector = new double[allUniqueWordsInSourceAndQueryCount];
            int queryCounter = 0;
            allUniqueWordsInSourceAndQuery.ForEach(uniqueWord =>
            {
                queryVector[queryCounter] = queryText.Contains(uniqueWord)
                    ? (double)queryText.Count(x => x == uniqueWord) / queryText.Count
                    : 0;
                queryCounter++;
            });

            // 计算 H(p), H(q) and H(p + q)
            MyDoubleDictionary similarityDictionary = new MyDoubleDictionary();
            sourceVectors.ToList().ForEach(sourceFileWithVector =>
            {
                var p = sourceFileWithVector.Value;
                var sumEntropy = (p.JensenSum(queryVector)).JensenEntropy();
                var pEntropy = 1.0 / 2 * p.JensenEntropy();
                var qEntropy = 1.0 / 2 * queryVector.JensenEntropy();

                var jensenDivergence = sumEntropy - pEntropy - qEntropy;
                var jensenSimilarity = 1 - jensenDivergence;
                // 源码文件编码-jensen相似度
                similarityDictionary.Add(sourceFileWithVector.Key, jensenSimilarity);
            });

            // 将文档向量降序写入文件ZXing\001\Results\Jen.txt
            WriteDocumentVectorToFileOrderedDescending(outputFolderPath + JenFileName, similarityDictionary);

            Utility.Status("DONE Computing JEN: " + bugName);
        }


        internal static readonly Dictionary<string, double[]> SourceVectors = new Dictionary<string, double[]>();
        internal static readonly List<string> AllUniqueWordsInSourceAndQuery = new List<string>();

        internal static void InitializeJen(string datasetFolderPath)
        {
            List<DirectoryInfo> bugs = new DirectoryInfo(datasetFolderPath).GetDirectories().Where(x => x.Name != "Corpus").ToList();
            var allQueryTexts = bugs.SelectMany(x => File.ReadAllLines(x.FullName + @"\" + QueryWithFilterFileName)).Distinct().ToList();

            // create the vector for each source code
            AllUniqueWordsInSourceAndQuery.AddRange(IdfDictionary.Keys.Union(allQueryTexts).Distinct().ToList());
            int allUniqueWordsInSourceAndQueryCount = AllUniqueWordsInSourceAndQuery.Count;

            TfDictionary.ToList().ForEach(fileWithTfCount =>
            {
                MyDoubleDictionary tfDictionary = fileWithTfCount.Value;
                int totalWordsInFile = CodeFilesWithContent[fileWithTfCount.Key].Count;

                double[] vector = new double[allUniqueWordsInSourceAndQueryCount];
                int counter = 0;
                AllUniqueWordsInSourceAndQuery.ForEach(uniqueWord =>
                {
                    vector[counter] = tfDictionary.ContainsKey(uniqueWord)
                        ? tfDictionary[uniqueWord] / totalWordsInFile
                        : 0;
                    counter++;
                });

                SourceVectors.Add(fileWithTfCount.Key, vector);
            });
        }

        private static void ComputeJen2Eclipse(string outputFolderPath, string bugName, List<string> queryText)
        {
            Utility.Status("Computing Source Vectors Jensen: " + bugName);

            if (File.Exists(outputFolderPath + JenFileName))
            {
                Utility.Status("Jen File Exists.");
                return;
            }

            // create the vector for query
            double[] queryVector = new double[AllUniqueWordsInSourceAndQuery.Count];
            int queryCounter = 0;
            AllUniqueWordsInSourceAndQuery.ForEach(uniqueWord =>
            {
                queryVector[queryCounter] = queryText.Contains(uniqueWord)
                    ? (double)queryText.Count(x => x == uniqueWord) / queryText.Count
                    : 0;
                queryCounter++;
            });

            // calculate H(p), H(q) and H(p + q)
            MyDoubleDictionary similarityDictionary = new MyDoubleDictionary();
            SourceVectors.ToList().ForEach(sourceFileWithVector =>
            {
                var p = sourceFileWithVector.Value;
                var sumEntropy = (p.JensenSum(queryVector)).JensenEntropy();
                var pEntropy = 1.0 / 2 * p.JensenEntropy();
                var qEntropy = 1.0 / 2 * queryVector.JensenEntropy();

                var jensenDivergence = sumEntropy - pEntropy - qEntropy;
                var jensenSimilarity = 1 - jensenDivergence;

                similarityDictionary.Add(sourceFileWithVector.Key, jensenSimilarity);
            });

            // done
            WriteDocumentVectorToFileOrderedDescending(outputFolderPath + JenFileName, similarityDictionary);

            Utility.Status("DONE Computing Source Vectors Jensen: " + bugName);
        }

        #endregion

    }
}
