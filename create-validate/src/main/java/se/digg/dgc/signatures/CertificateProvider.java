/*
 * MIT License
 * 
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.signatures;

import java.security.cert.X509Certificate;
import java.util.List;

/**
 * A functional interface for finding certificates that may be used to verify the signature of a DCC.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
@FunctionalInterface
public interface CertificateProvider {

  /**
   * Given a country code and a key identifier the method finds all certificates that matches this criteria.
   * <p>
   * At least one of the criteria is set in a call.
   * </p>
   * <p>
   * If the key identifier (kid) is {@code null} the provider should return all certificates for the given country.
   * </p>
   * <p>
   * If the country code is {@code null} the provider should return all certificates matching the key identifier.
   * </p>
   * 
   * @param country
   *          the two-letter country code
   * @param kid
   *          the key identifier
   * @return a list of certificates (never null)
   */
  List<X509Certificate> getCertificates(final String country, final byte[] kid);

}
