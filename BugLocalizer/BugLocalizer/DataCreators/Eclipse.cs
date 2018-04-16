using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Xml.Linq;
using BugLocalizer.Models;

namespace BugLocalizer.DataCreators
{
    public class Eclipse : BaseExecutable
    {
        private const string DataSetFolderName = @"Eclipse\";     //数据集文件夹名称

        private const string OutputCorpusFoldername = @"Corpus\";  //输出语料库文件夹名称
        private const string OutputBugQueryFileName = @"BugQuery.txt";  //输出bug查询文件名称
        private const string OutputRelListFileName = @"RelList.txt";  //
        private const string OutputFileListFileName = @"FileList.txt";
        private const string EclipseQuerySourceFileName = @"EclipseBugRepository.xml";
        /// <summary>
        /// 为Eclipse项目建立数据集
        /// </summary>
        public override void Execute()
        {
            var eclipseDataSourceFolderPath = Utility.DatasetFolderPath + DataSetFolderName + @"Source\"; //数据源文件目录

            // Create file mapping since its hard to find
            MyListTDictionary<FileInfo> javaFiles = new MyListTDictionary<FileInfo>();
            GetFiles(new DirectoryInfo(eclipseDataSourceFolderPath)).ForEach(x => javaFiles.Add(x.Name.ToLowerInvariant(), x));

            Dictionary<string, EclipseRelevanceFile> allRelevanceFileToResolvePath = new Dictionary<string, EclipseRelevanceFile>();
            List<EclipseBug> allBugs = new List<EclipseBug>();

            ///解析XML文件提取bug信息
            // Create list of query and Relevance 
            XDocument xmlDocument = XDocument.Load(Utility.DatasetFolderPath + DataSetFolderName + EclipseQuerySourceFileName);
            var bugs = xmlDocument.Descendants("bug").ToList();
            int bugsCount = bugs.Count;
            int currentBugsCount = 0;
            foreach (var bug in bugs)
            {
                ++currentBugsCount;
                Utility.Status("Reading bug (" + currentBugsCount + " of " + bugsCount + ")");

                EclipseBug eclipseBug = new EclipseBug();

                // query
                var bugInformation = bug.Element("buginformation");
                eclipseBug.Summary = bugInformation.Element("summary").Value;
                eclipseBug.Description = bugInformation.Element("description").Value;

                // id
                eclipseBug.BugId = bug.Attribute("id").Value;

                // create relList
                var files = bug.Element("fixedFiles").Elements("file");
                foreach (var fileNode in files)
                {
                    eclipseBug.FixedFiles.Add(fileNode.Value.ToLowerInvariant());
                    if (!allRelevanceFileToResolvePath.ContainsKey(fileNode.Value.ToLowerInvariant()))
                        allRelevanceFileToResolvePath.Add(fileNode.Value.ToLowerInvariant(), new EclipseRelevanceFile(fileNode.Value));
                }

                allBugs.Add(eclipseBug);
            }

            // Match file name to get file path 
            // If file name try to match with folder name else throw and error
            Dictionary<string, List<FileInfo>> relevanceMappingDictionary = new Dictionary<string, List<FileInfo>>();
            foreach (var relevanceFileWithEclipseRelevanceObject in allRelevanceFileToResolvePath)
            {
                var matchingFile = ResolvePath(relevanceFileWithEclipseRelevanceObject.Value, javaFiles);
                relevanceMappingDictionary.Add(relevanceFileWithEclipseRelevanceObject.Key, matchingFile);
            }

            // Create relevance list
            Dictionary<FileInfo, int> allIndexedFiles = new Dictionary<FileInfo, int>();
            int counter = 1;
            foreach (var fileList in javaFiles.Values)
            {
                fileList.ForEach(x => allIndexedFiles.Add(x, counter++));
            }

            const string eclipseReportFolderPath = Utility.ReportFolderPath + DataSetFolderName;
            // Create a directory in eclipse saying Corpus
            if (Directory.Exists(eclipseReportFolderPath))
                Directory.Delete(eclipseReportFolderPath, true);
            Directory.CreateDirectory(eclipseReportFolderPath);

            const string eclipseCorpusFolderPath = eclipseReportFolderPath + OutputCorpusFoldername;
            Directory.CreateDirectory(eclipseCorpusFolderPath);

            int corpusCounter = 1;
            int totalCorpus = allIndexedFiles.Count;
            foreach (var fileWithIndex in allIndexedFiles)
            {
                File.WriteAllLines(eclipseCorpusFolderPath + fileWithIndex.Value + ".txt", TextWithFilter(File.ReadAllText(fileWithIndex.Key.FullName)));
                Console.WriteLine("Writing corpus " + corpusCounter + " of " + totalCorpus);
                ++corpusCounter;
            }

            File.WriteAllLines(eclipseReportFolderPath + OutputFileListFileName, allIndexedFiles.Select(x => x.Value + " " + x.Key.FullName.Substring(eclipseDataSourceFolderPath.Length)));

            // Create stuff 
            int bugCounter = 1;
            int totalBug = allBugs.Count;
            foreach (var bug in allBugs)
            {
                string bugFolderPath = eclipseReportFolderPath + bug.BugId + @"\";
                Directory.CreateDirectory(bugFolderPath);

                File.WriteAllLines(bugFolderPath + OutputBugQueryFileName, TextWithFilter(bug.Summary + " " + bug.Description));

                List<string> relevanceList = bug.FixedFiles.SelectMany(x => relevanceMappingDictionary[x].Select(y => allIndexedFiles[y].ToString())).Distinct().ToList();

                File.WriteAllLines(bugFolderPath + OutputRelListFileName, relevanceList);

                Console.WriteLine("Done writing bug " + bugCounter + " of " + totalBug);
                ++bugCounter;
            }
        }
        /// <summary>
        /// 获取指定目录下所有的java文件
        /// </summary>
        /// <param name="directory"></param>
        /// <returns></returns>
        private static List<FileInfo> GetFiles(DirectoryInfo directory)
        {
            List<FileInfo> result = directory.GetFiles("*.java").ToList();

            directory.GetDirectories().ToList().ForEach(x => result.AddRange(GetFiles(x)));

            return result;
        }

        private static List<FileInfo> ResolvePath(EclipseRelevanceFile eclipseRelevanceFile, MyListTDictionary<FileInfo> allJavaFiles)
        {
            string fileName = eclipseRelevanceFile.GetFileName();
            if (!allJavaFiles.ContainsKey(fileName))
                throw new FileNotFoundException(fileName + " in eclipseRelevanceFile was not found in allJavaFiles");

            if (allJavaFiles[fileName].Count == 1)
                return allJavaFiles[fileName];

            Dictionary<FileInfo, int> returnFileInfos = new Dictionary<FileInfo, int>();
            foreach (var fileInfo in allJavaFiles[fileName].ToList())
            {
                int currentMatchScore = eclipseRelevanceFile.GetMatchWithFile(fileInfo);
                returnFileInfos.Add(fileInfo, currentMatchScore);
            }

            int maxScore = returnFileInfos.Max(x => x.Value);
            List<FileInfo> results = returnFileInfos.Where(x => x.Value == maxScore && x.Value != 0).Select(x => x.Key).ToList();

            if (results.Count == 0)
                throw new InvalidDataException("Could not find file for " + eclipseRelevanceFile.GetFileName());

            return results;
        }
    }
    /// <summary>
    /// Eclipse的bug类定义
    /// BugId, Summary, Description, FixedFiles
    /// </summary>
    public class EclipseBug
    {
        public string BugId { get; set; }

        public string Summary { get; set; }

        public string Description { get; set; }

        public List<string> FixedFiles { get; set; }

        public EclipseBug()
        {
            FixedFiles = new List<string>();
        }
    }
    /// <summary>
    /// Eclipse 相关文件类
    /// </summary>
    public class EclipseRelevanceFile
    {
        private readonly string _fileNamePath;

        public EclipseRelevanceFile(string fileNamePath)
        {
            _fileNamePath = fileNamePath;
        }

        private string[] _splits;

        public string GetFileName()
        {
            if (_splits == null)
                _splits = _fileNamePath.Split(new[] { "." }, StringSplitOptions.RemoveEmptyEntries);

            int length = _splits.Length;
            return (_splits[length - 2] + "." + _splits[length - 1]).ToLowerInvariant();
        }

        public int GetMatchWithFile(FileInfo file)
        {
            if (_splits == null)
                _splits = _fileNamePath.Split(new[] { "." }, StringSplitOptions.RemoveEmptyEntries);

            string[] fileSplits = file.FullName.Split(new[] { ".", "\\" }, StringSplitOptions.RemoveEmptyEntries);

            int splitLength = _splits.Length;
            int fileSplitLength = fileSplits.Length;

            int match = 0;
            int counter = 1;

            do
            {
                if (_splits[splitLength - counter] == fileSplits[fileSplitLength - counter])
                    match++;
                counter++;
            } while (splitLength >= counter && fileSplitLength >= counter && _splits[splitLength - counter] == fileSplits[fileSplitLength - counter]);

            return match;
        }
    }
}