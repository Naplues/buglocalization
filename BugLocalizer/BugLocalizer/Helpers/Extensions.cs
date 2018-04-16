using System;
using System.Linq;

namespace BugLocalization.Helpers
{
    /// <summary>
    /// 扩展
    /// </summary>
    public static class Extensions
    {
        public static double[] JensenSum(this double[] vector1, double[] vector2)
        {
            //向量长度不一致异常
            if (vector1.Length != vector2.Length)
                throw new Exception("Length " + vector1.Length + " does not match vector 2 length: " + vector2.Length);

            double[] result = new double[vector1.Length];
            for (int i = 0; i < vector1.Length; i++)
                result[i] = (vector1[i] + vector2[i]) / 2;

            return result;
        }

        public static double JensenEntropy(this double[] vector)
        {
            return -1.0 * vector.ToList().Select(x => x * (x == 0 ? 0 : Math.Log(x))).Sum();
        }
    }
}
