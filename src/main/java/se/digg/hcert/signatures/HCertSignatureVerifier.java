/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.hcert.signatures;

import java.security.SignatureException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import java.time.Instant;

/**
 * An interface for a HCERT signature verifier.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public interface HCertSignatureVerifier {

  /**
   * Verifies the signature and validity of the supplied signed HCERT.
   * <p>
   * Note: This method only checks the signature and the validity of the HCERT. Any other checks must be done after this
   * method has completed successfully.
   * </p>
   * 
   * @param signedHcert
   *          the signed HCERT (Base45-decoded and de-compressed)
   * @param certificateProvider
   *          the provider that is used to find the certificate(s) to use when validating the signature
   * @return if signature verification a Result object containing the HCERT payload along with its metadata is returned
   * @throws SignatureException
   *           for signature validation errors
   * @throws CertificateExpiredException
   *           if the HCERT has expired
   */
  Result verify(final byte[] signedHcert, final CertificateProvider certificateProvider)
      throws SignatureException, CertificateExpiredException;

  /**
   * Represents the successful result of a HCERT signature verification.
   */
  public static class Result {

    /** The HCERT payload in its CBOR representation. */
    private final byte[] hcert;

    /** The certificate that was successfully used to verify the signature. */
    private final X509Certificate signerCertificate;

    /** The ISO 3166 code for the issuing country. */
    private final String country;

    /** The issuance time of the HCERT. */
    private final Instant issuedAt;

    /** The expiration time of the HCERT. */
    private final Instant expires;

    /**
     * Constructor.
     * 
     * @param hcert
     *          the HCERT payload in its CBOR representation
     * @param signerCertificate
     *          the certificate that was successfully used to verify the signature
     * @param country
     *          the ISO 3166 code for the issuing country
     * @param issuedAt
     *          the issuance time of the HCERT
     * @param expires
     *          the expiration time of the HCERT
     */
    public Result(final byte[] hcert, final X509Certificate signerCertificate, final String country, final Instant issuedAt,
        final Instant expires) {
      this.hcert = hcert;
      this.signerCertificate = signerCertificate;
      this.country = country;
      this.issuedAt = issuedAt;
      this.expires = expires;
    }

    /**
     * Gets the HCERT payload in its CBOR representation.
     * 
     * @return the HCERT
     */
    public byte[] getHcert() {
      return this.hcert;
    }

    /**
     * Gets the certificate that was successfully used to verify the signature.
     * 
     * @return the certificate used to verify the signature
     */
    public X509Certificate getSignerCertificate() {
      return this.signerCertificate;
    }

    /**
     * Gets the ISO 3166 code for the issuing country.
     * 
     * @return country code
     */
    public String getCountry() {
      return this.country;
    }

    /**
     * Gets the issuance time of the HCERT.
     * 
     * @return issuance time
     */
    public Instant getIssuedAt() {
      return this.issuedAt;
    }

    /**
     * Gets the expiration time of the HCERT.
     * 
     * @return the expiration time
     */
    public Instant getExpires() {
      return this.expires;
    }
  }

}
