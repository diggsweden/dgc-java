/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.hcert.encoding;

import java.nio.charset.Charset;

/**
 * An interface for decoding barcodes (QR/Aztec).
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public interface BarcodeDecoder {

  /**
   * Locates the barcode in the supplied image and decodes it.
   * 
   * @param image
   *          the bytes of the images holding the barcode
   * @return the contents of the barcode
   * @throws BarcodeException
   *           for decoding errors
   */
  byte[] decode(final byte[] image) throws BarcodeException;

  /**
   * Locates the barcode in the supplied image and decodes it into a string of the given character set.
   * 
   * @param image
   *          the bytes of the images holding the barcode
   * @param characterSet
   *          the character set
   * @return the contents of the barcode
   * @throws BarcodeException
   *           for decoding errors
   */
  String decodeToString(final byte[] image, final Charset characterSet) throws BarcodeException;

}
