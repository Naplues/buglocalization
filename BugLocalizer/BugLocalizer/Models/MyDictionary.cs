using System.Collections.Generic;

namespace BugLocalizer.Models
{
    // string-double 字典
    public class MyDoubleDictionary : Dictionary<string, double>
    {
        /// <summary>
        /// 向字典中添加键，相应的值+1
        /// </summary>
        /// <param name="term"></param>
        public void Add(string term)
        {
            if (ContainsKey(term))
            {
                this[term] = this[term] + 1;
            }
            else
            {
                Add(term, 1);
            }
        }
    }
    // string-List 字典
    public class MyListTDictionary<T> : Dictionary<string, List<T>>
    {
        public void Add(string term, T value)
        {
            if (!ContainsKey(term))
                Add(term, new List<T>());

            if (!this[term].Contains(value))
                this[term].Add(value);
        }
    }

    public class DocumentDictionaryAny<T> : Dictionary<string, T>
    {
        public new void Add(string term, T value)
        {
            if (ContainsKey(term))
            {
                this[term] = value;
            }
            else
            {
                base.Add(term, value);
            }
        }
    }
}
