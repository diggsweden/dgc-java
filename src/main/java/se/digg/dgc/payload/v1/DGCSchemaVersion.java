/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.payload.v1;

/**
 * Holds the schema version of the DGC schema.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class DGCSchemaVersion {

  /**
   * The version of the schema that we used to generate the {@link DigitalGreenCertificate} class.
   */
  public static final String DGC_SCHEMA_VERSION = "1.0.0";

  private DGCSchemaVersion() {
  }

}
