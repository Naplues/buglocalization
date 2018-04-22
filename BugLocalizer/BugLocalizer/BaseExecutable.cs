using System.Collections.Generic;
using System.Linq;
using System.Text.RegularExpressions;

using SourceCodeIndexer.STAC.Stemmer;

using BugLocalizer.Properties;

namespace BugLocalizer
{
    public abstract class BaseExecutable
    {
        //执行方法
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

    }
}