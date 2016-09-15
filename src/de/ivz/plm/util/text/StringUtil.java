package de.ivz.plm.util.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * StringUtil - kleine Helferlein beim Umgang mit Strings
 *
 * @author Ryczard Haase
 * @version 1.0
 */
public class StringUtil {

    /**
     * Ersetzt in einem String den gesuchten String durch einen anderen String
     * @param text der zu behandelnde String
     * @param search der zu findende String
     * @param replace der ersetzende String
     * @return der mainpulierte String
     */
    public static String replaceAll(String text, String search, String replace) {
        for (int i = 0; (i = text.indexOf(search)) != -1; ) {
            String prefix = "";
            if (i != 0) {
                prefix = text.substring(0, i);
            }
            String postfix = "";
            if (i + search.length() < text.length()) {
                postfix = text.substring(i + search.length(), text.length());
            }
            text = prefix + replace + postfix;
        }
        return text;
    }

    /**
     * Zerlegt einen String in seine Wortbestandteile
     * @param text der zu zerlegende String
     * @param ignoreQuotes sollen Quotes ignoriert werden?
     * @return das Array mit den Worten
     */
    public static String[] split(String text, boolean ignoreQuotes) {
        List<String> buffer = new ArrayList<String>();
        char splitChar = ' ';
        char chars[] = text.trim().toCharArray();
        StringBuffer currentToken = new StringBuffer();
        for (char c : chars)
            if (c == splitChar) {
                if (currentToken.toString().length() != 0) {
                    buffer.add(currentToken.toString());
                    currentToken = new StringBuffer();
                    splitChar = ' ';
                }
            } else {
                if (c == 39) {
                    if (currentToken.length() == 0 && splitChar == ' ') {
                        splitChar = c;
                    }
                } else if (c != 34) {
                    currentToken.append(c);
                }
            }
        if (currentToken.length() != 0) {
            buffer.add(currentToken.toString());
        }
        String result[] = new String[buffer.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (String) buffer.get(i);
        }
        return result;
    }

    /**
     * Versucht einen InputStream als String einzulesen
     * @param is der InputStream
     * @return der eingelesene String
     * @throws IOException falls aufgetreten
     */
    public static String getFromInputStream(InputStream is) throws IOException {
        if (is != null) {
            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
            return sb.toString();
        }
        return null;
    }

}
