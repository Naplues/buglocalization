namespace BugLocalizer.Models
{
    /// <summary>
    /// 作者周的文件信息
    /// </summary>
    public class ZhouFileInfo
    {
        public ZhouFileInfo(int id, string relativeFilePath, string fullPath)
        {
            Id = id;
            RelativeFilePath = relativeFilePath;
            FullPath = fullPath;
        }

        public int Id { get; set; }

        public string RelativeFilePath { get; set; }

        public string FullPath { get; set; }
    }
}
