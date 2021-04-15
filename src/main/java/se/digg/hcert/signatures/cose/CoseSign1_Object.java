/*
 * MIT License
 * 
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.hcert.signatures.cose;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Optional;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.impl.ECDSA;
import com.upokecenter.cbor.CBORException;
import com.upokecenter.cbor.CBORObject;
import com.upokecenter.cbor.CBORType;

import se.digg.hcert.signatures.cwt.Cwt;

/**
 * A representation of a COSE_Sign1 object.
 *
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Martin Lindström (martin@idsec.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class CoseSign1_Object {

  /** The COSE_Sign1 message tag. */
  public static final int MESSAGE_TAG = 18;

  /** Should the message tag be included? The default is {@code false}. */
  private boolean includeMessageTag = false;

  /** The protected attributes. */
  private CBORObject protectedAttributes;

  /** The encoding of the protected attributes. */
  private byte[] protectedAttributesEncoding;

  /** The unprotected attributes. */
  private CBORObject unprotectedAttributes;

  /** The data content (data that is signed). */
  private byte[] content;

  /** The signature. */
  private byte[] signature;

  /** We don't support external data - so it's static. */
  private static final byte[] externalData = new byte[0];

  /** The COSE_Sign1 context string. */
  private static final String contextString = "Signature1";
    
  static {
    // Make sure Bouncy Castle is present - needed for SHAxxxwithRSA/PSS support.
    ensureBouncyCastlePresent();
  }

  /**
   * Default constructor.
   */
  public CoseSign1_Object() {
    this.protectedAttributes = CBORObject.NewMap();
    this.unprotectedAttributes = CBORObject.NewMap();
  }

  /**
   * Constructor that accepts the binary representation of a signed COSE_Sign1 object.
   * 
   * @param data
   *          the binary representation of the COSE_Sign1 object
   * @throws CBORException
   *           for invalid data
   */
  public CoseSign1_Object(final byte[] data) throws CBORException {
    final CBORObject message = CBORObject.DecodeFromBytes(data);
    if (message.getType() != CBORType.Array) {
      throw new CBORException("Supplied message is not a valid COSE security object");
    }

    // If the message is tagged, it must have the message tag for a COSE_Sign1 message.
    //
    if (message.isTagged()) {
      if (message.GetAllTags().length != 1) {
        throw new CBORException("Invalid object - too many tags");
      }
      if (MESSAGE_TAG != message.getMostInnerTag().ToInt32Unchecked()) {
        throw new CBORException(String.format(
          "Invalid COSE_Sign1 structure - Expected %d tag - but was %d",
          MESSAGE_TAG, message.getMostInnerTag().ToInt32Unchecked()));
      }
    }
    if (message.size() != 4) {
      throw new CBORException(String.format(
        "Invalid COSE_Sign1 structure - Expected an array of 4 items - but array has %d items", message.size()));
    }
    if (message.get(0).getType() == CBORType.ByteString) {
      this.protectedAttributesEncoding = message.get(0).GetByteString();

      if (message.get(0).GetByteString().length == 0) {
        this.protectedAttributes = CBORObject.NewMap();
      }
      else {
        this.protectedAttributes = CBORObject.DecodeFromBytes(this.protectedAttributesEncoding);
        if (this.protectedAttributes.size() == 0) {
          this.protectedAttributesEncoding = new byte[0];
        }
      }
    }
    else {
      throw new CBORException(String.format("Invalid COSE_Sign1 structure - " +
          "Expected item at position 1/4 to be a bstr which is the encoding of the protected attributes, but was %s",
        message.get(0).getType()));
    }

    if (message.get(1).getType() == CBORType.Map) {
      this.unprotectedAttributes = message.get(1);
    }
    else {
      throw new CBORException(String.format(
        "Invalid COSE_Sign1 structure - Expected item at position 2/4 to be a Map for unprotected attributes, but was %s",
        message.get(1).getType()));
    }

    if (message.get(2).getType() == CBORType.ByteString) {
      this.content = message.get(2).GetByteString();
    }
    else if (!message.get(2).isNull()) {
      throw new CBORException(String.format(
        "Invalid COSE_Sign1 structure - Expected item at position 3/4 to be a bstr holding the payload, but was %s",
        message.get(2).getType()));
    }

    if (message.get(3).getType() == CBORType.ByteString) {
      this.signature = message.get(3).GetByteString();
    }
    else {
      throw new CBORException(String.format(
        "Invalid COSE_Sign1 structure - Expected item at position 4/4 to be a bstr holding the signature, but was %s",
        message.get(2).getType()));
    }
  }

  /**
   * Creates a {@link CoseSign1_ObjectBuilder}.
   * 
   * @return a builder
   */
  public static CoseSign1_ObjectBuilder builder() {
    return new CoseSign1_ObjectBuilder();
  }

  /**
   * Decodes the supplied data into a CoseSign1_Object object.
   * 
   * @param data
   *          the encoded data
   * @return a CoseSign1_Object object
   * @throws CBORException
   *           if the supplied encoding is not a valid CoseSign1_Object
   */
  public static CoseSign1_Object decode(final byte[] data) throws CBORException {
    return new CoseSign1_Object(data);
  }

  /**
   * Gets the binary representation of this object.
   * <p>
   * Note: Only complete objects that have been signed may be encoded.
   * </p>
   * 
   * @return the bytes for the binary representation
   * @throws CBORException
   *           for encoding errors
   */
  public byte[] encode() throws CBORException {

    if (this.signature == null || this.protectedAttributesEncoding == null) {
      throw new CBORException("Cannot encode COSE_Sign1 message - missing signature");
    }
    CBORObject obj = CBORObject.NewArray();
    obj.Add(this.protectedAttributesEncoding);
    obj.Add(this.unprotectedAttributes);
    obj.Add(this.content);
    obj.Add(this.signature);

    if (this.includeMessageTag) {
      obj = CBORObject.FromObjectAndTag(obj, MESSAGE_TAG);
    }

    return obj.EncodeToBytes();
  }

  /**
   * Signs the COSE_Sign1 object using the supplied key.
   * 
   * @param signingKey
   *          the signing key
   * @param provider
   *          the security provider to use (may be null)
   * @throws SignatureException
   *           for signature errors
   * @throws CBORException
   *           for CBOR coding errors
   */
  public void sign(final PrivateKey signingKey, final Provider provider) throws SignatureException, CBORException {

    // Initial checks ...
    //
    if (this.signature != null) {
      throw new SignatureException("Object has already been signed");
    }
    if (this.content == null) {
      throw new SignatureException("No content specified");
    }

    // Prepare ...
    //
    if (this.protectedAttributesEncoding == null) {
      if (this.protectedAttributes.size() > 0) {
        this.protectedAttributesEncoding = this.protectedAttributes.EncodeToBytes();
      }
      else {
        this.protectedAttributesEncoding = new byte[0];
      }
    }

    final CBORObject obj = CBORObject.NewArray();
    obj.Add(contextString);
    obj.Add(this.protectedAttributesEncoding);
    obj.Add(externalData);
    obj.Add(this.content);

    final byte[] tbsData = obj.EncodeToBytes();

    // OK, its time to sign.
    //
    // First find out which algorithm to use by searching for the algorithm ID in the protected attributes.
    //
    final CBORObject registeredAlgorithm = this.protectedAttributes.get(HeaderParameterKey.ALG.getCborObject());
    if (registeredAlgorithm == null) {
      throw new SignatureException("No algorithm ID stored in protected attributes - cannot sign");
    }
    final SignatureAlgorithm algorithm = SignatureAlgorithm.fromCborObject(registeredAlgorithm);

    try {
      Signature signature = provider != null
          ? Signature.getInstance(algorithm.getJcaAlgorithmName(), provider)
          : Signature.getInstance(algorithm.getJcaAlgorithmName());
      signature.initSign(signingKey);
      signature.update(tbsData);
      byte[] result = signature.sign();

      // For ECDSA, process the signature according to section 8.1 of RFC8152.
      //
      if (algorithm == SignatureAlgorithm.ES256) {
        this.signature = ECDSA.transcodeSignatureToConcat(result, 32 * 2);
      }
      else if (algorithm == SignatureAlgorithm.ES384) {
        this.signature = ECDSA.transcodeSignatureToConcat(result, 48 * 2);
      }
      else if (algorithm == SignatureAlgorithm.ES512) {
        this.signature = ECDSA.transcodeSignatureToConcat(result, 66 * 2);
      }
      else {
        this.signature = result;
      }
    }
    catch (NoSuchAlgorithmException | InvalidKeyException | JOSEException e) {
      throw new SignatureException("Failed to sign - " + e.getMessage(), e);
    }
  }

  /**
   * A utility method that looks for the key identifier (kid) in the protected (and unprotected) attributes.
   * 
   * @return the key identifier as a byte string
   */
  public byte[] getKeyIdentifier() {
    final CBORObject kid = Optional
      .ofNullable(this.protectedAttributes.get(HeaderParameterKey.KID.getCborObject()))
      .orElse(this.unprotectedAttributes.get(HeaderParameterKey.KID.getCborObject()));

    if (kid == null) {
      return null;
    }
    return kid.GetByteString();
  }

  /**
   * A utility method that gets the contents as a {@link Cwt}.
   * 
   * @return the CWT or null if no contents is available
   * @throws CBORException
   *           if the contents do not hold a valid CWT
   */
  public Cwt getCwt() throws CBORException {
    if (this.content == null) {
      return null;
    }
    return Cwt.decode(this.content);
  }

  /**
   * Verifies the signature of the COSE_Sign1 object.
   * <p>
   * Note: This method only verifies the signature. Not the payload.
   * </p>
   * 
   * @param publicKey
   *          the key to use when verifying the signature
   * @throws SignatureException
   *           for signature verification errors
   */
  public void verifySignature(final PublicKey publicKey) throws SignatureException {
    if (this.signature == null) {
      throw new SignatureException("Object is not signed");
    }

    final CBORObject obj = CBORObject.NewArray();
    obj.Add(contextString);
    obj.Add(this.protectedAttributesEncoding);
    obj.Add(externalData);
    if (this.content != null) {
      obj.Add(this.content);
    }
    else {
      obj.Add(null);
    }

    final byte[] signedData = obj.EncodeToBytes();

    // First find out which algorithm to use by searching for the algorithm ID in the protected attributes.
    //
    final CBORObject registeredAlgorithm = this.protectedAttributes.get(HeaderParameterKey.ALG.getCborObject());
    if (registeredAlgorithm == null) {
      throw new SignatureException("No algorithm ID stored in protected attributes - cannot sign");
    }
    final SignatureAlgorithm algorithm = SignatureAlgorithm.fromCborObject(registeredAlgorithm);

    byte[] signatureToVerify = this.signature;

    try {
      // For ECDSA, convert the signature according to section 8.1 of RFC8152.
      //
      if (algorithm == SignatureAlgorithm.ES256
          || algorithm == SignatureAlgorithm.ES384
          || algorithm == SignatureAlgorithm.ES512) {

        signatureToVerify = ECDSA.transcodeSignatureToDER(this.signature);
      }

      final Signature verifier = Signature.getInstance(algorithm.getJcaAlgorithmName());
      verifier.initVerify(publicKey);
      verifier.update(signedData);

      if (!verifier.verify(signatureToVerify)) {
        throw new SignatureException("Signature did not verify correctly");
      }
    }
    catch (NoSuchAlgorithmException | InvalidKeyException | JOSEException e) {
      throw new SignatureException("Failed to verify signature - " + e.getMessage(), e);
    }
  }

  /**
   * Adds a protected attribute.
   * 
   * @param label
   *          the attribute label
   * @param value
   *          the attribute value
   * @throws CBORException
   *           if the object already has been signed
   */
  public void addProtectedAttribute(final CBORObject label, final CBORObject value) throws CBORException {
    if (this.signature != null) {
      throw new CBORException("Cannot add protected attribute to already signed COSE_Sign1 object");
    }
    this.removeProtectedAttribute(label);
    this.protectedAttributes.Add(label, value);
  }

  /**
   * Removes a protected attribute.
   * 
   * @param label
   *          the attribute label
   * @throws CBORException
   *           if the object already has been signed
   */
  public void removeProtectedAttribute(final CBORObject label) throws CBORException {
    if (this.protectedAttributes.ContainsKey(label)) {
      if (this.signature != null) {
        throw new CBORException("Cannot remove protected attribute from signed COSE_Sign1 object");
      }
      this.protectedAttributes.Remove(label);
    }
  }

  /**
   * Adds an unprotected attribute.
   * 
   * @param label
   *          the attribute label
   * @param value
   *          the attribute value
   */
  public void addUnprotectedAttribute(final CBORObject label, final CBORObject value) {
    this.removeUnprotectedAttribute(label);
    this.unprotectedAttributes.Add(label, value);
  }

  /**
   * Removes an unprotected attribute.
   * 
   * @param label
   *          the attribute label
   * @throws CBORException
   *           if the object already has been signed
   */
  public void removeUnprotectedAttribute(final CBORObject label) {
    if (this.unprotectedAttributes.ContainsKey(label)) {
      this.unprotectedAttributes.Remove(label);
    }
  }

  /**
   * Assigns the payload/content (usually a CWT).
   * 
   * @param content
   *          the binary representation of the payload
   */
  public void setContent(final byte[] content) {
    this.content = content;
  }

  /**
   * Tells whether to include the COSE_Sign1 message tag in encodings. The default is {@code false}
   * 
   * @param includeMessageTag
   *          whether to include the message tag
   */
  public void setIncludeMessageTag(final boolean includeMessageTag) {
    this.includeMessageTag = includeMessageTag;
  }

  /**
   * Ensures that the Bouncy Castle security provider is installed, and if not, installs it. This provider is needed to
   * handle {@link SignatureAlgorithm#PS256}, {@link SignatureAlgorithm#PS384} and {@link SignatureAlgorithm#PS512}.
   */
  private static void ensureBouncyCastlePresent() {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }

  /**
   * A builder for {@link CoseSign1_Object} objects.
   */
  public static class CoseSign1_ObjectBuilder {

    /** The object that we are building. */
    private final CoseSign1_Object object;

    /**
     * Default constructor.
     */
    public CoseSign1_ObjectBuilder() {
      this.object = new CoseSign1_Object();
    }

    /**
     * Builds the Cose_Sign1 object.
     * 
     * @return a Cose_Sign1 object
     */
    public CoseSign1_Object build() {
      return this.object;
    }

    /**
     * Adds a protected attribute.
     * 
     * @param label
     *          the attribute label
     * @param value
     *          the attribute value
     * @return the builder
     */
    public CoseSign1_ObjectBuilder protectedAttribute(final CBORObject label, final CBORObject value) {
      this.object.addProtectedAttribute(label, value);
      return this;
    }

    /**
     * Adds an unprotected attribute.
     * 
     * @param label
     *          the attribute label
     * @param value
     *          the attribute value
     * @return the builder
     */
    public CoseSign1_ObjectBuilder unprotectedAttribute(final CBORObject label, final CBORObject value) {
      this.object.addUnprotectedAttribute(label, value);
      return this;
    }

    /**
     * Assigns the payload/content (usually a CWT).
     * 
     * @param content
     *          the binary representation of the payload
     * @return the builder
     */
    public CoseSign1_ObjectBuilder content(final byte[] content) {
      this.object.setContent(content);
      return this;
    }

    /**
     * Tells whether to include the COSE_Sign1 message tag in encodings. The default is {@code false}
     * 
     * @param include
     *          whether to include the message tag
     * @return the builder
     */
    public CoseSign1_ObjectBuilder includeMessageTag(final boolean include) {
      this.object.setIncludeMessageTag(include);
      return this;
    }
  }

}
