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
 * An interface for a DCC signature verifier.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public interface DGCSignatureVerifier {

  /**
   * Verifies the signature and validity of the supplied signed DCC.
   * <p>
   * Note: This method only checks the signature and the validity of the DCC. Any other checks must be done after this
   * method has completed successfully.
   * </p>
   * 
   * @param signedCwt
   *          the signed CWT holding the DCC
   * @param certificateProvider
   *          the provider that is used to find the certificate(s) to use when validating the signature
   * @return if signature verification a Result object containing the DCC payload along with its metadata is returned
   * @throws SignatureException
   *           for signature validation errors
   * @throws CertificateExpiredException
   *           if the DCC has expired
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

    /** The key id that was used to locate the signer certificate. */
    private final byte[] kid;

    /** The ISO-3166 code for the issuing country. */
    private final String country;

    /** The issuance time of the DCC. */
    private final Instant issuedAt;

    /** The expiration time of the DCC. */
    private final Instant expires;

    /**
     * Constructor.
     * 
     * @param dccPayload
     *          the CBOR encoded DCC payload
     * @param signerCertificate
     *          the certificate that was used to verify the signature
     * @param kid
     *          he key id that was used to locate the signer certificate
     * @param country
     *          the ISO-3166 code for the issuing country
     * @param issuedAt
     *          the issuance time of the DCC
     * @param expires
     *          the expiration time of the DCC
     */
    public Result(final byte[] dccPayload, final X509Certificate signerCertificate,
        final byte[] kid, final String country, final Instant issuedAt, final Instant expires) {
      this.dgcPayload = dccPayload;
      this.signerCertificate = signerCertificate;
      this.kid = kid;
      this.country = country;
      this.issuedAt = issuedAt;
      this.expires = expires;
    }

    /**
     * Gets the CBOR encoded DCC payload.
     * 
     * @return the CBOR encoded DCC payload
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
     * Gets the key identifier that was used to locate the signer certificate.
     * 
     * @return the key identifier
     */
    public byte[] getKid() {
      return this.kid;
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
