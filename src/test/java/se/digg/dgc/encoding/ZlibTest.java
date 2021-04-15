/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.encoding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.ZipException;

import org.junit.Assert;
import org.junit.Test;

import se.digg.dgc.encoding.Zlib;

/**
 * Test cases for Zlib.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class ZlibTest {

  @Test
  public void testDeflateInflate() throws Exception {

    final byte[] data = { 'M', 'A', 'R', 'T', 'I', 'N' };

    final byte[] compressed = Zlib.compress(data);
    
    Assert.assertTrue(Zlib.isCompressed(compressed));
    Assert.assertEquals((byte) 0x78, compressed[0]);
    Assert.assertEquals((byte) 0xDA, compressed[1]);

    final byte[] decompressed = Zlib.decompress(compressed, true);

    Assert.assertArrayEquals(data, decompressed);
  }

  @Test
  public void testNotCompressed() throws Exception {
    final byte[] data = { 'N', 'O', 'T', 'Z', 'I', 'P', 'P', 'E', 'D' };

    Assert.assertFalse(Zlib.isCompressed(data));
    
    try {
      Zlib.decompress(data, true);
      Assert.fail("Expected ZipException");
    }
    catch (ZipException e) {
    }

    final byte[] data2 = Zlib.decompress(data, false);
    Assert.assertArrayEquals(data, data2);
  }

  @Test
  public void testLevels() throws Exception {
    
    final byte[] data = { 'D', 'A', 'T', 'A' };

    final int[] levels = { Deflater.BEST_SPEED, Deflater.DEFAULT_COMPRESSION,
        Deflater.HUFFMAN_ONLY, Deflater.NO_COMPRESSION };
    
    for (int i = 0; i < levels.length; i++) {
      final byte[] compressed = testCompress(data, levels[i]);
      final byte[] decompressed = Zlib.decompress(compressed, true);
      Assert.assertArrayEquals(data, decompressed);
    }
  }

  private static byte[] testCompress(final byte[] data, final int level) {

    final Deflater deflater = new Deflater(level);
    try {
      try (final ByteArrayOutputStream bos = new ByteArrayOutputStream();
          final DeflaterOutputStream dos = new DeflaterOutputStream(bos, deflater)) {
        dos.write(data, 0, data.length);
        dos.finish();
        bos.flush();
        return bos.toByteArray();
      }
      catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    finally {
      deflater.end();
    }
  }

}
