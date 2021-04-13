/*
 * MIT License
 * 
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.hcert.cose;

import org.junit.Assert;
import org.junit.Test;

import com.upokecenter.cbor.CBORObject;

import se.digg.hcert.signatures.cose.SignatureAlgorithm;

/**
 * Test cases for SignatureAlgorithmId.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class SignatureAlgorithmTest {

  @Test
  public void testFromValue() throws Exception {
    SignatureAlgorithm id = SignatureAlgorithm.fromValue(-7);
    Assert.assertEquals(SignatureAlgorithm.ES256, id);
    
    id = SignatureAlgorithm.fromValue(-35);
    Assert.assertEquals(SignatureAlgorithm.ES384, id);
    
    id = SignatureAlgorithm.fromValue(-36);
    Assert.assertEquals(SignatureAlgorithm.ES512, id);
    
    id = SignatureAlgorithm.fromValue(-37);
    Assert.assertEquals(SignatureAlgorithm.PS256, id);
    
    id = SignatureAlgorithm.fromValue(-38);
    Assert.assertEquals(SignatureAlgorithm.PS384, id);
    
    id = SignatureAlgorithm.fromValue(-39);
    Assert.assertEquals(SignatureAlgorithm.PS512, id);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testFromValueNoMatch() throws Exception {
    SignatureAlgorithm.fromValue(47);
  }
  
  @Test
  public void testFromCborObject() throws Exception {
    SignatureAlgorithm id = SignatureAlgorithm.fromCborObject(CBORObject.FromObject(-7));
    Assert.assertEquals(SignatureAlgorithm.ES256, id);
    
    id = SignatureAlgorithm.fromCborObject(CBORObject.FromObject(-35));
    Assert.assertEquals(SignatureAlgorithm.ES384, id);
    
    id = SignatureAlgorithm.fromCborObject(CBORObject.FromObject(-36));
    Assert.assertEquals(SignatureAlgorithm.ES512, id);
    
    id = SignatureAlgorithm.fromCborObject(CBORObject.FromObject(-37));
    Assert.assertEquals(SignatureAlgorithm.PS256, id);
    
    id = SignatureAlgorithm.fromCborObject(CBORObject.FromObject(-38));
    Assert.assertEquals(SignatureAlgorithm.PS384, id);
    
    id = SignatureAlgorithm.fromCborObject(CBORObject.FromObject(-39));
    Assert.assertEquals(SignatureAlgorithm.PS512, id);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testFromCborObjectNoMatch() throws Exception {
    SignatureAlgorithm.fromCborObject(CBORObject.False);
  }
  
  @Test
  public void testGetValue() throws Exception {
    SignatureAlgorithm id = SignatureAlgorithm.fromValue(-7);
    Assert.assertEquals(-7, id.getValue());
  }
  
  @Test
  public void testGetCborObject() throws Exception {
    SignatureAlgorithm id = SignatureAlgorithm.fromValue(-7);
    Assert.assertEquals(CBORObject.FromObject(-7), id.getCborObject());
  }
  
}
