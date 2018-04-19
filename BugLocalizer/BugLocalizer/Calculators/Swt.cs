using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;

using CenterSpace.NMath.Core;
using CenterSpace.NMath.Matrix;

using BugLocalization.Helpers;
using BugLocalizer.Models;

namespace BugLocalizer.Calculators
{
    public class Swt : BaseExecutable
    {
        #region Const 

        private const string VsmCompletedFile = @"CompletedVsm.txt";
        private const string LsiCompletedFile = @"CompletedLsi.txt";
        private const string NgdCompletedFile = @"CompletedNgd.txt";
        private const string SimCompletedFile = @"CompletedSim.txt";
        private const string JenCompletedFile = @"CompletedJen.txt";
        private const string APmCompletedFile = @"CompletedAPm.txt";

        private static bool _cleanPrevious;
        private static bool _runVsm;
        private static bool _runLsi;
        private static bool _runNgd;
        private static bool _runSim;
        private static bool _runJen;
        private static bool _runAPm;

        private const string CorpusWithFilterFolderName = @"Corpus\";
        private const string QueryWithFilterFileName = @"BugQuery.txt";

        private const string VsmFileName = @"Results\Vsm.txt";
        private const string PmiSimFileName = @"Results\Pmi.txt";
        private const string NgdFileName = @"Results\Ngd.txt";
        private const string JenFileName = @"Results\Jen.txt";
        private const string APmFileName = @"Results\APm.txt";
        private const string LsiOutputFolderName = @"Results\Lsi\";

        private static readonly Dictionary<string, List<string>> CodeFilesWithContent = new Dictionary<string, List<string>>();

        public override void Execute()
        {
            _cleanPrevious = Utility.CleanPrevious;
            _runVsm = Utility.RunVsm;
            _runLsi = Utility.RunLsi;
            _runNgd = Utility.RunNgd;
            _runSim = Utility.RunSim;
            _runJen = Utility.RunJen;
            _runAPm = Utility.RunAPm;

            RunTssOnDataset(@"Swt\");
        }

        public static object MyObj1 = new object();
        private static void RunTssOnDataset(string dataset)
        {
            string datasetFolderPath = Utility.ReportFolderPath + dataset;

            string vsmCompletedFilePath = datasetFolderPath + VsmCompletedFile;
            string lsiCompletedFilePath = datasetFolderPath + LsiCompletedFile;
            string ngdCompletedFilePath = datasetFolderPath + NgdCompletedFile;
            string simCompletedFilePath = datasetFolderPath + SimCompletedFile;
            string jenCompletedFilePath = datasetFolderPath + JenCompletedFile;
            string aPmCompletedFilePath = datasetFolderPath + APmCompletedFile;

            if (_runVsm && (!File.Exists(vsmCompletedFilePath) || _cleanPrevious))
                File.Create(vsmCompletedFilePath).Close();
            if (_runLsi && (!File.Exists(lsiCompletedFilePath) || _cleanPrevious))
                File.Create(lsiCompletedFilePath).Close();
            if (_runNgd && (!File.Exists(ngdCompletedFilePath) || _cleanPrevious))
                File.Create(ngdCompletedFilePath).Close();
            if (_runSim && (!File.Exists(simCompletedFilePath) || _cleanPrevious))
                File.Create(simCompletedFilePath).Close();
            if (_runJen && (!File.Exists(jenCompletedFilePath) || _cleanPrevious))
                File.Create(jenCompletedFilePath).Close();
            if (_runAPm && (!File.Exists(aPmCompletedFilePath) || _cleanPrevious))
                File.Create(aPmCompletedFilePath).Close();

            // read completed uis
            List<string> completedVsm = _runVsm ? File.ReadAllLines(vsmCompletedFilePath).ToList() : new List<string>();
            List<string> completedLsi = _runLsi ? File.ReadAllLines(lsiCompletedFilePath).ToList() : new List<string>();
            List<string> completedNgd = _runNgd ? File.ReadAllLines(ngdCompletedFilePath).ToList() : new List<string>();
            List<string> completedSim = _runSim ? File.ReadAllLines(simCompletedFilePath).ToList() : new List<string>();
            List<string> completedJen = _runJen ? File.ReadAllLines(jenCompletedFilePath).ToList() : new List<string>();
            List<string> completedAPm = _runAPm ? File.ReadAllLines(aPmCompletedFilePath).ToList() : new List<string>();

            List<DirectoryInfo> bugs = new DirectoryInfo(datasetFolderPath).GetDirectories().Where(x => x.Name != "Corpus").ToList();
            int totalbugsCount = bugs.Count;

            // Read all files
            foreach (var file in new DirectoryInfo(datasetFolderPath + CorpusWithFilterFolderName).GetFiles())
            {
                string[] text = File.ReadAllLines(file.FullName);
                CodeFilesWithContent.Add(Path.GetFileNameWithoutExtension(file.FullName), text.ToList());
            }

            if (_runVsm || _runSim || _runAPm || _runLsi || _runJen)
                InitializeForVsmSimLsi();

            if (_runNgd || _runAPm || _runSim)
                InitializeForNgdPmiSim();

            if (_runLsi)
                DoSvd();

            // Create files
            int completedCount = 0;
            Parallel.For(0, totalbugsCount, new ParallelOptions() { MaxDegreeOfParallelism = Utility.ParallelThreadCount }, i =>
            {
                ++completedCount;
                try
                {
                    Utility.Status("Creating Stuff: " + bugs[i].Name + " " + completedCount + " of " + totalbugsCount);
                    string bugFolderPath = datasetFolderPath + bugs[i].Name + @"\";

                    if (!Directory.Exists(bugFolderPath + "Results"))
                        Directory.CreateDirectory(bugFolderPath + "Results");

                    List<string> queryText = File.ReadAllLines(bugFolderPath + QueryWithFilterFileName).ToList();

                    if (_runAPm && !completedAPm.Contains(bugs[i].Name))
                    {
                        ComputeAPm(bugFolderPath, bugs[i].Name, queryText);
                        completedAPm.Add(bugs[i].Name);
                    }

                    if (_runVsm && !completedVsm.Contains(bugs[i].Name))
                    {
                        ComputeVsm(bugFolderPath, bugs[i].Name, queryText);
                        completedVsm.Add(bugs[i].Name);
                    }

                    if (_runLsi && !completedLsi.Contains(bugs[i].Name))
                    {
                        if (!Directory.Exists(bugFolderPath + LsiOutputFolderName))
                            Directory.CreateDirectory(bugFolderPath + LsiOutputFolderName);

                        ComputeLsi(bugFolderPath, bugs[i].Name, queryText);
                        completedLsi.Add(bugs[i].Name);
                    }

                    if (_runNgd && !completedNgd.Contains(bugs[i].Name))
                    {
                        ComputeNgd(bugFolderPath, bugs[i].Name, queryText);
                        completedNgd.Add(bugs[i].Name);
                    }

                    if (_runSim && !completedSim.Contains(bugs[i].Name))
                    {
                        ComputePmiSim(bugFolderPath, bugs[i].Name, queryText);
                        completedSim.Add(bugs[i].Name);
                    }

                    if (_runJen && !completedJen.Contains(bugs[i].Name))
                    {
                        ComputeJen(bugFolderPath, bugs[i].Name, queryText);
                        completedJen.Add(bugs[i].Name);
                    }

                    Utility.Status("DONE Creating Stuff: " + bugs[i].Name + " (" + completedCount + " of " + totalbugsCount + ")");
                }
                catch (Exception e)
                {
                    Utility.WriteErrorCommon(dataset + bugs[i].Name, e.Message);
                    Utility.Status("ERROR Creating Stuff: " + dataset + bugs[i].Name + " (" + completedCount + " of " + totalbugsCount + ")");
                }
                finally
                {
                    lock (MyObj1)
                    {
                        if (_runVsm) File.WriteAllLines(vsmCompletedFilePath, completedVsm);
                        if (_runLsi) File.WriteAllLines(lsiCompletedFilePath, completedLsi);
                        if (_runNgd) File.WriteAllLines(ngdCompletedFilePath, completedNgd);
                        if (_runSim) File.WriteAllLines(simCompletedFilePath, completedSim);
                        if (_runJen) File.WriteAllLines(jenCompletedFilePath, completedJen);
                        if (_runAPm) File.WriteAllLines(aPmCompletedFilePath, completedAPm);
                    }
                }
            });
        }

        #endregion

        #region Jensen-Shannon

        private static void ComputeJen(string outputFolderPath, string bugName, List<string> queryText)
        {
            Utility.Status("Computing Source Vectors Jensen: " + bugName);

            // create the vector for each source code
            List<string> allUniqueWordsInSourceAndQuery = IdfDictionary.Keys.Union(queryText).Distinct().ToList();
            int allUniqueWordsInSourceAndQueryCount = allUniqueWordsInSourceAndQuery.Count;

            Dictionary<string, double[]> sourceVectors = new Dictionary<string, double[]>();
            TfDictionary.ToList().ForEach(fileWithTfCount =>
            {
                MyDoubleDictionary tfDictionary = fileWithTfCount.Value;
                int totalWordsInFile = CodeFilesWithContent[fileWithTfCount.Key].Count;

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

            // create the vector for query
            double[] queryVector = new double[allUniqueWordsInSourceAndQueryCount];
            int queryCounter = 0;
            allUniqueWordsInSourceAndQuery.ForEach(uniqueWord =>
            {
                queryVector[queryCounter] = queryText.Contains(uniqueWord)
                    ? (double)queryText.Count(x => x == uniqueWord) / queryText.Count
                    : 0;
                queryCounter++;
            });

            // calculate H(p), H(q) and H(p + q)
            MyDoubleDictionary similarityDictionary = new MyDoubleDictionary();
            sourceVectors.ToList().ForEach(sourceFileWithVector =>
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

        #region VSM

        private static readonly MyDoubleDictionary IdfDictionary = new MyDoubleDictionary();
        private static readonly Dictionary<string, MyDoubleDictionary> TfDictionary = new Dictionary<string, MyDoubleDictionary>();
        private static readonly Dictionary<string, MyDoubleDictionary> TfIdfDictionary = new Dictionary<string, MyDoubleDictionary>();

        private static void InitializeForVsmSimLsi()
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

        private static void ComputeVsm(string outputFolderPath, string bugName, List<string> queryText)
        {
            Utility.Status("Creating VSM: " + bugName);

            // CREATE QUERY TFIDF
            MyDoubleDictionary queryTfIdfDictionary = new MyDoubleDictionary();
            queryText.ForEach(queryTfIdfDictionary.Add);

            // max frequency
            double maxFrequency = queryTfIdfDictionary.Max(x => x.Value);

            // now multiply each by idf to get tfidf for query
            foreach (var queryWordWithTf in queryTfIdfDictionary.ToList())
            {
                queryTfIdfDictionary[queryWordWithTf.Key] = IdfDictionary.ContainsKey(queryWordWithTf.Key)
                    ? (queryWordWithTf.Value / maxFrequency) * IdfDictionary[queryWordWithTf.Key]
                    : 0;
            }

            // CALCULATE SIMILARITY
            MyDoubleDictionary similarityDictionary = new MyDoubleDictionary();
            CosineSimilarityCalculator cosineSimilarityCalculator = new CosineSimilarityCalculator(queryTfIdfDictionary);

            // compute similarity of fileText with each _codeFiles
            foreach (var codeFileWithTfIdfDictionary in TfIdfDictionary)
            {
                double cosineSimilarityWithUseCase = cosineSimilarityCalculator.GetSimilarity(codeFileWithTfIdfDictionary.Value);
                similarityDictionary.Add(codeFileWithTfIdfDictionary.Key, cosineSimilarityWithUseCase);
            }

            // WRITE TO FILE
            WriteDocumentVectorToFileOrderedDescending(outputFolderPath + VsmFileName, similarityDictionary);

            Utility.Status("Completed VSM: " + bugName);
        }

        #endregion

        #region LSI

        private static Dictionary<int, DoubleMatrix> _uk;
        private static Dictionary<int, DoubleMatrix> _sk;
        private static Dictionary<int, DoubleMatrix> _vkTranspose;

        private static void DoSvd()
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

        private static void ComputeLsi(string outputFolderPath, string bugName, List<string> queryText)
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

        #region NGD

        private static void ComputeNgd(string ngdOutputFolderPath, string bugName, List<string> fileText)
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

        #region PMI 

        private static readonly Dictionary<string, List<string>> WordAndContainingFiles = new Dictionary<string, List<string>>();

        private static void InitializeForNgdPmiSim()
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

        private static void ComputePmiSim(string simOutputFolderPath, string bugName, List<string> fileText)
        {
            Utility.Status("Creating Pmi: " + bugName);

            MyDoubleDictionary tssDocumentDictionary = new MyDoubleDictionary();

            // Create list of word contained in query
            List<string> distinctQueryWordList = fileText.Distinct().ToList(); // DISTINCT HERE but since its calculating PMI done remove it
            DocumentDictionaryAny<MyDoubleDictionary> nPmiMatrix = new DocumentDictionaryAny<MyDoubleDictionary>();
            int n = CodeFilesWithContent.Count;

            // Compute pmi for each word in WordAndContainingFiles and unique words in query
            foreach (var queryWordW2 in distinctQueryWordList)
            {
                MyDoubleDictionary nPmiDictionary = new MyDoubleDictionary();

                foreach (var sourceWordW1 in WordAndContainingFiles.Keys)
                {
                    bool sourceContainsUseCaseWord = WordAndContainingFiles.ContainsKey(queryWordW2);

                    int countW1 = WordAndContainingFiles[sourceWordW1].Count;
                    int countW2 = sourceContainsUseCaseWord ? WordAndContainingFiles[queryWordW2].Count : 0;
                    // if query contains source then add 1 (query contains usecase word + source word
                    // if source contains query word find the intersection of files containing both words
                    int countW1W2 = sourceContainsUseCaseWord ? WordAndContainingFiles[sourceWordW1].Intersect(WordAndContainingFiles[queryWordW2]).Count() : 0;

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
                            nPmi = Math.Log10((double)countW1 / n * countW2 / n) / Math.Log10((double)countW1W2 / n) - 1;
                        }
                    }
                    nPmiDictionary.Add(sourceWordW1, nPmi);
                }
                nPmiMatrix.Add(queryWordW2, nPmiDictionary);
            }

            //List<string> distinctUseCaseWordListForTss = fileText.Distinct().ToList(); //DISTINCT HERE
            List<string> distinctQueryWordListForTss = fileText.ToList(); //DISTINCT HERE
            int totalNumberOfDocumentInSource = CodeFilesWithContent.Count;
            // Once the PMI is create compute Sim
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
                        double currentnPmi = nPmiMatrix[queryWord][sourceWord];
                        if (maxSim < currentnPmi)
                            maxSim = currentnPmi;
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
                    foreach (string useCaseWord in distinctQueryWordListForTss)
                    {
                        double currentNPmi = nPmiMatrix[useCaseWord][sourceWord];
                        if (maxSim < currentNPmi)
                            maxSim = currentNPmi;
                    }

                    // sourceWord has to be in IdfDictionary
                    double idf = Math.Log10((double)totalNumberOfDocumentInSource / WordAndContainingFiles[sourceWord].Count);

                    sumCorpusIdf += idf;
                    sumCorpusTimeIdf += (maxSim * idf);
                }

                double tss = (1.0 / 2) * ((sumQueryTimeIdf / sumQueryIdf) + (sumCorpusTimeIdf / sumCorpusIdf));
                tssDocumentDictionary.Add(sourceFileWithWords.Key, tss);
            }

            WriteDocumentVectorToFileOrderedDescending(simOutputFolderPath + PmiSimFileName, tssDocumentDictionary);

            Utility.Status("Completed Pmi: " + bugName);
        }

        #endregion



        private static void ComputeAPm(string pmiOutputFolderPath, string reqName, List<string> reqText)
        {
            Utility.Status("Creating Pmi: " + reqName);

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

        private static double GetSim(string w1, string w2, DocumentDictionaryAny<MyDoubleDictionary> matrix, double noMatch)
        {
            if (matrix.ContainsKey(w1) && matrix[w1].ContainsKey(w2))
                return matrix[w1][w2];

            if (matrix.ContainsKey(w2) && matrix[w2].ContainsKey(w1))
                return matrix[w2][w1];

            return noMatch;
        }

    }
}
