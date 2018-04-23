using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace Viewer
{
    public partial class ResultViewer : Form
    {
        public ResultViewer()
        {
            InitializeComponent();

            //GenerateTopNGraph();     //TopN图  OK
            //GenerateMrrBarGraph(); //MRR柱状图  OK
            //GenerateMapBarGraph(); //MAP柱状图  OK

            GenerateTable();         //创建表  OK
            //QueryTextSimilarity();   //查询文本相似度  OK
            //GenerateTableAll();      //生成所有表  OK

            //GenerateLsiGraph();    //生成LSI图 OK
            //GenerateLsiQueryGraph(); //生成LSI查询图
            //IndividualQueryResult(); //单个查询结果
            //TextMatcher();           //文本匹配
            //TextMatcherAttempt2();

            ///Clipboard.SetText(TextBoxResult.Text);
        }


        #region Query/Source similarity

        private void QueryTextSimilarity()
        {
            Dictionary<string, Tuple<double, double>> result = new GraphCreators().GetQueryTextSimDictionary();

            StringBuilder builder = new StringBuilder();
            result.ToList().ForEach(projectWithResult =>
            {
                builder.AppendLine(projectWithResult.Key + ": " + projectWithResult.Value.Item1 + ", " + projectWithResult.Value.Item2);
            });

            TextBoxResult.Text = builder.ToString();
        }

        #endregion

        #region Text Matcher

        private void TextMatcher()
        {
            var result = new GraphCreators().TextMatcher();

            List<string> projectList = result.Keys.ToList();
            string projectNames = string.Join(", ", projectList);
            StringBuilder builder = new StringBuilder();

            builder.AppendLine(@"%%");
            builder.AppendLine(@"%%Text Match Match Tss Better");
            builder.AppendLine(@"%%");
            builder.AppendLine(@"\begin{figure*}");
            builder.AppendLine(@"\centering");

            new List<string>() { "VSM", "LSI", "JSM" }.ForEach(method =>
            {
                builder.AppendLine(@"\begin{tikzpicture}[xscale=0.5,yscale=0.5]");
                builder.AppendLine(@"\begin{axis}[xticklabel style={rotate=45}, boxplot/draw direction=y, title=TSS$>$" + method + ", xtick={1,2,3,4,5}, legend style={at={(0.5,-0.2)}, anchor=north, legend columns=-1}, xmin=0, xmax=6, /pgfplots/boxplot/box extend=0.35, xticklabels={" + projectNames + "}]");

                int projectCounter = 1;
                projectList.ForEach(project =>
                {
                    builder.Append(@"\addplot[boxplot prepared={" + GetBoxPlot(result[project][method]["TssBetter"].Values).GetLatex() + ", draw position=" + ((double)projectCounter - 0.2) + "}]");
                    builder.AppendLine(@"coordinates {};");
                    builder.Append(@"\addplot[boxplot prepared={" + GetBoxPlot(result[project][method]["OthBetter"].Values).GetLatex() + ", draw position=" + ((double)projectCounter + 0.2) + ",every box/.style={fill=gray}}]");
                    builder.AppendLine(@"coordinates {};");
                    projectCounter++;
                });

                builder.AppendLine(@"\end{axis}");
                builder.AppendLine(@"\end{tikzpicture}");
            });

            builder.AppendLine(@"\label{Stuff1}");
            builder.AppendLine(@"\end{figure*}");
            builder.AppendLine(@"%%");
            builder.AppendLine(@"%%End Text Matcher Tss Better");
            builder.AppendLine(@"%%");

            TextBoxResult.Text = builder.ToString();
        }

        private void TextMatcherAttempt2()
        {
            
            var result = new GraphCreators().TextMatcherAttempt2_CheckMatchForTopMostFile();

            //List<string> methods = new List<string>() { "VSM", "LSI", "JSM", "PMI", "NGD" };
            List<string> methods = new List<string>() { "VSM", "JSM", "PMI", "NGD" };

            string methodNames = string.Join(", ", methods);

            StringBuilder builder = new StringBuilder();
            
            builder.AppendLine(@"%%");
            builder.AppendLine(@"%%Text Match Match Tss Better");
            builder.AppendLine(@"%%");
            builder.AppendLine(@"\begin{figure}");
            builder.AppendLine(@"\centering");

            int projectCounter = 0;
            result.ToList().ForEach(projectWithResult =>
            {
                builder.AppendLine(@"\begin{tikzpicture}[xscale=1,yscale=1]");
                builder.AppendLine(@"\begin{axis}[xticklabel style={rotate=45}, boxplot/draw direction=y, title=" + projectWithResult.Key + ", xtick={1,2,3,4}, xticklabels={" + methodNames + "}]");

                methods.ForEach(method =>
                {
                    builder.AppendLine(@"\addplot[boxplot prepared={" + GetBoxPlot(result[projectWithResult.Key][method].Values).GetLatex() + "}]");
                    builder.AppendLine(@"coordinates {(" + projectCounter + ",0)};");
                    projectCounter++;
                });

                builder.AppendLine(@"\end{axis}");
                builder.AppendLine(@"\end{tikzpicture}");
            });

            builder.AppendLine(@"\end{figure}");
            builder.AppendLine(@"%%");
            builder.AppendLine(@"%%End Text Matcher Tss Better");
            builder.AppendLine(@"%%");

            
            TextBoxResult.Text = builder.ToString();
            
        }

        private static BoxPlot GetBoxPlot(IEnumerable<double> unsortedList)
        {
            List<double> list = unsortedList.OrderBy(x => x).ToList();

            var medianTuple = GetMedianForSortedList(list);
            var q1Tuple = GetMedianForSortedList(list.Take(medianTuple.Item1).ToList());
            var q3Tuple = GetMedianForSortedList(list.Skip(medianTuple.Item1 + (medianTuple.Item3 ? 0 : 1)).ToList());

            return new BoxPlot()
            {
                Minimum = list.Min(),
                FirstQuartile = q1Tuple.Item2,
                Median = medianTuple.Item2,
                ThirdQuartile = q3Tuple.Item2,
                Maximum = list.Max()
            };
        }

        public static Tuple<int, double, bool> GetMedianForSortedList(List<double> sortedList)
        {
            switch (sortedList.Count)
            {
                case 0:
                    return new Tuple<int, double, bool>(0, 0, true);
                case 1:
                    return new Tuple<int, double, bool>(0, sortedList.First(), true);
            }

            int size = sortedList.Count;
            int mid = size / 2;
            double median = (size % 2 == 0) ? (sortedList[mid] + sortedList[mid - 1]) / 2 : sortedList[mid];
            return new Tuple<int, double, bool>(mid, median, (size % 2 == 0));
        }

        #endregion

        #region All Result

        private void IndividualQueryResult()
        {
            //List<string> methods = new List<string>() { "VSM", "LSI", "JSM", "PMI", "NGD" };
            List<string> methods = new List<string>() { "VSM", "JSM", "PMI", "NGD" };

            var result = new GraphCreators().IndividualQueryResult(1);
            StringBuilder builder = new StringBuilder();

            result.ToList().ForEach(projectWithResult =>
            {
                builder.AppendLine("----------------------------------------------------------");
                builder.AppendLine(projectWithResult.Key);
                builder.AppendLine("----------------------------------------------------------");
                
                projectWithResult.Value.ToList().ForEach(bugWithResult =>
                {
                    builder.Append(bugWithResult.Key + ", ");
                    methods.ForEach(method =>
                    {
                        builder.Append(bugWithResult.Value[method]["AP"] + ",");
                        builder.Append(bugWithResult.Value[method]["RR"] + ",");
                        builder.Append(bugWithResult.Value[method]["Top1"] + ",");
                        builder.Append(bugWithResult.Value[method]["Top5"] + ",");
                        builder.Append(bugWithResult.Value[method]["Top10"] + ",");
                    });
                    builder.AppendLine();
                });
            });

            TextBoxResult.Text = builder.ToString();
        }

        #endregion

        #region MAP

        private void GenerateMapBarGraph()
        {
            var result = new GraphCreators().GetMethodProjectMetricResult(1);
            
            List<string> additionalContentList = new List<string>()
            {
                "[pattern=north west lines]",
                "[pattern=north east lines]",
                "",
                "[fill=white]",
                "[fill=black]",
                "[fill=green]",
            };

            string projectNames = string.Join(", ", result.ToList().First().Value.ToList().Select(x => x.Key.FilterName()));

            StringBuilder builder = new StringBuilder();
            builder.AppendLine(@"%%");
            builder.AppendLine(@"%%MAP Bug Localization");
            builder.AppendLine(@"%%");
            builder.AppendLine(@"\begin{figure}[t]");
            builder.AppendLine(@"\centering");
            builder.AppendLine(@"\begin{tikzpicture}[xscale=0.9,yscale=0.9]");
            builder.AppendLine(@"\selectcolormodel{gray}");
            builder.AppendLine(@"\begin{axis}[x tick label style={/pgf/number format/1000 sep=}, symbolic x coords={" + projectNames + "}, xtick={" + projectNames + "}, enlargelimits=0.15, ymax=1, legend style={at={(1,1)}, anchor=north east, legend columns=-1}, ybar, bar width=4pt, ymajorgrids=true, xticklabel style={rotate=45}]");

            int counter = 0;
            result.ToList().ForEach(methodWithProjectResult =>
            {
                builder.Append(@"\addplot" + additionalContentList[counter++] + " coordinates {");
                methodWithProjectResult.Value.ToList().ForEach(projectWithResult =>
                {
                    builder.Append(@"(");
                    builder.Append(projectWithResult.Key.FilterName());
                    builder.Append(@",");
                    builder.Append(projectWithResult.Value["MAP"].ToString("####.00"));
                    builder.Append(@")");
                });
                builder.AppendLine(@"};");
            });

            string legends = string.Join(", ", result.ToList().Select(x => GetGraphName(x.Key.FilterName())));
            builder.AppendLine(@"\legend{" + legends + "}");
            builder.AppendLine(@"\end{axis}");
            builder.AppendLine(@"\end{tikzpicture}");
            builder.AppendLine(@"\caption{The performance measures of the TSS methods and the baseline methods in terms of MAP on bug localization datasets}");
            builder.AppendLine(@"\label{MapBarGraph}");
            builder.AppendLine(@"\end{figure}");
            builder.AppendLine(@"%%");
            builder.AppendLine(@"%%End MAP Bug Localization");
            builder.AppendLine(@"%%");

            TextBoxResult.Text = builder.ToString();
        }

        #endregion

        #region LSI Number

        private void GenerateLsiGraph()
        {
            var result = new GraphCreators().GetLsiNumber();

            List<string> linePatterns = new List<string>()
            {
                "densely dotted",
                "loosely dotted",
                "solid",
                "dashed",
                "dash pattern=on 3pt off 6pt on 6pt off 6pt",
                "dash pattern=on 2pt off 4pt on 4pt off 4pt",
                "dash pattern=on 2pt off 1pt on 5pt off 3pt",
            };

            List<string> colors = new List<string>() { "orange", "black", "green", "blue", "red" };

            StringBuilder builder = new StringBuilder();
            builder.AppendLine(@"%%");
            builder.AppendLine(@"%%LSI K");
            builder.AppendLine(@"%%");
            builder.AppendLine(@"\begin{figure}");
            builder.AppendLine(@"\centering");
            builder.AppendLine(@"\begin{tikzpicture}[xscale=1,yscale=1]");
            builder.AppendLine(@"\begin{axis}[xmin=0, xmax=900, ymin=0, xmajorgrids, ymajorgrids, xlabel=k, ylabel=Average MRR]");

            int lineCounter = 0;
            result.ToList().ForEach(datasetResult =>
            {
                builder.AppendLine(@"\addplot[smooth,color=" + colors[lineCounter] + "," + linePatterns[lineCounter++] + "]");
                builder.Append(@"coordinates{");
                datasetResult.Value.OrderBy(k => k.Key).ToList().ForEach(kWithCount =>
                {
                    builder.Append("(");
                    builder.Append(kWithCount.Key); // k
                    builder.Append(",");
                    builder.Append((kWithCount.Value).ToString("0.000")); // value
                    builder.Append(")");
                });
                builder.AppendLine(@"};");
                builder.AppendLine(@"\addlegendentry{" + GetGraphName(datasetResult.Key) + "};");
            });
            builder.AppendLine(@"\end{axis}");
            builder.AppendLine(@"\end{tikzpicture}");
            builder.AppendLine(@"\caption{Best average MRR of queries with corresponding value of k for LSI}");
            builder.AppendLine(@"\label{LsiK}");
            builder.AppendLine(@"\end{figure}");
            builder.AppendLine(@"%%");
            builder.AppendLine(@"%%LSI K");
            builder.AppendLine(@"%%");

            TextBoxResult.Text = builder.ToString();
        }

        private void GenerateLsiQueryGraph()
        {
            var result = new GraphCreators().GetLsiQueryNumber();

            List<string> linePatterns = new List<string>()
            {
                "densely dotted",
                "loosely dotted",
                "solid",
                "dashed",
                "dash pattern=on 3pt off 6pt on 6pt off 6pt",
                "dash pattern=on 2pt off 4pt on 4pt off 4pt",
                "dash pattern=on 2pt off 1pt on 5pt off 3pt",
            };

            List<string> colors = new List<string>() { "orange", "black", "green", "blue", "red" };

            StringBuilder builder = new StringBuilder();
            builder.AppendLine(@"%%");
            builder.AppendLine(@"%%LSI K");
            builder.AppendLine(@"%%");
            builder.AppendLine(@"\begin{figure}[t]");
            builder.AppendLine(@"\centering");
            builder.AppendLine(@"\begin{tikzpicture}[xscale=1,yscale=1]");
            builder.AppendLine(@"\begin{axis}[xmin=0, xmax=900, ymin=0, xmajorgrids, ymajorgrids, xlabel=$k$, ylabel=\% of queries]");

            int lineCounter = 0;
            result.ToList().ForEach(datasetResult =>
            {
                builder.AppendLine(@"\addplot[smooth,color=" + colors[lineCounter] + "," + linePatterns[lineCounter] + "]");
                builder.Append(@"coordinates{");
                builder.Append("(0,0)");
                datasetResult.Value.OrderBy(k => k.Key).ToList().ForEach(kWithCount =>
                {
                    builder.Append("(");
                    builder.Append(kWithCount.Key); // k
                    builder.Append(",");
                    builder.Append((kWithCount.Value.Item1).ToString("0.000")); // value
                    builder.Append(")");
                });
                builder.AppendLine(@"};");
                builder.AppendLine(@"\addlegendentry{" + GetGraphName(datasetResult.Key) + "};");
                int maxXAxis = datasetResult.Value.ToList().Where(x => x.Value.Item1 == datasetResult.Value.ToList().Max(y => y.Value.Item1)).Select(x => x.Key).Single();
                string rrAtMaxXAxis = datasetResult.Value[maxXAxis].Item2.ToString("0.00");
                string yAxis = datasetResult.Value[maxXAxis].Item1.ToString("00.00");
                builder.AppendLine(@"\node[label=right:\textcolor{" + colors[lineCounter] + "}{" + rrAtMaxXAxis + "},circle,fill,inner sep=1pt, " + colors[lineCounter] + "] at (axis cs:" + maxXAxis + "," + yAxis + ") {};");
                builder.AppendLine();
                lineCounter++;
            });
            builder.AppendLine(@"\end{axis}");
            builder.AppendLine(@"\end{tikzpicture}");
            builder.AppendLine(@"\caption{The percentage of queries for which LSI achieves highest MRR for the corresponding value of $k$. Each node represent the average of the best values of MRR achieved at that $k$.}");
            builder.AppendLine(@"\label{LsiK}");
            builder.AppendLine(@"\end{figure}");
            builder.AppendLine(@"%%");
            builder.AppendLine(@"%%LSI K");
            builder.AppendLine(@"%%");

            TextBoxResult.Text = builder.ToString();
        }

        #endregion

        #region MRR

        private void GenerateMrrBarGraph()
        {
            var result = new GraphCreators().GetMethodProjectMetricResult(1);

            List<string> additionalContentList = new List<string>()
            {
                "[pattern=north west lines]",
                "[pattern=north east lines]",
                "",
                "[fill=white]",
                "[fill=black]"
            };

            string projectNames = string.Join(", ", result.ToList().First().Value.ToList().Select(x => x.Key.FilterName()));

            StringBuilder builder = new StringBuilder();
            builder.AppendLine(@"%%");
            builder.AppendLine(@"%%MRR Bug Localization");
            builder.AppendLine(@"%%");
            builder.AppendLine(@"\begin{figure}[t]");
            builder.AppendLine(@"\centering");
            builder.AppendLine(@"\begin{tikzpicture}[xscale=0.9,yscale=0.9]");
            builder.AppendLine(@"\selectcolormodel{gray}");
            builder.AppendLine(@"\begin{axis}[x tick label style={/pgf/number format/1000 sep=}, symbolic x coords={" + projectNames + "}, xtick={" + projectNames + "}, enlargelimits=0.15, ymax=1, legend style={at={(1,1)}, anchor=north east, legend columns=-1}, ybar, bar width=4pt, ymajorgrids=true, xticklabel style={rotate=45}]");

            int counter = 0;
            result.ToList().ForEach(methodWithProjectResult =>
            {
                builder.Append(@"\addplot" + additionalContentList[counter++] + " coordinates {");
                methodWithProjectResult.Value.ToList().ForEach(projectWithResult =>
                {
                    builder.Append(@"(");
                    builder.Append(projectWithResult.Key.FilterName());
                    builder.Append(@",");
                    builder.Append(projectWithResult.Value["MRR"].ToString("####.00"));
                    builder.Append(@")");
                });
                builder.AppendLine(@"};");
            });

            string legends = string.Join(", ", result.ToList().Select(x => GetGraphName(x.Key.FilterName())));
            builder.AppendLine(@"\legend{" + legends + "}");
            builder.AppendLine(@"\end{axis}");
            builder.AppendLine(@"\end{tikzpicture}");
            builder.AppendLine(@"\caption{The performance measures of the TSS methods and the baseline methods in terms of MRR on bug localization datasets}");
            builder.AppendLine(@"\label{MrrBarGraph}");
            builder.AppendLine(@"\end{figure}");
            builder.AppendLine(@"%%");
            builder.AppendLine(@"%%End MRR Bug Localization");
            builder.AppendLine(@"%%");

            TextBoxResult.Text = builder.ToString();
        }

        #endregion

        #region Top N

        private void GenerateTopNGraph()
        {
            var result = new GraphCreators().GetMethodProjectMetricResult(0.7);

            List<string> additionalContentList = new List<string>()
            {
                "[pattern=north west lines]",
                "[pattern=north east lines]",
                "",
                "[fill=white]",
                "[fill=black]"
            };

            string projectNames = string.Join(", ", result.ToList().First().Value.ToList().Select(x => x.Key.FilterName()));

            List<string> metricName = new List<string>() { "Top1", "Top5", "Top10" };
            string legends = string.Join(", ", result.ToList().Select(x => GetGraphName(x.Key.FilterName())));

            StringBuilder builder = new StringBuilder();
            builder.AppendLine("%%");
            builder.AppendLine("%%Begin Top N Graph Bug Localization");
            builder.AppendLine("%%");
            builder.AppendLine(@"\begin{figure*}");
            builder.AppendLine(@"\selectcolormodel{gray}");
            builder.AppendLine(@"\centering");

            metricName.ForEach(metric =>
            {
                builder.AppendLine(@"\begin{tikzpicture}[xscale=0.7,yscale=0.7]");
                builder.AppendLine(@"\begin{axis}[title=" + GetGraphName(metric) + ", x tick label style={/pgf/number format/1000 sep=}, symbolic x coords={" + projectNames + "}, xtick={" + projectNames + "}, enlargelimits=0.15, legend style={at={(1,1)}, anchor=north east, legend columns=-1}, ybar, ymax=100, bar width=4pt, ymajorgrids=true, xticklabel style={rotate=45}]");

                int counter = 0;
                result.ToList().ForEach(projectWithMethodResult =>
                {
                    builder.Append(@"\addplot" + additionalContentList[counter++] + " coordinates {");
                    projectWithMethodResult.Value.ToList().ForEach(methodWithResult =>
                    {
                        builder.Append(@"(");
                        builder.Append(methodWithResult.Key.FilterName());
                        builder.Append(@",");
                        builder.Append(methodWithResult.Value[metric].ToString("####.00"));
                        builder.Append(@")");
                    });
                    builder.AppendLine(@"};");
                });

                builder.AppendLine(@"\legend{" + legends + "}");
                builder.AppendLine(@"\end{axis}");
                builder.AppendLine(@"\end{tikzpicture}");
            });

            builder.AppendLine(@"\caption{The performance measures of the TSS methods and the baseline methods in terms of TR\textsubscript{N} on bug localization datasets}");
            builder.AppendLine(@"\label{TopNBarGraph}");
            builder.AppendLine(@"\end{figure*}");
            builder.AppendLine("%%");
            builder.AppendLine("%%End Top N Graph Bug Localization");
            builder.AppendLine("%%");
            TextBoxResult.Text = builder.ToString();
        }

        #endregion

        #region Table

        private void GenerateTable()
        {
            var result = new GraphCreators().GetMrrProjectThresholdMethodResult();
            var thresholds = new List<double>() { 0.2, 0.4, 0.6, 0.8, 1 };

            StringBuilder builder = new StringBuilder();
            builder.AppendLine(@"\begin{table*}");
            builder.AppendLine(@"\renewcommand{\arraystretch}{1.1}");
            builder.AppendLine(@"\centering");
            builder.AppendLine(@"\caption{The performance measures of the TSS methods and the baseline methods on bug localization datasets in terms of MRR at different threshold levels}");
            builder.AppendLine(@"\begin{tabular}{c|c|ccccc}");
            builder.AppendLine(@"\bottomrule");
            builder.AppendLine(@"\multirow{2}{*}{\bfseries System} & \multirow{2}{*}{\bfseries Method} & \multicolumn{5}{*}{\bfseries Threshold} \\");
            builder.AppendLine(@"& & 0.2 & 0.4 & 0.6 & 0.8 & 1.0 \\");
            builder.AppendLine(@"\hline");

            result.Values.ForEach(projectMethodThreholdResult =>
            {
                builder.Append(@"\multirow{4}{*}{\textbf{" + projectMethodThreholdResult.ProjectName.FilterName() + "}} ");
                projectMethodThreholdResult.Values.ForEach(methodThresholdResult =>
                {
                    builder.Append(@"& " + methodThresholdResult.MethodName.FilterName());
                    thresholds.ForEach(threshold =>
                    {
                        builder.Append(@" & " + methodThresholdResult.Get(threshold).Value.ToString("0.000"));
                    });

                    builder.AppendLine(@"\\");
                    builder.AppendLine(@"\cline{2-7}");
                });

                builder.AppendLine(@"\hline");
            });

            builder.AppendLine(@"\end{tabular}");
            builder.AppendLine(@"\label{MrrBugLocalization}");
            builder.AppendLine(@"\end{table*}");

            TextBoxResult.Text = builder.ToString();
        }

        private void GenerateTableAll()
        {
            var result = new GraphCreators().GetAllResult(0.7);

            var resultKeys = new List<string>() { "Top1", "Top5", "Top10", "MRR", "MAP" };

            StringBuilder builder = new StringBuilder();
            builder.AppendLine(@"\begin{table}[t]");
            builder.AppendLine(@"\renewcommand{\arraystretch}{2}");
            builder.AppendLine(@"\centering");
            builder.AppendLine(@"\resizebox{\linewidth}{!}{");
            builder.AppendLine(@"\begin{tabular}{c|c|ccccc}");
            builder.AppendLine(@"\bottomrule");
            builder.AppendLine(@"\bfseries System & \bfseries Method & \bfseries Top\textsubscript{1} & \bfseries Top\textsubscript{5} & \bfseries Top\textsubscript{10} & MRR & MAP \\");
            builder.AppendLine(@"\hline");

            result.ToList().ForEach(projectWithResult =>
            {
                builder.Append(@"\multirow{5}{*}{\bfseries " + projectWithResult.Key.FilterName() + "} ");
                projectWithResult.Value.ToList().ForEach(methodWithResult =>
                {
                    builder.Append(@"& " + methodWithResult.Key.FilterName());
                    resultKeys.ForEach(resultKey =>
                    {
                        if (resultKey == "MRR" || resultKey == "MAP")
                        {
                            builder.Append(@" & " + methodWithResult.Value[resultKey].ToString("00.00"));
                        }
                        else
                        {
                            builder.Append(@" & \makecell{" + methodWithResult.Value[resultKey + "Num"] + @"\\ (" + methodWithResult.Value[resultKey].ToString("00.00") + @"\%)}");
                        }
                    });

                    builder.AppendLine(@"\\");
                    if (methodWithResult.Key != "NGD")
                    {
                        builder.AppendLine(@"\cline{2-7}");
                    }
                });

                builder.AppendLine(@"\Xhline{1pt}");
            });

            builder.AppendLine(@"\end{tabular}}");
            builder.AppendLine(@"\caption{The performance measures of the TSS methods and the baseline methods on bug localization datasets at 0.7 threshold}");
            builder.AppendLine(@"\label{ResultsBugLocalization}");
            builder.AppendLine(@"\end{table}");

            TextBoxResult.Text = builder.ToString();
        }

        #endregion Table

        private static string GetGraphName(string methodName)
        {
            switch (methodName)
            {
                case "PMI":
                    return @"TSS\textsubscript{PMI}";
                case "NGD":
                    return @"TSS\textsubscript{NGD}";
                case "Top1":
                    return @"TR\textsubscript{1}";
                case "Top5":
                    return @"TR\textsubscript{5}";
                case "Top10":
                    return @"TR\textsubscript{10}";

                default:
                    return methodName;
            }
        }
    }
}
