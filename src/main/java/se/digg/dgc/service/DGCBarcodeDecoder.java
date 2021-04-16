/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.service;

import java.io.IOException;
import java.security.SignatureException;
import java.security.cert.CertificateExpiredException;

import se.digg.dgc.encoding.BarcodeException;
import se.digg.dgc.payload.v1.DGCSchemaException;
import se.digg.dgc.payload.v1.DigitalGreenCertificate;

/**
 * An extension to the {@link DGCDecoder} interface that defines methods for decoding from a barcode image. 
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public interface DGCBarcodeDecoder extends DGCDecoder {
  
  /**
   * Given a barcode image the method verifies amd decodes the contents into its DGC payload representation.
   * 
   * @param image
   *          the barcode image holding the encoded and signed DGC
   * @return the DGC payload
   * @throws DGCSchemaException
   *           for DGC schema errors
   * @throws SignatureException
   *           for signature verification errors
   * @throws CertificateExpiredException
   *           if the DGC has expired
   * @throws BarcodeException
   *           for errors reading the barcode
   * @throws IOException
   *           for errors decoding data, for example CBOR related errors
   * @see #decodeBarcodeToBytes(byte[])
   */
  DigitalGreenCertificate decodeBarcode(final byte[] image)
      throws DGCSchemaException, SignatureException, CertificateExpiredException, BarcodeException, IOException;

  /**
   * Given a barcode image the method verifies and decodes the contents into the CBOR encoding of the DGC payload.
   * 
   * @param image
   *          the barcode image holding the encoded and signed DGC
   * @return the DGC payload in its CBOR representation
   * @throws SignatureException
   *           for signature verification errors
   * @throws CertificateExpiredException
   *           if the DGC has expired
   * @throws BarcodeException
   *           for errors reading the barcode
   * @throws IOException
   *           for errors decoding data, for example CBOR related errors
   * @see #decodeBarcode(byte[])
   */
  byte[] decodeBarcodeToBytes(final byte[] image)
      throws SignatureException, CertificateExpiredException, BarcodeException, IOException;
  
}
