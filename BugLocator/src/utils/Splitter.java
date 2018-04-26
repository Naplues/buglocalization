package utils;

import java.util.ArrayList;

public class Splitter
{
    public static String[] splitNatureLanguage(String natureLanguage)
    {
        ArrayList<String> wordList = new ArrayList();
        StringBuffer wordBuffer = new StringBuffer();
        char[] arrayOfChar;
        int j = (arrayOfChar = natureLanguage.toCharArray()).length;
        for (int i = 0; i < j; i++)
        {
            char c = arrayOfChar[i];
            if (((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z')) ||
                    ((c >= '0') && (c <= '9')) || (c == '\''))
            {
                wordBuffer.append(c);
            }
            else
            {
                String word = wordBuffer.toString();
                if (!word.equals("")) {
                    wordList.add(word);
                }
                wordBuffer = new StringBuffer();
            }
        }
        if (wordBuffer.length() != 0)
        {
            String word = wordBuffer.toString();
            if (!word.equals("")) {
                wordList.add(word);
            }
            wordBuffer = new StringBuffer();
        }
        return (String[])wordList.toArray(new String[wordList.size()]);
    }

    public static String[] splitSourceCode(String sourceCode)
    {
        StringBuffer contentBuf = new StringBuffer();
        StringBuffer wordBuf = new StringBuffer();
        sourceCode = sourceCode + "$";
        char[] arrayOfChar;
        int j = (arrayOfChar = sourceCode.toCharArray()).length;
        for (int i = 0; i < j; i++)
        {
            char c = arrayOfChar[i];
            if (((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z')))
            {
                wordBuf.append(c);
            }
            else
            {
                int length = wordBuf.length();
                if (length != 0)
                {
                    int k = 0;
                    int i = 0;
                    for (int j = 1; i < length - 1; j++)
                    {
                        char first = wordBuf.charAt(i);
                        char second = wordBuf.charAt(j);
                        if ((first >= 'A') && (first <= 'Z') &&
                                (second >= 'a') && (second <= 'z'))
                        {
                            contentBuf.append(wordBuf.substring(k, i));
                            contentBuf.append(' ');
                            k = i;
                        }
                        else if ((first >= 'a') && (first <= 'z') &&
                                (second >= 'A') && (second <= 'Z'))
                        {
                            contentBuf.append(wordBuf.substring(k, j));
                            contentBuf.append(' ');
                            k = j;
                        }
                        i++;
                    }
                    if (k < length)
                    {
                        contentBuf.append(wordBuf.substring(k));
                        contentBuf.append(" ");
                    }
                    wordBuf = new StringBuffer();
                }
            }
        }
        String[] words = contentBuf.toString().split(" ");
        contentBuf = new StringBuffer();
        for (int i = 0; i < words.length; i++) {
            if ((!words[i].trim().equals("")) && (words[i].length() >= 2)) {
                contentBuf.append(words[i] + " ");
            }
        }
        return contentBuf.toString().trim().split(" ");
    }
}
