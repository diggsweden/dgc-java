/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.payload.v1;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;

import com.upokecenter.cbor.CBORObject;
import com.upokecenter.cbor.CBORType;

/**
 * Test cases for encoding/decoding of DigitalCovidCertificate.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class DigitalCovidCertificateTest {

  /**
   * Test CBOR encode/decode.
   * 
   * @throws Exception
   *           for errors
   */
  @Test
  public void testEncodeDecode() throws Exception {
    final DigitalCovidCertificate dgc = (DigitalCovidCertificate) new DigitalCovidCertificate()
      .withNam(new PersonName().withGn("Martin").withFn("Lindström"))
      .withDob("1969-11-11")
      .withV(Arrays.asList(new VaccinationEntry()
        .withTg("840539006")
        .withVp("1119349007")
        .withMp("EU/1/20/1507")
        .withMa("ORG-100030215")
        .withDn(Integer.valueOf(1))
        .withSd(Integer.valueOf(2))
        .withDt(LocalDate.parse("2021-04-17"))
        .withCo("SE")
        .withIs("Swedish eHealth Agency")
        .withCi("01:SE:JKJKHJGHG6768686HGJGH#M")));

    byte[] encoding = dgc.encode();
    
    DigitalCovidCertificate dgc2 = DigitalCovidCertificate.decode(encoding);
    
    Assert.assertEquals(dgc, dgc2);
  }
  
  /**
   * Test JSON representation.
   * 
   * @throws Exception
   *           for errors
   */
  @Test
  public void testJson() throws Exception {
    final DigitalCovidCertificate dgc = (DigitalCovidCertificate) new DigitalCovidCertificate()
      .withNam(new PersonName().withGn("Martin").withFn("Lindström"))
      .withDob("1969-11-11")
      .withV(Arrays.asList(new VaccinationEntry()
        .withTg("840539006")
        .withVp("1119349007")
        .withMp("EU/1/20/1507")
        .withMa("ORG-100030215")
        .withDn(Integer.valueOf(1))
        .withSd(Integer.valueOf(2))
        .withDt(LocalDate.parse("2021-04-17"))
        .withCo("SE")
        .withIs("Swedish eHealth Agency")
        .withCi("01:SE:JKJKHJGHG6768686HGJGH#M")));

    String json = dgc.toJSONString();

    DigitalCovidCertificate dgc2 = DigitalCovidCertificate.fromJsonString(json);
    
    Assert.assertEquals(dgc, dgc2);
  }

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
    final DigitalCovidCertificate dgc = new DigitalCovidCertificate();
    dgc.setDob(LocalDate.parse(date));
    final byte[] cbor = DigitalCovidCertificate.getCBORMapper().writeValueAsBytes(dgc);

    // Assert using the detailed com.upokecenter.cbor library.
    //
    final CBORObject object = CBORObject.DecodeFromBytes(cbor);
    final CBORObject dateObject = object.get("dob");
    Assert.assertNotNull(dateObject);
    Assert.assertEquals(date, dateObject.AsString());

    // Decode
    final DigitalCovidCertificate dgc2 = DigitalCovidCertificate.getCBORMapper().readValue(cbor, DigitalCovidCertificate.class);
    Assert.assertEquals(date, dgc2.getDob().toString());
  }

  /**
   * Tests where we make sure that {@link Instant} works for date-time.
   * 
   * <p>
   * Note important anymore since we represent date-times using seconds since epoch.
   * </p>
   * 
   * @throws Exception
   *           for test errors
   */
  @Test
  public void testDateTime() throws Exception {

    final String dateTime = "2021-04-14T14:17:50.525450Z";

    // Encode
    final DigitalCovidCertificate dgc = new DigitalCovidCertificate(); 
    final TestEntry tst = new TestEntry();
    tst.setCo("SE");
    tst.setSc(Instant.parse(dateTime));
    dgc.setT(Arrays.asList(tst));
    final byte[] cbor = dgc.encode();

    // Assert using the detailed com.upokecenter.cbor library.
    //
    final CBORObject object = CBORObject.DecodeFromBytes(cbor);

    System.out.println(object.ToJSONString());
    System.out.println(Hex.encodeHexString(object.EncodeToBytes()));

    final CBORObject dateObject = object.get("t").get(0).get("sc");
    Assert.assertNotNull(dateObject);

    //
    // Assert that the tag is there ...
    //
    Assert.assertTrue(dateObject.isTagged());
    Assert.assertTrue(dateObject.HasMostOuterTag(0));
    Assert.assertTrue(dateObject.getType() == CBORType.TextString);

    Assert.assertEquals(dateTime, dateObject.AsString());

    // Decode
    final DigitalCovidCertificate dgc2 = DigitalCovidCertificate.decode(cbor);  
    Assert.assertEquals(dateTime, dgc2.getT().get(0).getSc().toString());
  }
  
  @Test
  public void testDateTimeIsoOffset() throws Exception {

    final String dateTime = "2021-05-03T20:00:00+02:00";

    // Encode
    CBORObject tst = CBORObject.NewMap();
    tst.set("co", CBORObject.FromObject("SE"));
    CBORObject dateObject = CBORObject.FromObject(dateTime);
    tst.set("sc", CBORObject.FromObjectAndTag(dateObject, 0));
    
    byte[] cbor = tst.EncodeToBytes();
    
    // Make sure that our CBOR mapper can handle the offset time
    TestEntry tst2 = DigitalCovidCertificate.getCBORMapper().readValue(cbor, TestEntry.class);
    
    // Assert that that it is serialized with no offset
    Assert.assertTrue(tst2.getSc().toString().endsWith("Z"));
  }

  @Test
  public void testDecodeDateTimeWithTag0() throws Exception {
    final String dateTime = "2021-04-14T14:17:50.525Z";
    final Date date = Date.from(Instant.parse(dateTime));

    CBORObject object = CBORObject.NewMap();

    object.set("sc", CBORObject.FromObject(date));
    object.set("co", CBORObject.FromObject("SE"));

    // Just assert that this is a CBOR date-time in string format
    CBORObject dtrObject = object.get("sc");
    Assert.assertTrue(dtrObject.isTagged());
    Assert.assertTrue(dtrObject.HasMostOuterTag(0));
    Assert.assertEquals(dateTime, dtrObject.AsString());

    // Decode using FasterXML
    final TestEntry tst = DigitalCovidCertificate.getCBORMapper().readValue(object.EncodeToBytes(), TestEntry.class);
    Assert.assertEquals(dateTime, tst.getSc().toString());
  }

  @Test
  public void testDecodeDateTimeWithTag1() throws Exception {
    final String dateTime = "2021-04-14T14:17:50.525Z";
    final Date date = Date.from(Instant.parse(dateTime));
    final int seconds = (int) (date.getTime() / 1000);

    CBORObject object = CBORObject.NewMap();
    CBORObject dtrObject = CBORObject.FromObject(seconds);
    object.set("sc", dtrObject.WithTag(1));
    object.set("co", CBORObject.FromObject("SE"));

    // Just assert that this is a CBOR date-time in numeric format
    CBORObject dtrObject2 = object.get("sc");
    Assert.assertTrue(dtrObject2.isTagged());
    Assert.assertTrue(dtrObject2.HasMostOuterTag(1));
    Assert.assertEquals(seconds, dtrObject2.AsInt32Value());

    // Decode using FasterXML
    final TestEntry tst = DigitalCovidCertificate.getCBORMapper().readValue(object.EncodeToBytes(), TestEntry.class);
    Assert.assertEquals((long) seconds, tst.getSc().getEpochSecond());
  }

}
