/*
 * MIT License
 * 
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.signatures.cose;

import com.upokecenter.cbor.CBORObject;

/**
 * Representation of COSE header parameter keys.
 * <p>
 * Only those relevant for our use case are represented.
 * </p>
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public enum HeaderParameterKey {

  /** Algorithm used for security processing. */
  ALG(1),

  /** Critical headers to be understood. */
  CRIT(2),

  /** This parameter is used to indicate the content type of the data in the payload or ciphertext fields. */
  CONTENT_TYPE(3),

  /** This parameter identifies one piece of data that can be used as input to find the needed cryptographic key. */
  KID(4);

  /** The CBOR value for this header parameter. */
  private CBORObject value;

  /**
   * Constructor.
   * 
   * @param val
   *          the value for the parameter key
   */
  private HeaderParameterKey(final int val) {
    this.value = CBORObject.FromObject(val);
  }

  /**
   * Given a CBOR object the method gets the corresponding {@code HeaderParameterKey}.
   * 
   * @param value
   *          the value
   * @return a HeaderParameterKey
   */
  public static HeaderParameterKey fromCborObject(final CBORObject value) {
    if (value == null) {
      throw new IllegalArgumentException("value must not be null");
    }
    for (final HeaderParameterKey p : HeaderParameterKey.values()) {
      if (value.equals(p.value))
        return p;
    }
    throw new IllegalArgumentException("No HeaderParameterKey matching " + value);
  }

  /**
   * Given a value the method gets the corresponding {@code HeaderParameterKey}.
   * 
   * @param value
   *          the value
   * @return a HeaderParameterKey
   */
  public static HeaderParameterKey fromValue(final int value) {
    for (final HeaderParameterKey p : HeaderParameterKey.values()) {

      if (p.value.AsInt32Value() == value) {
        return p;
      }
    }
    throw new IllegalArgumentException("No HeaderParameterKey matching " + value);
  }

  /**
   * Gets the header parameter key as a CBOR object.
   * 
   * @return a CBORObject
   */
  public CBORObject getCborObject() {
    return this.value;
  }

  /**
   * Gets the CBOR value for the header parameter key.
   * 
   * @return the value
   */
  public int getValue() {
    return this.value.AsInt32Value();
  }

}
