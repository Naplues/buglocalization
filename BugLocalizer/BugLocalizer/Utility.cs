using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using SharpCompress.Reader;
using SharpCompress.Common;
using CenterSpace.NMath.Core;
using CenterSpace.NMath.Matrix;

namespace BugLocalizer
{
    public static class Utility
    {
        public const string ReportFolderPath = @"D:\Research-Dataset\Bug\Report\";        //报告文件夹路径
        public const string DatasetFolderPath = @"D:\Research-Dataset\Bug\Source\";       //数据源文件夹路径
        public const string MoreBugDatasetRelativeFolderPath = @"moreBugs\";              //moreBugs数据集文件夹
        public const string CommonErrorPathFile = @"D:\Research-Dataset\Bug\Error.txt";   //运行错误记录文件

        public static int ParallelThreadCount = 10;   //并行线程数

        public const bool CleanPrevious = false;
        public const bool RunVsm = false;
        public const bool RunLsi = true;
        public const bool RunJen = false;

        public const bool RunSim = false;
        public const bool RunNgd = false;
        public const bool RunAPm = false;
        //LSI 列表
        public static readonly List<int> LsiKs = new List<int>() { 50, 100, 150, 200, 250, 300, 350, 400, 450, 500, 550, 600, 650, 700, 750, 800, 850, 900 };
        //打印状态
        public static void Status(string text)
        {
            Console.WriteLine(text);
        }

        public static string NewLine(int count)
        {
            string s = "";
            for (int i = 0; i < count; i++)
                s += Environment.NewLine;
            return s;
        }
        
        public static object MyObj = new object();
        public static void WriteErrorCommon(string location, string message)
        {
            lock (MyObj)
            {
                File.AppendAllText(CommonErrorPathFile, location + NewLine(1) + message + NewLine(2));
            }
        }

        //将二维数组转为一维
        public static double[] ToOne(double[,] array)
        {
            double[] result = new double[array.GetLength(0) * array.GetLength(1)];

            for (int i = 0; i < array.GetLength(0); i++)
            {
                for (int j = 0; j < array.GetLength(1); j++)
                {
                    result[i * array.GetLength(1) + j] = array[i, j];
                }
            }
            return result;
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
    }
}
