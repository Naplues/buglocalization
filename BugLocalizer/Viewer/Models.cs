using System;
using System.Collections.Generic;
using System.Linq;

namespace Viewer
{
    public class FileWithIndex
    {
        public string File { get; set; }

        public int Index { get; set; }

        public FileWithIndex(int index, string file)
        {
            File = file;
            Index = index;
        }
    }

    public class BoxPlot
    {
        public double Minimum { get; set; }

        public double FirstQuartile { get; set; }

        public double Median { get; set; }

        public double ThirdQuartile { get; set; }

        public double Maximum { get; set; }

        private const string TextFormat = "lower whisker={0}, lower quartile={1}, median={2}, upper quartile={3}, upper whisker={4}";

        public string GetLatex()
        {
            return string.Format(TextFormat, Minimum, FirstQuartile, Median, ThirdQuartile, Maximum);
        }
    }

    public class MrrResult
    {
        public readonly List<ProjectResult> Values = new List<ProjectResult>();

        public ProjectResult Get(string projectName)
        {
            return Values.Single(x => x.ProjectName == projectName);
        }

        public ProjectResult AddGet(string projectName)
        {
            var projectResult = new ProjectResult() { ProjectName = projectName };
            Values.Add(projectResult);

            return projectResult;
        }
    }

    [Serializable]
    public class ProjectResult
    {
        public string ProjectName { get; set; }

        public readonly List<MethodResult> Values = new List<MethodResult>();

        public MethodResult Get(string methodName)
        {
            return Values.Single(x => x.MethodName == methodName);
        }

        public MethodResult AddGet(string methodName)
        {
            var methodResult = new MethodResult() { MethodName = methodName };
            Values.Add(methodResult);

            return methodResult;
        }
    }

    [Serializable]
    public class MethodResult
    {
        public string MethodName { get; set; }

        public readonly List<ThresholdResult> Values = new List<ThresholdResult>();

        public ThresholdResult Get(double threshold)
        {
            string thresholdStr = threshold.ToString("0.0");
            return Values.Single(x => x.Threshold == thresholdStr);
        }

        public ThresholdResult AddGet(double threshold, double value)
        {
            var thresholdResult = new ThresholdResult() { Threshold = threshold.ToString("0.0"), Value = value };
            Values.Add(thresholdResult);

            return thresholdResult;
        }
    }

    [Serializable]
    public class ThresholdResult
    {
        public string Threshold { get; set; }

        public double Value { get; set; }
    }
}
