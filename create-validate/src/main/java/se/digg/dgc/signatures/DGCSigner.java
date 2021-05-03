/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.signatures;

import java.security.SignatureException;
import java.time.Instant;

/**
 * An interface for a DGC signer.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public interface DGCSigner {

  /**
   * Creates a CWT including the CBOR encoded DGC payload and signs it.
   * <p>
   * Note: It is the caller's responsibility to ensure that the validity of the issued DGC does not exceed the
   * validity of the signer's certificate (see {@link #getSignerExpiration()}).
   * </p>
   * 
   * @param dgcPayload
   *          the CBOR encoding of the DGC payload
   * @param expiration
   *          the expiration time for the DGC
   * @return the CBOR encoding of the signed CWT holding the DGC payload
   * @throws SignatureException
   *           for signature errors
   */
  byte[] sign(final byte[] dgcPayload, final Instant expiration) throws SignatureException;

  /**
   * Gets the point in time when this signer's certificate expires. For maximum interoperability, the validity of a
   * DGC should not stretch beyond this time.
   * 
   * @return the signer certificate expiration time
   */
  Instant getSignerExpiration();

  /**
   * Gets the ISO-3166 country code of the signer (issuer).
   * 
   * @return the country code of the signer
   */
  String getSignerCountry();

}
