/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.payload.v1;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.upokecenter.cbor.CBORObject;

/**
 * Test cases for MapperUtils.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class MapperUtilsTest {

  /**
   * Support for serializing/deserializing of {@link LocalData} doesn't come for free in FasterXML so we'll test this a
   * bit extra.
   * 
   * @throws Exception
   *           for test errors
   */
  @Test
  public void testLocalDate() throws Exception {

    final String date = "1969-11-29";

    // Encode
    final Rec rec = new Rec("Covid-19", LocalDate.parse(date), "SE");
    final byte[] cbor = MapperUtils.getCBORMapper().writeValueAsBytes(rec);

    // Assert using the detailed com.upokecenter.cbor library.
    //
    final CBORObject object = CBORObject.DecodeFromBytes(cbor);
    final CBORObject dateObject = object.get("dat");
    Assert.assertNotNull(dateObject);
    Assert.assertEquals(date, dateObject.AsString());

    // Decode
    final Rec rec2 = MapperUtils.getCBORMapper().readValue(cbor, Rec.class);
    Assert.assertEquals(date, rec2.getDat().toString());
  }
  
  /**
   * Tests where we make sure that {@link Instant} works for date-time.
   * 
   * <p>Note important anymore since we represent date-times using seconds since epoch.</p>
   * 
   * @throws Exception for test errors
   */
  @Test
  public void testDateTime() throws Exception {
       
    final String dateTime = "2021-04-14T14:17:50.525450Z";
    
    // Encode 
    final Tst2 tst = new Tst2();
    tst.setTna("Acme");
    tst.setDtr(Instant.parse(dateTime));
    final byte[] cbor = MapperUtils.getCBORMapper().writeValueAsBytes(tst);
    
    // Assert using the detailed com.upokecenter.cbor library.
    //
    final CBORObject object = CBORObject.DecodeFromBytes(cbor);
    
    System.out.println(object.ToJSONString());
    
    final CBORObject dateObject = object.get("dtr");
    Assert.assertNotNull(dateObject);
  
    //
    // Bug in FasterXML: Missing tag 0 or 1
    //
//    Assert.assertTrue(dateObject.isTagged());
    
    Assert.assertEquals(dateTime, dateObject.AsString());
    
    // Decode
    final Tst2 tst2 = MapperUtils.getCBORMapper().readValue(cbor, Tst2.class);
    Assert.assertEquals(dateTime, tst2.getDtr().toString());
  }
  
  @Test
  public void testDecodeDateTimeWithTag0() throws Exception {
    final String dateTime = "2021-04-14T14:17:50.525Z";
    final Date date = Date.from(Instant.parse(dateTime));
    
    CBORObject object = CBORObject.NewMap();

    object.set("dtr", CBORObject.FromObject(date)); 
    object.set("tna", CBORObject.FromObject("Acme"));

    // Just assert that this is a CBOR date-time in string format
    CBORObject dtrObject = object.get("dtr");
    Assert.assertTrue(dtrObject.isTagged());
    Assert.assertTrue(dtrObject.HasMostOuterTag(0));
    Assert.assertEquals(dateTime, dtrObject.AsString());
    
    // Decode using FasterXML
    final Tst2 tst = MapperUtils.getCBORMapper().readValue(object.EncodeToBytes(), Tst2.class);
    Assert.assertEquals(dateTime, tst.getDtr().toString());
  }
  
  @Test
  public void testDecodeDateTimeWithTag1() throws Exception {
    final String dateTime = "2021-04-14T14:17:50.525Z";
    final Date date = Date.from(Instant.parse(dateTime));
    final int seconds = (int) (date.getTime() / 1000);
    
    CBORObject object = CBORObject.NewMap();
    CBORObject dtrObject = CBORObject.FromObject(seconds);
    object.set("dtr", dtrObject.WithTag(1)); 
    object.set("tna", CBORObject.FromObject("Acme"));

    // Just assert that this is a CBOR date-time in string format
    CBORObject dtrObject2 = object.get("dtr");
    Assert.assertTrue(dtrObject2.isTagged());
    Assert.assertTrue(dtrObject2.HasMostOuterTag(1));
    Assert.assertEquals(seconds, dtrObject2.AsInt32Value());
    
    // Decode using FasterXML
    final Tst2 tst = MapperUtils.getCBORMapper().readValue(object.EncodeToBytes(), Tst2.class);
    Assert.assertEquals((long) seconds, tst.getDtr().getEpochSecond());
  }
  
  public static class Tst2 {
    private Instant dtr;
    private String tna;
    
    public Instant getDtr() {
      return this.dtr;
    }
    public void setDtr(final Instant dtr) {
      this.dtr = dtr;
    }
    public String getTna() {
      return this.tna;
    }
    public void setTna(final String tna) {
      this.tna = tna;
    }
  }
  

}
