/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.signatures;

import java.security.SignatureException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import java.time.Instant;

/**
 * An interface for a DGC signature verifier.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public interface DGCSignatureVerifier {

  /**
   * Verifies the signature and validity of the supplied signed DGC.
   * <p>
   * Note: This method only checks the signature and the validity of the DGC. Any other checks must be done after this
   * method has completed successfully.
   * </p>
   * 
   * @param signedCwt
   *          the signed CWT holding the DGC
   * @param certificateProvider
   *          the provider that is used to find the certificate(s) to use when validating the signature
   * @return if signature verification a Result object containing the DGC payload along with its metadata is returned
   * @throws SignatureException
   *           for signature validation errors
   * @throws CertificateExpiredException
   *           if the DGC has expired
   */
  Result verify(final byte[] signedCwt, final CertificateProvider certificateProvider) 
      throws SignatureException, CertificateExpiredException;

  /**
   * Represents the successful result of a HCERT signature verification.
   */
  public static class Result {

    /** The CBOR encoded DGC payload. */
    private final byte[] dgcPayload;

    /** The certificate that was used to verify the signature. */
    private final X509Certificate signerCertificate;

    /** The ISO-3166 code for the issuing country. */
    private final String country;

    /** The issuance time of the DGC. */
    private final Instant issuedAt;

    /** The expiration time of the DGC. */
    private final Instant expires;

    /**
     * Constructor.
     * 
     * @param dgcPayload
     *          the CBOR encoded DGC payload
     * @param signerCertificate
     *          the certificate that was used to verify the signature
     * @param country
     *          the ISO-3166 code for the issuing country
     * @param issuedAt
     *          the issuance time of the DGC
     * @param expires
     *          the expiration time of the DGC
     */
    public Result(final byte[] dgcPayload, final X509Certificate signerCertificate, 
        final String country, final Instant issuedAt, final Instant expires) {
      this.dgcPayload = dgcPayload;
      this.signerCertificate = signerCertificate;
      this.country = country;
      this.issuedAt = issuedAt;
      this.expires = expires;
    }

    /**
     * Gets the CBOR encoded DGC payload.
     * 
     * @return the CBOR encoded DGC payload
     */
    public byte[] getDgcPayload() {
      return this.dgcPayload;
    }

    /**
     * Gets the certificate that was used to verify the signature.
     * 
     * @return the certificate used to verify the signature
     */
    public X509Certificate getSignerCertificate() {
      return this.signerCertificate;
    }

    /**
     * Gets the ISO-3166 code for the issuing country.
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
