using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;

using SharpCompress.Reader;
using SharpCompress.Common;
using CenterSpace.NMath.Core;

using BugLocalizer.Models;

namespace BugLocalizer
{
    public static class Utility
    {
        // 数据集相关总目录
        public const string ReportFolderPath = @"D:\Research-Dataset\Bug\Report\";        //报告文件夹路径
        public const string DatasetFolderPath = @"D:\Research-Dataset\Bug\Source\";       //数据源文件夹路径
        public const string MoreBugDatasetRelativeFolderPath = @"moreBugs\";              //moreBugs数据集文件夹
        public const string CommonErrorPathFile = @"D:\Research-Dataset\Bug\Error.txt";   //运行错误记录文件

        // 系统运行配置
        public const bool CleanPrevious = false;
        public const bool RunVsm = false;
        public const bool RunLsi = true;
        public const bool RunJen = false;

        public const bool RunSim = false;
        public const bool RunNgd = false;
        public const bool RunAPm = false;

        public static int ParallelThreadCount = 10;   //并行线程数

        //LSI 列表
        public static readonly List<int> LsiKs = new List<int>() { 50, 100, 150, 200, 250, 300, 350, 400, 450, 500, 550, 600, 650, 700, 750, 800, 850, 900 };
        
        // 互斥操作加锁对象
        public static object MyObj = new object();

        /// <summary>
        /// 打印状态信息
        /// </summary>
        /// <param name="text"></param>
        public static void Status(string text)
        {
            Console.WriteLine(text);
        }

        /// <summary>
        /// 加入n个空行
        /// </summary>
        /// <param name="count"></param>
        /// <returns></returns>
        public static string NewLine(int count)
        {
            string s = "";
            for (int i = 0; i < count; i++)
                s += Environment.NewLine;
            return s;
        }

        /// <summary>
        /// 记录程序运行时错误, 互斥操作
        /// </summary>
        /// <param name="location"></param>
        /// <param name="message"></param>
        public static void WriteErrorCommon(string location, string message)
        {
            lock (MyObj)
            {
                File.AppendAllText(CommonErrorPathFile, location + NewLine(1) + message + NewLine(2));
            }
        }

        /// <summary>
        /// 根据数组创建对角阵
        /// </summary>
        public static DoubleMatrix GetDiagonal(DoubleVector vector, int dim)
        {
            var array = vector.ToArray();
            double[,] data = new double[dim, dim];
            for (int i = 0; i < dim; i++)
            {
                for (int j = 0; j < dim; j++)
                {
                    if (i == j)
                    {
                        data[i, j] = array[i];
                    }
                }
            }
            DoubleMatrix matrix = new DoubleMatrix(data);
            return matrix;
        }

        /// <summary>
        /// 根据矩阵 创建矩阵
        /// </summary>
        public static DoubleMatrix GetDimMatrix(DoubleMatrix origin, int row, int col)
        {
            double[,] data = new double[row, col];
            for (int i = 0; i < col; i++)
            {
                var array = origin.Col(i).ToArray();
                for(int j = 0; j < row; j++)
                {
                    data[j, i] = array[j];
                }
            }
            DoubleMatrix matrix = new DoubleMatrix(data);
            return matrix;
        }

        /// <summary>
        /// 获取两个列表之间的相似度
        /// </summary>
        /// <param name="a1"></param>
        /// <param name="a2"></param>
        /// <returns></returns>
        public static double GetSimilarity(IReadOnlyList<double> a1, IReadOnlyList<double> a2)
        {
            double dotProduct = 0;
            double aSum = 0, bSum = 0;

            for (int i = 0; i < a1.Count; i++)
            {
                dotProduct += a1[i] * a2[i];
                aSum += Math.Pow(a1[i], 2);
                bSum += Math.Pow(a2[i], 2);
            }

            return dotProduct / (Math.Sqrt(aSum) * Math.Sqrt(bSum));
        }

        /// <summary>
        /// 加压tar文件
        /// </summary>
        /// <param name="tarFilePath">目标文件路径</param>
        /// <param name="directoryPath">结果文件路径</param>
        public static void unTAR(string tarFilePath, string directoryPath)
        {
            // UTF7: support chinese font -> UTF.7
            SharpCompress.Common.ArchiveEncoding.Default = Encoding.UTF7;

            using (Stream stream = File.OpenRead(tarFilePath))
            {
                var reader = ReaderFactory.Open(stream);

                while (reader.MoveToNextEntry())
                {
                    if (!reader.Entry.IsDirectory)
                        reader.WriteEntryToDirectory(directoryPath,
                           ExtractOptions.ExtractFullPath | ExtractOptions.Overwrite);
                }
            }
            Console.WriteLine(tarFilePath + " finish...");
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
