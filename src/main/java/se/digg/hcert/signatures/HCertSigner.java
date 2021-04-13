/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.hcert.signatures;

import java.security.SignatureException;
import java.time.Instant;

/**
 * An interface for a HCERT signer.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public interface HCertSigner {

  /**
   * Signs the supplied binary representation (CBOR) of a HCERT.
   * <p>
   * Note: It is the caller's responsibility to ensure that the validity of the issued HCERT does not exceed the
   * validity of the signer's certificate (see {@link #getSignerExpiration()}).
   * </p>
   * 
   * @param hcert
   *          the CBOR encoding of the HCERT
   * @param expiration
   *          the expiration time for the HCERT
   * @return the signed HCERT
   * @throws SignatureException
   *           for signature errors
   */
  byte[] sign(final byte[] hcert, final Instant expiration) throws SignatureException;

  /**
   * Gets the point in time when this signer's certificate expires. For maximum interoperability, the validity of a
   * HCERT should not stretch beyond this time.
   * 
   * @return the signer certificate expiration time
   */
  Instant getSignerExpiration();

  /**
   * Gets the ISO 3166 country code of the signer (issuer).
   * 
   * @return the country code of the signer
   */
  String getSignerCountry();

}
