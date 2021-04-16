/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.service.impl;

import java.io.IOException;
import java.security.SignatureException;
import java.security.cert.CertificateExpiredException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.digg.dgc.encoding.BarcodeDecoder;
import se.digg.dgc.encoding.BarcodeException;
import se.digg.dgc.payload.v1.DGCSchemaException;
import se.digg.dgc.payload.v1.DigitalGreenCertificate;
import se.digg.dgc.payload.v1.MapperUtils;
import se.digg.dgc.service.DGCBarcodeDecoder;
import se.digg.dgc.signatures.CertificateProvider;
import se.digg.dgc.signatures.DGCSignatureVerifier;
import se.digg.dgc.signatures.impl.DefaultDGCSignatureVerifier;

/**
 * A bean implementing the {@link DGCBarcodeDecoder} interface.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class DefaultDGCBarcodeDecoder extends DefaultDGCDecoder implements DGCBarcodeDecoder {

  /** Logger */
  private static final Logger log = LoggerFactory.getLogger(DefaultDGCBarcodeDecoder.class);

  /** The barcode decoder. */
  private BarcodeDecoder barcodeDecoder;

  /**
   * Constructor.
   * 
   * @param dgcSignatureVerifier
   *          the signature verifier - if null, an instance of {@link DefaultDGCSignatureVerifier} will be used
   * @param certificateProvider
   *          the certificate provider that is used to locate certificates to use when verifying signatures
   * @param barcodeDecoder
   *          the barcode decoder to use
   */
  public DefaultDGCBarcodeDecoder(final DGCSignatureVerifier dgcSignatureVerifier, 
      final CertificateProvider certificateProvider, final BarcodeDecoder barcodeDecoder) {
    
    super(dgcSignatureVerifier, certificateProvider);
    this.barcodeDecoder = barcodeDecoder;
  }

  /** {@inheritDoc} */
  @Override
  public DigitalGreenCertificate decodeBarcode(final byte[] image)
      throws DGCSchemaException, SignatureException, CertificateExpiredException, BarcodeException, IOException {

    final byte[] encodedDgc = this.decodeBarcodeToBytes(image);

    log.trace("CBOR decoding DGC ...");
    final DigitalGreenCertificate dgc = MapperUtils.toDigitalGreenCertificate(encodedDgc);
    log.trace("Decoded into: {}", dgc);

    return dgc;
  }

  /** {@inheritDoc} */
  @Override
  public byte[] decodeBarcodeToBytes(final byte[] image)
      throws SignatureException, CertificateExpiredException, BarcodeException, IOException {

    // Get the barcode from the image and decode it ...
    //
    log.trace("Decoding barcode image ...");
    String base45 = this.barcodeDecoder.decodeToString(image, null);
    log.trace("Decoded barcode image into {}", base45);
    
    return this.decodeToBytes(base45);
  }

}
