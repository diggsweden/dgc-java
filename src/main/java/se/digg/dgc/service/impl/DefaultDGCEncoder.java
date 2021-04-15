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

import com.upokecenter.cbor.CBORException;

import se.digg.dgc.encoding.Barcode;
import se.digg.dgc.encoding.BarcodeCreator;
import se.digg.dgc.encoding.BarcodeException;
import se.digg.dgc.encoding.Base45;
import se.digg.dgc.encoding.DGCConstants;
import se.digg.dgc.encoding.Zlib;
import se.digg.dgc.encoding.impl.DefaultBarcodeCreator;
import se.digg.dgc.payload.v1.DGCSchemaException;
import se.digg.dgc.payload.v1.DigitalGreenCertificate;
import se.digg.dgc.payload.v1.MapperUtils;
import se.digg.dgc.service.DGCEncoder;
import se.digg.dgc.signatures.DGCSigner;

/**
 * A bean implementing the {@link DGCEncoder} interface.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class DefaultDGCEncoder implements DGCEncoder {

  /** Logger */
  private static final Logger log = LoggerFactory.getLogger(DefaultDGCEncoder.class);

  /** The DGC signer instance. */
  private final DGCSigner dgcSigner;

  /** For creating barcodes. */
  private final BarcodeCreator barcodeCreator;

  /**
   * Constructor.
   * <p>
   * See {@link #DefaultDGCEncoder(DGCSigner, BarcodeCreator)}.
   * </p>
   * 
   * @param dgcSigner
   *          for signing the DGC:s
   */
  public DefaultDGCEncoder(final DGCSigner dgcSigner) {
    this(dgcSigner, null);
  }

  /**
   * Constructor.
   * 
   * @param dgcSigner
   *          a signer for signing the DGC:s
   * @param barcodeCreator
   *          a barcode creator, if null, a {@link DefaultBarcodeCreator} with default settings is used
   */
  public DefaultDGCEncoder(final DGCSigner dgcSigner, final BarcodeCreator barcodeCreator) {
    this.dgcSigner = Optional.ofNullable(dgcSigner).orElseThrow(() -> new IllegalArgumentException("dgcSigner must not be null"));
    this.barcodeCreator = Optional.ofNullable(barcodeCreator).orElse(new DefaultBarcodeCreator());
  }

  /** {@inheritDoc} */
  @Override
  public Barcode encode(final DigitalGreenCertificate dgc, final Instant expiration)
      throws DGCSchemaException, IOException, SignatureException, BarcodeException {
    
    log.trace("Creating a barcode from DGC payload: {}");
        
    log.trace("Encoding to CBOR ...");
    final byte[] cborDgc = MapperUtils.toCBOREncoding(dgc);
    log.trace("Encoded DGC into {} bytes", cborDgc.length);
    
    return this.encode(cborDgc, expiration);
  }

  /** {@inheritDoc} */
  @Override
  public Barcode encode(final byte[] dgc, final Instant expiration) throws IOException, SignatureException, BarcodeException {

    log.trace("Creating a barcode from CBOR-encoded DGC-payload (length: {}) ...", dgc.length);
    
    // Create a signed CWT ...
    //
    byte[] cwt = this.sign(dgc, expiration);
    
    // Compression and Base45 encoding ...
    //
    log.trace("Compressing the signed CWT of length {} ...", cwt.length);
    cwt = Zlib.compress(cwt);
    log.trace("Signed CWT was compressed into {} bytes", cwt.length);
    
    log.trace("Base45 encoding compressed CWT ...");
    final String base45 = Base45.getEncoder().encodeToString(cwt);
    log.trace("Base45 encoding: {}", base45);
    
    // Create the Barcode ...
    //
    log.trace("Creating barcode ...");
    final Barcode barcode = this.barcodeCreator.create(DGCConstants.DGC_V1_HEADER + base45, StandardCharsets.US_ASCII);
    log.trace("Successfully created: {}", barcode);
    
    return barcode;
  }

  /** {@inheritDoc} */
  @Override
  public byte[] sign(final DigitalGreenCertificate dgc, final Instant expiration)
      throws DGCSchemaException, IOException, SignatureException {

    log.trace("Signing DGC: {}", dgc);
    
    // Transform the DGC payload into its CBOR encoding ...
    log.trace("CBOR encoding DGC ...");
    final byte[] cborDgc = MapperUtils.toCBOREncoding(dgc);
    log.trace("Encoded DGC into {} bytes", cborDgc.length);

    return this.sign(cborDgc, expiration);
  }

  /** {@inheritDoc} */
  @Override
  public byte[] sign(final byte[] dgc, final Instant expiration) throws IOException, SignatureException {

    try {
      // Sign the DGC ...
      //
      log.trace("Creating CWT and signing CBOR-encoded DGC (length: {}) ...", dgc.length);
      return this.dgcSigner.sign(dgc, expiration);
    }
    catch (final CBORException e) {
      log.info("Internal CBOR error - {}", e.getMessage(), e);
      throw new IOException("Internal CBOR error - " + e.getMessage(), e);
    }
  }

}
