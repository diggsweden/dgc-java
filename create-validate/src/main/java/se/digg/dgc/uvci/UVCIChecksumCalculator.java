/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.uvci;

/**
 * A utility to calculate the checksum for UVCI:s according to
 * <a href="https://github.com/ehn-digital-green-development/ehn-dgc-schema/wiki/FAQ#uvci-checksum">the DCC schema
 * wiki</a>.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class UVCIChecksumCalculator {

  /** The character set for the Luhn mod N calculation. */
  public static final String LUHN_MOD_N_CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789/:";
  
  private static final String VALID_REGEXP = "^[" + LUHN_MOD_N_CHARSET + "]+$";

  /** The delimiter character used to separate the UVCI from the checksum character. */
  public static final char DELIMITER = '#';

  /**
   * Calculates the control character for the supplied input. Note that the input must only contain characters from the
   * {@link #LUHN_MOD_N_CHARSET}.
   *
   * <p>
   * Implementation stolen from <a href="https://en.wikipedia.org/wiki/Luhn_mod_N_algorithm#Algorithm_in_Java">Wikipedia</a>.
   * </p>
   *
   * @param input
   *          the input
   * @return the checksum control character
   */
  public static char calculateChecksum(final String input) {
    
//    if (!input.chars().allMatch(c -> LUHN_MOD_N_CHARSET.indexOf(c) != -1)) {
//      throw new IllegalArgumentException("Supplied input contains illegal characters");
//    }
    
    if (!input.matches(VALID_REGEXP)) {
      throw new IllegalArgumentException("Supplied input contains illegal characters");
    }

    int factor = 2;
    int sum = 0;
    int n = LUHN_MOD_N_CHARSET.length();

    // Starting from the right and working leftwards is easier since
    // the initial "factor" will always be "2".
    for (int i = input.length() - 1; i >= 0; i--) {
      int codePoint = LUHN_MOD_N_CHARSET.indexOf(input.charAt(i));
      int addend = factor * codePoint;

      // Alternate the "factor" that each "codePoint" is multiplied by
      factor = (factor == 2) ? 1 : 2;

      // Sum the digits of the "addend" as expressed in base "n"
      addend = (addend / n) + (addend % n);
      sum += addend;
    }

    // Calculate the number that must be added to the "sum"
    // to make it divisible by "n".
    int remainder = sum % n;
    int checkCodePoint = (n - remainder) % n;

    return LUHN_MOD_N_CHARSET.charAt(checkCodePoint);
  }

  /**
   * Given an UVCI, the method calculates a checksum character and returns the modified string (with a checksum
   * appended).
   * 
   * @param input
   *          the input
   * @return the string with an appended checksum character
   */
  public static String addChecksum(final String input) {
    final char checksum = calculateChecksum(input);
    return input + DELIMITER + checksum;
  }

  /**
   * Given a string containing a checksum control character the method controls whether this is correct.
   * <p>
   * If the supplied string doesn't contain a checksum control character {@code false} is returned.
   * </p>
   * 
   * @param data
   *          the string to check
   * @return if the present checksum control character is correct true is returned, otherwise false
   */
  public static boolean validateChecksum(final String data) {
    try {
      if (data.length() > 2 && data.charAt(data.length() - 2) == DELIMITER) {
        final char expected = data.charAt(data.length() - 1);
        final char checksum = calculateChecksum(data.substring(0, data.length() - 2));
        return expected == checksum;
      }
      else {
        // Passed data does not contain a checksum character.
        return false;
      }
    }
    catch (Exception e) {
      return false;
    }
  }

  // Hidden constructor.
  private UVCIChecksumCalculator() {
  }

}
