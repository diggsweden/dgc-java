/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.hcert.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.time.Instant;
import java.util.Optional;

import com.upokecenter.cbor.CBORException;

import lombok.extern.slf4j.Slf4j;
import se.digg.dgc.payload.v1.DGCSchemaException;
import se.digg.dgc.payload.v1.DigitalGreenCertificate;
import se.digg.dgc.payload.v1.MapperUtils;
import se.digg.hcert.encoding.Barcode;
import se.digg.hcert.encoding.BarcodeCreator;
import se.digg.hcert.encoding.BarcodeException;
import se.digg.hcert.encoding.Base45;
import se.digg.hcert.encoding.Zlib;
import se.digg.hcert.encoding.impl.DefaultBarcodeCreator;
import se.digg.hcert.service.HCertEncoder;
import se.digg.hcert.signatures.HCertSigner;

/**
 * A bean implementing the {@link HCertEncoder} interface.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
@Slf4j
public class DefaultHCertEncoder implements HCertEncoder {

  public static final String HCERT_HEADER = "HC1";

  /** The HCERT signer instance. */
  private final HCertSigner hcertSigner;

  private final BarcodeCreator barcodeCreator;

  /**
   * Constructor.
   * 
   * @param hcertSigner
   *          a HCertSigner for signing the HCERT:s
   */
  public DefaultHCertEncoder(final HCertSigner hcertSigner) {
    this(hcertSigner, null);
  }

  /**
   * Constructor.
   * 
   * @param hcertSigner
   *          a HCertSigner for signing the HCERT:s
   * @param barcodeCreator
   *          a barcode creator, if null, a {@link DefaultBarcodeCreator} with default settings is used
   */
  public DefaultHCertEncoder(final HCertSigner hcertSigner, final BarcodeCreator barcodeCreator) {
    this.hcertSigner = Optional.ofNullable(hcertSigner).orElseThrow(() -> new IllegalArgumentException("hcertSigner must not be null"));
    this.barcodeCreator = Optional.ofNullable(barcodeCreator).orElse(new DefaultBarcodeCreator());
  }

  /** {@inheritDoc} */
  @Override
  public Barcode encode(final DigitalGreenCertificate dgc, final Instant expiration)
      throws DGCSchemaException, IOException, SignatureException, BarcodeException {

    // Get the raw encoding of the signed HCERT ...
    //
    byte[] hcert = this.encodeRaw(dgc, expiration);

    // Compression and Base45 encoding ...
    //
    hcert = Zlib.compress(hcert);
    final String base45 = Base45.getEncoder().encodeToString(hcert);

    // Add the HCERT header and create the barcode ...
    //
    return this.barcodeCreator.create(HCERT_HEADER + base45, StandardCharsets.US_ASCII);
  }

  /** {@inheritDoc} */
  @Override
  public Barcode encode(final byte[] proof, final Instant expiration) throws IOException, SignatureException, BarcodeException {
    byte[] hcert = this.encodeRaw(proof, expiration);
    hcert = Zlib.compress(hcert);
    final String base45 = Base45.getEncoder().encodeToString(hcert);
    return this.barcodeCreator.create(HCERT_HEADER + base45, StandardCharsets.US_ASCII);
  }

  /** {@inheritDoc} */
  @Override
  public byte[] encodeRaw(final DigitalGreenCertificate dgc, final Instant expiration) 
      throws DGCSchemaException, IOException, SignatureException {
    
    // Transform the HCERT payload into its CBOR encoding ...
    final byte[] cborProof = MapperUtils.toCBOREncoding(dgc);

    return this.encodeRaw(cborProof, expiration);
  }

  /** {@inheritDoc} */
  @Override
  public byte[] encodeRaw(final byte[] proof, final Instant expiration) throws IOException, SignatureException {

    try {
      // Sign the HCERT ...
      //
      return this.hcertSigner.sign(proof, expiration);
    }
    catch (final CBORException e) {
      log.info("Internal CBOR error - {}", e.getMessage(), e);
      throw new IOException("Internal CBOR error - " + e.getMessage(), e);
    }
  }

}
