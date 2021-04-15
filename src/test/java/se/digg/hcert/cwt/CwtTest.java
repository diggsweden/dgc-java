/*
 * MIT License
 * 
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.hcert.cwt;

import java.time.Duration;
import java.time.Instant;

import org.junit.Assert;
import org.junit.Test;

import com.upokecenter.cbor.CBORObject;

import se.digg.hcert.signatures.cwt.Cwt;

/**
 * Test cases for the Cwt class.
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class CwtTest {

  @Test
  public void testCreate() {
    
    final Instant now = Instant.now(); 
    final long seconds = now.getEpochSecond();
    
    final CBORObject object = CBORObject.FromObject("value");
    
    Cwt cwt = Cwt.builder()
      .issuer("Kalle")
      .issuedAt(now)
      .expiration(now.plus(Duration.ofDays(30)))
      .claim("98", object.EncodeToBytes())
      .claim(99, object)
      .build();
    
    Cwt cwt2 = Cwt.decode(cwt.encode());
    
    Assert.assertEquals("Kalle", cwt2.getIssuer());
    Assert.assertEquals(seconds, cwt2.getIssuedAt().getEpochSecond());
    Assert.assertEquals("value", cwt2.getClaim("98").AsString());
    Assert.assertEquals("value", cwt2.getClaim(99).AsString());
  }
  
}
