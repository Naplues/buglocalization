using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;

namespace BugLocalizer.Calculators
{
    public class ZXing : BaseExecutable
    {

        #region Const

        // 各方法运行配置
        private static bool _cleanPrevious;
        private static bool _runVsm;
        private static bool _runLsi;
        private static bool _runNgd;
        private static bool _runSim;
        private static bool _runJen;
        private static bool _runAPm;


        public override void Execute()
        {
            _cleanPrevious = Utility.CleanPrevious;
            _runVsm = Utility.RunVsm;
            _runLsi = Utility.RunLsi;
            _runNgd = Utility.RunNgd;
            _runSim = Utility.RunSim;
            _runJen = Utility.RunJen;
            _runAPm = Utility.RunAPm;

            RunTssOnDataset(@"ZXing\");
        }

        public static object MyObj1 = new object();

        private static void RunTssOnDataset(string dataset)
        {
            // 数据集文件夹路径
            string datasetFolderPath = Utility.ReportFolderPath + dataset;
            // 各方法完成情况文件路径
            string vsmCompletedFilePath = datasetFolderPath + Method.VsmCompletedFile;
            string lsiCompletedFilePath = datasetFolderPath + Method.LsiCompletedFile;
            string ngdCompletedFilePath = datasetFolderPath + Method.NgdCompletedFile;
            string simCompletedFilePath = datasetFolderPath + Method.SimCompletedFile;
            string jenCompletedFilePath = datasetFolderPath + Method.JenCompletedFile;
            string aPmCompletedFilePath = datasetFolderPath + Method.APmCompletedFile;
            // 根据配置创建相应的方法完成情况文件
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

            // 读取各方法完成情况列表
            List<string> completedVsm = _runVsm ? File.ReadAllLines(vsmCompletedFilePath).ToList() : new List<string>();
            List<string> completedLsi = _runLsi ? File.ReadAllLines(lsiCompletedFilePath).ToList() : new List<string>();
            List<string> completedNgd = _runNgd ? File.ReadAllLines(ngdCompletedFilePath).ToList() : new List<string>();
            List<string> completedSim = _runSim ? File.ReadAllLines(simCompletedFilePath).ToList() : new List<string>();
            List<string> completedJen = _runJen ? File.ReadAllLines(jenCompletedFilePath).ToList() : new List<string>();
            List<string> completedAPm = _runAPm ? File.ReadAllLines(aPmCompletedFilePath).ToList() : new List<string>();

            // 获取数据集中每个bug的目录
            List<DirectoryInfo> bugs = new DirectoryInfo(datasetFolderPath).GetDirectories().Where(x => x.Name != "Corpus").ToList();
            // bug总数目
            int totalbugsCount = bugs.Count;
            
            /// 提取file-token并进行初始化
            Utility.Status("Reading sources and Initiailizing");

            // 读取Corpus中所有处理后源文件并且制作file-token索引
            foreach (var file in new DirectoryInfo(datasetFolderPath + Method.CorpusWithFilterFolderName).GetFiles())
            {
                string[] text = File.ReadAllLines(file.FullName);
                Method.CodeFilesWithContent.Add(Path.GetFileNameWithoutExtension(file.FullName), text.ToList());
            }

            // 对各项配置进行初始化工作
            if (_runVsm || _runSim || _runAPm || _runLsi || _runJen)
                ClassicalMethod.InitializeForVsmSimLsi();

            if (_runNgd || _runAPm || _runSim)
                ProposedMethod.InitializeForNgdPmiSim();

            if (_runLsi)
                ClassicalMethod.DoSvd();

            Utility.Status("DONE Reading sources and Initiailizing");


            // Create files
            // 完成情况计数
            int completedCount = 0;
            //并行处理
            Parallel.For(0, totalbugsCount, new ParallelOptions() { MaxDegreeOfParallelism = Utility.ParallelThreadCount }, i =>
            {
                ++completedCount;
                try
                {
                    Utility.Status("Creating Stuff: " + bugs[i].Name + " " + completedCount + " of " + totalbugsCount);
                    string bugFolderPath = datasetFolderPath + bugs[i].Name + @"\";
                    // 创建结果文件夹
                    if (!Directory.Exists(bugFolderPath + "Results"))
                        Directory.CreateDirectory(bugFolderPath + "Results");

                    // 提取查询文本
                    List<string> queryText = File.ReadAllLines(bugFolderPath + Method.QueryWithFilterFileName).ToList();

                    //按照配置执行各个方法，并且在记录上加入执行成功的bug

                    if (_runVsm && !completedVsm.Contains(bugs[i].Name))
                    {
                        ClassicalMethod.ComputeVsm(bugFolderPath, bugs[i].Name, queryText);
                        completedVsm.Add(bugs[i].Name);
                    }

                    if (_runLsi && !completedLsi.Contains(bugs[i].Name))
                    {
                        if (!Directory.Exists(bugFolderPath + Method.LsiOutputFolderName))
                            Directory.CreateDirectory(bugFolderPath + Method.LsiOutputFolderName);

                        ClassicalMethod.ComputeLsi(bugFolderPath, bugs[i].Name, queryText);
                        completedLsi.Add(bugs[i].Name);
                    }

                    if (_runJen && !completedJen.Contains(bugs[i].Name))
                    {
                        ClassicalMethod.ComputeJen(bugFolderPath, bugs[i].Name, queryText);
                        completedJen.Add(bugs[i].Name);
                    }

                    if (_runNgd && !completedNgd.Contains(bugs[i].Name))
                    {
                        ProposedMethod.ComputeNgd(bugFolderPath, bugs[i].Name, queryText);
                        completedNgd.Add(bugs[i].Name);
                    }

                    if (_runSim && !completedSim.Contains(bugs[i].Name))
                    {
                        ProposedMethod.ComputePmiSim(bugFolderPath, bugs[i].Name, queryText);
                        completedSim.Add(bugs[i].Name);
                    }

                    if (_runAPm && !completedAPm.Contains(bugs[i].Name))
                    {
                        ProposedMethod.ComputeAPm(bugFolderPath, bugs[i].Name, queryText);
                        completedAPm.Add(bugs[i].Name);
                    }

                    Utility.Status("DONE Creating Stuff: " + bugs[i].Name + " (" + completedCount + " of " + totalbugsCount + ")");
                }
                catch (Exception e)
                {
                    // 记录出错信息
                    Utility.WriteErrorCommon(dataset + bugs[i].Name, e.Message);
                    Utility.Status("ERROR Creating Stuff: " + dataset + bugs[i].Name + " (" + completedCount + " of " + totalbugsCount + ")");
                }
                finally
                {
                    // 将完成情况记录在文件上
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

    }
}
