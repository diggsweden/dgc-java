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
import se.digg.dgc.payload.v1.DigitalCovidCertificate;

/**
 * Service for creating Digital Covid Certificates.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public interface DGCEncoder {

  /**
   * Based on the DCC payload and a expiration time, the method encodes the payload to CBOR, signs it, deflates it, and
   * delivers it in Base45 encoding (with a HCERT header).
   * 
   * @param dcc
   *          the contents of the DCC
   * @param expiration
   *          the expiration of the DCC
   * @return the Base45 encoding of the signed DCC
   * @throws DGCSchemaException
   *           for DCC schema errors
   * @throws IOException
   *           for errors encoding data, for example CBOR related errors
   * @throws SignatureException
   *           errors concerning signing the DCC
   */
  String encode(final DigitalCovidCertificate dcc, final Instant expiration)
      throws DGCSchemaException, IOException, SignatureException;

  /**
   * Based on the CBOR encoded DCC payload and a expiration time, the method signs it, deflates it, and delivers it in
   * Base45 encoding (with a HCERT header).
   * 
   * @param dcc
   *          the contents of the DCC in its CBOR encoding
   * @param expiration
   *          the expiration of the DCC
   * @return the Base45 encoding of the signed DCC
   * @throws IOException
   *           for errors encoding data, for example CBOR related errors
   * @throws SignatureException
   *           errors concerning signing the DCC
   */
  String encode(final byte[] dcc, final Instant expiration) throws IOException, SignatureException;

  /**
   * Given the DCC payload the method creates a CWT and signs it.
   * 
   * @param dcc
   *          the DCC payload
   * @param expiration
   *          the expiration time of the DCC
   * @return the CBOR encoding of the signed DCC (CWT)
   * @throws DGCSchemaException
   *           for DCC schema errors
   * @throws IOException
   *           for errors encoding data, for example CBOR related errors
   * @throws SignatureException
   *           errors concerning signing the DCC
   */
  byte[] sign(final DigitalCovidCertificate dcc, final Instant expiration) throws DGCSchemaException, IOException, SignatureException;

  /**
   * Given the CBOR-encoding of the DCC payload the method creates a CWT and signs it.
   * 
   * @param dcc
   *          the DCC payload in its CBOR encoding
   * @param expiration
   *          the expiration of the DCC
   * @return the CBOR encoding of the signed DCC (CWT)
   * @throws IOException
   *           for errors encoding data, for example CBOR related errors
   * @throws SignatureException
   *           errors concerning signing the DCC
   */
  byte[] sign(final byte[] dcc, final Instant expiration) throws IOException, SignatureException;

}
