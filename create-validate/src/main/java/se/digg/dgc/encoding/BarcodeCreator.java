/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.encoding;

import java.nio.charset.Charset;

/**
 * An interface for creating barcodes (QR/Aztec).
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public interface BarcodeCreator {

  /**
   * Creates a barcode containing the supplied contents.
   * 
   * @param contents
   *          the contents
   * @return a Barcode
   * @throws BarcodeException
   *           for creation errors
   */
  Barcode create(final String contents) throws BarcodeException;

  /**
   * Creates a barcode containing the supplied contents which is a string of the given character set.
   * 
   * @param contents
   *          the contents
   * @param characterSet
   *          the character set
   * @return a Barcode
   * @throws BarcodeException
   *           for creation errors
   */
  Barcode create(final String contents, final Charset characterSet) throws BarcodeException;

}
