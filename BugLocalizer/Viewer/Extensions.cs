namespace Viewer
{
    public static class Extensions
    {
        public static string FilterName(this string name)
        {
            return name.Replace("-", "").Replace("_", @"\_");
        }
    }
}
