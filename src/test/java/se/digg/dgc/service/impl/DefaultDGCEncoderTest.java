/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.service.impl;

import java.time.Duration;
import java.time.Instant;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import se.digg.dgc.encoding.Barcode;
import se.digg.dgc.payload.v1.DigitalGreenCertificate;
import se.digg.dgc.payload.v1.Sub;
import se.digg.dgc.service.impl.DefaultDGCEncoder;
import se.digg.dgc.signatures.DGCSigner;
import se.digg.dgc.signatures.impl.DefaultDGCSigner;
import se.swedenconnect.security.credential.KeyStoreCredential;
import se.swedenconnect.security.credential.PkiCredential;

/**
 * Test cases for DefaultHCertEncoder.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class DefaultDGCEncoderTest {
  
  private PkiCredential rsa;
  private PkiCredential ecdsa;

  private static final char[] password = "secret".toCharArray();
  
//  private static final byte[] HCERT = "dummy-hcert".getBytes(StandardCharset.UTF_8);

  public DefaultDGCEncoderTest() throws Exception {
    this.rsa = new KeyStoreCredential(new ClassPathResource("rsa.jks"), password, "signer", password);
    this.rsa.init();
    this.ecdsa = new KeyStoreCredential(new ClassPathResource("ecdsa.jks"), password, "signer", password);
    this.ecdsa.init();
  }
  
  @Test
  public void testCreateDefault() throws Exception {
    
    final Instant now = Instant.now();
    
    final DGCSigner signer = new DefaultDGCSigner(this.ecdsa);    
    DefaultDGCEncoder encoder = new DefaultDGCEncoder(signer);
    
    final DigitalGreenCertificate dgc = new DigitalGreenCertificate();
    final Sub sub = new Sub();
//    sub.setDob("19691129");
    dgc.setSub(sub);
    
    Barcode barcode = encoder.encode(dgc, now.plus(Duration.ofDays(30)));
    
  }


}
