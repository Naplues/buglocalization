using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Xml.Linq;
using BugLocalizer.Models;

namespace BugLocalizer.DataCreators
{
    public class ZXing : BaseExecutable
    {
        private const string DataSetFolderName = @"ZXing\";

        private const string IncompleteRelevanceErrorFileName = "Incomplete_Relevance_Data_ZXing.txt";

        private const string OutputCorpusFoldername = @"Corpus\";
        private const string OutputBugQueryFileName = @"BugQuery.txt";
        private const string OutputRelListFileName = @"RelList.txt";
        private const string OutputFileListFileName = @"FileList.txt";

        private const string BugRepositoryRootNodeName = "bugrepository";
        private const string BugNodeName = "bug";
        private const string BugIdAttributeName = "id";
        private const string BugInformationNodeName = "buginformation";
        private const string SummaryNodeName = "summary";
        private const string DescriptionNodeName = "description";
        private const string FixedFilesNodeName = "fixedFiles";
        private const string FileNodeName = "file";

        private const string ZXingSourceDirectoryPath = Utility.DatasetFolderPath + @"zhou\source\ZXing-1.6\";
        private const string ZXingBugRepository = Utility.DatasetFolderPath + @"zhou\bugReports\ZXingBugRepository.xml";
        /// <summary>
        /// 为ZXing项目的bug报告建立数据集
        /// </summary>
        public override void Execute()
        {
            string outputFolderPath = Utility.ReportFolderPath + DataSetFolderName;

            // filter source 获取所有的源文件
            List<FileInfo> allSourceFile = GetAllSourceFile(ZXingSourceDirectoryPath, "*.java");

            // create file list
            Utility.Status("Creating File List");
            Dictionary<string, ZhouFileInfo> fileList = new Dictionary<string, ZhouFileInfo>();
            int fileIdCounter = 1;
            allSourceFile.ForEach(x =>
            {
                string fullPath = x.FullName;
                string fullRelativePath = ZXingFileFilter(fullPath);
                string fullRelativePathInRepository = fullRelativePath.Replace('\\', '.');

                fileList.Add(fullRelativePathInRepository, new ZhouFileInfo(fileIdCounter++, fullRelativePath, fullPath));
            });
            ///后来改的
            if (Directory.Exists(outputFolderPath))      //若目录存在则删除该目录及其子目录和文件，recursive为true
                Directory.Delete(outputFolderPath, true);
            Directory.CreateDirectory(outputFolderPath);  //创建指定路径的文件夹

            File.WriteAllLines(outputFolderPath + OutputFileListFileName, fileList.Select(x => x.Value.Id + " " + x.Value.RelativeFilePath));

            // create source files 创建语料库
            Utility.Status("Creating source file corpus");
            string outputCorpusFolderPath = outputFolderPath + OutputCorpusFoldername;
            Directory.CreateDirectory(outputCorpusFolderPath);
            fileList.Values.ToList().ForEach(z =>
            {
                var fileTextList = TextWithFilter(File.ReadAllText(z.FullPath));
                File.WriteAllLines(outputCorpusFolderPath + z.Id + ".txt", fileTextList);
            });

            // work on query 创建bug报告
            Utility.Status("Creatibg Individual Bug Report");
            XDocument xmlDocument = XDocument.Load(ZXingBugRepository);
            var rootNode = xmlDocument.Element(BugRepositoryRootNodeName);
            var bugs = rootNode.Elements(BugNodeName).ToList();
            int bugsCount = bugs.Count;
            int currentBugsCount = 0;
            foreach (var bug in bugs)
            {
                try
                {
                    // bug id
                    string id = bug.Attribute(BugIdAttributeName).Value;

                    ++currentBugsCount;
                    Utility.Status("Creating " + id + " (" + currentBugsCount + " of " + bugsCount + ")");

                    // create directory
                    string outputBugFolderPath = outputFolderPath + id + @"\";
                    Directory.CreateDirectory(outputBugFolderPath);

                    // query
                    var bugInformation = bug.Element(BugInformationNodeName);
                    var summary = TextWithFilter(bugInformation.Element(SummaryNodeName).Value);
                    var description = TextWithFilter(bugInformation.Element(DescriptionNodeName).Value);
                    File.WriteAllLines(outputBugFolderPath + OutputBugQueryFileName, summary.Union(description));

                    // create relList
                    List<string> relList = new List<string>();
                    var files = bug.Element(FixedFilesNodeName).Elements(FileNodeName);
                    foreach (var fileNode in files)
                    {
                        string repoFilePath = fileNode.Value;
                        relList.Add(fileList[repoFilePath].Id.ToString());
                    }
                    File.WriteAllLines(outputBugFolderPath + OutputRelListFileName, relList);

                    // check relevance
                    List<string> fileIdList = fileList.Select(x => x.Value.Id.ToString()).ToList();
                    if (relList.Count == 0 || relList.Any(x => !fileIdList.Contains(x)))
                        throw new Exception("Relevance");
                }
                catch (Exception e)
                {
                    if (e.Message == "Relevance")
                    {
                        File.AppendAllText(Utility.ReportFolderPath + IncompleteRelevanceErrorFileName, bug.Name + Environment.NewLine);
                    }
                    else
                    {
                        Utility.WriteErrorCommon("ZXing " + bug.Attribute(BugIdAttributeName).Value, e.Message);
                    }
                }
            }
        }
        /// <summary>
        /// 获取所有的源文件
        /// </summary>
        /// <param name="sourceDirectoryPath">源文件目录</param>
        /// <param name="searchString">后缀名</param>
        /// <returns></returns>
        private List<FileInfo> GetAllSourceFile(string sourceDirectoryPath, string searchString)
        {
            DirectoryInfo directoryInfo = new DirectoryInfo(sourceDirectoryPath);
            List<FileInfo> result = directoryInfo.GetFiles(searchString).ToList();
            directoryInfo.GetDirectories().ToList().ForEach(x => result.AddRange(GetAllSourceFile(x.FullName, searchString)));

            return result;
        }

        private string ZXingFileFilter(string fullFilePath)
        {
            return fullFilePath.Remove(0, fullFilePath.IndexOf(@"com\google\zxing", StringComparison.OrdinalIgnoreCase));
        }
    }
}
