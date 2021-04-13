/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.hcert.signatures.impl;

import java.security.SignatureException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.List;

import com.upokecenter.cbor.CBORException;

import lombok.extern.slf4j.Slf4j;
import se.digg.hcert.signatures.CertificateProvider;
import se.digg.hcert.signatures.HCertSignatureVerifier;
import se.digg.hcert.signatures.cose.CoseSign1_Object;
import se.digg.hcert.signatures.cwt.Cwt;

/**
 * Implementation of the {@link HCertSignatureVerifier} interface.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
@Slf4j
public class DefaultHCertSignatureVerifier implements HCertSignatureVerifier {

  /** {@inheritDoc} */
  @Override
  public HCertSignatureVerifier.Result verify(final byte[] signedHcert, final CertificateProvider certificateProvider)
      throws SignatureException, CertificateExpiredException {

    if (certificateProvider == null) {
      throw new IllegalArgumentException("certificateProvider must be supplied");
    }

    try {
      final CoseSign1_Object object = CoseSign1_Object.decode(signedHcert);
      
      final byte[] kid = object.getKeyIdentifier();
      final String country = object.getCwt().getIssuer();

      if (kid == null && country == null) {
        throw new SignatureException("Signed object does not contain key identifier or country - cannot find certificate");
      }
      
      final List<X509Certificate> certs = certificateProvider.getCertificates(country, kid);
      
      for (X509Certificate cert : certs) {
        log.debug("Attempting HCERT signature verification using certificate '{}'", cert.getSubjectX500Principal().getName());

        try {
          object.verifySignature(cert.getPublicKey());
          log.debug("HCERT signature verification succeeded using certificate '{}'", cert.getSubjectX500Principal().getName());

          // OK, before we are done - let's ensure that the HCERT hasn't expired.
          final Cwt cwt = object.getCwt();

          final Instant expiration = cwt.getExpiration();
          if (expiration != null) {
            if (Instant.now().isAfter(expiration)) {
              throw new CertificateExpiredException("Signed HCERT has expired");
            }
          }
          else {
            log.warn("Signed HCERT did not contain an expiration time - assuming it is valid");
          }
          
          return new HCertSignatureVerifier.Result(cwt.getHcertv1(), cert, country, cwt.getIssuedAt(), cwt.getExpiration());
        }
        catch (CBORException | SignatureException e) {
          log.info("HCERT signature verification failed using certificate '{}' - {}",
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
