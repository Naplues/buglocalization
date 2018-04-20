using System;
using System.IO;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

using BugLocalization.Helpers;
using BugLocalizer.Models;

namespace BugLocalizer.Calculators
{
    /// <summary>
    /// 新提出的IR方法实现
    /// </summary>
    public class ProposedMethod : Method
    {

        // 单词和包含该单词的文件
        internal static readonly Dictionary<string, List<string>> WordAndContainingFiles = new Dictionary<string, List<string>>();


        #region Init for NGD PMI
        // 初始化
        public static void InitializeForNgdPmiSim()
        {
            foreach (var sourceFileWithWords in CodeFilesWithContent)
            {
                sourceFileWithWords.Value.Distinct().ToList().ForEach(word =>
                {
                    if (!WordAndContainingFiles.ContainsKey(word))
                        WordAndContainingFiles.Add(word, new List<string>());
                    WordAndContainingFiles[word].Add(sourceFileWithWords.Key);
                });
            }
        }

        #endregion



        #region PMI

        /// <summary>
        /// 计算 PMI 方法
        /// </summary>
        /// <param name="simOutputFolderPath"></param>
        /// <param name="bugName"></param>
        /// <param name="fileText"></param>
        public static void ComputePmiSim(string simOutputFolderPath, string bugName, List<string> fileText)
        {
            Utility.Status("Creating Pmi: " + bugName);

            MyDoubleDictionary tssDocumentDictionary = new MyDoubleDictionary();

            // Create list of word contained in query
            // 创建查询列表(每个单词唯一)
            List<string> distinctQueryWordList = fileText.Distinct().ToList(); // DISTINCT HERE but since its calculating PMI done remove it
            // 单词共现矩阵
            DocumentDictionaryAny<MyDoubleDictionary> nPmiMatrix = new DocumentDictionaryAny<MyDoubleDictionary>();

            // 源文件数目
            int n = CodeFilesWithContent.Count;

            // 为查询中的每个单词W2计算 带文件单词的 PMI值
            foreach (var queryWordW2 in distinctQueryWordList)
            {
                MyDoubleDictionary nPmiDictionary = new MyDoubleDictionary();
                // 对源码中的每个单词W1
                foreach (var sourceWordW1 in WordAndContainingFiles.Keys)
                {
                    // 源码中是否包含查询W2
                    bool sourceContainsUseCaseWord = WordAndContainingFiles.ContainsKey(queryWordW2);
                    // 包含 W1的数目C(W1), C(W2), C(W1,W2)
                    int countW1 = WordAndContainingFiles[sourceWordW1].Count;
                    int countW2 = sourceContainsUseCaseWord ? WordAndContainingFiles[queryWordW2].Count : 0;
                    // if query contains source then add 1 (query contains usecase word + source word
                    // if source contains query word find the intersection of files containing both words
                    int countW1W2 = sourceContainsUseCaseWord ? WordAndContainingFiles[sourceWordW1].Intersect(WordAndContainingFiles[queryWordW2]).Count() : 0;


                    // 归一化的 PMI, d1 and d2 != 0, d1d2 可能
                    double nPmi;
                    // 从未共现, nPMI = -1
                    if (countW1W2 == 0)
                    {
                        nPmi = -1;
                    }
                    else
                    {
                        // 完全共现, nPMI = 1
                        if (countW1 == countW1W2 && countW2 == countW1W2)
                        {
                            nPmi = 1;
                        }
                        else
                        {
                            nPmi = Math.Log10((double)countW1 / n * countW2 / n) / Math.Log10((double)countW1W2 / n) - 1;
                        }
                    }
                    nPmiDictionary.Add(sourceWordW1, nPmi);
                }
                nPmiMatrix.Add(queryWordW2, nPmiDictionary);
            }

            //List<string> distinctUseCaseWordListForTss = fileText.Distinct().ToList(); //DISTINCT HERE
            List<string> distinctQueryWordListForTss = fileText.ToList(); //DISTINCT HERE
            // 源码中总文件数
            int totalNumberOfDocumentInSource = CodeFilesWithContent.Count;

            // Once the PMI is create compute Sim
            foreach (var sourceFileWithWords in CodeFilesWithContent)
            {
                //List<string> distinctSourceWords = sourceFileWithWords.Value.Distinct().ToList(); //DISTINCT HERE
                //该处应该是Distinct
                List<string> distinctSourceWords = sourceFileWithWords.Value.ToList(); //DISTINCT HERE
                double sumQueryTimeIdf = 0.0;
                double sumQueryIdf = 0.0;

                foreach (var queryWord in distinctQueryWordListForTss)
                {
                    // 计算maxSim
                    double maxSim = -1;
                    foreach (var sourceWord in distinctSourceWords)
                    {
                        double currentnPmi = nPmiMatrix[queryWord][sourceWord];
                        if (maxSim < currentnPmi)
                            maxSim = currentnPmi;
                    }

                    // if term does not occur in any corpus then its only in use case hence 1
                    double idf = 0;
                    if (WordAndContainingFiles.ContainsKey(queryWord))
                        idf = Math.Log10((double)totalNumberOfDocumentInSource / WordAndContainingFiles[queryWord].Count);
                    sumQueryTimeIdf += (maxSim * idf);
                    sumQueryIdf += idf;
                }

                double sumCorpusTimeIdf = 0.0;
                double sumCorpusIdf = 0.0;

                foreach (string sourceWord in distinctSourceWords)
                {
                    double maxSim = -1;
                    foreach (string useCaseWord in distinctQueryWordListForTss)
                    {
                        double currentNPmi = nPmiMatrix[useCaseWord][sourceWord];
                        if (maxSim < currentNPmi)
                            maxSim = currentNPmi;
                    }

                    // sourceWord has to be in IdfDictionary
                    double idf = Math.Log10((double)totalNumberOfDocumentInSource / WordAndContainingFiles[sourceWord].Count);

                    sumCorpusTimeIdf += (maxSim * idf);
                    sumCorpusIdf += idf;

                }

                double tss = (1.0 / 2) * ((sumQueryTimeIdf / sumQueryIdf) + (sumCorpusTimeIdf / sumCorpusIdf));
                tssDocumentDictionary.Add(sourceFileWithWords.Key, tss);
            }

            WriteDocumentVectorToFileOrderedDescending(simOutputFolderPath + PmiFileName, tssDocumentDictionary);

            Utility.Status("Completed Pmi: " + bugName);
        }

        #endregion


        #region NGD

        public static void ComputeNgd(string ngdOutputFolderPath, string bugName, List<string> fileText)
        {
            Utility.Status("Creating NGD: " + bugName);

            MyDoubleDictionary tssDocumentDictionary = new MyDoubleDictionary();
            double logD = Math.Log10(CodeFilesWithContent.Count + 1); // just make the N bigger than any

            // Create list of word contained in query
            List<string> distinctQueryWordList = fileText.Distinct().ToList(); // DISTINCT HERE but since its calculating NGD done remove it
            DocumentDictionaryAny<MyDoubleDictionary> ngdMatrix = new DocumentDictionaryAny<MyDoubleDictionary>();

            foreach (var queryWordW2 in distinctQueryWordList)
            {
                MyDoubleDictionary ngdDictionary = new MyDoubleDictionary();

                foreach (var sourceWordW1 in WordAndContainingFiles.Keys)
                {
                    bool sourceContainsUseCaseWord = WordAndContainingFiles.ContainsKey(queryWordW2);

                    int countD1 = WordAndContainingFiles[sourceWordW1].Count;
                    // number of file containing W1 + if query also contains the word
                    int countD2 = sourceContainsUseCaseWord ? WordAndContainingFiles[queryWordW2].Count : 0;
                    // if query contains source then add 1 (query contains usecase word + source word
                    // if source contains query word find the intersection of files containing both words
                    int countD1D2 = sourceContainsUseCaseWord ? WordAndContainingFiles[sourceWordW1].Intersect(WordAndContainingFiles[queryWordW2]).Count() : 0;

                    // d1 and d2 will never be 0, d1d2 however can be
                    double ngd = (countD1D2 == 0) ? 0 : ComputenNgd(countD1, countD2, countD1D2, logD);

                    ngdDictionary.Add(sourceWordW1, ngd);
                }
                ngdMatrix.Add(queryWordW2, ngdDictionary);
            }

            //List<string> distinctUseCaseWordListForTss = fileText.Distinct().ToList(); //DISTINCT HERE
            List<string> distinctQueryWordListForTss = fileText.ToList(); //DISTINCT HERE
            int totalNumberOfDocumentInSource = CodeFilesWithContent.Count;

            foreach (var sourceFileWithWords in CodeFilesWithContent)
            {
                //List<string> distinctSourceWords = sourceFileWithWords.Value.Distinct().ToList(); //DISTINCT HERE
                List<string> distinctSourceWords = sourceFileWithWords.Value.ToList(); //DISTINCT HERE
                double sumQueryTimeIdf = 0.0;
                double sumQueryIdf = 0.0;

                foreach (var queryWord in distinctQueryWordListForTss)
                {
                    double maxSim = -1;
                    foreach (var sourceWord in distinctSourceWords)
                    {
                        double currentNgd = ngdMatrix[queryWord][sourceWord];
                        if (maxSim < currentNgd)
                            maxSim = currentNgd;
                    }

                    // if term does not occur in any corpus then its only in use case hence 1
                    double idf = 0;
                    if (WordAndContainingFiles.ContainsKey(queryWord))
                        idf = Math.Log10((double)totalNumberOfDocumentInSource / WordAndContainingFiles[queryWord].Count);
                    sumQueryIdf += idf;
                    sumQueryTimeIdf += (maxSim * idf);
                }

                double sumCorpusTimeIdf = 0.0;
                double sumCorpusIdf = 0.0;

                foreach (string sourceWord in distinctSourceWords)
                {
                    double maxSim = -1;
                    foreach (string queryWord in distinctQueryWordListForTss)
                    {
                        double currentNgd = ngdMatrix[queryWord][sourceWord];
                        if (maxSim < currentNgd)
                            maxSim = currentNgd;
                    }

                    // sourceWord has to be in IdfDictionary
                    double idf = Math.Log10((double)totalNumberOfDocumentInSource / WordAndContainingFiles[sourceWord].Count);

                    sumCorpusIdf += idf;
                    sumCorpusTimeIdf += (maxSim * idf);
                }

                double tss = (1.0 / 2) * ((sumQueryTimeIdf / sumQueryIdf) + (sumCorpusTimeIdf / sumCorpusIdf));
                tssDocumentDictionary.Add(sourceFileWithWords.Key, tss);
            }

            WriteDocumentVectorToFileOrderedDescending(ngdOutputFolderPath + NgdFileName, tssDocumentDictionary);

            Utility.Status("Completed NGD: " + bugName);
        }


        /// <summary>
        /// d1, d2 and d1d2 are NOT zero
        /// </summary>
        private static double ComputenNgd(double d1, double d2, double d1D2, double logD)
        {
            double logD1 = Math.Log10(d1);
            double logD2 = Math.Log10(d2);
            double logD1D2 = Math.Log10(d1D2);

            double upper = Math.Max(logD1, logD2) - logD1D2;
            double lower = logD - Math.Min(logD1, logD2);
            double ngd = upper / lower;

            return Math.Pow(Math.E, -2 * ngd);
        }

        #endregion


        #region APM
        public static void ComputeAPm(string pmiOutputFolderPath, string reqName, List<string> reqText)
        {
            Utility.Status("Creating Apm: " + reqName);

            // Create list of word contained in query
            List<string> distinctReqWordList = reqText.Distinct().ToList();
            DocumentDictionaryAny<MyDoubleDictionary> nPmiMatrix = new DocumentDictionaryAny<MyDoubleDictionary>();
            int n = CodeFilesWithContent.Count;

            // Compute pmi for each word in WordAndContainingFiles and unique words in query
            foreach (var reqWordW2 in distinctReqWordList)
            {
                MyDoubleDictionary nPmiDictionary = new MyDoubleDictionary();

                foreach (var sourceWordW1 in WordAndContainingFiles.Keys)
                {
                    bool sourceContainsUseCaseWord = WordAndContainingFiles.ContainsKey(reqWordW2);

                    int countW1 = WordAndContainingFiles[sourceWordW1].Count;
                    //double averageCountW1Files = _wordAndContainingFiles[sourceWordW1].Select(x => _codeFilesWithContent[x].Count).Average();
                    int countW2 = sourceContainsUseCaseWord ? WordAndContainingFiles[reqWordW2].Count : 0;
                    //double averageCountW2Files = sourceContainsUseCaseWord ? _wordAndContainingFiles[reqWordW2].Select(x => _codeFilesWithContent[x].Count).Average() : 0;

                    // if query contains source then add 1 (query contains usecase word + source word
                    // if source contains query word find the intersection of files containing both words
                    int countW1W2 = sourceContainsUseCaseWord ? WordAndContainingFiles[sourceWordW1].Intersect(WordAndContainingFiles[reqWordW2]).Count() : 0;
                    //double averageCountW1W2Files = sourceContainsUseCaseWord ? _wordAndContainingFiles[sourceWordW1].Intersect(_wordAndContainingFiles[reqWordW2]).Select(x => _codeFilesWithContent[x].Count).Average() : 0;

                    // d1 and d2 will never be 0, d1d2 however can be
                    double nPmi;
                    if (countW1W2 == 0)
                    {
                        // no cooccurence
                        nPmi = -1;
                    }
                    else
                    {
                        if (countW1 == countW1W2 && countW2 == countW1W2)
                        {
                            nPmi = 1;
                        }
                        else
                        {
                            nPmi = (Math.Log10((double)countW1 / n * countW2 / n) / Math.Log10((double)countW1W2 / n) - 1) * ((double)countW1W2 / CodeFilesWithContent.Count);
                        }
                    }
                    nPmiDictionary.Add(sourceWordW1, nPmi);
                }
                nPmiMatrix.Add(reqWordW2, nPmiDictionary);
            }

            MyDoubleDictionary tssDocumentDictionary = GetTssAltered(reqText, nPmiMatrix, -1);

            WriteDocumentVectorToFileOrderedDescending(pmiOutputFolderPath + APmFileName, tssDocumentDictionary);

            Utility.Status("Completed APm: " + reqName);
        }


        private static MyDoubleDictionary GetTssAltered(List<string> reqFileText, DocumentDictionaryAny<MyDoubleDictionary> simMatrix, double noMatch)
        {
            MyDoubleDictionary tssDocumentDictionary = new MyDoubleDictionary();
            Dictionary<string, double> reqTfDictionary = new Dictionary<string, double>();
            reqFileText.ForEach(reqWord =>
            {
                if (!reqTfDictionary.ContainsKey(reqWord))
                    reqTfDictionary.Add(reqWord, 0);
                reqTfDictionary[reqWord] = reqTfDictionary[reqWord] + 1;
            });

            List<string> reqWordListForTss = reqFileText.ToList();
            int totalNumberOfDocumentInSource = CodeFilesWithContent.Count;
            foreach (var sourceFileWithWords in CodeFilesWithContent)
            {
                List<string> sourceWords = sourceFileWithWords.Value.ToList();
                double sumReqTimeIdf = 0.0;
                double sumReqIdf = 0.0;

                foreach (var reqWord in reqWordListForTss)
                {
                    double maxSim = -1;
                    foreach (var sourceWord in sourceWords)
                    {
                        double currentSim = GetSim(reqWord, sourceWord, simMatrix, noMatch);
                        if (maxSim < currentSim)
                        {
                            maxSim = currentSim;
                        }
                    }

                    // if term does not occur in any source then its only in use case hence 1
                    double idf = 0;
                    if (WordAndContainingFiles.ContainsKey(reqWord))
                        idf = Math.Log10((double)totalNumberOfDocumentInSource / WordAndContainingFiles[reqWord].Count);

                    sumReqIdf += idf;
                    sumReqTimeIdf += (maxSim * idf);
                }

                double sumSourceTimeIdf = 0.0;
                double sumSourceIdf = 0.0;

                foreach (string sourceWord in sourceWords)
                {
                    double maxSim = -1;
                    foreach (string reqWord in reqWordListForTss)
                    {
                        double currentSim = GetSim(reqWord, sourceWord, simMatrix, noMatch);
                        if (maxSim < currentSim)
                            maxSim = currentSim;
                    }

                    // sourceWord has to be in IdfDictionary
                    double idf = Math.Log10((double)totalNumberOfDocumentInSource / WordAndContainingFiles[sourceWord].Count);

                    sumSourceTimeIdf += (maxSim * idf);
                    sumSourceIdf += idf;
                }

                double tss = (1.0 / 2) * ((sumReqTimeIdf / sumReqIdf) + (sumSourceTimeIdf / sumSourceIdf));
                tssDocumentDictionary.Add(sourceFileWithWords.Key, tss);
            }

            return tssDocumentDictionary;
        }


        /// <summary>
        /// 获取相似度
        /// </summary>
        /// <param name="w1"></param>
        /// <param name="w2"></param>
        /// <param name="matrix"></param>
        /// <param name="noMatch"></param>
        /// <returns></returns>
        private static double GetSim(string w1, string w2, DocumentDictionaryAny<MyDoubleDictionary> matrix, double noMatch)
        {
            if (matrix.ContainsKey(w1) && matrix[w1].ContainsKey(w2))
                return matrix[w1][w2];

            if (matrix.ContainsKey(w2) && matrix[w2].ContainsKey(w1))
                return matrix[w2][w1];

            return noMatch;
        }

        #endregion

    }
}
