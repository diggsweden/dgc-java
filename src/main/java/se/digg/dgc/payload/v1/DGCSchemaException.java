/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.payload.v1;

/**
 * Exception for DGC schema errors, i.e., if a DGC encoding cannot be mapped into the Java classes for the DGC schema.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class DGCSchemaException extends Exception {

  /** For serializing. */
  private static final long serialVersionUID = -1857239970468537025L;

  /**
   * Constructor.
   * 
   * @param message the error message
   */
  public DGCSchemaException(final String message) {
    super(message);
  }

  /**
   * Constructor.
   * 
   * @param message the error message
   * @param cause the cause of the error
   */
  public DGCSchemaException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
