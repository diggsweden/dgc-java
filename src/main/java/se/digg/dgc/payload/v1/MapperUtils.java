/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.payload.v1;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Utility methods for serializing and deserializing a {@link DigitalGreenCertificate}.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class MapperUtils {

  /** The CBOR mapper. */
  private static CBORMapper cborMapper = new CBORMapper();

  /** The JSON mapper. */
  private static ObjectMapper jsonMapper = new ObjectMapper();

  static {
    cborMapper.registerModule(new JavaTimeModule());
    cborMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    jsonMapper.registerModule(new JavaTimeModule());
    jsonMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }

  /**
   * Given a HCERT payload its CBOR encoding is returned.
   * 
   * @param dgc
   *          the HCERT/DGC payload to encode
   * @return the CBOR encoding
   * @throws DGCSchemaException
   *           for encoding errors
   */
  public static byte[] toCBOREncoding(final DigitalGreenCertificate dgc) throws DGCSchemaException {
    try {
      return cborMapper.writeValueAsBytes(dgc);
    }
    catch (final JsonProcessingException e) {
      throw new DGCSchemaException("Failed to serialize to CBOR", e);
    }
  }

  /**
   * Given a HCERT payload its string JSON representation is returned.
   * <p>
   * Mainly for debugging.
   * </p>
   * 
   * @param dgc
   *          the HCERT/DGC payload to encode
   * @return JSON string
   * @throws DGCSchemaException
   *           fort encoding errors
   */
  public static String toJSONString(final DigitalGreenCertificate dgc) throws DGCSchemaException {
    try {
      return jsonMapper.writeValueAsString(dgc);
    }
    catch (final JsonProcessingException e) {
      throw new DGCSchemaException("Failed to serialize to JSON", e);
    }
  }

  /**
   * Decodes a CBOR encoding to a {@link DigitalGreenCertificate}.
   * 
   * @param cbor
   *          the CBOR encoding
   * @return a DigitalGreenCertificate
   * @throws DGCSchemaException
   *           for decoding errors
   */
  public static DigitalGreenCertificate toDigitalGreenCertificate(final byte[] cbor) throws DGCSchemaException {
    try {
      return cborMapper.readValue(cbor, DigitalGreenCertificate.class);
    }
    catch (final IOException e) {
      throw new DGCSchemaException("Failed to decode DGC from CBOR encoding", e);
    }
  }

  /**
   * Decodes a JSON string into a {@link DigitalGreenCertificate}.
   * 
   * @param json
   *          the JSON representation
   * @return a DigitalGreenCertificate
   * @throws DGCSchemaException
   *           for decoding errors
   */
  public static DigitalGreenCertificate toDigitalGreenCertificate(final String json) throws DGCSchemaException {
    try {
      return jsonMapper.readValue(json, DigitalGreenCertificate.class);
    }
    catch (final IOException e) {
      throw new DGCSchemaException("Failed to decode DGC from JSON", e);
    }
  }

  /**
   * Gets a configured {@link CBORMapper} to use for serializing and deserializing.
   * 
   * @return a CBORMapper
   */
  public static CBORMapper getCBORMapper() {
    return cborMapper;
  }

  // Hidden constructor
  private MapperUtils() {
  }

}
