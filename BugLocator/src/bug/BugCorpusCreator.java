package bug;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import property.Property;
import utils.Splitter;
import utils.Stem;
import utils.Stopword;

public class BugCorpusCreator
{
    public static void main(String[] args)
    {
        BugCorpusCreator creator = new BugCorpusCreator();
        ArrayList<Bug> list = creator.parseXML();
    }

    private void writeCorpus(Bug bug, String storeDir)
            throws IOException
    {
        String content = bug.getBugSummary() + " " + bug.getBugDescription();
        String[] splitWords = Splitter.splitNatureLanguage(content);
        StringBuffer corpus = new StringBuffer();
        String[] arrayOfString1;
        int j = (arrayOfString1 = splitWords).length;
        for (int i = 0; i < j; i++)
        {
            String word = arrayOfString1[i];
            word = Stem.stem(word.toLowerCase());
            if (!Stopword.isEnglishStopword(word)) {
                corpus.append(word + " ");
            }
        }
        FileWriter writer = new FileWriter(storeDir + bug.getBugId() + ".txt");
        writer.write(corpus.toString().trim());
        writer.flush();
        writer.close();
    }

    private ArrayList<Bug> parseXML()
    {
        ArrayList<Bug> list = new ArrayList();
        DocumentBuilderFactory domFactory =
                DocumentBuilderFactory.newInstance();
        try
        {
            DocumentBuilder domBuilder = domFactory.newDocumentBuilder();

            InputStream is = new FileInputStream(
                    Property.getInstance().getBugFilePath());
            Document doc = domBuilder.parse(is);
            Element root = doc.getDocumentElement();
            NodeList bugRepository = root.getChildNodes();
            if (bugRepository != null) {
                for (int i = 0; i < bugRepository.getLength(); i++)
                {
                    Node bugNode = bugRepository.item(i);
                    if (bugNode.getNodeType() == 1)
                    {
                        String bugId = bugNode.getAttributes().getNamedItem(
                                "id").getNodeValue();
                        String openDate = bugNode.getAttributes().getNamedItem(
                                "opendate").getNodeValue();
                        String fixDate = bugNode.getAttributes().getNamedItem(
                                "fixdate").getNodeValue();
                        Bug bug = new Bug();
                        bug.setBugId(bugId);
                        bug.setOpenDate(openDate);
                        bug.setFixDate(fixDate);
                        for (Node node = bugNode.getFirstChild(); node != null; node = node
                                .getNextSibling()) {
                            if (node.getNodeType() == 1)
                            {
                                if (node.getNodeName().equals("buginformation"))
                                {
                                    NodeList _l = node.getChildNodes();
                                    for (int j = 0; j < _l.getLength(); j++)
                                    {
                                        Node _n = _l.item(j);
                                        if (_n.getNodeName().equals("summary"))
                                        {
                                            String summary = _n
                                                    .getTextContent();
                                            bug.setBugSummary(summary);
                                        }
                                        if (_n.getNodeName().equals(
                                                "description"))
                                        {
                                            String description = _n
                                                    .getTextContent();
                                            bug.setBugDescription(description);
                                        }
                                    }
                                }
                                if (node.getNodeName().equals("fixedFiles"))
                                {
                                    NodeList _l = node.getChildNodes();
                                    for (int j = 0; j < _l.getLength(); j++)
                                    {
                                        Node _n = _l.item(j);
                                        if (_n.getNodeName().equals("file"))
                                        {
                                            String fileName = _n
                                                    .getTextContent();
                                            bug.addFixedFile(fileName);
                                        }
                                    }
                                }
                            }
                        }
                        list.add(bug);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return list;
    }

    public void create()
            throws IOException
    {
        ArrayList<Bug> list = new BugCorpusCreator().parseXML();
        String dirPath = Property.getInstance().getWorkDir() +
                Property.getInstance().getSeparator() + "BugCorpus" +
                Property.getInstance().getSeparator();
        File file = new File(dirPath);
        Property.getInstance().setBugReportCount(list.size());
        if (!file.exists()) {
            file.mkdir();
        }
        for (Bug bug : list) {
            writeCorpus(bug, dirPath);
        }
        FileWriter writer = new FileWriter(Property.getInstance().getWorkDir() +
                Property.getInstance().getSeparator() + "SortedId.txt");
        FileWriter writerFix = new FileWriter(
                Property.getInstance().getWorkDir() +
                        Property.getInstance().getSeparator() + "FixLink.txt");
        Iterator localIterator3;
        for (Iterator localIterator2 = list.iterator(); localIterator2.hasNext(); localIterator3.hasNext())
        {
            Bug bug = (Bug)localIterator2.next();
            writer.write(bug.getBugId() + "\t" + bug.getFixDate() +
                    Property.getInstance().getLineSeparator());
            writer.flush();
            localIterator3 = bug.set.iterator();
            continue;
            String fixName = (String)localIterator3.next();
            writerFix.write(bug.getBugId() + "\t" + fixName +
                    Property.getInstance().getLineSeparator());
            writerFix.flush();
        }
        writer.close();
        writerFix.close();
    }
}
