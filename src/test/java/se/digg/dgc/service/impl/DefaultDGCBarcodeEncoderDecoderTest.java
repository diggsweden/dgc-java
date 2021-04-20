/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import se.digg.dgc.encoding.Barcode;
import se.digg.dgc.encoding.impl.DefaultBarcodeCreator;
import se.digg.dgc.encoding.impl.DefaultBarcodeDecoder;
import se.digg.dgc.payload.v1.DigitalGreenCertificate;
import se.digg.dgc.payload.v1.Id;
import se.digg.dgc.payload.v1.Sub;
import se.digg.dgc.payload.v1.Vac;
import se.digg.dgc.payload.v1.Id.IdentifierType;
import se.digg.dgc.signatures.impl.DefaultDGCSignatureVerifier;
import se.digg.dgc.signatures.impl.DefaultDGCSigner;
import se.swedenconnect.security.credential.KeyStoreCredential;
import se.swedenconnect.security.credential.PkiCredential;

/**
 * Test cases for {@link DefaultDGCBarcodeEncoder}.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class DefaultDGCBarcodeEncoderDecoderTest {
  
  private PkiCredential ecdsa;

  private static final char[] password = "secret".toCharArray();

  public DefaultDGCBarcodeEncoderDecoderTest() throws Exception {
    this.ecdsa = new KeyStoreCredential(new ClassPathResource("ecdsa.jks"), password, "signer", password);
    this.ecdsa.init();
  }
  
  @Test
  public void testEncodeDecodeBarcode() throws Exception {
    
    final Instant expire = Instant.now().plus(Duration.ofDays(30));
    final DigitalGreenCertificate dgc = getTestDGC();
    
    final DefaultDGCBarcodeEncoder encoder = new DefaultDGCBarcodeEncoder(new DefaultDGCSigner(this.ecdsa), new DefaultBarcodeCreator());
    encoder.setTransliterateNames(true);
    
    final Barcode barcode = encoder.encodeToBarcode(dgc, expire);
    
    final DefaultDGCBarcodeDecoder decoder = new DefaultDGCBarcodeDecoder(
      new DefaultDGCSignatureVerifier(), (x,y) -> Arrays.asList(this.ecdsa.getCertificate()), new DefaultBarcodeDecoder());
    
    final DigitalGreenCertificate dgc2 = decoder.decodeBarcode(barcode.getImage());
    Assert.assertEquals(dgc, dgc2);
    Assert.assertEquals("KARL<MAARTEN", dgc2.getSub().getGnt());
    Assert.assertEquals("LINDSTROEM", dgc2.getSub().getFnt());
  }
  
  private DigitalGreenCertificate getTestDGC() {
    DigitalGreenCertificate dgc = new DigitalGreenCertificate();
    dgc.setV("1.0.0");
    dgc.setDgcid(UUID.randomUUID().toString());
    
    Sub sub = new Sub();
    sub
      .withGn("Karl Mårten")
      .withFn("Lindström")
      .withDob(LocalDate.parse("1969-11-29"));
    
    final Id pnr = new Id().withT(IdentifierType.NN).withI("196911292932");
    final Id passport = new Id().withT(IdentifierType.PP).withI("56987413").withC("SE");    
    sub.withId(Arrays.asList(pnr, passport));
    
    dgc.setSub(sub);
    
    Vac vac = new Vac();
    vac
      .withDis("Covid-19")
      .withVap("vap-value")
      .withMep("mep-value")
      .withAut("aut-value")
      .withSeq(Integer.valueOf(1))
      .withTot(Integer.valueOf(2))
      .withDat(LocalDate.parse("2021-04-02"))
      .withCou("SE");
      
    dgc.setVac(Arrays.asList(vac));
    
    return dgc;
  }

}
