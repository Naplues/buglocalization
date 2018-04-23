using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Xml.Serialization;

namespace Viewer
{
    /// <summary>
    /// 图表生成器
    /// </summary>
    public class GraphCreators
    {
        private const double Tolerance = 0.0000;

        private readonly Dictionary<string, string> _simMethods = new Dictionary<string, string>()
        {
            { "VSM", @"Vsm.txt" },
            { "LSI", @"Lsi\" },
            { "JSM", @"Jen.txt" },
            { "PMI", @"Pmi.txt" },
            { "NGD", @"Ngd.txt" },
            //{ "APM", @"APm.txt" },
        };

        private readonly List<string> _projects = new List<string>()
        {
            //"AspectJ",
            //"Eclipse",
            //"JodaTime",
            "SWT",
            "ZXing",
        };

        private const string ResultFolderName = @"Results\";
        private const string RelevanceFileName = @"RelList.txt";
        private const string BestLsiFileName = @"BestLsi.txt";
        private const string BugQueryFileName = @"BugQuery.txt";
        private const string ResultDumpFileName = @"BugThresholdMrrResult.txt";
        

        #region Query/Source similarity
        /// <summary>
        /// 查询/源码的相似度
        /// </summary>
        /// <returns>项目：(源码最高相似度均值, 查询最高相似度均值)</returns>
        public Dictionary<string, Tuple<double, double>> GetQueryTextSimDictionary()
        {
            var result = new Dictionary<string, Tuple<double, double>>();
            _projects.ForEach(project =>
            {
                var bugs = new DirectoryInfo(Utility.ReportFolderPath + project).GetDirectories().Where(x => x.Name != "Corpus").ToList();
                var bugSimilarityTupleList = bugs.Select(bugFolderInfo =>
                {
                    string bugFolderPath = bugFolderInfo.FullName + @"\";
                    string corpusFolderPath = Utility.GetCorpusFolderPath(project, bugFolderPath);

                    List<string> relList = Utility.GetRelevanceList(bugFolderPath + RelevanceFileName);
                    List<string> queryContent = File.ReadAllLines(bugFolderPath + BugQueryFileName).ToList();

                    double highestSourceSimilarity = relList.Select(relFileName =>
                    {
                        var sourceContent = File.ReadAllLines(corpusFolderPath + relFileName + ".txt");
                        return (double)queryContent.Count(x => sourceContent.Contains(x)) / sourceContent.Length * 100.0;
                    }).Max();

                    double highestQuerySimilarity = relList.Select(relFileName =>
                    {
                        var sourceContent = File.ReadAllLines(corpusFolderPath + relFileName + ".txt");
                        return (double)queryContent.Count(x => sourceContent.Contains(x)) / queryContent.Count * 100.0;
                    }).Max();

                    return new Tuple<double, double>(highestSourceSimilarity, highestQuerySimilarity);
                }).ToList();

                var resultTuple = new Tuple<double, double>(bugSimilarityTupleList.Select(x => x.Item1).Average(), bugSimilarityTupleList.Select(x => x.Item2).Average());
                result.Add(project, resultTuple);
            });

            return result;
        }

        #endregion


        #region Word Count

        // assumption that matching exact keywords in source and query has better performance in TSS
        // for localization this is usually true
        // show the percentage of relevant text from actual best relevant file and the query
        // find relation
        public Dictionary<string, Dictionary<string, Dictionary<string, Dictionary<string, double>>>> TextMatcher()
        {
            var result = _projects.ToDictionary(x => x, x => new[] { "VSM", "LSI", "JSM" }.ToDictionary(y => y, y => new[] { "TssBetter", "OthBetter" }.ToDictionary(z => z, z => new Dictionary<string, double>())));
            
            _projects.ForEach(project =>
            {
                // 数据集文件夹
                string datasetFolderPath = Utility.ReportFolderPath + project + @"\";

                var bugs = new DirectoryInfo(datasetFolderPath).GetDirectories().Where(x => x.Name != "Corpus").ToList();
                var bugsCount = bugs.Count;
                int counter = 1;

                bugs.ForEach(bugFolderInfo =>
                {
                    Utility.Status(project + " " + counter++ + " of " + bugsCount);
                    // bug文件夹
                    string bugFolderPath = bugFolderInfo.FullName + @"\";
                    List<string> relList = Utility.GetRelevanceList(bugFolderPath + RelevanceFileName);

                    // get first file for pmi
                    // 字典中第一个相关文件
                    Dictionary<string, FileWithIndex> firstRelevantFileDictionary = new Dictionary<string, FileWithIndex>
                    {
                        {"VSM", ReadResultFile(bugFolderPath + ResultFolderName + _simMethods["VSM"]).First(x => relList.Contains(x.File))},
                        {"LSI", ReadResultFile(bugFolderPath + ResultFolderName + _simMethods["LSI"] + Convert.ToInt32(File.ReadAllText(bugFolderInfo.FullName + @"\" + ResultFolderName + BestLsiFileName)) + ".txt").First(x => relList.Contains(x.File))},
                        {"JSM", ReadResultFile(bugFolderPath + ResultFolderName + _simMethods["JSM"]).First(x => relList.Contains(x.File))},
                        {"PMI", ReadResultFile(bugFolderPath + ResultFolderName + _simMethods["PMI"]).First(x => relList.Contains(x.File))},
                        {"NGD", ReadResultFile(bugFolderPath + ResultFolderName + _simMethods["NGD"]).First(x => relList.Contains(x.File))},
                    };

                    // read query
                    List<string> queryContents = File.ReadAllLines(bugFolderPath + BugQueryFileName).ToList();
                    var minTss = firstRelevantFileDictionary.Where(x => new List<string>() { "PMI", "NGD" }.Contains(x.Key)).OrderByDescending(x => x.Value.Index).First();

                    new[] { "VSM", "LSI", "JSM" }.ToList().ForEach(method =>
                    {
                        //首个相关文件
                        var minOth = firstRelevantFileDictionary[method];
                        if (minTss.Value.Index < minOth.Index)
                        {
                            //源码文件单词列表内容
                            List<string> sourceFileContents = File.ReadAllLines(Utility.GetCorpusFolderPath(project, bugFolderPath) + minTss.Value.File + ".txt").ToList();
                            result[project][method]["TssBetter"].Add(bugFolderInfo.Name, (double)sourceFileContents.Count(x => queryContents.Contains(x)) / sourceFileContents.Count * 100.0);
                        }
                        else if (minTss.Value.Index > minOth.Index)
                        {
                            List<string> sourceFileContents = File.ReadAllLines(Utility.GetCorpusFolderPath(project, bugFolderPath) + minOth.File + ".txt").ToList();
                            result[project][method]["OthBetter"].Add(bugFolderInfo.Name, (double)sourceFileContents.Count(x => queryContents.Contains(x)) / sourceFileContents.Count * 100.0);
                        }
                    });
                });
            });

            return result;
        }

        /// <summary>
        /// here match the first item to the query
        /// </summary>
        public Dictionary<string, Dictionary<string, Dictionary<string, double>>> TextMatcherAttempt2_CheckMatchForTopMostFile()
        {
            var result = _projects.ToDictionary(x => x, y => _simMethods.ToDictionary(x => x.Key, x => new Dictionary<string, double>()));
            _projects.ForEach(project =>
            {
                string datasetFolderPath = Utility.ReportFolderPath + project + @"\";
                var bugs = new DirectoryInfo(datasetFolderPath).GetDirectories().Where(x => x.Name != "Corpus").ToList();
                var bugsCount = bugs.Count;
                int counter = 1;
                bugs.ForEach(bugFolderInfo =>
                {
                    Utility.Status(project + " " + counter++ + " of " + bugsCount);

                    string bugFolderPath = bugFolderInfo.FullName + @"\";

                    // get first file for pmi
                    Dictionary<string, string> firstCandidateFileDictionary = new Dictionary<string, string>
                    {
                        {"VSM", ReadResultFileFirstLine(bugFolderPath + ResultFolderName + _simMethods["VSM"])},
                        {"LSI", ReadResultFileFirstLine(bugFolderPath + ResultFolderName + _simMethods["LSI"] + Convert.ToInt32(File.ReadAllText(bugFolderInfo.FullName + @"\" + ResultFolderName + BestLsiFileName)) + ".txt")},
                        {"JSM", ReadResultFileFirstLine(bugFolderPath + ResultFolderName + _simMethods["JSM"])},
                        {"PMI", ReadResultFileFirstLine(bugFolderPath + ResultFolderName + _simMethods["PMI"])},
                        {"NGD", ReadResultFileFirstLine(bugFolderPath + ResultFolderName + _simMethods["NGD"])},
                    };

                    List<string> queryContents = File.ReadAllLines(bugFolderPath + BugQueryFileName).ToList();
                    firstCandidateFileDictionary.ToList().ForEach(methodWithFile =>
                    {
                        List<string> sourceFileContents = File.ReadAllLines(Utility.GetCorpusFolderPath(project, bugFolderPath) + methodWithFile.Value + ".txt").ToList();
                        result[project][methodWithFile.Key].Add(bugFolderInfo.Name, (double)sourceFileContents.Count(x => queryContents.Contains(x)) / sourceFileContents.Count * 100.0);
                    });
                });
            });

            return result;
        }


        private static string ReadResultFileFirstLine(string filePath)
        {
            return File.ReadLines(filePath).First().Split(new[] { " " }, StringSplitOptions.None)[0];
        }

        private static List<FileWithIndex> ReadResultFile(string filePath)
        {
            List<FileWithIndex> list = File.ReadAllLines(filePath).Select(
                (x, index) =>
                {
                    var split = x.Split(new[] { " " }, StringSplitOptions.None);
                    return new FileWithIndex(index, split[0]);
                }).OrderBy(x => x.Index).ToList();

            return list;
        }

        
        #endregion

        #region Metrics
        /// <summary>
        /// 获取项目方法度量结果
        /// </summary>
        /// <param name="threshold"></param>
        /// <returns></returns>
        public Dictionary<string, Dictionary<string, Dictionary<string, double>>> GetProjectMethodMetricResult(double threshold)
        {
            var result = _projects.ToDictionary(project => project, y => new Dictionary<string, Dictionary<string, double>>());

            _projects.ForEach(project =>
            {
                _simMethods.ToList().ForEach(methodWithFolderName =>
                {
                    result[project].Add(methodWithFolderName.Key, ComputeMatrices(methodWithFolderName.Key, project, threshold));
                });
            });

            return result;
        }

        /// <summary>
        /// 获取方法项目度量结果
        /// </summary>
        /// <param name="threshold"></param>
        /// <returns></returns>
        public Dictionary<string, Dictionary<string, Dictionary<string, double>>> GetMethodProjectMetricResult(double threshold)
        {
            var result = _simMethods.ToDictionary(method => method.Key, y => new Dictionary<string, Dictionary<string, double>>());

            _simMethods.ToList().ForEach(methodWithFolderName =>
            {
                _projects.ForEach(project =>
                {
                    result[methodWithFolderName.Key].Add(project, ComputeMatrices(methodWithFolderName.Key, project, threshold));
                });
            });

            return result;
        }

        /// <summary>
        /// 获取 MRR 项目阈值方法结果
        /// </summary>
        /// <returns></returns>
        public MrrResult GetMrrProjectThresholdMethodResult()
        {
            const string dumpFilePath = Utility.ResultDumpsFolderPath + ResultDumpFileName;
            XmlSerializer xmlSerializer = new XmlSerializer(typeof(MrrResult));
            if (File.Exists(dumpFilePath))
                return (MrrResult)xmlSerializer.Deserialize(File.OpenRead(dumpFilePath));

            List<double> thresholds = new List<double>() { 0.0, 0.2, 0.4, 0.6, 0.8, 1 };
            MrrResult result = new MrrResult();

            _projects.ForEach(project =>
            {
                var projectResult = result.AddGet(project);
                _simMethods.ToList().ForEach(methodWithFolderName =>
                {
                    var methodResult = projectResult.AddGet(methodWithFolderName.Key);
                    thresholds.ForEach(threshold =>
                    {
                        var response = ComputeMatrices(methodWithFolderName.Key, project, threshold);
                        methodResult.AddGet(threshold, response["MRR"]);
                    });
                });
            });

            try
            {
                using (FileStream stream = File.OpenWrite(dumpFilePath))
                {
                    xmlSerializer.Serialize(stream, result);
                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e.Message);
            }
            return result;
        }

        /// <summary>
        /// 获取所有结果
        /// </summary>
        /// <param name="threshold"></param>
        /// <returns></returns>
        public Dictionary<string, Dictionary<string, Dictionary<string, double>>> GetAllResult(double threshold)
        {
            var result = _projects.ToDictionary(project => project, y => _simMethods.ToDictionary(methodWithKey => methodWithKey.Key, z => new Dictionary<string, double>()));

            _projects.ForEach(project =>
            {
                _simMethods.ToList().ForEach(methodWithFolderName =>
                {
                    result[project][methodWithFolderName.Key] = ComputeMatricesWithNumber(methodWithFolderName.Key, project, threshold);
                });
            });

            return result;
        }
        
        /// <summary>
        /// 计算矩阵值
        /// </summary>
        /// <param name="methodName">方法名</param>
        /// <param name="project">项目</param>
        /// <param name="threshold">阈值</param>
        /// <returns></returns>
        private Dictionary<string, double> ComputeMatrices(string methodName, string project, double threshold)
        {
            double top1MatchesForDataset = 0;
            double top5MatchesForDataset = 0;
            double top10MatchesForDataset = 0;
            double sumRr = 0.0;
            double sumAp = 0.0;
            // bugs 文件夹列表
            var bugs = new DirectoryInfo(Utility.ReportFolderPath + project).GetDirectories().Where(x => x.Name != "Corpus").ToList();
            int bugsCount = 0;
            bugs.ForEach(bugDirectoryInfo =>
            {
                // bug文件夹 bug结果文件夹
                string bugFolderPath = bugDirectoryInfo.FullName + @"\";
                string resultFolderPath = bugFolderPath + ResultFolderName;
                // LSI 和 非LSI 方法度量值的计算
                var metricValue = methodName == "LSI"
                    ? GetMetricsForBugLsi(resultFolderPath, bugFolderPath + RelevanceFileName, threshold)
                    : GetMetricsForBug(resultFolderPath + _simMethods[methodName], bugFolderPath + RelevanceFileName, threshold);

                top1MatchesForDataset += metricValue["Top1"];
                top5MatchesForDataset += metricValue["Top5"];
                top10MatchesForDataset += metricValue["Top10"];
                sumRr += metricValue["RR"];
                sumAp += metricValue["AP"];

                bugsCount++;
            });
            // 输出值
            Dictionary<string, double> output = new Dictionary<string, double>
            {
                {"Top1", top1MatchesForDataset/bugsCount*100},
                {"Top5", top5MatchesForDataset/bugsCount*100},
                {"Top10", top10MatchesForDataset/bugsCount*100},
                {"MAP", sumAp/bugsCount},
                {"MRR", sumRr/bugsCount}
            };
            return output;
        }
        
        /// <summary>
        /// 计算带数值的度量值
        /// </summary>
        /// <param name="methodName"></param>
        /// <param name="project"></param>
        /// <param name="threshold"></param>
        /// <returns></returns>
        private Dictionary<string, double> ComputeMatricesWithNumber(string methodName, string project, double threshold)
        {
            double top1MatchesForDataset = 0;
            double top5MatchesForDataset = 0;
            double top10MatchesForDataset = 0;
            double sumRr = 0.0;
            double sumAp = 0.0;

            var bugs = new DirectoryInfo(Utility.ReportFolderPath + project).GetDirectories().Where(x => x.Name != "Corpus").ToList();
            int bugsCount = 0;
            bugs.ForEach(bugDirectoryInfo =>
            {
                string bugFolderPath = bugDirectoryInfo.FullName + @"\";
                string resultFolderPath = bugFolderPath + ResultFolderName;
                var metricValue = methodName == "LSI"
                    ? GetMetricsForBugLsi(resultFolderPath, bugFolderPath + RelevanceFileName, threshold)
                    : GetMetricsForBug(resultFolderPath + _simMethods[methodName], bugFolderPath + RelevanceFileName, threshold);

                top1MatchesForDataset += metricValue["Top1"];
                top5MatchesForDataset += metricValue["Top5"];
                top10MatchesForDataset += metricValue["Top10"];
                sumRr += metricValue["RR"];
                sumAp += metricValue["AP"];

                bugsCount++;
            });

            Dictionary<string, double> output = new Dictionary<string, double>
            {
                {"Top1", top1MatchesForDataset/bugsCount*100},
                {"Top1Num", top1MatchesForDataset},
                {"Top5", top5MatchesForDataset/bugsCount*100},
                {"Top5Num", top5MatchesForDataset},
                {"Top10", top10MatchesForDataset/bugsCount*100},
                {"Top10Num", top10MatchesForDataset},
                {"MAP", sumAp/bugsCount},
                {"MRR", sumRr/bugsCount}
            };
            return output;
        }
        
        /// <summary>
        /// 获取 LSI 方法的度量值
        /// </summary>
        /// <param name="resultFolderPath">结果文件路径</param>
        /// <param name="relevanceFilePath">相关文件路径</param>
        /// <param name="threshold">阈值</param>
        /// <returns></returns>
        private Dictionary<string, double> GetMetricsForBugLsi(string resultFolderPath, string relevanceFilePath, double threshold)
        {
            // 如果best LSI 文件存在, 用通用方法计算LSI
            if (File.Exists(resultFolderPath + BestLsiFileName))
                return GetMetricsForBug(resultFolderPath + _simMethods["LSI"] + File.ReadAllText(resultFolderPath + BestLsiFileName) + ".txt", relevanceFilePath, threshold);
            
            // LSI方法的所有结果
            var allResults = new DirectoryInfo(resultFolderPath + _simMethods["LSI"]).GetFiles("*.txt").Select(kLsi => new { K = Path.GetFileNameWithoutExtension(kLsi.FullName), Metric = GetMetricsForBug(kLsi.FullName, relevanceFilePath, threshold) }).ToList();

            var maxAp = allResults.Max(x => x.Metric["RR"]);
            var obj = allResults.First(x => Math.Abs(x.Metric["RR"] - maxAp) <= Tolerance);

            File.WriteAllText(resultFolderPath + BestLsiFileName, obj.K);

            return obj.Metric;
        }

        /// <summary>
        /// 获取其他方法的度量值
        /// </summary>
        /// <param name="similarityFilePath">相似度文件路径</param>
        /// <param name="relevanceFilePath">相关文件路径</param>
        /// <param name="threshold">阈值</param>
        /// <returns></returns>
        private static Dictionary<string, double> GetMetricsForBug(string similarityFilePath, string relevanceFilePath, double threshold)
        {
            // 相似度列表 相关文件列表
            var similarityList = Utility.GetSimilarityList(similarityFilePath, threshold);
            var relevanceList = Utility.GetRelevanceList(relevanceFilePath);
            return GetMetricNumbers(similarityList, relevanceList);
        }

        /// <summary>
        /// 获取度量值数值
        /// </summary>
        /// <param name="similarityList">相似度列表</param>
        /// <param name="relevanceList">相关文件列表</param>
        /// <returns></returns>
        private static Dictionary<string, double> GetMetricNumbers(List<string> similarityList, List<string> relevanceList)
        {
            // top N
            var top1MatchesForBug = similarityList.Take(1).Any(top1 => relevanceList.Any(r => top1 == r)) ? 1 : 0;
            var top5MatchesForBug = similarityList.Take(5).Any(top5 => relevanceList.Any(r => top5 == r)) ? 1 : 0;
            var top10MatchesForBug = similarityList.Take(10).Any(top10 => relevanceList.Any(r => top10 == r)) ? 1 : 0;

            var indexedSimilarityList = similarityList.Select((value, index) => new { FileNum = value, Index = index }).ToList();

            // RR
            var firstMachedObject = indexedSimilarityList.FirstOrDefault(simFileWithIndex => relevanceList.Any(y => y == simFileWithIndex.FileNum));
            var rr = 1.0 / (firstMachedObject?.Index + 1) ?? 0;

            // AP
            double sumPrecision = 0.0;
            indexedSimilarityList.ForEach(simFileWithIndex =>
            {
                // If pos(i) is 0, it won't contribute to sum of AP
                if (relevanceList.All(relFile => relFile != simFileWithIndex.FileNum))
                    return;

                // calculate precision
                int matchAtThisPoint = relevanceList.Intersect(similarityList.Take(simFileWithIndex.Index + 1)).Count(); // how many match between rel file and sim file at this point
                double precision = (double)matchAtThisPoint / (simFileWithIndex.Index + 1); //prec is total match / total file found similar (at this point)
                sumPrecision += precision;
            });
            double ap = sumPrecision / relevanceList.Count;

            return new Dictionary<string, double>()
            {
                {"Top1", top1MatchesForBug},
                {"Top5", top5MatchesForBug},
                {"Top10", top10MatchesForBug},
                {"RR", rr},
                {"AP", ap}
            };
        }

        #endregion



        #region Individual Query Result

        // project -> query -> method -> metric
        public Dictionary<string, Dictionary<string, Dictionary<string, Dictionary<string, double>>>> IndividualQueryResult(double threshold)
        {
            var result = _projects.ToDictionary(project => project, y => new Dictionary<string, Dictionary<string, Dictionary<string, double>>>());

            _projects.ToList().ForEach(project =>
            {
                new DirectoryInfo(Utility.ReportFolderPath + project).GetDirectories().Where(x => x.Name != "Corpus").ToList().ForEach(bugFolderInfo =>
                {
                    result[project].Add(bugFolderInfo.Name, new Dictionary<string, Dictionary<string, double>>());
                    _simMethods.ToList().ForEach(methodWithFolderName =>
                    {
                        string bugFolderPath = bugFolderInfo.FullName + @"\";
                        string methodName = methodWithFolderName.Key;
                        string resultFolderPath = bugFolderPath + ResultFolderName;

                        var metricValue = methodName == "LSI"
                            ? GetMetricsForBugLsi(resultFolderPath, bugFolderPath + RelevanceFileName, threshold)
                            : GetMetricsForBug(resultFolderPath + _simMethods[methodName], bugFolderPath + RelevanceFileName, threshold);

                        result[project][bugFolderInfo.Name].Add(methodName, metricValue);
                    });
                });
            });

            return result;
        }

        #endregion



        #region LSI Number

        internal class LsiNumber
        {
            public int Count { get; set; }

            public List<double> Rrs { get; }

            public LsiNumber()
            {
                Rrs = new List<double>();
            }
        }

        public Dictionary<string, Dictionary<int, Tuple<double, double>>> GetLsiQueryNumber()
        {
            Dictionary<string, Dictionary<int, LsiNumber>> tempResult = new Dictionary<string, Dictionary<int, LsiNumber>>();
            _projects.ForEach(project =>
            {
                Dictionary<int, LsiNumber> record = new Dictionary<int, LsiNumber>();
                new DirectoryInfo(Utility.ReportFolderPath + project).GetDirectories().Where(x => x.Name != "Corpus").ToList().ForEach(bugFolderInfo =>
                {
                    string bugFolderPath = bugFolderInfo.FullName + @"\";

                    int bestLsi = Convert.ToInt32(File.ReadAllText(bugFolderInfo.FullName + @"\" + ResultFolderName + BestLsiFileName));
                    double rr = GetMetricsForBugLsi(bugFolderPath + ResultFolderName, bugFolderPath + RelevanceFileName, 1)["RR"];

                    if (!record.ContainsKey(bestLsi))
                    {
                        record.Add(bestLsi, new LsiNumber());
                    }

                    record[bestLsi].Count = record[bestLsi].Count + 1;
                    record[bestLsi].Rrs.Add(rr);
                });
                tempResult.Add(project, record);
            });

            Dictionary<string, Dictionary<int, Tuple<double, double>>> result = new Dictionary<string, Dictionary<int, Tuple<double, double>>>();
            tempResult.ToList().ForEach(r =>
            {
                int totalQueries = new DirectoryInfo(Utility.ReportFolderPath + r.Key).GetDirectories().Count(x => x.Name != "Corpus");
                var kValues = r.Value.ToDictionary(x => x.Key, y => new Tuple<double, double>((double)y.Value.Count / totalQueries, y.Value.Rrs.Average()));
                result.Add(r.Key, kValues);
            });

            return result;
        }

        public Dictionary<string, Dictionary<int, double>> GetLsiNumber()
        {
            Dictionary<string, Dictionary<int, List<double>>> tempResult = new Dictionary<string, Dictionary<int, List<double>>>();
            _projects.ForEach(project =>
            {
                Dictionary<int, List<double>> record = new Dictionary<int, List<double>>();
                new DirectoryInfo(Utility.ReportFolderPath + project).GetDirectories().Where(x => x.Name != "Corpus").ToList().ForEach(bugFolderInfo =>
                {
                    int bestLsi = Convert.ToInt32(File.ReadAllText(bugFolderInfo.FullName + @"\" + ResultFolderName + BestLsiFileName));
                    string bugFolderPath = bugFolderInfo.FullName + @"\";
                    double rr = GetMetricsForBugLsi(bugFolderPath + ResultFolderName, bugFolderPath + RelevanceFileName, 1)["RR"];
                    if (!record.ContainsKey(bestLsi))
                    {
                        record.Add(bestLsi, new List<double>());
                    }
                    record[bestLsi].Add(rr);
                });
                tempResult.Add(project, record);
            });

            Dictionary<string, Dictionary<int, double>> result = new Dictionary<string, Dictionary<int, double>>();
            tempResult.ToList().ForEach(r =>
            {
                var kValues = r.Value.ToDictionary(x => x.Key, y => y.Value.Average());
                result.Add(r.Key, kValues);
            });
            return result;
        }

        #endregion
    }
}
