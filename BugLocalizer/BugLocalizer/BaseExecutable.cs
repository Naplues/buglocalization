using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text.RegularExpressions;
using BugLocalizer.Models;
using BugLocalizer.Properties;
using SourceCodeIndexer.STAC.Stemmer;

namespace BugLocalizer
{
    public abstract class BaseExecutable
    {
        //生成数据集或执行算法处理数据集
        public abstract void Execute();
        /// <summary>
        /// 过滤文本
        /// </summary>
        /// <param name="text"></param>
        /// <returns></returns>
        public static IEnumerable<string> TextWithFilter(string text)
        {
            return Regex.Split(text, "[^a-zA-Z]+")
                .Where(x => !string.IsNullOrWhiteSpace(x))
                .SelectMany(x => Regex.Split(x, "(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z](?:s[a-z]|[abcdefghijklmnopqrtuvwxyz]))"))
                .Select(x => x.ToLowerInvariant().Trim())
                .Where(x => !Resources.StopWords.Contains(x))
                .Select(x => new PorterStemmer().GetStemmedText(x).Trim())
                .Where(x => !string.IsNullOrWhiteSpace(x));
        }
        /// <summary>
        /// 未过滤文本
        /// </summary>
        /// <param name="text"></param>
        /// <returns></returns>
        public static IEnumerable<string> TextNoFilter(string text)
        {
            return Regex.Split(text, "[^a-zA-Z]+")
                .Where(x => !string.IsNullOrWhiteSpace(x))
                .SelectMany(x => Regex.Split(x, "(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z](?:s[a-z]|[abcdefghijklmnopqrtuvwxyz]))"))
                .Select(x => x.ToLowerInvariant())
                .Where(x => !Resources.StopWords.Contains(x))
                .Where(x => !string.IsNullOrWhiteSpace(x));
        }

        /// <summary>
        /// Writes vector to file
        /// 将文档向量写入文件
        /// </summary>
        /// <param name="filePath">文件路径</param>
        /// <param name="vector">文档向量</param>
        /// <param name="asInt">是否作为整数</param>
        protected static void WriteDocumentVectorToFile1(string filePath, MyDoubleDictionary vector, bool asInt = false)
        {
            string pattern = asInt ? "##" : "##.00000";
            File.WriteAllLines(filePath, vector.Select(x => x.Key + " " + x.Value.ToString(pattern)));
        }

        /// <summary>
        /// Writes vector to file ordered
        /// 降序排列写入文件
        /// </summary>
        /// <param name="filePath"></param>
        /// <param name="vector"></param>
        /// <param name="asInt"></param>
        protected static void WriteDocumentVectorToFileOrderedDescending(string filePath, MyDoubleDictionary vector, bool asInt = false)
        {
            string pattern = asInt ? "##" : "##.00000";
            File.WriteAllLines(filePath, vector.ToList().OrderByDescending(x => x.Value).Select(x => x.Key + " " + x.Value.ToString(pattern)));
        }
    }
}