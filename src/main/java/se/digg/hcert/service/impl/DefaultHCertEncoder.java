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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.upokecenter.cbor.CBORException;

import lombok.extern.slf4j.Slf4j;
import se.digg.hcert.encoding.Barcode;
import se.digg.hcert.encoding.BarcodeCreator;
import se.digg.hcert.encoding.BarcodeException;
import se.digg.hcert.encoding.Base45;
import se.digg.hcert.encoding.Zlib;
import se.digg.hcert.encoding.impl.DefaultBarcodeCreator;
import se.digg.hcert.eu_hcert.v1.VaccinationProof;
import se.digg.hcert.service.HCertEncoder;
import se.digg.hcert.service.HCertSchemaException;
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
  public Barcode encode(final VaccinationProof proof, final Instant expiration) 
      throws HCertSchemaException, IOException, SignatureException, BarcodeException {

    // Get the raw encoding of the signed HCERT ...
    //
    byte[] hcert = this.encodeRaw(proof, expiration);

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
  public byte[] encodeRaw(final VaccinationProof proof, final Instant expiration) throws HCertSchemaException, IOException, SignatureException {
    try {
      // Transform the HCERT payload into its CBOR encoding ...
      //
      final ObjectMapper cborMapper = new CBORMapper();
      final byte[] cborProof = cborMapper.writeValueAsBytes(proof);

      return this.encodeRaw(cborProof, expiration);
    }
    catch (final JsonProcessingException e) {
      final String msg = String.format("Failed to transform HCERT payload into CBOR - %s", e.getMessage());
      log.info("{}", msg, e);
      throw new HCertSchemaException(msg, e);
    }
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
