/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.valueset.v1;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Representation of a value set.
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "valueSetId", "valueSetDate", "valueSetValues" })
public class ValueSet {

  /** The JSON mapper. */
  private static ObjectMapper jsonMapper = new ObjectMapper();

  static {
    jsonMapper.registerModule(new JavaTimeModule());
    jsonMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }

  /** Value set ID. */
  @JsonProperty("valueSetId")
  @JsonPropertyDescription("Value Set Identifier")
  @NotNull
  private String id;

  /** Value set date - version. */
  @JsonProperty("valueSetDate")
  @JsonPropertyDescription("Value Set Version")
  @NotNull
  private LocalDate date;

  /** The value set values. */
  @JsonProperty("valueSetValues")
  @JsonPropertyDescription("Allowed values in Value Set")
  @NotNull
  private Map<String, ValueSetValue> values;

  /**
   * Constructor.
   */
  public ValueSet() {
  }

  /**
   * All-args constructor.
   *
   * @param id
   *          the value set ID
   * @param date
   *          the value set version
   * @param values
   *          the value set values
   */
  public ValueSet(final String id, final LocalDate date, final Map<String, ValueSetValue> values) {
    this.id = Optional.ofNullable(id).orElseThrow(() -> new IllegalArgumentException("valueSetId is required"));
    this.date = date;
    this.values = values;
  }

  /**
   * Constructor initializing the object from a stream holding the JSON representation of the value set (for example a
   * JSON file).
   * 
   * @param stream
   *          the input stream
   * @throws IOException
   *           for JSON parsing errors
   */
  public ValueSet(final InputStream stream) throws IOException {
    this(jsonMapper.readValue(stream, ValueSet.class));
  }

  /**
   * Copy constructor.
   * 
   * @param valueSet
   *          the value set to copy from
   */
  protected ValueSet(final ValueSet valueSet) {
    this(valueSet.getId(), valueSet.getDate(), valueSet.getValues());
  }

  /**
   * Given a code for a value the method gets the value set value.
   * 
   * @param code
   *          the code
   * @return the value or null if it is not found
   */
  public ValueSetValue getValue(final String code) {    
    return this.getValues().get(code); 
  }

  /**
   * Gets the value set ID
   *
   * @return the ID
   */
  public String getId() {
    return this.id;
  }

  /**
   * Sets the value set ID
   *
   * @param id
   *          the ID
   */
  public void setId(final String id) {
    this.id = Optional.ofNullable(id).orElseThrow(() -> new IllegalArgumentException("valueSetId is required"));
  }

  /**
   * Gets the value set date/version.
   *
   * @return the date/version
   */
  public LocalDate getDate() {
    return this.date;
  }

  /**
   * Sets the value set date/version.
   *
   * @param date
   *          the date/version
   */
  public void setDate(final LocalDate date) {
    this.date = date;
  }

  /**
   * Gets the value set values.
   *
   * @return the values
   */
  public Map<String, ValueSetValue> getValues() {
    return this.values != null ? this.values : Collections.emptyMap();
  }

  /**
   * Sets the value set values
   *
   * @param values
   *          the values to set
   */
  public void setValues(final Map<String, ValueSetValue> values) {
    this.values = values;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return String.format("ValueSet [id='%s', date='%s', values=%s]", this.id, this.date, this.values);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return Objects.hash(this.date, this.id, this.values);
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ValueSet)) {
      return false;
    }
    final ValueSet other = (ValueSet) obj;
    return Objects.equals(this.date, other.date)
        && Objects.equals(this.id, other.id)
        && Objects.equals(this.values, other.values);
  }

}
