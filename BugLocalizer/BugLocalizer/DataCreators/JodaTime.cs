using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using System.Xml.Linq;

namespace BugLocalizer.DataCreators
{
    public class JodaTime : BaseExecutable
    {
        private const string DataSetFolderName = @"JodaTime\";

        private const string SourceQueryTitleFileName = @"title.txt";
        private const string SourceQueryDescriptionFileName = @"description.txt";
        private const string SourceRelListRelativeFileName = @"corpus\RelList.txt";
        private const string SourceFileListRelativFileName = @"corpus\fileList.txt";

        private const string IncompleteErrorFileName = "Incomplete_Data_JodaTime.txt";
        private const string IncompleteRelevanceErrorFileName = "Incomplete_Relevance_Data_JodaTime.txt";

        private const string OutputCorpusFoldername = @"Corpus\";
        private const string OutputBugQueryFileName = @"BugQuery.txt";
        private const string OutputRelListFileName = @"RelList.txt";
        private const string OutputFileListFileName = @"FileList.txt";

        private const string CorpusExtractedRelativeFolderPath = @"corpus\temp\xml\";

        private const string XmlRootNodeName = "DOC";
        private const string XmlDocNumberNodeName = "DOCNO";
        private const string XmlTextNodeName = "TEXT";

        public override void Execute()
        {
            List<DirectoryInfo> bugs = new DirectoryInfo(Utility.DatasetFolderPath + Utility.MoreBugDatasetRelativeFolderPath + DataSetFolderName + "bugs").GetDirectories().ToList();

            int totalBugs = bugs.Count();
            int currentBugCount = 0;

            string outputFolderPath = Utility.ReportFolderPath + DataSetFolderName;
            Parallel.For(0, totalBugs, new ParallelOptions() { MaxDegreeOfParallelism = Utility.ParallelThreadCount }, i =>
            {
                var bug = bugs[i];
                try
                {
                    currentBugCount++;
                    Utility.Status("Updating Bug: " + bug.Name + " (" + currentBugCount + " of " + totalBugs + ")");
                    CreateDatasetForSingleBug(outputFolderPath, bug.FullName + @"\", bug.Name + @"\");
                    Utility.Status("DONE Bug: " + bug.Name + " (" + currentBugCount + " of " + totalBugs + ")");
                }
                catch (Exception e)
                {
                    if (e.Message == "Incomplete")
                    {
                        File.AppendAllText(Utility.ReportFolderPath + IncompleteErrorFileName, bug.Name + Environment.NewLine);
                    }
                    else if (e.Message == "Relevance")
                    {
                        File.AppendAllText(Utility.ReportFolderPath + IncompleteRelevanceErrorFileName, bug.Name + Environment.NewLine);
                    }
                    else
                    {
                        Utility.WriteErrorCommon("JodaTime " + bug.Name, e.Message);
                    }
                }
            });
        }

        private static void CreateDatasetForSingleBug(string outputFolderPath, string sourceBugFolderPath, string bugFolderName)
        {
            if (!CheckBugReportIsComplete(sourceBugFolderPath))
            {
                throw new Exception("Incomplete");
            }

            // create directory
            string outputBugFolderPath = outputFolderPath + bugFolderName;
            if (Directory.Exists(outputBugFolderPath))
                Directory.Delete(outputBugFolderPath, true);
            Directory.CreateDirectory(outputBugFolderPath);

            var query = TextWithFilter(File.ReadAllText(sourceBugFolderPath + SourceQueryTitleFileName) + " " + File.ReadAllText(sourceBugFolderPath + SourceQueryDescriptionFileName));

            // create query
            File.WriteAllLines(outputBugFolderPath + OutputBugQueryFileName, query);

            // copy all corpus
            string outputBugCorpusFolder = outputBugFolderPath + OutputCorpusFoldername;
            Directory.CreateDirectory(outputBugCorpusFolder);

            ///解压文件
            //Utility.unTAR(@"D:\test\xml.tgz", @"D:\test\");
            Utility.unTAR(sourceBugFolderPath + @"corpus\xml.tgz", sourceBugFolderPath + @"corpus\temp\");

            foreach (FileInfo xmlCorpusFile in new DirectoryInfo(sourceBugFolderPath + CorpusExtractedRelativeFolderPath).GetFiles("*.xml"))
            {
                // Read the xml document
                XDocument xmlDocument = XDocument.Load(xmlCorpusFile.FullName);
                var rootNode = xmlDocument.Element(XmlRootNodeName);
                if (rootNode == null)
                {
                    throw new ArgumentException("Xml file not in proper format");
                }

                XElement docNode = rootNode.Element(XmlDocNumberNodeName);
                XElement textNode = rootNode.Element(XmlTextNodeName);
                if (docNode != null && textNode != null)
                {
                    // Create source code
                    string text = textNode.Value;
                    string doc = docNode.Value.Trim();

                    File.WriteAllLines(outputBugCorpusFolder + doc + ".txt", TextWithFilter(text));
                }
            }

            // copy the relevant list and file list
            File.WriteAllLines(outputBugFolderPath + OutputRelListFileName, File.ReadAllLines(sourceBugFolderPath + SourceRelListRelativeFileName));
            File.WriteAllLines(outputBugFolderPath + OutputFileListFileName,
                File.ReadAllLines(sourceBugFolderPath + SourceFileListRelativFileName).Select(
                    x =>
                    {
                        string[] split = x.Split(new[] { " " }, StringSplitOptions.None);
                        return split[0] + " " + split[1].Trim(' ', '"');
                    }));

            // check relevance
            List<string> relevanceList = File.ReadAllLines(outputBugFolderPath + OutputRelListFileName).ToList();
            List<string> corpusFileNames = new DirectoryInfo(outputBugCorpusFolder).GetFiles("*.txt").Select(x => x.Name.Substring(0, x.Name.Length - 4)).ToList();
            if (relevanceList.Count == 0 || relevanceList.Any(x => !corpusFileNames.Contains(x)))
                throw new Exception("Relevance");
        }

        /// <summary>
        /// Title, Description, Relevant file list and corpus
        /// </summary>
        private static bool CheckBugReportIsComplete(string bugFolder)
        {
            return File.Exists(bugFolder + SourceQueryTitleFileName) &&
                   File.Exists(bugFolder + SourceQueryDescriptionFileName) &&
                   File.Exists(bugFolder + SourceRelListRelativeFileName) &&
                   File.Exists(bugFolder + SourceFileListRelativFileName);
        }
    }
}