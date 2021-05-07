/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.interop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jose.util.StandardCharset;

import se.digg.dgc.encoding.Barcode;
import se.digg.dgc.encoding.BarcodeCreator;
import se.digg.dgc.encoding.Base45;
import se.digg.dgc.encoding.DGCConstants;
import se.digg.dgc.encoding.Zlib;
import se.digg.dgc.encoding.impl.DefaultBarcodeCreator;
import se.digg.dgc.payload.v1.DGCSchemaVersion;
import se.digg.dgc.payload.v1.DigitalGreenCertificate;
import se.digg.dgc.signatures.DGCSigner;
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

  public static final String SRC_DIR = "src/test/resources/interop/se-payloads";
  public static final String TARGET_DIR = "target/interop";

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
    final DigitalGreenCertificate dgc = readDgcFile("dgc1.json", DigitalGreenCertificate.class); 
    final Instant issueTime = Instant.now();
    final Instant expiration = issueTime.plus(Duration.ofDays(90));    
    final DGCSigner signer = new DefaultDGCSigner(this.ecdsa);

    final TestStatement test = createTestStatement(dgc, dgc.encode(), issueTime, expiration, signer, null, null, this.ecdsa.getCertificate());
    test.getTestCtx().setDescription("1: One vaccination entry - Everything should verify fine");

    // Before we write the test we want to make sure that we can handle it ...
    DGCTestDataVerifier.validate("Test #1", test);

    writeTestFile("1.json", test);
  }

  /**
   * Creates a test statement.
   * 
   * @param dgcPayload
   *          the DGC payload
   * @param cborPayload
   *          the CBOR encoding of the DGC payload
   * @param issueTime
   *          the issuance time
   * @param expirationTime
   *          the expiration time
   * @param signer
   *          the DGC signer
   * @param compress
   *          compress flag (if null, compression will be done)
   * @param validationClock
   *          the time when validation should be done
   * @param signerCert
   *          the signer certificate
   * @return a test statement
   * @throws Exception
   *           for errors
   */
  private static TestStatement createTestStatement(final Object dgcPayload, final byte[] cborPayload,
      final Instant issueTime, final Instant expirationTime, final DGCSigner signer,
      final Boolean compress, final Instant validationClock, final X509Certificate signerCert) throws Exception {

    TestStatement test = new TestStatement();

    // Payload
    test.setJson(dgcPayload);

    // CBOR
    test.setCbor(cborPayload);

    // COSE
    final byte[] cose = signer.sign(cborPayload, expirationTime);
    test.setCose(cose);

    // Compress and Base45
    final String base45;
    if (compress == null || compress.booleanValue()) {
      final byte[] compressed = Zlib.compress(cose);
      base45 = Base45.getEncoder().encodeToString(compressed);
      test.setBase45(base45);
    }
    else {
      base45 = Base45.getEncoder().encodeToString(cose);
      test.setBase45(base45);
    }

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
    if (validationClock != null) {
      testCtx.setValidationClock(validationClock);
    }
    else {
      testCtx.setValidationClock(issueTime.plus(Duration.ofDays(1)));
    }
    testCtx.setCertificate(signerCert);
    test.setTestCtx(testCtx);

    // Expected result
    TestStatement.ExpectedResults res = new TestStatement.ExpectedResults();
    res.setAllPositive();
    test.setExpectedResults(res);

    return test;
  }

  private static void writeTestFile(final String testFile, final TestStatement testStatement) throws IOException {
    final File directory = new File(TARGET_DIR);
    if (!directory.exists()) {
      directory.mkdir();
    }
    final File file = new File(TARGET_DIR + "/" + testFile);
    System.out.println("Writing testfile " + file.getAbsolutePath());
    try (FileOutputStream fos = new FileOutputStream(file, false)) {
      fos.write(testStatement.toJson().getBytes(StandardCharset.UTF_8));
      fos.flush();
    }
  }

  private static <T> T readDgcFile(final String file, final Class<T> clazz) throws IOException {
    return jsonMapper.readValue(new File(SRC_DIR + "/" + file), clazz);
  }

}
