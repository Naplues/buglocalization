using System;
using System.IO;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

using BugLocalizer.Models;

namespace BugLocalizer.Calculators
{
    /// <summary>
    /// 方法实现
    /// </summary>
    public class Method
    {
        // 各方法完成情况文件名
        internal const string VsmCompletedFile = @"CompletedVsm.txt";
        internal const string LsiCompletedFile = @"CompletedLsi.txt";
        internal const string JenCompletedFile = @"CompletedJen.txt";

        internal const string NgdCompletedFile = @"CompletedNgd.txt";
        internal const string SimCompletedFile = @"CompletedSim.txt";
        internal const string APmCompletedFile = @"CompletedAPm.txt";

        // 语料库目录及bug查询文件
        internal const string CorpusWithFilterFolderName = @"Corpus\";
        internal const string QueryWithFilterFileName = @"BugQuery.txt";

        // 各方法结果文件
        internal const string VsmFileName = @"Results\Vsm.txt";
        internal const string PmiFileName = @"Results\Pmi.txt";
        internal const string NgdFileName = @"Results\Ngd.txt";
        internal const string JenFileName = @"Results\Jen.txt";
        internal const string APmFileName = @"Results\APm.txt";
        internal const string LsiOutputFolderName = @"Results\Lsi\";
        
        // 源文件的 file-token 索引
        internal static readonly Dictionary<string, List<string>> CodeFilesWithContent = new Dictionary<string, List<string>>();


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

        /// <summary>
        /// Writes vector to file
        /// 将文档向量写入文件
        /// </summary>
        /// <param name="filePath">文件路径</param>
        /// <param name="vector">文档向量</param>
        /// <param name="asInt">是否作为整数</param>
        public static void WriteDocumentVectorToFile(string filePath, MyDoubleDictionary vector, bool asInt = false)
        {
            string pattern = asInt ? "##" : "##.00000";
            File.WriteAllLines(filePath, vector.Select(x => x.Key + " " + x.Value.ToString(pattern)));
        }
    }
}
