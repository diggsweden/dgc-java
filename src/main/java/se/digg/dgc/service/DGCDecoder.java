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
import se.digg.dgc.payload.v1.DigitalGreenCertificate;

/**
 * Service for decoding a Digital Green Certificate from its image representation into the actual DGC payload.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public interface DGCDecoder {
  
  /**
   * Given the base45-encoding of a signed DGC the method verifies and decodes it into the DGC payload representation. 
   * 
   * @param base45
   *          the base45-encoding of the signed DGC (including the HCERT header)
   * @return the DGC payload
   * @throws DGCSchemaException
   *           for DGC schema errors
   * @throws SignatureException
   *           for signature verification errors
   * @throws CertificateExpiredException
   *           if the DGC has expired
   * @throws IOException
   *           for errors decoding data, for example CBOR related errors
   * @see #decodeBarcodeToBytes(byte[])
   */
  DigitalGreenCertificate decode(final String base45)
      throws DGCSchemaException, SignatureException, CertificateExpiredException, IOException;

  /**
   * Given the base45-encoding of a signed DGC the method verifies and decodes it into the CBOR encoding of the DGC payload.
   * 
   * @param base45
   *          the base45-encoding of the signed DGC (including the HCERT header)
   * @return the CBOR encoding of the DGC payload
   * @throws SignatureException
   *           for signature verification errors
   * @throws CertificateExpiredException
   *           if the DGC has expired
   * @throws IOException
   *           for errors decoding data, for example CBOR related errors
   * @see #decodeBarcode(byte[])
   */
  byte[] decodeToBytes(final String base45) throws SignatureException, CertificateExpiredException, IOException;

  /**
   * Verifies a "raw" DGC (i.e., a signed CWT holding the DGC payload) and decodes it to the actual DGC payload.
   * 
   * @param cwt
   *          the signed CWT holding the DGC
   * @return the DGC payload
   * @throws DGCSchemaException
   *           for DGC schema errors
   * @throws SignatureException
   *           for signature verification errors
   * @throws CertificateExpiredException
   *           if the DGC has expired
   * @throws IOException
   *           for errors decoding data, for example CBOR related errors
   * @see #decodeRawToBytes(byte[])
   */
  DigitalGreenCertificate decodeRaw(final byte[] cwt)
      throws DGCSchemaException, SignatureException, CertificateExpiredException, IOException;

  /**
   * Verifies a "raw" DGC (i.e., a signed CWT holding the DGC payload) and returns the CBOR encoding of the DGC payload.
   * 
   * @param cwt
   *          the signed CWT holding the DGC
   * @return the CBOR encoded DGC payload
   * @throws SignatureException
   *           for signature verification errors
   * @throws CertificateExpiredException
   *           if the DGC has expired
   * @throws IOException
   *           for errors decoding data, for example CBOR related errors
   * @see #decodeRaw(byte[])
   */
  byte[] decodeRawToBytes(final byte[] cwt) throws SignatureException, CertificateExpiredException, IOException;

}
