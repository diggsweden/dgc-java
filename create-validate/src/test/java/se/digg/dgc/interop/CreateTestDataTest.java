/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.interop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jose.util.StandardCharset;
import com.upokecenter.cbor.CBORObject;

import se.digg.dgc.encoding.Barcode;
import se.digg.dgc.encoding.BarcodeCreator;
import se.digg.dgc.encoding.BarcodeDecoder;
import se.digg.dgc.encoding.Base45;
import se.digg.dgc.encoding.DGCConstants;
import se.digg.dgc.encoding.Zlib;
import se.digg.dgc.encoding.impl.DefaultBarcodeCreator;
import se.digg.dgc.encoding.impl.DefaultBarcodeDecoder;
import se.digg.dgc.interop.TestStatement.TestCtx;
import se.digg.dgc.payload.v1.DGCSchemaVersion;
import se.digg.dgc.payload.v1.DigitalGreenCertificate;
import se.digg.dgc.signatures.CertificateProvider;
import se.digg.dgc.signatures.DGCSignatureVerifier;
import se.digg.dgc.signatures.DGCSigner;
import se.digg.dgc.signatures.impl.DefaultDGCSignatureVerifier;
import se.digg.dgc.signatures.impl.DefaultDGCSigner;
import se.swedenconnect.security.credential.KeyStoreCredential;
import se.swedenconnect.security.credential.PkiCredential;

/**
 * Creates test data for this library. Not really a test ...
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class CreateTestDataTest {

  public static final String targetDirectory = "target/interop";
  
  private static ObjectMapper jsonMapper = new ObjectMapper();

  public static final String BASE_DCG = "{"
      + "\"ver\" : \"1.0.0\","
      + "\"nam\" : { \"fn\" : \"Lövström\", \"fnt\" : \"LOEVSTROEM\", \"gn\" : \"Oscar\", \"gnt\" : \"OSCAR\"},"
      + "\"dob\" : \"1958-11-11\","
      + "\"v\" : [{\"tg\" : \"840539006\", \"vp\" : \"J07BX03\", \"mp\" : \"EU/1/21/1529\" , \"ma\" : \"ORG-100030215\", "
      + "\"dn\" : 2, \"sd\" : 2, \"dt\" : \"2021-03-18\", \"co\" : \"SE\", \"is\" : \"Swedish eHealth Agency\","
      + "\"ci\" : \"urn:uvci:01:SE:EHM/100000024GI5HMGZKSMS\"}]"
      + "}";

  private PkiCredential rsa;
  private PkiCredential ecdsa;

  private static final char[] password = "secret".toCharArray();
  
  static {
    jsonMapper.registerModule(new JavaTimeModule());
    jsonMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    jsonMapper.setSerializationInclusion(Include.NON_NULL);
    jsonMapper.setSerializationInclusion(Include.NON_EMPTY);
  }

  public CreateTestDataTest() throws Exception {
    this.rsa = new KeyStoreCredential(new ClassPathResource("rsa.jks"), password, "signer", password);
    this.rsa.init();
    this.ecdsa = new KeyStoreCredential(new ClassPathResource("ecdsa.jks"), password, "signer", password);
    this.ecdsa.init();
  }
  
  @Test
  public void test1() throws Exception {
    final Instant issueTime = Instant.now();
    final Instant expiration = issueTime.plus(Duration.ofDays(90));
    
    TestStatement test = new TestStatement();

    // Payload
    DigitalGreenCertificate dgc = DigitalGreenCertificate.fromJsonString(BASE_DCG);
    test.setJson(dgc);
    
    // CBOR
    final byte[] cbor = dgc.encode();
    test.setCbor(cbor);

    // COSE
    //
    DGCSigner signer = new DefaultDGCSigner(this.ecdsa);
    final byte[] cose = signer.sign(cbor, expiration);
    test.setCose(cose);
    
    // Compress and Base45
    final byte[] compressed = Zlib.compress(cose);
    final String base45 = Base45.getEncoder().encodeToString(compressed);
    test.setBase45(base45);

    // Prefix
    test.setPrefix(DGCConstants.DGC_V1_HEADER + base45);

    // Barcode
    final BarcodeCreator barcodeCreator = new DefaultBarcodeCreator();
    final Barcode barcode = barcodeCreator.create(test.getPrefix());
    test.setBarCode(barcode.getImage());

    // Test context
    TestStatement.TestCtx testCtx = new TestStatement.TestCtx();
    testCtx.setVersion(1);
    testCtx.setSchema(DGCSchemaVersion.DGC_SCHEMA_VERSION);
    testCtx.setValidationClock(issueTime.plus(Duration.ofDays(1)));
    testCtx.setCertificate(this.ecdsa.getCertificate());
    testCtx.setDescription("1: One vaccination entry - Everything should verify fine");
    test.setTestCtx(testCtx);    

    // Expected result
    TestStatement.ExpectedResults res = new TestStatement.ExpectedResults();
    res.setAllPositive();
    test.setExpectedResults(res);

    // Before we write the test we want to make sure that we can handle it ...
    assertTestStatement("Test #1", test);

    writeTestFile("1.json", test);
  }

  public static void assertTestStatement(final String testName, final TestStatement test) throws Exception {
    TestStatement.ExpectedResults expected = test.getExpectedResults();
    if (expected == null) {
      // If missing, assume test should be successful
      expected = new TestStatement.ExpectedResults();
      expected.setAllPositive();
    }
    if (test.getTestCtx() == null) {
      test.setTestCtx(new TestCtx());
    }

    // Barcode decode
    //
    if (test.getBarCode() != null && test.getPrefix() != null) {
      BarcodeDecoder decoder = new DefaultBarcodeDecoder();
      final String payload = decoder.decodeToString(Base64.getDecoder().decode(test.getBarCode()), StandardCharsets.UTF_8);

      if (expected.expectedPictureDecode.booleanValue()) {
        Assert.assertEquals(String.format("Barcode test for test '%s' failed", testName),
          test.getPrefix(), payload);
      }
    }

    // Base45 prefix test
    if (test.getPrefix() != null && test.getBase45() != null) {
      if (expected.expectedUnprefix.booleanValue()) {
        Assert.assertTrue(String.format("Base45 prefix test failed for test '%s'", testName),
          test.getPrefix().startsWith(DGCConstants.DGC_V1_HEADER));
        Assert.assertEquals(String.format("Base45 prefix test failed for test '%s'", testName),
          test.getBase45(), test.getPrefix().substring(DGCConstants.DGC_V1_HEADER.length()));
      }
    }
    
    // Base45 decode test (and decompress)
    if (test.getBase45() != null && test.getCose() != null) {
      if (expected.expectedBase45Decode) {
        final byte[] decoded = Base45.getDecoder().decode(test.getBase45());
        
        if (!Zlib.isCompressed(decoded)) {
          Assert.fail(String.format("Base45 decode and decompress failed for test '%s' - Not compressed", testName));
        }
        final byte[] decompressed = Zlib.decompress(decoded, false);
        
        Assert.assertArrayEquals(String.format("Base45 decode and decompress failed for test '%s'", testName),
          test.getCoseBytes(), decompressed);
      }
    }
    
    // COSE and CBOR test
    byte[] cosePayload = null;
    if (test.getCose() != null) {
      final DefaultDGCSignatureVerifier verifier = new DefaultDGCSignatureVerifier();
      verifier.setTestValidationTime(test.getTestCtx().getValidationClock());
      
      final X509Certificate cert = test.getTestCtx().getCertificateObject();       
      CertificateProvider certProvider = (c, k) -> Arrays.asList(cert);
      
      try {
        final DGCSignatureVerifier.Result vResult = verifier.verify(test.getCoseBytes(), certProvider);
        cosePayload = vResult.getDgcPayload();
      }
      catch (CertificateExpiredException e) {
        if (expected.expectedExpirationCheck) {
          Assert.fail(String.format("COSE validation failed for test '%s' - Expired", testName));
        }
      }
      catch (Exception e) {
        if (!expected.expectedVerify) {
          Assert.fail(String.format("COSE validation failed for test '%s' - Signature validation error", testName));
        }
      }
    }
    
    // CBOR test
    if (test.getCbor() != null && cosePayload != null) {
      
      if (expected.expectedDecode) {
        if (!Arrays.equals(test.getCborBytes(), cosePayload)) {
          // OK, not byte equal - see if they are the same if we use the same decoder and encoder.
          CBORObject obj1 = CBORObject.DecodeFromBytes(test.getCborBytes());
          CBORObject obj2 = CBORObject.DecodeFromBytes(cosePayload);
          Assert.assertArrayEquals(String.format("Payload extract from COSE failed for '%s'", testName),
            obj1.EncodeToBytes(), obj2.EncodeToBytes());
        }
      }      
    }

  }

  private static void writeTestFile(final String testFile, final TestStatement testStatement) throws IOException {
    final File directory = new File(targetDirectory);
    if (!directory.exists()) {
      directory.mkdir();
    }
    final File file = new File(targetDirectory + "/" + testFile);
    System.out.println("Writing testfile " + file.getAbsolutePath());
    try (FileOutputStream fos = new FileOutputStream(file, false)) {
      fos.write(testStatement.toJson().getBytes(StandardCharset.UTF_8));
      fos.flush();
    }
  }

}
