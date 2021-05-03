/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.service;

import java.io.IOException;
import java.security.SignatureException;
import java.time.Instant;

import se.digg.dgc.payload.v1.DGCSchemaException;
import se.digg.dgc.payload.v1.DigitalGreenCertificate;

/**
 * Service for creating Digital Green Certificates.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public interface DGCEncoder {

  /**
   * Based on the DGC payload and a expiration time, the method encodes the payload to CBOR, signs it, deflates it, and
   * delivers it in Base45 encoding (with a HCERT header).
   * 
   * @param dgc
   *          the contents of the DGC
   * @param expiration
   *          the expiration of the DGC
   * @return the Base45 encoding of the signed DGC
   * @throws DGCSchemaException
   *           for DGC schema errors
   * @throws IOException
   *           for errors encoding data, for example CBOR related errors
   * @throws SignatureException
   *           errors concerning signing the DGC
   */
  String encode(final DigitalGreenCertificate dgc, final Instant expiration)
      throws DGCSchemaException, IOException, SignatureException;

  /**
   * Based on the CBOR encoded DGC payload and a expiration time, the method signs it, deflates it, and delivers it in
   * Base45 encoding (with a HCERT header).
   * 
   * @param dgc
   *          the contents of the DGC in its CBOR encoding
   * @param expiration
   *          the expiration of the DGC
   * @return the Base45 encoding of the signed DGC
   * @throws IOException
   *           for errors encoding data, for example CBOR related errors
   * @throws SignatureException
   *           errors concerning signing the DGC
   */
  String encode(final byte[] dgc, final Instant expiration) throws IOException, SignatureException;

  /**
   * Given the DGC payload the method creates a CWT and signs it.
   * 
   * @param dgc
   *          the DGC payload
   * @param expiration
   *          the expiration time of the DGC
   * @return the CBOR encoding of the signed DGC (CWT)
   * @throws DGCSchemaException
   *           for DGC schema errors
   * @throws IOException
   *           for errors encoding data, for example CBOR related errors
   * @throws SignatureException
   *           errors concerning signing the DGC
   */
  byte[] sign(final DigitalGreenCertificate dgc, final Instant expiration) throws DGCSchemaException, IOException, SignatureException;

  /**
   * Given the CBOR-encoding of the DGC payload the method creates a CWT and signs it.
   * 
   * @param dgc
   *          the DGC payload in its CBOR encoding
   * @param expiration
   *          the expiration of the DGC
   * @return the CBOR encoding of the signed DGC (CWT)
   * @throws IOException
   *           for errors encoding data, for example CBOR related errors
   * @throws SignatureException
   *           errors concerning signing the DGC
   */
  byte[] sign(final byte[] dgc, final Instant expiration) throws IOException, SignatureException;

}
