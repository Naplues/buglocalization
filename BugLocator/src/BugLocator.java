import java.io.File;
import java.io.PrintStream;
import property.Property;

public class BugLocator
{
    public static void main(String[] args)
    {
        System.out.println("(c)Copyright Tsinghua University, 2012");
        try
        {
            if (args.length == 0)
            {
                showHelp();
            }
            else
            {
                boolean isLegal = parseArgs(args);
                if (isLegal)
                {
                    Core core = new Core();
                    core.process();
                }
            }
        }
        catch (Exception ex)
        {
            showHelp();
        }
    }

    private static void showHelp()
    {
        String usage = "Usage:java -jar BugLocator [-options] \r\nwhere options must include:\r\n-b\tindicates the bug information file\r\n-s\tindicates the source code directory\r\n-a\tindicates the alpha value for combining vsmScore and simiScore\r\n-o\tindicates the result file";

        System.out.println(usage);
    }

    private static boolean parseArgs(String[] args)
    {
        int i = 0;
        String bugFilePath = "";
        String sourceCodeDir = "";
        String alphaStr = "";
        float alpha = 0.0F;
        String outputFile = "";
        while (i < args.length - 1)
        {
            if (args[i].equals("-b"))
            {
                i++;
                bugFilePath = args[i];
            }
            else if (args[i].equals("-s"))
            {
                i++;
                sourceCodeDir = args[i];
            }
            else if (args[i].equals("-a"))
            {
                i++;
                alphaStr = args[i];
            }
            else if (args[i].equals("-o"))
            {
                i++;
                outputFile = args[i];
            }
            i++;
        }
        boolean isLegal = true;
        if ((bugFilePath.equals("")) || (bugFilePath == null))
        {
            isLegal = false;
            System.out.println("you must indicate the bug information file");
        }
        if ((sourceCodeDir.equals("")) || (sourceCodeDir == null))
        {
            isLegal = false;
            System.out.println("you must indicate the source code directory");
        }
        if ((!alphaStr.equals("")) && (alphaStr != null)) {
            try
            {
                alpha = Float.parseFloat(alphaStr);
            }
            catch (Exception ex)
            {
                isLegal = false;
                System.out
                        .println("-a argument is ilegal,it must be a float value");
            }
        }
        if ((outputFile.equals("")) || (outputFile == null))
        {
            isLegal = false;
            System.out.println("you must indicate the output file");
        }
        else
        {
            File file = new File(outputFile);
            if (file.isDirectory())
            {
                if (!file.exists()) {
                    file.mkdir();
                }
                outputFile = outputFile + "output.txt";
            }
        }
        if (!isLegal)
        {
            showHelp();
        }
        else
        {
            File file = new File(System.getProperty("user.dir"));
            if (file.getFreeSpace() / 1024L / 1024L / 1024L < 2L)
            {
                System.out.println("Not enough free disk space, please ensure your current disk space are bigger than 2G.");
                isLegal = false;
            }
            else
            {
                File dir = new File("tmp");
                if (!dir.exists()) {
                    dir.mkdir();
                }
                Property.createInstance(bugFilePath, sourceCodeDir,
                        dir.getAbsolutePath(), alpha, outputFile);
            }
        }
        return isLegal;
    }
}
