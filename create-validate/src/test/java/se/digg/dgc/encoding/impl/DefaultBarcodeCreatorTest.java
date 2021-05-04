/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.encoding.impl;

import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

import se.digg.dgc.encoding.Barcode;

/**
 * Test cases for DefaultBarcodeCreator.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class DefaultBarcodeCreatorTest {

  @Test
  public void testSimpleCreate() throws Exception {
    final DefaultBarcodeCreator creator = new DefaultBarcodeCreator();
    
    final Barcode barcode = creator.create("LINDSTRÖM");
    
//    try (FileOutputStream fos = new FileOutputStream("target/barcode." + barcode.getImageFormat().getName())) {
//      fos.write(barcode.getCode());
//    }
    
    final DefaultBarcodeDecoder decoder = new DefaultBarcodeDecoder();
    
    //final String value = decoder.decodeToString(barcode.getCode(), StandardCharsets.US_ASCII.name());
    final String value = decoder.decodeToString(barcode.getImage(), StandardCharsets.UTF_8);
    
    Assert.assertEquals("LINDSTRÖM", value);
  }

}
