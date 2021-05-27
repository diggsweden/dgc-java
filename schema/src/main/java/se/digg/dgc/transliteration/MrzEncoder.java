/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.transliteration;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

/*
From https://github.com/ZsBT/mrz-java/blob/master/src/main/java/com/innovatrics/mrz/MrzParser.java
 */

/**
 * MRZ name encoder according to ICAO format.
 * <p>
 * See <a href="https://www.icao.int/publications/Documents/9303_p3_cons_en.pdf">Doc 9303 - Machine Readable Travel
 * Documents Seventh Edition, 2015 - Part 3: Specifications Common to all MRTDs</a>.
 * </p>
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class MrzEncoder {

  /** Character mappings */
  public static final Map<Character, String> CHAR_MAPPINGS = new HashMap<>();

  static {
    CHAR_MAPPINGS.put('\u00E5', "AA"); // å
    CHAR_MAPPINGS.put('\u00C5', "AA"); // Å
    CHAR_MAPPINGS.put('\u00E4', "AE"); // ä
    CHAR_MAPPINGS.put('\u00C4', "AE"); // Ä
    CHAR_MAPPINGS.put('\u00C6', "AE"); // Æ
    CHAR_MAPPINGS.put('\u00E6', "AE"); // æ    
    CHAR_MAPPINGS.put('\u00F6', "OE"); // ö
    CHAR_MAPPINGS.put('\u00D6', "OE"); // Ö
    CHAR_MAPPINGS.put('\u00F8', "OE"); // ø
    CHAR_MAPPINGS.put('\u00D8', "OE"); // Ø    
    CHAR_MAPPINGS.put('\u0132', "IJ"); // Ĳ
    CHAR_MAPPINGS.put('\u0133', "IJ"); // ĳ
    CHAR_MAPPINGS.put('\u00DC', "UE"); // Ü
    CHAR_MAPPINGS.put('\u00FC', "UE"); // ü
    CHAR_MAPPINGS.put('\u00DF', "SS"); // ß
  }

  /**
   * Encodes the supplied string to a MRZ encoded string.
   * <p>
   * This means removing all accents, mapping/expanding characters according to {@link #CHAR_MAPPINGS}, converting the
   * string to uppercase, and finally to replace all spaces and non-supported characters with the '&lt;' char.
   * </p>
   * 
   * @param input
   * @return
   */
  public static String encode(final String input) {
    
    final StringBuffer sb = new StringBuffer();
    
    for (int i = 0; i < input.trim().length(); i++) {
      final char c = input.charAt(i);
      final String mapping = CHAR_MAPPINGS.get(c);
      if (mapping != null) {
        sb.append(mapping);
      }
      else if (c == '’' || c == '\'') {
        // Remove
      }
      else if (Character.isWhitespace(c)) {
        sb.append('<');
      }
      else {
        sb.append(c);
      }
    }
    // Remove all accents and replace all invalid characters with <
    return Normalizer
        .normalize(sb.toString(), Normalizer.Form.NFD)
        .replaceAll("[^\\p{ASCII}]", "")
        .toUpperCase()
        .replaceAll("[^<A-Z0-9]", "<");
  }

}
