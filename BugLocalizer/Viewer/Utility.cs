using System.Diagnostics;

namespace Viewer
{
    public static class Utility
    {
        public const string ReportFolderPath = @"C:\Research-Dataset\Bug\Report\";            //bug报告文件夹
        public const string ResultDumpsFolderPath = @"C:\Research-Dataset\Bug\ResultDumps\";  //结果文件夹

        public const int ParallelThreadCount = 1;  //并行线程数
        /// <summary>
        /// 打印状态
        /// </summary>
        /// <param name="text"></param>
        public static void Status(string text)
        {
            Debug.WriteLine(text);
        }
        public static string FilterName(this string name)
        {
            return name.Replace("-", "").Replace("_", @"\_");
        }
    }
}
