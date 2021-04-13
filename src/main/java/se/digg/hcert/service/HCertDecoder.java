/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.hcert.service;

import java.io.IOException;
import java.security.SignatureException;
import java.security.cert.CertificateExpiredException;

import se.digg.hcert.encoding.BarcodeException;
import se.digg.hcert.eu_hcert.v1.VaccinationProof;

/**
 * Service for decoding a HCERT from its image representation into the actual HCERT payload.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public interface HCertDecoder {

  /**
   * Given a barcode image the method decodes, and verifies, the contents into its HCERT/DGC payload.
   * 
   * @param image
   *          the barcode image holding the signed HCERT
   * @return the HCERT payload
   * @throws HCertSchemaException
   *           for HCERT schema errors
   * @throws SignatureException
   *           for signature verification errors
   * @throws CertificateExpiredException
   *           if the HCERT has expired
   * @throws BarcodeException
   *           for errors reading the barcode
   * @throws IOException
   *           for errors decoding data, for example CBOR related errors
   */
  VaccinationProof decode(final byte[] image) throws HCertSchemaException, SignatureException, CertificateExpiredException,
      BarcodeException, IOException;

  /**
   * Given a barcode image the method decodes, and verifies, the contents into the binary CBOR representation of the
   * HCERT.
   * 
   * @param image
   *          the barcode image holding the signed HCERT
   * @return the HCERT payload in its CBOR representation
   * @throws SignatureException
   *           for signature verification errors
   * @throws CertificateExpiredException
   *           if the HCERT has expired
   * @throws BarcodeException
   *           for errors reading the barcode
   * @throws IOException
   *           for errors decoding data, for example CBOR related errors
   */
  byte[] decodeToEncodedHcert(final byte[] image) throws SignatureException, CertificateExpiredException, BarcodeException, IOException;

  /**
   * Decodes a "raw" HCERT (i.e., a signed CWT holding the HCERT) into the payload.
   * 
   * @param cwt
   *          the signed CWT holding the HCERT
   * @return the HCERT payload
   * @throws HCertSchemaException
   *           for HCERT schema errors
   * @throws SignatureException
   *           for signature verification errors
   * @throws CertificateExpiredException
   *           if the HCERT has expired
   * @throws IOException
   *           for errors decoding data, for example CBOR related errors
   */
  VaccinationProof decodeRaw(final byte[] cwt) throws HCertSchemaException, SignatureException, CertificateExpiredException, IOException;

  /**
   * Decodes a "raw" HCERT (i.e., a signed CWT holding the HCERT) into the binary CBOR representation of the HCERT.
   * 
   * @param cwt
   *          the signed CWT holding the HCERT
   * @return the HCERT payload in its CBOR representation
   * @throws SignatureException
   *           for signature verification errors
   * @throws CertificateExpiredException
   *           if the HCERT has expired
   * @throws IOException
   *           for errors decoding data, for example CBOR related errors
   */
  byte[] decodeRawToEncodedHcert(final byte[] cwt) throws SignatureException, CertificateExpiredException, IOException;

}
