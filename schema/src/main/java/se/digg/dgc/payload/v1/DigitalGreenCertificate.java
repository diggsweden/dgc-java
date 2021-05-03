/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.payload.v1;

import java.io.IOException;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import se.digg.dgc.transliteration.MrzEncoder;

/**
 * A wrapper for the root element generated from the Schema. This class offers methods for encoding and decoding to/from
 * CBOR and JSON.
 * <p>
 * The class also ensures that subject names are properly transliterated.
 * </p>
 * <p>
 * If you are using the builder pattern (withXX-methods) you need to cast the result to a
 * {@code DigitalGreenCertificate} instance.
 * </p>
 * 
 * <pre>
 * final DigitalGreenCertificate dgc = (DigitalGreenCertificate) new DigitalGreenCertificate()
 *       .withNam(new PersonName().withGn("...").withFn("..."))
 *       .withDob(LocalDate.parse("1969-11-11"))
 *       .withV(Arrays.asList(new VaccinationEntry()
 *       .withTg("840539006")
 *       .withVp("1119349007")
 *       ...
 *       .withCi("01:SE:JKJKHJGHG6768686HGJGH#M")));
 * </pre>
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
    cborMapper.setSerializationInclusion(Include.NON_NULL);
    cborMapper.setSerializationInclusion(Include.NON_EMPTY);    

    jsonMapper.registerModule(new JavaTimeModule());
    jsonMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    jsonMapper.setSerializationInclusion(Include.NON_NULL);
    jsonMapper.setSerializationInclusion(Include.NON_EMPTY);
  }

  /**
   * Default constructor.
   */
  public DigitalGreenCertificate() {
    super();
    this.setVer(DGCSchemaVersion.DGC_SCHEMA_VERSION);
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
   * Makes sure that the names supplied are transliterated.
   */
  @Override
  public void setNam(final PersonName name) {
    super.setNam(transliterate(name));
  }

  /**
   * Makes sure that the names supplied are transliterated.
   */
  @Override
  public Eudgc withNam(final PersonName name) {
    return super.withNam(transliterate(name));
  }

  /**
   * Ensures that the parts of the supplied name are transliterated.
   * 
   * @param name
   *          the name
   * @return the modified name (with transliteration)
   */
  private static PersonName transliterate(final PersonName name) {
    if (name != null) {
      if (name.getFnt() == null && name.getFn() != null) {
        name.setFnt(MrzEncoder.encode(name.getFn()));
      }
      if (name.getGnt() == null && name.getGn() != null) {
        name.setGnt(MrzEncoder.encode(name.getGn()));
      }
    }
    return name;
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
