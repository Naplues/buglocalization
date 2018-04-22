using System;
using System.IO;
using System.Linq;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace BugLocalizer.Calculators
{
    public class Eclipse : BaseExecutable
    {
        #region Const
        //运行配置
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

            RunTssOnDataset(@"Eclipse\");
        }
        //互斥操作加锁对象
        public static object MyObj1 = new object();
        private static void RunTssOnDataset(string dataset)
        {
            string datasetFolderPath = Utility.ReportFolderPath + dataset;

            string vsmCompletedFilePath = datasetFolderPath + Method.VsmCompletedFile;
            string lsiCompletedFilePath = datasetFolderPath + Method.LsiCompletedFile;
            string ngdCompletedFilePath = datasetFolderPath + Method.NgdCompletedFile;
            string simCompletedFilePath = datasetFolderPath + Method.SimCompletedFile;
            string jenCompletedFilePath = datasetFolderPath + Method.JenCompletedFile;
            string aPmCompletedFilePath = datasetFolderPath + Method.APmCompletedFile;

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

            Utility.Status("Reading Files");
            var allFiles = new DirectoryInfo(datasetFolderPath + Method.CorpusWithFilterFolderName).GetFiles();
            int counter = 1;
            // Read all files
            foreach (var file in allFiles)
            {
                Utility.Status("Reading " + counter++ + " of " + allFiles.Length);
                string[] text = File.ReadAllLines(file.FullName);
                Method.CodeFilesWithContent.Add(Path.GetFileNameWithoutExtension(file.FullName), text.ToList());
            }

            Utility.Status("Initializing");

            // 对各项配置进行初始化工作
            if (_runVsm || _runSim || _runAPm || _runLsi || _runJen)
                ClassicalMethod.InitializeForVsmSimLsi();

            if (_runLsi)
                ClassicalMethod.DoSvd();

            if (_runNgd || _runAPm || _runSim)
                ProposedMethod.InitializeForNgdPmiSim();

            // Create files
            int completedCount = 0;
            Parallel.For(0, totalbugsCount, new ParallelOptions() { MaxDegreeOfParallelism = Utility.ParallelThreadCount }, i =>
            {
                ++completedCount;
                try
                {
                    Utility.Status("Creating Stuffs: " + bugs[i].Name + " " + completedCount + " of " + totalbugsCount);

                    if (!((_runVsm && !completedVsm.Contains(bugs[i].Name)) || (_runAPm && !completedAPm.Contains(bugs[i].Name)) || (_runLsi && !completedLsi.Contains(bugs[i].Name)) || (_runNgd && !completedNgd.Contains(bugs[i].Name)) || (_runSim && !completedSim.Contains(bugs[i].Name)) || (_runJen && !completedJen.Contains(bugs[i].Name))))
                    {
                        Utility.Status("Already Completed Stuff: " + bugs[i].Name + " " + completedCount + " of " + totalbugsCount);
                        return;
                    }

                    string bugFolderPath = datasetFolderPath + bugs[i].Name + @"\";

                    if (!Directory.Exists(bugFolderPath + "Results"))
                        Directory.CreateDirectory(bugFolderPath + "Results");

                    List<string> queryText = File.ReadAllLines(bugFolderPath + Method.QueryWithFilterFileName).ToList();

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

    }
}
