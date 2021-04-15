/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.hcert.service.impl;

import java.io.IOException;
import java.security.SignatureException;
import java.security.cert.CertificateExpiredException;
import java.util.Arrays;
import java.util.Optional;

import com.upokecenter.cbor.CBORException;

import lombok.extern.slf4j.Slf4j;
import se.digg.dgc.payload.v1.DGCSchemaException;
import se.digg.dgc.payload.v1.DigitalGreenCertificate;
import se.digg.dgc.payload.v1.MapperUtils;
import se.digg.hcert.encoding.BarcodeDecoder;
import se.digg.hcert.encoding.BarcodeException;
import se.digg.hcert.encoding.Zlib;
import se.digg.hcert.encoding.impl.DefaultBarcodeDecoder;
import se.digg.hcert.service.HCertDecoder;
import se.digg.hcert.signatures.CertificateProvider;
import se.digg.hcert.signatures.HCertSignatureVerifier;
import se.digg.hcert.signatures.impl.DefaultHCertSignatureVerifier;

/**
 * A bean implementing the {@link HCertDecoder} interface.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
@Slf4j
public class DefaultHCertDecoder implements HCertDecoder {

  /** Expected prefix. */
  private static final byte[] HC1_PREFIX = { 'H', 'C', '1' };

  /** The HCERT signature verifier. */
  private final HCertSignatureVerifier hcertSignatureVerifier;

  /** The certificate provider for locating certificates for use when verifying signatures. */
  private final CertificateProvider certificateProvider;

  /** The barcode decoder. */
  private BarcodeDecoder barcodeDecoder = new DefaultBarcodeDecoder();

  public DefaultHCertDecoder(final HCertSignatureVerifier hcertSignatureVerifier, final CertificateProvider certificateProvider) {
    this.hcertSignatureVerifier = Optional.ofNullable(hcertSignatureVerifier).orElse(new DefaultHCertSignatureVerifier());
    this.certificateProvider = Optional.ofNullable(certificateProvider)
      .orElseThrow(() -> new IllegalArgumentException("certificateProvider must be supplied"));
  }

  /** {@inheritDoc} */
  @Override
  public DigitalGreenCertificate decode(final byte[] image)
      throws DGCSchemaException, SignatureException, CertificateExpiredException, BarcodeException, IOException {

    return MapperUtils.toDigitalGreenCertificate(this.decodeToEncodedHcert(image));
  }

  /** {@inheritDoc} */
  @Override
  public byte[] decodeToEncodedHcert(final byte[] image)
      throws SignatureException, CertificateExpiredException, BarcodeException, IOException {

    // Get the barcode from the image and decode it ...
    //
    byte[] base45 = this.barcodeDecoder.decode(image);
    if (Arrays.equals(HC1_PREFIX, Arrays.copyOfRange(base45, 0, HC1_PREFIX.length))) {
      base45 = Arrays.copyOfRange(base45, HC1_PREFIX.length, base45.length);
    }
    else {
      // TODO: Warning during interop-tests - remove later
      log.warn("Missing HCERT header");
    }

    // De-compress
    //
    if (!Zlib.isCompressed(base45)) {
      // TODO: Warning during interop-tests - remove later
      log.warn("CWT has not been compressed");
    }
    final byte[] cwt = Zlib.decompress(base45, false);

    return this.decodeRawToEncodedHcert(cwt);
  }

  /** {@inheritDoc} */
  @Override
  public DigitalGreenCertificate decodeRaw(final byte[] cwt)
      throws DGCSchemaException, SignatureException, CertificateExpiredException, IOException {

    return MapperUtils.toDigitalGreenCertificate(this.decodeRawToEncodedHcert(cwt));
  }

  /** {@inheritDoc} */
  @Override
  public byte[] decodeRawToEncodedHcert(final byte[] cwt) throws SignatureException, CertificateExpiredException, IOException {

    try {
      final HCertSignatureVerifier.Result result = this.hcertSignatureVerifier.verify(cwt, this.certificateProvider);
      
      // TODO: Log
      
      return result.getHcert();
    }
    catch (final CBORException e) {
      throw new IOException("CBOR error - " + e.getMessage(), e);
    }
  }

  /**
   * Assigns the barcode decoder to use.
   * <p>
   * The default is {@link DefaultBarcodeDecoder}.
   * </p>
   * 
   * @param barcodeDecoder
   *          the decoder to use
   */
  public void setBarcodeDecoder(final BarcodeDecoder barcodeDecoder) {
    if (barcodeDecoder != null) {
      this.barcodeDecoder = barcodeDecoder;
    }
  }

}
