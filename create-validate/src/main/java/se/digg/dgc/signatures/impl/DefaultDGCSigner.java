/*
 * MIT License
 * 
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.signatures.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Optional;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x509.Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.upokecenter.cbor.CBORException;
import com.upokecenter.cbor.CBORObject;

import se.digg.dgc.signatures.DGCSigner;
import se.digg.dgc.signatures.cose.CoseSign1_Object;
import se.digg.dgc.signatures.cose.HeaderParameterKey;
import se.digg.dgc.signatures.cose.SignatureAlgorithm;
import se.digg.dgc.signatures.cwt.Cwt;
import se.swedenconnect.security.credential.PkiCredential;

/**
 * A bean implementing the {@link DGCSigner} interface.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class DefaultDGCSigner implements DGCSigner {

  /** Logger */
  private static final Logger log = LoggerFactory.getLogger(DefaultDGCSigner.class);

  /** The credential that we are using for signing. */
  private final CredentialWrapper signerCredential;

  /** The Java Security Provider to use when signing. */
  private Provider securityProvider;

  /** The issuer country. */
  private final String country;

  /** The key identifier that is used. */
  private final byte[] keyIdentifier;

  /** The expiration time for the signer's certificate. */
  private final Instant signerExpiration;

  /** The algorithm to use when signing. */
  private SignatureAlgorithm algorithmIdentifier;

  /** Whether to include the Cose_Sign1 message tag. */
  private boolean includeCoseTag = true;

  /** Whether to include the CWT message tag. */
  private boolean includeCwtTag = false;

  /**
   * Constructor.
   * 
   * @param signerKey
   *          the signer key
   * @param signerCertificate
   *          the certificate holding the public key corresponding to the signer key
   * @throws CertificateException
   *           for certificate decoding errors
   */
  public DefaultDGCSigner(final PrivateKey signerKey, final X509Certificate signerCertificate) throws CertificateException {
    this(new CredentialWrapper(signerKey, signerCertificate));
  }

  /**
   * Constructor.
   * 
   * @param signerCredential
   *          the signer credential
   * @throws CertificateException
   *           for certificate decoding errors
   */
  public DefaultDGCSigner(final PkiCredential signerCredential) throws CertificateException {
    this(new CredentialWrapper(signerCredential));
  }

  /**
   * Constructor.
   * 
   * @param signerCredential
   *          the signer credential
   * @throws CertificateException
   *           for certificate decoding errors
   */
  private DefaultDGCSigner(final CredentialWrapper signerCredential) throws CertificateException {
    this.signerCredential = signerCredential;

    this.country = getCountry(signerCredential.getCertificate());
    this.keyIdentifier = calculateKid(signerCredential.getCertificate());
    this.signerExpiration = Instant.ofEpochMilli(signerCredential.getCertificate().getNotAfter().getTime());

    if (ECPublicKey.class.isInstance(this.signerCredential.getPublicKey())) {
      this.algorithmIdentifier = SignatureAlgorithm.ES256;
    }
    else if (RSAPublicKey.class.isInstance(this.signerCredential.getPublicKey())) {
      this.algorithmIdentifier = SignatureAlgorithm.PS256;
    }
    else {
      throw new SecurityException("Unsupported key");
    }
  }

  /** {@inheritDoc} */
  @Override
  public byte[] sign(final byte[] dgcPayload, final Instant expiration) throws SignatureException {

    if (expiration.isAfter(this.signerExpiration)) {
      log.warn("Expiration of DGC goes beyond the signer certificate validity");
    }

    try {
      final Cwt cwt = Cwt.builder()
        .issuer(this.country)
        .issuedAt(Instant.now())
        .expiration(expiration)
        .dgcV1(dgcPayload)
        .build();

      final CoseSign1_Object coseObject = CoseSign1_Object.builder()
        .includeMessageTag(this.includeCoseTag)
        .protectedAttribute(HeaderParameterKey.ALG.getCborObject(), this.algorithmIdentifier.getCborObject())
        .protectedAttribute(HeaderParameterKey.KID.getCborObject(), CBORObject.FromObject(this.keyIdentifier))
        .content(cwt.encode())
        .build();

      coseObject.sign(this.signerCredential.getPrivateKey(), this.securityProvider);

      final byte[] coseEncoding = coseObject.encode();

      if (this.includeCwtTag) {
        final CBORObject obj = CBORObject.DecodeFromBytes(coseEncoding);
        return CBORObject.FromObjectAndTag(obj, Cwt.MESSAGE_TAG).EncodeToBytes();
      }
      else {
        return coseEncoding;
      }
    }
    catch (CBORException e) {
      throw new SignatureException("CBOR error - " + e.getMessage(), e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public Instant getSignerExpiration() {
    return this.signerExpiration;
  }

  /** {@inheritDoc} */
  @Override
  public String getSignerCountry() {
    return this.country;
  }

  /**
   * Assigns the algorithm to use.
   * <p>
   * {@link SignatureAlgorithm#ES256} is the default for EC keys and {@link SignatureAlgorithm#PS256} is the default for
   * RSA keys.
   * </p>
   * 
   * @param algorithmIdentifier
   *          the algorithm to use
   */
  public void setAlgorithmIdentifier(final SignatureAlgorithm algorithmIdentifier) {
    if (algorithmIdentifier != null) {
      this.algorithmIdentifier = algorithmIdentifier;
    }
  }

  /**
   * Gets the country from the certificate's subject DN.
   * 
   * @param cert
   *          the certificate
   * @return the country as a string
   * @throws CertificateException
   *           for certificate decoding errors
   */
  private static String getCountry(final X509Certificate cert) throws CertificateException {

    try (ByteArrayInputStream bis = new ByteArrayInputStream(cert.getEncoded()); ASN1InputStream as = new ASN1InputStream(bis)) {
      final ASN1Sequence seq = (ASN1Sequence) as.readObject();
      final Certificate asn1Cert = Certificate.getInstance(seq);

      if (asn1Cert.getSubject() == null || asn1Cert.getSubject().getRDNs() == null) {
        throw new CertificateException("Missing country in certificate subject");
      }
      final RDN[] rdns = asn1Cert.getSubject().getRDNs(new ASN1ObjectIdentifier("2.5.4.6"));
      if (rdns == null || rdns.length == 0) {
        throw new CertificateException("Missing country in certificate subject");
      }
      final ASN1Primitive p = rdns[0].getFirst().getValue().toASN1Primitive();
      if (p instanceof ASN1String) {
        return ((ASN1String) p).getString();
      }
      throw new CertificateException("Missing country in certificate subject");
    }
    catch (IOException e) {
      throw new CertificateException("Failed to read certificate", e);
    }
  }

  /**
   * Given a certificate a 8-byte KID is calculated that is the SHA-256 digest over the certificate DER-encoding.
   * 
   * @param cert
   *          the certificate
   * @return a 8 byte KID
   */
  private static byte[] calculateKid(final X509Certificate cert) {

    try {
      final MessageDigest digest = MessageDigest.getInstance("SHA-256");

      final byte[] sha256 = digest.digest(cert.getEncoded());
      final byte[] kid = new byte[8];
      System.arraycopy(sha256, 0, kid, 0, 8);
      return kid;
    }
    catch (NoSuchAlgorithmException | CertificateEncodingException e) {
      throw new SecurityException(e);
    }
  }

  /**
   * Assigns a specific Java Security Provider that should be used when signing. If not assigned, a default provider
   * will be used.
   * 
   * @param securityProvider
   *          the security provider
   */
  public void setSecurityProvider(final Provider securityProvider) {
    this.securityProvider = securityProvider;
  }

  /**
   * Whether to include the Cose_Sign1 message tag in the resulting encoding. The default is {@code true}.
   * <p>
   * See <a href="https://tools.ietf.org/html/rfc8152">RFC8152</a>.
   * </p>
   * 
   * @param includeCoseTag
   *          whether to include the Cose_Sign1 message tag
   */
  public void setIncludeCoseTag(boolean includeCoseTag) {
    this.includeCoseTag = includeCoseTag;
  }

  /**
   * Sets whether to include the CWT message tag. The default is {@code false}.
   * <p>
   * See section 6, or <a href="https://tools.ietf.org/html/rfc8392">RFC8392</a>.
   * </p>
   * 
   * @param includeCwtTag
   *          whether to include the CWT message tag
   */
  public void setIncludeCwtTag(final boolean includeCwtTag) {
    this.includeCwtTag = includeCwtTag;
  }

  /**
   * A credential wrapper so that we don't have to require the use of the {@link PkiCredential} class.
   */
  private static class CredentialWrapper {

    private PrivateKey signerKey;
    private X509Certificate signerCertificate;
    private PkiCredential signerCredential;

    public CredentialWrapper(final PrivateKey signerKey, final X509Certificate signerCertificate) {
      this.signerKey = Optional.ofNullable(signerKey)
        .orElseThrow(() -> new IllegalArgumentException("signerKey must not be null"));
      this.signerCertificate = Optional.ofNullable(signerCertificate)
        .orElseThrow(() -> new IllegalArgumentException("signerCertificate must not be null"));
    }

    public CredentialWrapper(final PkiCredential signerCredential) {
      this.signerCredential = Optional.ofNullable(signerCredential)
        .orElseThrow(() -> new IllegalArgumentException("signerCredential must not be null"));
    }

    public PublicKey getPublicKey() {
      return this.getCertificate().getPublicKey();
    }

    public X509Certificate getCertificate() {
      return this.signerCredential != null ? this.signerCredential.getCertificate() : this.signerCertificate;
    }

    public PrivateKey getPrivateKey() {
      return this.signerCredential != null ? this.signerCredential.getPrivateKey() : this.signerKey;
    }
  }

}
