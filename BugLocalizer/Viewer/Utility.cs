using System;
using System.IO;
using System.Linq;
using System.Collections.Generic;
using System.Diagnostics;

namespace Viewer
{
    public static class Utility
    {
        public const string ReportFolderPath = @"D:\Research-Dataset\Bug\Report\";            //根目录文件夹
        public const string CorpusFolderName = @"Corpus\";                                    //语料库文件夹
        public const string ResultDumpsFolderPath = @"D:\Research-Dataset\Bug\ResultDumps\";  //结果文件夹

        public const int ParallelThreadCount = 1;  //并行线程数

        public static void Status(string text)
        {
            Debug.WriteLine(text);
        }
        public static string FilterName(this string name)
        {
            return name.Replace("-", "").Replace("_", @"\_");
        }


        /// <summary>
        /// 获取语料库文件夹路径
        /// </summary>
        /// <param name="datasetName">数据集名字</param>
        /// <param name="bugFolderPath">bug文件夹路径</param>
        /// <returns></returns>
        internal static string GetCorpusFolderPath(string datasetName, string bugFolderPath)
        {
            string datasetNameLower = datasetName.ToLowerInvariant();
            switch (datasetNameLower)
            {
                // moreBugs数据集 每个bug对应一个语料库
                case "aspectj":
                case "jodatime":
                    return bugFolderPath + CorpusFolderName;
                // zhou 数据集 整个项目对应一个语料库
                case "eclipse":
                case "zxing":
                case "swt":
                    return ReportFolderPath + datasetName + @"\" + CorpusFolderName;
                default:
                    throw new Exception("Unknown dataset: " + datasetName);
            }
        }

        /// <summary>
        /// 获取相似度列表
        /// </summary>
        /// <param name="similarityFilePath"></param>
        /// <param name="threshold"></param>
        /// <returns></returns>
        internal static List<string> GetSimilarityList(string similarityFilePath, double threshold)
        {
            List<string> similarityList = File.ReadAllLines(similarityFilePath).
                                Select(x =>
                                {
                                    var splits = x.Split(new[] { " " }, StringSplitOptions.RemoveEmptyEntries);
                                    return new { Name = splits[0].Trim(), Value = Convert.ToDouble(splits[1].Trim()) };
                                })
                                .Select(x => x.Name)
                                .ToList();
            int toTake = (int)Math.Truncate(Math.Ceiling(similarityList.Count * threshold));
            return similarityList.Take(toTake).ToList();
        }

        /// <summary>
        /// 获取相关文件列表
        /// </summary>
        /// <param name="relevanceFilePath"></param>
        /// <returns></returns>
        internal static List<string> GetRelevanceList(string relevanceFilePath)
        {
            return File.ReadAllLines(relevanceFilePath).Select(x => x.Trim()).ToList();
        }

    }
}
