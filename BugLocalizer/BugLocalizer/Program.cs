using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.IO;
namespace BugLocalizer
{
    class Program
    {
        static void Main(string[] args)
        {
            Console.ReadLine();
            ///预处理moreBugs数据集中的项目AspectJ和JodaTime
            /*
            string project = "AspectJ";
            string path = @"D:\Research-Dataset\Bug\Source\moreBugs\" + project + "\bugs\";
            List<DirectoryInfo> bugs = new DirectoryInfo(path).GetDirectories().ToList();
            int i = 0;
            foreach (DirectoryInfo x in bugs)
            {
                Utility.unTAR(x.FullName + @"\corpus\xml.tgz", x.FullName + @"\corpus\temp\");
                Console.WriteLine(i++);
            }*/

            ///制作数据集
            ///BugLocalizer.DataCreators.ZXing DC_project = new BugLocalizer.DataCreators.ZXing();
            ///DC_project.Execute();
            BugLocalizer.DataCreators.Swt DC_project = new BugLocalizer.DataCreators.Swt();
            DC_project.Execute();

            ///BugLocalizer.DataCreators.Eclipse DC_project = new BugLocalizer.DataCreators.Eclipse();
            ///DC_project.Execute();

            ///BugLocalizer.DataCreators.AspectJ DC_project = new BugLocalizer.DataCreators.AspectJ();
            ///DC_project.Execute();
            ///BugLocalizer.DataCreators.JodaTime DC_project = new BugLocalizer.DataCreators.JodaTime();
            ///DC_project.Execute();

            ///Console.WriteLine("-----------------------------------------------");

            ///计算排序结果
            ///BugLocalizer.Calculators.ZXing CA_project = new BugLocalizer.Calculators.ZXing();
            ///CA_project.Execute();
            ///BugLocalizer.Calculators.Swt CA_project = new BugLocalizer.Calculators.Swt();
            ///CA_project.Execute();
            
            ///BugLocalizer.Calculators.Eclipse CA_project = new BugLocalizer.Calculators.Eclipse();
            ///CA_project.Execute();

            ///BugLocalizer.Calculators.AspectJ CA_project = new BugLocalizer.Calculators.AspectJ();
            ///CA_project.Execute();
            ///BugLocalizer.Calculators.JodaTime CA_project = new BugLocalizer.Calculators.JodaTime();
            ///CA_project.Execute();

            Console.WriteLine("-------------Finish---------------");
            Console.ReadLine();
        }
    }
}
