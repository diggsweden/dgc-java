/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.payload.v1;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
 * {@code DigitalCovidCertificate} instance.
 * </p>
 * 
 * <pre>
 * final DigitalCovidCertificate dcc = (DigitalCovidCertificate) new DigitalCovidCertificate()
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
public class DigitalCovidCertificate extends Eudcc {

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
   * The specification dictates that we should tag date-time strings with 0, but during interoperability testing some
   * validator apps have had problems with this. Therefore, it is possible to turn off tagging.
   */
  private boolean tagDateTimes = true;

  /**
   * Default constructor.
   */
  public DigitalCovidCertificate() {
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
  public DigitalCovidCertificate(final PersonName name, final LocalDate dateOfBirth) {
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
  public Eudcc withNam(final PersonName name) {
    return super.withNam(transliterate(name));
  }

  /**
   * Gets the {@code dob} field as a {@link DateOfBirth} object.
   * 
   * @return a DateOfBirth object or null if the dob field is not present
   * @throws DateTimeException
   *           for date parsing errors
   */
  @JsonIgnore
  public DateOfBirth getDateOfBirth() throws DateTimeException {
    final String dob = this.getDob();
    return dob != null ? new DateOfBirth(dob) : null;
  }

  /**
   * Setter that takes a {@link LocalDate} representing the date of birth instead of string.
   * 
   * @param dob
   *          the date of birth
   */
  @JsonIgnore
  public void setDob(final LocalDate dob) {
    super.setDob(dob != null ? dob.toString() : null);
  }

  /**
   * Setter that takes a {@link DateOfBirth} representing the date of birth instead of string.
   * 
   * @param dob
   *          the date of birth
   */
  @JsonIgnore
  public void setDob(final DateOfBirth dob) {
    super.setDob(dob != null ? dob.toString() : null);
  }

  /**
   * An alternative to {@link Eudcc#withDob(String)} where the date of birth is represented as a {@link LocalDate}.
   * 
   * @param dob
   *          the date of birth
   * @return this object
   */
  public Eudcc withDob(final LocalDate dob) {
    return super.withDob(dob != null ? dob.toString() : null);
  }

  /**
   * An alternative to {@link Eudcc#withDob(String)} where the date of birth is represented as a {@link DateOfBirth}.
   * 
   * @param dob
   *          the date of birth
   * @return this object
   */
  public Eudcc withDob(final DateOfBirth dob) {
    return super.withDob(dob != null ? dob.toString() : null);
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
      final byte[] encoding = cborMapper.writeValueAsBytes(this);

      if (this.tagDateTimes) {
        final boolean containsTestEntries = this.getT() != null && this.getT().size() > 0;
        
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
        }
        return obj.EncodeToBytes();
      }
      else {
        return encoding;
      }
    }
    catch (final JsonProcessingException e) {
      throw new DGCSchemaException("Failed to serialize to CBOR", e);
    }
  }

  /**
   * Decodes a CBOR encoding to a {@link DigitalCovidCertificate}.
   * 
   * @param cbor
   *          the CBOR encoding
   * @return a DigitalCovidCertificate
   * @throws DGCSchemaException
   *           for decoding errors
   */
  public static DigitalCovidCertificate decode(final byte[] cbor) throws DGCSchemaException {
    try {
      return cborMapper.readValue(cbor, DigitalCovidCertificate.class);
    }
    catch (final IOException e) {
      throw new DGCSchemaException("Failed to decode DCC from CBOR encoding", e);
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
   * Decodes a JSON string into a {@link DigitalCovidCertificate}.
   * 
   * @param json
   *          the JSON representation
   * @return a DigitalCovidCertificate
   * @throws DGCSchemaException
   *           for decoding errors
   */
  public static DigitalCovidCertificate fromJsonString(final String json) throws DGCSchemaException {
    try {
      return jsonMapper.readValue(json, DigitalCovidCertificate.class);
    }
    catch (final IOException e) {
      throw new DGCSchemaException("Failed to decode DCC from JSON", e);
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
   * The specification dictates that we should tag date-time strings with 0, but during interoperability testing some
   * validator apps have had problems with this. Therefore, it is possible to turn off tagging.
   * <p>
   * The default is to add the CBOR tag 0 for date-times.
   * </p>
   * 
   * @param tagDateTimes
   *          whether to tag date-times.
   */
  public void setTagDateTimes(final boolean tagDateTimes) {
    this.tagDateTimes = tagDateTimes;
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
