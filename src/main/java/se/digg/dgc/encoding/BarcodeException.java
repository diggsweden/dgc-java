/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.encoding;

/**
 * Exception class used for errors during creation and decoding of barcodes. 
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class BarcodeException extends Exception {

  /** For serializing. */
  private static final long serialVersionUID = 5460710221161561782L;

  /**
   * Constructor.
   * 
   * @param message the error message 
   */
  public BarcodeException(final String message) {
    super(message);
  }

  /**
   * Constructor.
   * 
   * @param message the error message
   * @param cause the cause of the error
   */
  public BarcodeException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
