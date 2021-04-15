/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.hcert.service.impl;

import java.time.Duration;
import java.time.Instant;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import se.digg.hcert.encoding.Barcode;
import se.digg.hcert.eu_hcert.v1.DigitalGreenCertificate;
import se.digg.hcert.eu_hcert.v1.Sub;
import se.digg.hcert.signatures.HCertSigner;
import se.digg.hcert.signatures.impl.DefaultHCertSigner;
import se.swedenconnect.security.credential.KeyStoreCredential;
import se.swedenconnect.security.credential.PkiCredential;

/**
 * Test cases for DefaultHCertEncoder.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class DefaultHCertEncoderTest {
  
  private PkiCredential rsa;
  private PkiCredential ecdsa;

  private static final char[] password = "secret".toCharArray();
  
//  private static final byte[] HCERT = "dummy-hcert".getBytes(StandardCharset.UTF_8);

  public DefaultHCertEncoderTest() throws Exception {
    this.rsa = new KeyStoreCredential(new ClassPathResource("rsa.jks"), password, "signer", password);
    this.rsa.init();
    this.ecdsa = new KeyStoreCredential(new ClassPathResource("ecdsa.jks"), password, "signer", password);
    this.ecdsa.init();
  }
  
  @Test
  public void testCreateDefault() throws Exception {
    
    final Instant now = Instant.now();
    
    final HCertSigner signer = new DefaultHCertSigner(this.ecdsa);    
    DefaultHCertEncoder encoder = new DefaultHCertEncoder(signer);
    
    final DigitalGreenCertificate dgc = new DigitalGreenCertificate();
    final Sub sub = new Sub();
//    sub.setDob("19691129");
    dgc.setSub(sub);
    
    Barcode barcode = encoder.encode(dgc, now.plus(Duration.ofDays(30)));
    
  }


}
