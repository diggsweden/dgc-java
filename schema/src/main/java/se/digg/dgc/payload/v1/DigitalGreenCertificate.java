/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.payload.v1;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.upokecenter.cbor.CBORObject;

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
    SimpleModule timeModule = new JavaTimeModule();
    timeModule.addDeserializer(Instant.class, CustomInstantDeserializer.INSTANT);

    cborMapper.registerModule(timeModule);
    cborMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    cborMapper.setSerializationInclusion(Include.NON_NULL);
    cborMapper.setSerializationInclusion(Include.NON_EMPTY);

    jsonMapper.registerModule(timeModule);
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
      final boolean containsTestEntries = this.getT() != null && this.getT().size() > 0;
      final byte[] encoding = cborMapper.writeValueAsBytes(this);
      
      // If this object contains test entries we use CBORObject to make sure that
      // all Instant's are encoded as tagged strings. FasterXML won't include the tag.
      //
      if (!containsTestEntries) {
        return encoding;
      }
      final CBORObject obj = CBORObject.DecodeFromBytes(encoding);
      final CBORObject tArr = obj.get("t");
      for (int i = 0; i < tArr.size(); i++) {
        final CBORObject tObj = tArr.get(i);
        final CBORObject sc = tObj.get("sc");
        if (sc != null && !sc.HasMostOuterTag(0)) {
          tObj.set("sc", CBORObject.FromObjectAndTag(sc, 0));
        }
        final CBORObject dr = tObj.get("dr");
        if (dr != null && !dr.HasMostOuterTag(0)) {
          tObj.set("dr", CBORObject.FromObjectAndTag(dr, 0));
        }
      }
      return obj.EncodeToBytes();
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

  /**
   * Gets a configured {@link ObjectMapper} to use for JSON serializing and deserializing.
   * 
   * @return an ObjectMapper
   */
  public static ObjectMapper getJSONMapper() {
    return jsonMapper;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return super.toString();
  }

  /**
   * Deserializer that can handle also ISO OFFSET.
   */
  private static class CustomInstantDeserializer extends InstantDeserializer<Instant> {

    private static final long serialVersionUID = 3929100820024454525L;

    public static final CustomInstantDeserializer INSTANT = new CustomInstantDeserializer(
      Instant.class, DateTimeFormatter.ISO_OFFSET_DATE_TIME,
      Instant::from,
      a -> Instant.ofEpochMilli(a.value),
      a -> Instant.ofEpochSecond(a.integer, a.fraction),
      null,
      true);

    protected CustomInstantDeserializer(final Class<Instant> supportedType,
        final DateTimeFormatter formatter,
        final Function<TemporalAccessor, Instant> parsedToValue,
        final Function<FromIntegerArguments, Instant> fromMilliseconds,
        final Function<FromDecimalArguments, Instant> fromNanoseconds,
        final BiFunction<Instant, ZoneId, Instant> adjust,
        final boolean replaceZeroOffsetAsZ) {

      super(supportedType, formatter, parsedToValue, fromMilliseconds, fromNanoseconds, adjust, replaceZeroOffsetAsZ);
    }
  }

}
