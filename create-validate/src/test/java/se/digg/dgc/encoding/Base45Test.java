/*
 * MIT License
 * 
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.encoding;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test cases for Base45.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
@RunWith(Enclosed.class)
public class Base45Test {

  /**
   * Parameterized Base45 tests.
   */
  @RunWith(Parameterized.class)
  public static class Base45ParameterizedTests {

    private final byte[] src;
    private final String expected;

    public Base45ParameterizedTests(final byte[] src, final String expected) {
      this.src = src;
      this.expected = expected;
    }

    @Parameters
    public static Collection<Object[]> data() {
      return Arrays.asList(new Object[][] {
          { new byte[] { 'A', 'B' }, new String(new byte[] { 'B', 'B', '8' }, StandardCharsets.US_ASCII) },
          { new byte[] { 'H', 'e', 'l', 'l', 'o', '!', '!' },
              new String(new byte[] { '%', '6', '9', ' ', 'V', 'D', '9', '2', 'E', 'X', '0' }, StandardCharsets.US_ASCII) },
          { new byte[] { 'b', 'a', 's', 'e', '-', '4', '5' },
              new String(new byte[] { 'U', 'J', 'C', 'L', 'Q', 'E', '7', 'W', '5', '8', '1' }, StandardCharsets.US_ASCII) },
          { new byte[] { 'i', 'e', 't', 'f', '!' },
              new String(new byte[] { 'Q', 'E', 'D', '8', 'W', 'E', 'X', '0' }, StandardCharsets.US_ASCII) }
      });
    }

    @Test
    public void encodeDecode() {
      final byte[] encodedBytes = Base45.getEncoder().encode(this.src);
      Assert.assertArrayEquals(this.expected.getBytes(StandardCharsets.UTF_8), encodedBytes);

      final byte[] decodedBytes = Base45.getDecoder().decode(encodedBytes);
      Assert.assertArrayEquals(this.src, decodedBytes);

      final String encodedString = Base45.getEncoder().encodeToString(this.src);
      Assert.assertEquals(this.expected, encodedString);

      final byte[] decodedBytes2 = Base45.getDecoder().decode(encodedString);
      Assert.assertArrayEquals(this.src, decodedBytes2);
    }
  }

  /**
   * Additional tests for Base45.
   */
  public static class Base45AdditionalTests {

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeBadInput() throws Exception {
      Base45.getDecoder().decode("Bb8");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDecodeBadInput2() throws Exception {
      Base45.getDecoder().decode(new byte[] { 'B', 'b', '8' });
    }
    
    @Test(expected = NullPointerException.class)
    public void testDecodeNull() throws Exception {
      Base45.getDecoder().decode((String) null);
    }
    
    @Test(expected = NullPointerException.class)
    public void testDecodeNull2() throws Exception {
      Base45.getDecoder().decode((byte[]) null);
    }
    
    @Test(expected = NullPointerException.class)
    public void testEncodeNull() throws Exception {
      Base45.getEncoder().encode(null);
    }
    
    @Test(expected = NullPointerException.class)
    public void testEncodeToStringNull() throws Exception {
      Base45.getEncoder().encodeToString(null);
    }
    
  }

}
