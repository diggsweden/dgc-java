/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.uvci;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for UVCIBuilder.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class UVCIBuilderTest {

  @Test
  public void testGenerateOption1() {
    String uvci = UVCIBuilder.builder()
      .country("SE")
      .issuer("EHM")
      .vaccine("C878")
      .uniqueString("123456789ABC")
      .build();
    Assert.assertEquals(UVCIBuilder.UVCI_PREFIX + ":01:SE:EHM/C878/123456789ABC", uvci);

    uvci = UVCIBuilder.builder()
      .country("SE")
      .issuer("EHM")
      .vaccine("C878")
      .uniqueString("123456789ABC")
      .includeChecksum(true)
      .build();
    String expected = UVCIBuilder.UVCI_PREFIX + ":01:SE:EHM/C878/123456789ABC#";
    Assert.assertTrue(uvci.startsWith(expected));
    Assert.assertTrue(uvci.length() == expected.length() + 1);
  }

  @Test
  public void testGenerateOption2() {
    String uvci = UVCIBuilder.builder()
      .noPrefix()
      .version("02")
      .country("SE")
      .uniqueString("123456789ABC")
      .build();
    Assert.assertEquals("02:SE:123456789ABC", uvci);
  }
  
  @Test
  public void testGenerateOption3() {
    String uvci = UVCIBuilder.builder()
      .country("SE")
      .issuer("EHM")
      .uniqueString("123456789ABC")
      .build();
    Assert.assertEquals(UVCIBuilder.UVCI_PREFIX + ":01:SE:EHM/123456789ABC", uvci);

    uvci = UVCIBuilder.builder()
      .country("SE")
      .issuer("EHM")
      .uniqueString("123456789ABC")
      .includeChecksum(true)
      .build();
    String expected = UVCIBuilder.UVCI_PREFIX + ":01:SE:EHM/123456789ABC#";
    Assert.assertTrue(uvci.startsWith(expected));
    Assert.assertTrue(uvci.length() == expected.length() + 1);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testGenerateMissingIssuerOption1() {
    UVCIBuilder.builder()
      .country("SE")
      .vaccine("C878")
      .uniqueString("123456789ABC")
      .build();
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testGenerateMissingUniqueString() {
    UVCIBuilder.builder()
      .country("SE")
      .build();
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testGenerateMissingCountry() {
    UVCIBuilder.builder()
    .uniqueString("123456789ABC")
      .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidVersion() {
    UVCIBuilder.builder().version("AA");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidVersion2() {
    UVCIBuilder.builder().version("123");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidVersion3() {
    UVCIBuilder.builder().version("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidCountry() {
    UVCIBuilder.builder().country("S");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidCountry2() {
    UVCIBuilder.builder().country("SE1");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidCountry3() {
    UVCIBuilder.builder().country("SENO");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidCountry4() {
    UVCIBuilder.builder().country("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidIssuer() {
    UVCIBuilder.builder().issuer("ABC/");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidIssuer2() {
    UVCIBuilder.builder().issuer("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidIssuer3() {
    UVCIBuilder.builder().issuer(" ABC ");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidVaccine() {
    UVCIBuilder.builder().vaccine("ABC/");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidVaccine2() {
    UVCIBuilder.builder().vaccine("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidVaccine3() {
    UVCIBuilder.builder().vaccine(" ABC ");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUniqueString() {
    UVCIBuilder.builder().uniqueString("ABC/");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUniqueString2() {
    UVCIBuilder.builder().uniqueString("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUniqueString3() {
    UVCIBuilder.builder().uniqueString(" ABC ");
  }

}
