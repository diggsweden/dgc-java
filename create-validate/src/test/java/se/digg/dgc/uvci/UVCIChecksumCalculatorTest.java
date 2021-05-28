/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.uvci;

import java.security.SecureRandom;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for UVCIChecksumCalculator.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class UVCIChecksumCalculatorTest {
  
  private static final char[] UVCI_CHAR_SET = {
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
      'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
      'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '/', ':' };

  private static final SecureRandom random = new SecureRandom();

  @Test
  public void testAgaistWiki() throws Exception {
    Assert.assertTrue(UVCIChecksumCalculator.validateChecksum("URN:UVCI:01:NL:187/37512422923#Z")); 
  }
  
  @Test
  public void testGenerate() throws Exception {
    
    for (int i = 0; i < 10000; i++) {
      final String data = generateRandomString(40);
      final char c = UVCIChecksumCalculator.calculateChecksum(data);
      final String dataWCS = UVCIChecksumCalculator.addChecksum(data);
      Assert.assertTrue(dataWCS.endsWith("#" + c));
      
      UVCIChecksumCalculator.validateChecksum(dataWCS);
    }
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidChars() throws Exception {
    UVCIChecksumCalculator.calculateChecksum("jkdsda");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidChars2() throws Exception {
    UVCIChecksumCalculator.calculateChecksum("   98  ");
  }
    
  @Test(expected = IllegalArgumentException.class)
  public void testEmptyString() throws Exception {
    UVCIChecksumCalculator.calculateChecksum("");
  }
  
  private static String generateRandomString(int length) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < length; i++) {
      sb.append(UVCI_CHAR_SET[random.nextInt(UVCI_CHAR_SET.length)]);
    }
    return sb.toString();
  }
  

}
