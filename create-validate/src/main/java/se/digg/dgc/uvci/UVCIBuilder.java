/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.uvci;

/**
 * A builder that can be used to create any of the three options of UVCI:s as described in annex 2 of <a href=
 * "https://ec.europa.eu/health/sites/default/files/ehealth/docs/vaccination-proof_interoperability-guidelines_en.pdf">eHealthNetwork
 * Vaccination Interoperability Guidelines</a>.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class UVCIBuilder {

  /** Prefix for UVCI strings. */
  public static final String UVCI_PREFIX = "URN:UVCI";

  /** The current versin of the UVCI. */
  public static final String UVCI_VERSION = "01";

  /** The regexp for a valid version. */
  private static final String VERSION_REGEXP = "^[0-9][0-9]$";

  /** The regexp for a valid country code. */
  private static final String COUNTRY_REGEXP = "[A-Z]{2,3}";

  /** The regexp for other contents added. */
  private static final String CONTENTS_REGEXP = "^[0-9A-Z]+$";

  /** The prefix. */
  private String prefix = UVCI_PREFIX;

  /** The version string. */
  private String version;

  /** The country code. */
  private String country;

  /** The issuing entity (for option 1 and 3). */
  private String issuer;

  /** The vaccine (for option 1). */
  private String vaccine;

  /** The opaque unique string. */
  private String uniqueString;

  /** Whether a checksum control character should be appended to the UVCI. */
  private boolean includeChecksum = false;

  /**
   * Default constructor.
   */
  public UVCIBuilder() {
  }

  /**
   * Creates a builder.
   * 
   * @return a UVCIBuilder
   */
  public static UVCIBuilder builder() {
    return new UVCIBuilder();
  }

  /**
   * Builds the UVCI.
   * 
   * @return an UVCI string
   */
  public String build() {
    if (this.country == null) {
      throw new IllegalArgumentException("Country has not been assigned");
    }
    if (this.uniqueString == null) {
      throw new IllegalArgumentException("Opaque unique string has not been assigned");
    }
    
    StringBuilder sb = new StringBuilder();
    if (this.prefix != null) {
      sb.append(UVCI_PREFIX).append(':');
    }
    sb.append(this.version != null ? this.version : UVCI_VERSION).append(':');
    sb.append(this.country).append(':');
    if (this.issuer != null) {
      sb.append(this.issuer).append('/');
    }
    if (this.vaccine != null) {
      if (this.issuer == null) {
        throw new IllegalArgumentException(
          "Invalid UVCI - If vaccine info is assigned, the issuing entity is also required");
      }
      sb.append(this.vaccine).append('/');
    }
    sb.append(this.uniqueString);
    
    if (this.includeChecksum) {
      return UVCIChecksumCalculator.addChecksum(sb.toString());
    }
    else {    
      return sb.toString();
    }
  }

  /**
   * Tells the builder that the {@link #UVCI_PREFIX} should be used for the generated UVCI. This is the default
   * behaviour.
   * 
   * @return this builder
   */
  public UVCIBuilder prefix() {
    this.prefix = UVCI_PREFIX;
    return this;
  }

  /**
   * Tells the builder that no UVCI prefix should be used.
   * 
   * @return this builder
   */
  public UVCIBuilder noPrefix() {
    this.prefix = null;
    return this;
  }

  /**
   * Assigns the version.
   * 
   * <p>
   * If not assigned, the {@link #UVCI_VERSION} is used.
   * </p>
   * 
   * @param version
   *          the version
   * @return this builder
   */
  public UVCIBuilder version(final String version) {
    if (version != null) {
      if (!version.matches(VERSION_REGEXP)) {
        throw new IllegalArgumentException("Not a valid version string");
      }
      this.version = version;
    }
    return this;
  }

  /**
   * Assigns the country code (ISO 3166-1 alhpa-2 or alpha-3).
   * 
   * @param country
   *          the country code
   * @return this builder
   */
  public UVCIBuilder country(final String country) {
    if (country != null) {
      if (!country.matches(COUNTRY_REGEXP)) {
        throw new IllegalArgumentException("Not a valid country code (ISO 3166-1)");
      }
    }
    this.country = country;
    return this;
  }

  /**
   * Assigns the issuing entity (for option 1 and 3).
   * 
   * @param issuer
   *          issuing entity
   * @return this builder
   */
  public UVCIBuilder issuer(final String issuer) {
    if (issuer != null) {
      if (!issuer.matches(CONTENTS_REGEXP)) {
        throw new IllegalArgumentException("Issuer contains invalid characters");
      }
    }
    this.issuer = issuer;
    return this;
  }

  /**
   * Assigns the vaccine info (for option 1 only).
   * 
   * @param vaccine
   *          the vaccine info
   * @return this builder
   */
  public UVCIBuilder vaccine(final String vaccine) {
    if (vaccine != null) {
      if (!vaccine.matches(CONTENTS_REGEXP)) {
        throw new IllegalArgumentException("Vaccine string contains invalid characters");
      }
    }
    this.vaccine = vaccine;
    return this;
  }

  /**
   * Assigns the opaque unique string of the UVCI.
   * 
   * @param uniqueString
   *          the unique string
   * @return this builder
   */
  public UVCIBuilder uniqueString(final String uniqueString) {
    if (uniqueString != null) {
      if (!uniqueString.matches(CONTENTS_REGEXP)) {
        throw new IllegalArgumentException("Opaque unique string contains invalid characters");
      }
    }
    this.uniqueString = uniqueString;
    return this;
  }

  /**
   * Tells whether a checksum control character should be appended to the UVCI. The default is not to include the
   * checksum.
   * 
   * @param include
   *          flag telling whether to generate a checksum
   * @return this builder
   */
  public UVCIBuilder includeChecksum(final boolean include) {
    this.includeChecksum = include;
    return this;
  }

}
