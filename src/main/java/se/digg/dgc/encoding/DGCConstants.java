/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.encoding;

/**
 * Constants for Digital Green Certificates.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class DGCConstants {

  /**
   * Header string that is prefixed to Base45 encoded health care certificates containing version 1 of the Digital Green
   * Certificate payloads.
   */
  public static final String DGC_V1_HEADER = "HC1:";
  
  // Hidden constructor
  private DGCConstants() {
  }

}
