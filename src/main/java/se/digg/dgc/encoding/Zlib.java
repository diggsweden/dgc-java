/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.encoding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

/**
 * Compression/de-compression support.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class Zlib {

  /**
   * Compresses the supplied data.
   * 
   * @param data
   *          the data to compress
   * @return the compressed data
   */
  public static byte[] compress(final byte[] data) {

    final Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
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

  /**
   * De-compresses the supplied data.
   * <p>
   * If {@code requireCompressed} is {@code true} we require the input data to contain a ZLIB header (i.e., be
   * compressed using the ZLIB algorithm). If it is {@code false} and a {@link ZipException} is caught, the input data
   * is assumed to be un-compressed, and the method will return {@code data}.
   * </p>
   * 
   * @param data
   *          the data to de-compress
   * @param requireCompressed
   *          boolean telling whether we require the supplied data to be compressed
   * @return the de-compressed data
   * @throws ZipException
   *           if requireCompressed is true and an inflate error occurs
   */
  public static byte[] decompress(final byte[] data, final boolean requireCompressed) throws ZipException {

    final Inflater inflater = new Inflater();
    try {
      try (final ByteArrayInputStream bis = new ByteArrayInputStream(data);
          final InflaterInputStream iis = new InflaterInputStream(bis, inflater);
          final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

        bos.write(iis.readAllBytes());
        bos.flush();

        return bos.toByteArray();
      }
      catch (ZipException e) {
        if (requireCompressed) {
          throw e;
        }
        else {
          return data;
        }
      }
      catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    finally {
      inflater.end();
    }
  }

  /**
   * Predicate that checks if the supplied data is compressed using the ZLIB algorithm.
   * 
   * @param data
   *          the data to check
   * @return true if the supplied data is compressed and false otherwise
   */
  public static boolean isCompressed(final byte[] data) {
    return data[0] == 0x78;
  }

  // Hidden constructor
  private Zlib() {
  }

}
