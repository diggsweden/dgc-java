/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.digg.dgc.encoding.Barcode;
import se.digg.dgc.encoding.BarcodeCreator;
import se.digg.dgc.encoding.BarcodeException;
import se.digg.dgc.payload.v1.DGCSchemaException;
import se.digg.dgc.payload.v1.DigitalCovidCertificate;
import se.digg.dgc.service.DGCBarcodeEncoder;
import se.digg.dgc.signatures.DGCSigner;

/**
 * A bean implementing the {@link DGCBarcodeEncoder} interface.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class DefaultDGCBarcodeEncoder extends DefaultDGCEncoder implements DGCBarcodeEncoder {

  /** Logger */
  private static final Logger log = LoggerFactory.getLogger(DefaultDGCBarcodeEncoder.class);

  /** For creating barcodes. */
  private final BarcodeCreator barcodeCreator;

  /**
   * Constructor.
   * 
   * @param dgcSigner
   *          a signer for signing the DGC:s
   * @param barcodeCreator
   *          the barcode creator to use
   */
  public DefaultDGCBarcodeEncoder(final DGCSigner dgcSigner, final BarcodeCreator barcodeCreator) {
    super(dgcSigner);
    this.barcodeCreator = Optional.ofNullable(barcodeCreator).orElseThrow(() -> new IllegalArgumentException("barcodeCreator must be set"));
  }

  /** {@inheritDoc} */
  @Override
  public Barcode encodeToBarcode(final DigitalCovidCertificate dcc, final Instant expiration)
      throws DGCSchemaException, IOException, SignatureException, BarcodeException {
    
    log.trace("Creating barcode from DCC payload: {}", dcc);

    final String base45 = this.encode(dcc, expiration);
    
    // Create the Barcode ...
    //
    log.trace("Creating barcode ...");
    final Barcode barcode = this.barcodeCreator.create(base45, StandardCharsets.US_ASCII);
    log.trace("Successfully created: {}", barcode);

    return barcode;    
  }

  /** {@inheritDoc} */
  @Override
  public Barcode encodeToBarcode(final byte[] dcc, final Instant expiration) throws IOException, SignatureException, BarcodeException {

    final String base45 = this.encode(dcc, expiration);

    // Create the Barcode ...
    //
    log.trace("Creating barcode ...");
    final Barcode barcode = this.barcodeCreator.create(base45, StandardCharsets.US_ASCII);
    log.trace("Successfully created: {}", barcode);

    return barcode;
  }

}
