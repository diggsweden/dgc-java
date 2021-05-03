/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.valueset.v1;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Representation of a valueset value.
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "display", "lang", "active", "version", "system" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValueSetValue {

  /** The display name of the value. */
  @JsonProperty("display")
  @JsonPropertyDescription("The display name of the value")
  private String display;

  /** The language code. */
  @JsonProperty("lang")
  @JsonPropertyDescription("The language code")
  private String lang;

  /** Whether this value is "active". */
  @JsonProperty("active")
  @JsonPropertyDescription("Whether this value is active")
  private boolean active;

  /** The version of the value. */
  @JsonProperty("version")
  @JsonPropertyDescription("The version of the value")
  private String version;

  /** The system. */
  @JsonProperty("system")
  @JsonPropertyDescription("The system")
  private String system;

  /**
   * Default constructor.
   */
  public ValueSetValue() {
  }

  /**
   * All-args constructor.
   *
   * @param display
   *          the display name of the value
   * @param lang
   *          the language code
   * @param active
   *          whether this value is "active"
   * @param version
   *          the version of the value
   * @param system
   *          the system
   */
  public ValueSetValue(final String display, final String lang, final boolean active, final String version, final String system) {
    this.display = display;
    this.lang = lang;
    this.active = active;
    this.version = version;
    this.system = system;
  }

  /**
   * Gets the display name of the value.
   * 
   * @return the display name
   */
  public String getDisplay() {
    return this.display;
  }

  /**
   * Sets the display name of the value.
   * 
   * @param display
   *          the display name
   */
  public void setDisplay(final String display) {
    this.display = display;
  }

  /**
   * Gets the language code.
   * 
   * @return the language code
   */
  public String getLang() {
    return this.lang;
  }

  /**
   * Sets the language code
   * 
   * @param lang
   *          the language code
   */
  public void setLang(final String lang) {
    this.lang = lang;
  }

  /**
   * Tells whether this value is active.
   * 
   * @return whether this value is active
   */
  public boolean isActive() {
    return this.active;
  }

  /**
   * Assigns whether this value is active
   * 
   * @param active
   *          active/not active
   */
  public void setActive(final boolean active) {
    this.active = active;
  }

  /**
   * Gets the version of the value.
   * 
   * @return the version
   */
  public String getVersion() {
    return this.version;
  }

  /**
   * Sets the version of the value.
   * 
   * @param version
   *          the version
   */
  public void setVersion(final String version) {
    this.version = version;
  }

  /**
   * Gets the system.
   * 
   * @return the system
   */
  public String getSystem() {
    return this.system;
  }

  /**
   * Sets the system.
   * 
   * @param system
   *          the system
   */
  public void setSystem(final String system) {
    this.system = system;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return String.format("[display='%s', lang='%s', active='%s', version='%s', system='%s']",
      this.display, this.lang, this.active, this.version, this.system);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return Objects.hash(this.active, this.display, this.lang, this.system, this.version);
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ValueSetValue)) {
      return false;
    }
    final ValueSetValue other = (ValueSetValue) obj;
    return this.active == other.active
        && Objects.equals(this.display, other.display)
        && Objects.equals(this.lang, other.lang)
        && Objects.equals(this.system, other.system)
        && Objects.equals(this.version, other.version);
  }

}
