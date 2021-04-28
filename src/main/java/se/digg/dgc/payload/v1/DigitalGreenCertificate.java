/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.payload.v1;

import java.io.IOException;
import java.time.LocalDate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * A wrapper for the root element generated from the Schema. This class offers methods for encoding and decoding to/from
 * CBOR and JSON.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class DigitalGreenCertificate extends Eudgc {

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
   * Default constructor.
   */
  public DigitalGreenCertificate() {
    super();
  }

  /**
   * Constructor assigning the subject name and date of birth of the subject.
   * 
   * @param name
   *          the subject name
   * @param dateOfBirth
   *          the date of birth of the subject
   */
  public DigitalGreenCertificate(final PersonName name, final LocalDate dateOfBirth) {
    this();
    this.setVer(DGCSchemaVersion.DGC_SCHEMA_VERSION);
    this.setNam(name);
    this.setDob(dateOfBirth);
  }

  /**
   * Encodes this object to its CBOR byte representation.
   * 
   * @return the CBOR encoding
   * @throws DGCSchemaException
   *           for encoding errors
   */
  public byte[] encode() throws DGCSchemaException {
    try {
      return cborMapper.writeValueAsBytes(this);
    }
    catch (final JsonProcessingException e) {
      throw new DGCSchemaException("Failed to serialize to CBOR", e);
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
  public static DigitalGreenCertificate decode(final byte[] cbor) throws DGCSchemaException {
    try {
      return cborMapper.readValue(cbor, DigitalGreenCertificate.class);
    }
    catch (final IOException e) {
      throw new DGCSchemaException("Failed to decode DGC from CBOR encoding", e);
    }
  }

  /**
   * Gets this object as a JSON string.
   * <p>
   * Mainly for debugging.
   * </p>
   * 
   * @return JSON string
   * @throws DGCSchemaException
   *           fort encoding errors
   */
  public String toJSONString() throws DGCSchemaException {
    try {
      return jsonMapper.writeValueAsString(this);
    }
    catch (final JsonProcessingException e) {
      throw new DGCSchemaException("Failed to serialize to JSON", e);
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
  public static DigitalGreenCertificate fromJsonString(final String json) throws DGCSchemaException {
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

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return super.toString();
  }

}
