package se.digg.dgc.service.impl;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

/*
From https://github.com/ZsBT/mrz-java/blob/master/src/main/java/com/innovatrics/mrz/MrzParser.java
 */
public class MrzParser {

    /**
     * Converts given string to a MRZ string: removes all accents, converts the string to upper-case and replaces all spaces and invalid characters with '&lt;'.
     * <p/>
     * Several characters are expanded:
     * <table border="1">
     * <tr><th>Character</th><th>Expand to</th></tr>
     * <tr><td>Ä</td><td>AE</td></tr>
     * <tr><td>Å</td><td>AA</td></tr>
     * <tr><td>Æ</td><td>AE</td></tr>
     * <tr><td>Ĳ</td><td>IJ</td></tr>
     * <tr><td>IJ</td><td>IJ</td></tr>
     * <tr><td>Ö</td><td>OE</td></tr>
     * <tr><td>Ø</td><td>OE</td></tr>
     * <tr><td>Ü</td><td>UE</td></tr>
     * <tr><td>ß</td><td>SS</td></tr>
     * </table>
     * <p/>
     * Examples:<ul>
     * <li><code>toMrz("Sedím na konári", 20)</code> yields <code>"SEDIM&lt;NA&lt;KONARI&lt;&lt;&lt;&lt;&lt;"</code></li>
     * <li><code>toMrz("Pat, Mat", 8)</code> yields <code>"PAT&lt;&lt;MAT"</code></li>
     * <li><code>toMrz("foo/bar baz", 4)</code> yields <code>"FOO&lt;"</code></li>
     * <li><code>toMrz("*$()&/\", 8)</code> yields <code>"&lt;&lt;&lt;&lt;&lt;&lt;&lt;&lt;"</code></li>
     * </ul>
     * @param string the string to convert. Passing null is the same as passing in an empty string.
     * @param length required length of the string. If given string is longer, it is truncated. If given string is shorter than given length, '&lt;' characters are appended at the end. If -1, the string is neither truncated nor enlarged.
     * @return MRZ-valid string.
     */
    public static String toMrz(String string, int length) {
        if (string == null) {
            string = "";
        }
        for (final Map.Entry<String, String> e : EXPAND_CHARACTERS.entrySet()) {
            string = string.replace(e.getKey(), e.getValue());
        }
        string = string.replace("’", "");
        string = string.replace("'", "");
        string = deaccent(string).toUpperCase();
        if (length >= 0 && string.length() > length) {
            string = string.substring(0, length);
        }
        final StringBuilder sb = new StringBuilder(string);
        for (int i = 0; i < sb.length(); i++) {
            if (!isValid(sb.charAt(i))) {
                sb.setCharAt(i, FILLER);
            }
        }
        while (sb.length() < length) {
            sb.append(FILLER);
        }
        return sb.toString();
    }


    private static String deaccent(String str) {
        String n = Normalizer.normalize(str, Normalizer.Form.NFD);
        return n.replaceAll("[^\\p{ASCII}]", "").toLowerCase();
    }

    /**
     * The filler character, '&lt;'.
     */
    public static final char FILLER = '<';

    /**
     * Checks if given character is valid in MRZ.
     * @param c the character.
     * @return true if the character is valid, false otherwise.
     */
    private static boolean isValid(char c) {
        return ((c == FILLER) || (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z'));
    }

    private static final Map<String, String> EXPAND_CHARACTERS = new HashMap<String, String>();

    static {
        EXPAND_CHARACTERS.put("\u00C4", "AE"); // Ä
        EXPAND_CHARACTERS.put("\u00E4", "AE"); // ä
        EXPAND_CHARACTERS.put("\u00C5", "AA"); // Å
        EXPAND_CHARACTERS.put("\u00E5", "AA"); // å
        EXPAND_CHARACTERS.put("\u00C6", "AE"); // Æ
        EXPAND_CHARACTERS.put("\u00E6", "AE"); // æ
        EXPAND_CHARACTERS.put("\u0132", "IJ"); // Ĳ
        EXPAND_CHARACTERS.put("\u0133", "IJ"); // ĳ
        EXPAND_CHARACTERS.put("\u00D6", "OE"); // Ö
        EXPAND_CHARACTERS.put("\u00F6", "OE"); // ö
        EXPAND_CHARACTERS.put("\u00D8", "OE"); // Ø
        EXPAND_CHARACTERS.put("\u00F8", "OE"); // ø
        EXPAND_CHARACTERS.put("\u00DC", "UE"); // Ü
        EXPAND_CHARACTERS.put("\u00FC", "UE"); // ü
        EXPAND_CHARACTERS.put("\u00DF", "SS"); // ß
    }
}
