/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.service;

import java.io.IOException;
import java.security.SignatureException;
import java.security.cert.CertificateExpiredException;

import se.digg.dgc.payload.v1.DGCSchemaException;
import se.digg.dgc.payload.v1.DigitalCovidCertificate;

/**
 * Service for decoding a Digital Covid Certificate from its image representation into the actual DCC payload.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public interface DGCDecoder {
  
  /**
   * Given the base45-encoding of a signed DCC the method verifies and decodes it into the DCC payload representation. 
   * 
   * @param base45
   *          the base45-encoding of the signed DCC (including the HCERT header)
   * @return the DCC payload
   * @throws DGCSchemaException
   *           for DCC schema errors
   * @throws SignatureException
   *           for signature verification errors
   * @throws CertificateExpiredException
   *           if the DCC has expired
   * @throws IOException
   *           for errors decoding data, for example CBOR related errors
   * @see #decodeBarcodeToBytes(byte[])
   */
  DigitalCovidCertificate decode(final String base45)
      throws DGCSchemaException, SignatureException, CertificateExpiredException, IOException;

  /**
   * Given the base45-encoding of a signed DCC the method verifies and decodes it into the CBOR encoding of the DCC payload.
   * 
   * @param base45
   *          the base45-encoding of the signed DCC (including the HCERT header)
   * @return the CBOR encoding of the DCC payload
   * @throws SignatureException
   *           for signature verification errors
   * @throws CertificateExpiredException
   *           if the DCC has expired
   * @throws IOException
   *           for errors decoding data, for example CBOR related errors
   * @see #decodeBarcode(byte[])
   */
  byte[] decodeToBytes(final String base45) throws SignatureException, CertificateExpiredException, IOException;

  /**
   * Verifies a "raw" DCC (i.e., a signed CWT holding the DCC payload) and decodes it to the actual DCC payload.
   * 
   * @param cwt
   *          the signed CWT holding the DCC
   * @return the DCC payload
   * @throws DGCSchemaException
   *           for DCC schema errors
   * @throws SignatureException
   *           for signature verification errors
   * @throws CertificateExpiredException
   *           if the DCC has expired
   * @throws IOException
   *           for errors decoding data, for example CBOR related errors
   * @see #decodeRawToBytes(byte[])
   */
  DigitalCovidCertificate decodeRaw(final byte[] cwt)
      throws DGCSchemaException, SignatureException, CertificateExpiredException, IOException;

  /**
   * Verifies a "raw" DCC (i.e., a signed CWT holding the DCC payload) and returns the CBOR encoding of the DCC payload.
   * 
   * @param cwt
   *          the signed CWT holding the DCC
   * @return the CBOR encoded DCC payload
   * @throws SignatureException
   *           for signature verification errors
   * @throws CertificateExpiredException
   *           if the DCC has expired
   * @throws IOException
   *           for errors decoding data, for example CBOR related errors
   * @see #decodeRaw(byte[])
   */
  byte[] decodeRawToBytes(final byte[] cwt) throws SignatureException, CertificateExpiredException, IOException;

}
