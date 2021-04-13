/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.hcert.service;

/**
 * Exception for HCERT schema errors, i.e., if a HCERT encoding cannot be mapped into the Java classes for the HCERT schema.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class HCertSchemaException extends Exception {

  /** For serializing. */
  private static final long serialVersionUID = -1857239970468537025L;

  /**
   * Constructor.
   * 
   * @param message the error message
   */
  public HCertSchemaException(final String message) {
    super(message);
  }

  /**
   * Constructor.
   * 
   * @param message the error message
   * @param cause the cause of the error
   */
  public HCertSchemaException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
