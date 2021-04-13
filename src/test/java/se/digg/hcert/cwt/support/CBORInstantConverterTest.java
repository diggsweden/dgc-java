/*
 * MIT License
 * 
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.hcert.cwt.support;

import java.time.Instant;

import org.junit.Assert;
import org.junit.Test;

import com.upokecenter.cbor.CBORObject;

import se.digg.hcert.signatures.cwt.support.CBORInstantConverter;

/**
 * Test cases for CBORInstantConverter.
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class CBORInstantConverterTest {

  @Test
  public void testConvert() throws Exception {
    final CBORInstantConverter conv = new CBORInstantConverter();
    
    final Instant now = Instant.now();
    final long seconds = now.getEpochSecond();
    
    final CBORObject obj = conv.ToCBORObject(now);
    Instant instant = conv.FromCBORObject(obj);
    
    Assert.assertEquals(seconds, instant.getEpochSecond());
  }

}
