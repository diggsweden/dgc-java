/*
 * MIT License
 * 
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.hcert.signatures.cose;

import com.upokecenter.cbor.CBORObject;

/**
 * Representation of the supported signature algorithms.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public enum SignatureAlgorithm {

  /** ECDSA with SHA-256. */
  ES256(-7, "ES256", "SHA256withECDSA"),

  /** ECDSA with SHA-384. */
  ES384(-35, "ES384", "SHA384withECDSA"),

  /** ECDSA with SHA-512. */
  ES512(-36, "ES512", "SHA512withECDSA"),

  /** RSASSA-PSS with SHA-256. */
  PS256(-37, "PS256", "SHA256withRSA/PSS"),

  /** RSASSA-PSS with SHA-384. */
  PS384(-38, "PS384", "SHA384withRSA/PSS"),

  /** RSASSA-PSS with SHA-512. */
  PS512(-39, "PS512", "SHA512withRSA/PSS");

  /** The CBOR value for this algorithm identifier. */
  private final CBORObject value;

  /** The JOSE/COSE name for the algorithm. */
  private final String name;

  /** The JCA algorithm name for this algorithm. */
  private final String jcaAlgorithmName;

  /**
   * Constructor.
   * 
   * @param value
   *          the value for the signature identifier
   */
  private SignatureAlgorithm(final int value, final String name, final String jcaAlgorithmName) {
    this.value = CBORObject.FromObject(value);
    this.name = name;
    this.jcaAlgorithmName = jcaAlgorithmName;
  }

  /**
   * Given a CBOR object the method gets the corresponding {@code SignatureAlgorithmId}.
   * 
   * @param value
   *          the value
   * @return a SignatureAlgorithmId
   */
  public static SignatureAlgorithm fromCborObject(final CBORObject value) {
    if (value == null) {
      throw new IllegalArgumentException("value must not be null");
    }
    for (final SignatureAlgorithm a : SignatureAlgorithm.values()) {
      if (value.equals(a.value))
        return a;
    }
    throw new IllegalArgumentException("No SignatureAlgorithmID matching " + value);
  }

  /**
   * Given a value the method gets the corresponding {@code SignatureAlgorithmId}.
   * 
   * @param value
   *          the value
   * @return a SignatureAlgorithmId
   */
  public static SignatureAlgorithm fromValue(final int value) {
    for (final SignatureAlgorithm a : SignatureAlgorithm.values()) {

      if (a.value.AsInt32Value() == value) {
        return a;
      }
    }
    throw new IllegalArgumentException("No SignatureAlgorithmId matching " + value);
  }

  /**
   * Gets the signature identifier as a CBOR object.
   * 
   * @return a CBORObject
   */
  public CBORObject getCborObject() {
    return this.value;
  }

  /**
   * Gets the CBOR value for the signature identifier.
   * 
   * @return the value
   */
  public int getValue() {
    return this.value.AsInt32Value();
  }

  /**
   * Gets the JCA algorithm name for this algorithm.
   * 
   * @return the JCA algorithm name
   */
  public String getJcaAlgorithmName() {
    return this.jcaAlgorithmName;
  }

  /**
   * Gets the COSE/JOSE name of the algorithm.
   * 
   * @return the name
   */
  public String getName() {
    return this.name;
  }

}
