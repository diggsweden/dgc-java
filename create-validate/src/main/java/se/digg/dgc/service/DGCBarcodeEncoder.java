/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.service;

import java.io.IOException;
import java.security.SignatureException;
import java.time.Instant;

import se.digg.dgc.encoding.Barcode;
import se.digg.dgc.encoding.BarcodeException;
import se.digg.dgc.payload.v1.DGCSchemaException;
import se.digg.dgc.payload.v1.DigitalCovidCertificate;

/**
 * An extension to the {@link DGCEncoder} interface that defines methods for encoding to a barcode representation. 
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public interface DGCBarcodeEncoder extends DGCEncoder {
  
  /**
   * Based on the DCC payload and a expiration time, the method encodes the payload to CBOR, signs it, deflates it, Base45 encodes it,
   * and finally delivers it as a barcode.
   * 
   * @param dcc
   *          the contents of the DCC
   * @param expiration
   *          the expiration of the DCC
   * @return a barcode containing the signed DCC
   * @throws DGCSchemaException
   *           for DCC schema errors
   * @throws IOException
   *           for errors encoding data, for example CBOR related errors
   * @throws SignatureException
   *           errors concerning signing the DCC
   * @throws BarcodeException
   *           errors creating the barcode
   */
  Barcode encodeToBarcode(final DigitalCovidCertificate dcc, final Instant expiration)
      throws DGCSchemaException, IOException, SignatureException, BarcodeException;

  /**
   * Based on the CBOR encoded DCC payload and a expiration time, the method signs it, deflates it, Base45 encodes it,
   * and finally delivers it as a barcode.
   * 
   * @param dcc
   *          the contents of the DCC in its CBOR encoding
   * @param expiration
   *          the expiration of the DCC
   * @return a barcode containing the signed DCC
   * @throws IOException
   *           for errors encoding data, for example CBOR related errors
   * @throws SignatureException
   *           errors concerning signing the DCC
   * @throws BarcodeException
   *           errors creating the barcode
   */
  Barcode encodeToBarcode(final byte[] dcc, final Instant expiration) throws IOException, SignatureException, BarcodeException;


}
