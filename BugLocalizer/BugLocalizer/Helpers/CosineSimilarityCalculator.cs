using System;
using System.Linq;
using BugLocalizer.Models;

namespace BugLocalizer.Helpers
{
    /// <summary>
    /// 余弦相似度计算器
    /// </summary>
    public class CosineSimilarityCalculator
    {
        private readonly MyDoubleDictionary _vector1;

        public CosineSimilarityCalculator(MyDoubleDictionary vector1)
        {
            _vector1 = vector1;
        }

        public double GetSimilarity(MyDoubleDictionary vector2)
        {
            double length1 = GetLength(_vector1);
            double length2 = GetLength(vector2);

            double dotProduct = _vector1.Where(wordWithCount => vector2.ContainsKey(wordWithCount.Key)).Sum(wordWithCount => (wordWithCount.Value * vector2[wordWithCount.Key]));

            return vector2.Count == 0 ? 0 : dotProduct / (length1 * length2);
        }

        public string GetSimilarityText(MyDoubleDictionary vector2)
        {
            double length1 = GetLength(_vector1);
            double length2 = GetLength(vector2);

            var dotProductObj =
                _vector1.Where(wordWithCount => vector2.ContainsKey(wordWithCount.Key))
                    .Select(
                        wordWithCount =>
                            new
                            {
                                Word = wordWithCount.Key,
                                Value1 = wordWithCount.Value,
                                Value2 = vector2[wordWithCount.Key]
                            })
                            .Select(x => $"{x.Word} {x.Value1.ToString("##.000")}, {x.Value2.ToString("##.000")}");

            var dotProductString = string.Join(Environment.NewLine, dotProductObj);
            return dotProductString;
        }
        /// <summary>
        /// 计算向量的欧氏距离
        /// </summary>
        /// <param name="vector"></param>
        /// <returns></returns>
        public static double GetLength(MyDoubleDictionary vector)
        {
            double length = Math.Sqrt(vector.Sum(x => Math.Pow(x.Value, 2)));
            return length;
        }
    }
}
