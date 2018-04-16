using System;
using System.Collections.Generic;
using System.Linq;
using System.IO;
using System.Text;
using System.Threading.Tasks;

namespace BugLocalizer
{
    class Program
    {
        static void Main(string[] args)
        {
            /*
            string path = @"D:\Research-Dataset\Bug\Source\moreBugs\JodaTime\bugs\";
            List<DirectoryInfo> bugs = new DirectoryInfo(path).GetDirectories().ToList();
            bugs.ForEach(x => Utility.unTAR(x.FullName + @"\corpus\xml.tgz", x.FullName + @"\corpus\temp\"));
            */

            ///制作数据集
            ///BugLocalizer.DataCreators.Swt DC_project = new BugLocalizer.DataCreators.Swt();
            ///DC_project.Execute();
            ///BugLocalizer.DataCreators.ZXing DC_project = new BugLocalizer.DataCreators.ZXing();
            ///DC_project.Execute();
            ///BugLocalizer.DataCreators.Eclipse DC_project = new BugLocalizer.DataCreators.Eclipse();
            ///DC_project.Execute();
            ///BugLocalizer.DataCreators.AspectJ DC_project = new BugLocalizer.DataCreators.AspectJ();
            ///DC_project.Execute();
            ///BugLocalizer.DataCreators.JodaTime DC_project = new BugLocalizer.DataCreators.JodaTime();
            ///DC_project.Execute();

            ///Console.WriteLine("-----------------------------------------------");

            ///计算排序结果
            ///BugLocalizer.Calculators.Swt CA_project = new BugLocalizer.Calculators.Swt();
            ///CA_project.Execute();
            BugLocalizer.Calculators.ZXing CA_project = new BugLocalizer.Calculators.ZXing();
            CA_project.Execute();
            ///BugLocalizer.Calculators.Eclipse CA_project = new BugLocalizer.Calculators.Eclipse();
            ///CA_project.Execute();
            ///BugLocalizer.Calculators.AspectJ CA_project = new BugLocalizer.Calculators.AspectJ();
            ///CA_project.Execute();
            ///BugLocalizer.Calculators.JodaTime CA_project = new BugLocalizer.Calculators.JodaTime();
            ///CA_project.Execute();





        }
    }
}
