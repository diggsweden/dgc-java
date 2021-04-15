/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.signatures.impl;

import java.security.SignatureException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.upokecenter.cbor.CBORException;

import se.digg.dgc.signatures.CertificateProvider;
import se.digg.dgc.signatures.DGCSignatureVerifier;
import se.digg.dgc.signatures.cose.CoseSign1_Object;
import se.digg.dgc.signatures.cwt.Cwt;

/**
 * Implementation of the {@link DGCSignatureVerifier} interface.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class DefaultDGCSignatureVerifier implements DGCSignatureVerifier {
  
  /** Logger */
  private static final Logger log = LoggerFactory.getLogger(DefaultDGCSignatureVerifier.class);

  /** {@inheritDoc} */
  @Override
  public DGCSignatureVerifier.Result verify(final byte[] signedCwt, final CertificateProvider certificateProvider)
      throws SignatureException, CertificateExpiredException {

    if (certificateProvider == null) {
      throw new IllegalArgumentException("certificateProvider must be supplied");
    }

    try {
      final CoseSign1_Object coseObject = CoseSign1_Object.decode(signedCwt);
      
      final byte[] kid = coseObject.getKeyIdentifier();
      final String country = coseObject.getCwt().getIssuer();

      if (kid == null && country == null) {
        throw new SignatureException("Signed object does not contain key identifier or country - cannot find certificate");
      }
      
      final List<X509Certificate> certs = certificateProvider.getCertificates(country, kid);
      
      for (final X509Certificate cert : certs) {
        log.trace("Attempting DGC signature verification using certificate '{}'", cert.getSubjectX500Principal().getName());

        try {
          coseObject.verifySignature(cert.getPublicKey());
          log.debug("DGC signature verification succeeded using certificate '{}'", cert.getSubjectX500Principal().getName());

          // OK, before we are done - let's ensure that the HCERT hasn't expired.
          final Cwt cwt = coseObject.getCwt();

          final Instant expiration = cwt.getExpiration();
          if (expiration != null) {
            if (Instant.now().isAfter(expiration)) {
              throw new CertificateExpiredException("Signed DGC has expired");
            }
          }
          else {
            log.warn("Signed HCERT did not contain an expiration time - assuming it is valid");
          }
          
          final byte[] dgcPayload = cwt.getDgcV1();
          if (dgcPayload == null) {
            throw new SignatureException("No DGC payload available in CWT");
          }
          
          return new DGCSignatureVerifier.Result(dgcPayload, cert, country, cwt.getIssuedAt(), cwt.getExpiration());
        }
        catch (CBORException | SignatureException e) {
          log.info("DGC signature verification failed using certificate '{}' - {}",
            cert.getSubjectX500Principal().getName(), e.getMessage(), e);
        }
      }
      if (certs.isEmpty()) {
        throw new SignatureException("No signer certificates could be found");
      }
      else {
        throw new SignatureException("Signature verification failed for all attempted certificates");
      }

    }
    catch (final CBORException e) {
      throw new SignatureException("Invalid signature - " + e.getMessage(), e);
    }
  }

}
