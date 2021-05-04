/*
 * MIT License
 * 
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.signatures.cwt;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.upokecenter.cbor.CBORException;
import com.upokecenter.cbor.CBORObject;
import com.upokecenter.cbor.CBORType;

import se.digg.dgc.signatures.cwt.support.CBORInstantConverter;

/**
 * A representation of a CWT according to <a href="https://tools.ietf.org/html/rfc8392">RFC 8392</a>.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class Cwt {

  /** 
   * HCERT message tag. See <a href="https://www.iana.org/assignments/cwt/cwt.xhtml">https://www.iana.org/assignments/cwt/cwt.xhtml</a>. 
   */  
  public static final int HCERT_CLAIM_KEY = -260;
  
  // TODO: remove after interop-tests ...
  public static final int OLD_HCERT_CLAIM_KEY = -65537;

  /** The message tag for eu_dgc_v1 that is added under the HCERT claim. */
  public static final int EU_DGC_V1_MESSAGE_TAG = 1;
  
  /** The CBOR CWT message tag. */
  public static final int MESSAGE_TAG = 61;

  /** For handling of instants. */
  private static final CBORInstantConverter instantConverter = new CBORInstantConverter();

  /** The CBOR representation of the CWT. */
  private final CBORObject cwtObject;

  /**
   * Constructor creating an empty CWT.
   */
  public Cwt() {
    this.cwtObject = CBORObject.NewMap();
  }

  /**
   * Constructor creating a CWT from a supplied encoding.
   * 
   * @param data
   *          the encoding
   * @throws CBORException
   *           if the supplied encoding is not a valid CWT
   */
  public Cwt(final byte[] data) throws CBORException {
    final CBORObject object = CBORObject.DecodeFromBytes(data);
    if (object.getType() != CBORType.Map) {
      throw new CBORException("Not a valid CWT");
    }
    this.cwtObject = object;
  }

  /**
   * Creates a {@link CwtBuilder}.
   * 
   * @return a CwtBuilder
   */
  public static CwtBuilder builder() {
    return new CwtBuilder();
  }

  /**
   * Decodes the supplied data into a Cwt object.
   * 
   * @param data
   *          the encoded data
   * @return a Cwt object
   * @throws CBORException
   *           if the supplied encoding is not a valid CWT
   */
  public static Cwt decode(final byte[] data) throws CBORException {
    return new Cwt(data);
  }

  /**
   * Gets the binary representation of the CWT.
   * 
   * @return a byte array
   */
  public byte[] encode() {
    return this.cwtObject.EncodeToBytes();
  }

  /**
   * Sets the "iss" (issuer) claim.
   * 
   * @param issuer
   *          the issuer value
   */
  public void setIssuer(final String issuer) {
    this.cwtObject.set(1, CBORObject.FromObject(issuer));
  }

  /**
   * Gets the "iss" (issuer) claim.
   * 
   * @return the issuer value, or null
   */
  public String getIssuer() {
    return Optional.ofNullable(this.cwtObject.get(1)).map(CBORObject::AsString).orElse(null);
  }

  /**
   * Sets the "sub" (subject) claim.
   * 
   * @param subject
   *          the subject value
   */
  public void setSubject(final String subject) {
    this.cwtObject.set(2, CBORObject.FromObject(subject));
  }

  /**
   * Gets the "sub" (subject) claim.
   * 
   * @return the subject value, or null
   */
  public String getSubject() {
    return Optional.ofNullable(this.cwtObject.get(2)).map(CBORObject::AsString).orElse(null);
  }

  /**
   * Sets a single value to the "aud" (audience) claim.
   * 
   * @param audience
   *          the audience value
   */
  public void setAudience(final String audience) {
    this.cwtObject.set(3, CBORObject.FromObject(audience));
  }

  /**
   * Sets multiple values to the "aud" (audience) claim.
   * 
   * @param audiences
   *          the values
   */
  public void setAudience(final List<String> audiences) {
    if (audiences != null && audiences.size() == 1) {
      this.setAudience(audiences.get(0));
    }
    else if (audiences != null && !audiences.isEmpty()) {
      final CBORObject arr = CBORObject.NewArray();
      for (String a : audiences) {
        arr.Add(CBORObject.FromObject(a));
      }
      this.cwtObject.set(3, arr);
    }
  }

  /**
   * Gets the values for the "aud" claim
   * 
   * @return the value, or null
   */
  public List<String> getAudience() {
    final CBORObject aud = this.cwtObject.get(3);
    if (aud == null) {
      return null;
    }
    if (aud.getType() == CBORType.Array) {
      final Collection<CBORObject> values = aud.getValues();
      return values.stream().map(CBORObject::AsString).collect(Collectors.toList());
    }
    else {
      return Arrays.asList(aud.AsString());
    }
  }

  /**
   * Sets the "exp" (expiration time) claim.
   * 
   * @param exp
   *          the expiration
   */
  public void setExpiration(final Instant exp) {
    this.cwtObject.set(4, instantConverter.ToCBORObject(exp));
  }

  /**
   * Gets the value of the "exp" (expiration time) claim.
   * 
   * @return the instant, or null
   */
  public Instant getExpiration() {
    return instantConverter.FromCBORObject(this.cwtObject.get(4));
  }

  /**
   * Sets the "nbf" (not before) claim.
   * 
   * @param exp
   *          the not before time
   */
  public void setNotBefore(final Instant nbf) {
    this.cwtObject.set(5, instantConverter.ToCBORObject(nbf));
  }

  /**
   * Gets the value of the "nbf" (not before) claim.
   * 
   * @return the instant, or null
   */
  public Instant getNotBefore() {
    return instantConverter.FromCBORObject(this.cwtObject.get(5));
  }

  /**
   * Sets the "iat" (issued at) claim.
   * 
   * @param iat
   *          the issued at time
   */
  public void setIssuedAt(final Instant iat) {
    this.cwtObject.set(6, instantConverter.ToCBORObject(iat));
  }

  /**
   * Gets the value of the "iat" (issued at) claim.
   * 
   * @return the instant, or null
   */
  public Instant getIssuedAt() {
    return instantConverter.FromCBORObject(this.cwtObject.get(6));
  }

  /**
   * Sets the "cti" (CWT ID) claim.
   * 
   * @param cti
   *          the CWT ID
   */
  public void setCwtId(final byte[] cti) {
    this.cwtObject.set(7, CBORObject.FromObject(cti));
  }

  /**
   * Gets the value of the "cti" (CWT ID) claim.
   * 
   * @return the ID, or null
   */
  public byte[] getCwtId() {
    return Optional.ofNullable(this.cwtObject.get(7)).map(CBORObject::GetByteString).orElse(null);
  }

  /**
   * Adds DGC v1 payload.
   * 
   * @param dgcPayload
   *          the CBOR encoding of DGC payload
   */
  public void setDgcV1(final byte[] dgcPayload) {
    final CBORObject m = CBORObject.NewMap();
    m.set(EU_DGC_V1_MESSAGE_TAG, CBORObject.DecodeFromBytes(dgcPayload));
    this.cwtObject.set(HCERT_CLAIM_KEY, m);
  }

  /**
   * Gets the CBOR encoding of a DGC v1 payload.
   * 
   * @return the CBOR encoding of a DGC v1 payload or null if no payload is stored
   */
  public byte[] getDgcV1() {
    CBORObject hcert = this.cwtObject.get(HCERT_CLAIM_KEY);
    if (hcert == null) {
      hcert = this.cwtObject.get(OLD_HCERT_CLAIM_KEY);
    }
    return Optional.ofNullable(hcert.get(EU_DGC_V1_MESSAGE_TAG)).map(CBORObject::EncodeToBytes).orElse(null);
  }

  /**
   * Sets a claim identified by {@code claimKey}.
   * 
   * @param claimKey
   *          the claim key
   * @param value
   *          the claim value (in its CBOR encoding)
   */
  public void setClaim(final int claimKey, final byte[] value) {
    this.cwtObject.set(claimKey, CBORObject.DecodeFromBytes(value));
  }
  
  /**
   * Sets a claim identified by {@code claimKey}.
   * 
   * @param claimKey
   *          the claim key
   * @param value
   *          the claim value
   */
  public void setClaim(final int claimKey, final CBORObject value) {
    this.cwtObject.set(claimKey, value);
  }
  
  /**
   * Sets a claim identified by {@code claimKey}.
   * 
   * @param claimKey
   *          the claim key
   * @param value
   *          the claim value (in its CBOR encoding)
   */
  public void setClaim(final String claimKey, final byte[] value) {
    this.cwtObject.set(claimKey, CBORObject.DecodeFromBytes(value));
  }  

  /**
   * Sets a claim identified by {@code claimKey}.
   * 
   * @param claimKey
   *          the claim key
   * @param value
   *          the claim value
   */
  public void setClaim(final String claimKey, final CBORObject value) {
    this.cwtObject.set(claimKey, value);
  }  

  /**
   * Gets the claim identified by {@code claimKey}.
   * 
   * @param claimKey
   *          the claim key
   * @return the claim value as a CBORObject, or null
   */
  public CBORObject getClaim(final int claimKey) {
    return this.cwtObject.get(claimKey);
  }

  /**
   * Gets the claim identified by {@code claimKey}.
   * 
   * @param claimKey
   *          the claim key
   * @return the claim value as a CBORObject, or null
   */
  public CBORObject getClaim(final String claimKey) {    
    return this.cwtObject.get(claimKey);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return this.cwtObject.toString();
  }

  /**
   * A builder for creating {@link Cwt} objects.
   */
  public static class CwtBuilder {

    /** The object that we are building. */
    private final Cwt cwt;

    /**
     * Constructor.
     */
    public CwtBuilder() {
      this.cwt = new Cwt();
    }

    /**
     * Builds the CWT.
     * 
     * @return the CWT
     */
    public Cwt build() {
      return this.cwt;
    }

    /**
     * Sets the "iss" claim.
     * 
     * @param issuer
     *          the issuer value
     * @return the builder
     */
    public CwtBuilder issuer(final String issuer) {
      this.cwt.setIssuer(issuer);
      return this;
    }

    /**
     * Sets the "sub" claim.
     * 
     * @param subject
     *          the subject value
     * @return the builder
     */
    public CwtBuilder subject(final String subject) {
      this.cwt.setSubject(subject);
      return this;
    }

    /**
     * Adds a audience to the "aud" claim.
     * 
     * @param audience
     *          the audience to add
     * @return the builder
     */
    public CwtBuilder audience(final String audience) {
      final List<String> contents = this.cwt.getAudience();
      if (contents == null) {
        this.cwt.setAudience(audience);
      }
      else {
        final List<String> audiences = new ArrayList<>(contents);
        audiences.add(audience);
        this.cwt.setAudience(audiences);
      }
      return this;
    }

    /**
     * Sets the "exp" claim.
     * 
     * @param exp
     *          the expiration time
     * @return the builder
     */
    public CwtBuilder expiration(final Instant exp) {
      this.cwt.setExpiration(exp);
      return this;
    }

    /**
     * Sets the "nbf" claim.
     * 
     * @param nbf
     *          the not before time
     * @return the builder
     */
    public CwtBuilder notBefore(final Instant nbf) {
      this.cwt.setNotBefore(nbf);
      return this;
    }

    /**
     * Sets the "iat" claim.
     * 
     * @param iat
     *          the issued at time
     * @return the builder
     */
    public CwtBuilder issuedAt(final Instant iat) {
      this.cwt.setIssuedAt(iat);
      return this;
    }

    /**
     * Sets the "cti" claim.
     * 
     * @param cti
     *          the CWT ID
     * @return the builder
     */
    public CwtBuilder cwtId(final byte[] cti) {
      this.cwt.setCwtId(cti);
      return this;
    }

    /**
     * Sets the DGC v1 payload.
     * 
     * @param dgcPayload
     *          the CBOR encoded DGC payload
     * @return the builder
     */
    public CwtBuilder dgcV1(final byte[] dgcPayload) {
      this.cwt.setDgcV1(dgcPayload);
      return this;
    }

    /**
     * Sets a claim identified by {@code claimKey}.
     * 
     * @param claimKey
     *          the claim key
     * @param value
     *          the claim value (in its CBOR encoding)
     * @return the builder
     */
    public CwtBuilder claim(final int claimKey, final byte[] value) {
      this.cwt.setClaim(claimKey, value);
      return this;
    }
    
    /**
     * Sets a claim identified by {@code claimKey}.
     * 
     * @param claimKey
     *          the claim key
     * @param value
     *          the claim value
     * @return the builder
     */
    public CwtBuilder claim(final int claimKey, final CBORObject value) {
      this.cwt.setClaim(claimKey, value);
      return this;
    }    

    /**
     * Sets a claim identified by {@code claimKey}.
     * 
     * @param claimKey
     *          the claim key
     * @param value
     *          the claim value (in its CBOR encoding)
     * @return the builder
     */
    public CwtBuilder claim(final String claimKey, final byte[] value) {
      this.cwt.setClaim(claimKey, value);
      return this;
    }
    
    /**
     * Sets a claim identified by {@code claimKey}.
     * 
     * @param claimKey
     *          the claim key
     * @param value
     *          the claim value
     * @return the builder
     */
    public CwtBuilder claim(final String claimKey, final CBORObject value) {
      this.cwt.setClaim(claimKey, value);
      return this;
    }    

  }

}
