using System;

namespace BugLocalizer
{
    class Program
    {
        static void Main(string[] args)
        {
            Console.WriteLine("请输入线程数: ");
            Utility.ParallelThreadCount = Int32.Parse(Console.ReadLine());
            
            ///制作数据集  一个语料库/多个语料库
            //new BugLocalizer.DataCreators.ZXing().Execute();
            //new BugLocalizer.DataCreators.Swt().Execute();
            //new BugLocalizer.DataCreators.Eclipse().Execute();
            //new BugLocalizer.DataCreators.JodaTime().Execute();
            //new BugLocalizer.DataCreators.AspectJ().Execute();

            ///计算排序结果
            //new BugLocalizer.Calculators.ZXing().Execute();
            //new BugLocalizer.Calculators.Swt().Execute();
            //new BugLocalizer.Calculators.Eclipse().Execute();
            //new BugLocalizer.Calculators.JodaTime().Execute();
            //new BugLocalizer.Calculators.AspectJ().Execute();

            Console.WriteLine("-------------Finish---------------");
            Console.ReadLine();
        }
    }
}
