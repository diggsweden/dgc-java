/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.hcert.service;

import java.io.IOException;
import java.security.SignatureException;
import java.time.Instant;

import se.digg.hcert.encoding.Barcode;
import se.digg.hcert.encoding.BarcodeException;
import se.digg.hcert.eu_hcert.v1.DigitalGreenCertificate;
import se.digg.hcert.eu_hcert.v1.HCertSchemaException;

/**
 * Service for creating HCERT:s/Digital Green Certificates.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public interface HCertEncoder {

  /**
   * Based on the HCERT payload and a expiration time, the method creates a HCERT/DGC and delivers it as a barcode.
   * 
   * @param dgc
   *          the contents of the HCERT
   * @param expiration
   *          the expiration of the HCERT
   * @return a barcode containing the signed HCERT
   * @throws HCertSchemaException
   *           for HCERT schema errors
   * @throws IOException
   *           for errors encoding data, for example CBOR related errors
   * @throws SignatureException
   *           errors concerning signing the HCERT
   * @throws BarcodeException
   *           errors creating the barcode
   */
  Barcode encode(final DigitalGreenCertificate dgc, final Instant expiration)
      throws HCertSchemaException, IOException, SignatureException, BarcodeException;

  /**
   * Based on the HCERT payload (in its CBOR encoding) and a expiration time, the method creates a HCERT/DGC and
   * delivers it as a barcode.
   * 
   * <p>
   * Note: This method is mainly for interoperability testing during the phase where the HCERT schema is not finalized.
   * </p>
   * 
   * @param dgc
   *          the contents of the HCERT in its CBOR encoding
   * @param expiration
   *          the expiration of the HCERT
   * @return a barcode containing the signed HCERT
   * @throws IOException
   *           for errors encoding data, for example CBOR related errors
   * @throws SignatureException
   *           errors concerning signing the HCERT
   * @throws BarcodeException
   *           errors creating the barcode
   */
  Barcode encode(final byte[] dgc, final Instant expiration) throws IOException, SignatureException, BarcodeException;

  /**
   * Performs a "raw" encode of the supplied HCERT payload meaning that the method creates a signed CWT holding the
   * HCERT.
   * 
   * @param dgc
   *          the contents of the HCERT
   * @param expiration
   *          the expiration of the HCERT
   * @return the CBOR encoding of the signed HCERT
   * @throws HCertSchemaException
   *           for HCERT schema errors
   * @throws IOException
   *           for errors encoding data, for example CBOR related errors
   * @throws SignatureException
   *           errors concerning signing the HCERT
   */
  byte[] encodeRaw(final DigitalGreenCertificate dgc, final Instant expiration) throws HCertSchemaException, IOException, SignatureException;

  /**
   * Performs a "raw" encode of the supplied HCERT payload meaning that the method creates a signed CWT holding the
   * HCERT.
   * 
   * <p>
   * Note: This method is mainly for interoperability testing during the phase where the HCERT schema is not finalized.
   * </p>
   * 
   * @param dgc
   *          the contents of the HCERT in its CBOR encoding
   * @param expiration
   *          the expiration of the HCERT
   * @return the CBOR encoding of the signed HCERT
   * @throws IOException
   *           for errors encoding data, for example CBOR related errors
   * @throws SignatureException
   *           errors concerning signing the HCERT
   */
  byte[] encodeRaw(final byte[] dgc, final Instant expiration) throws IOException, SignatureException;

}
