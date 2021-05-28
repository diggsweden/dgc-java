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
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Base64;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import com.nimbusds.jose.util.StandardCharset;

import se.digg.dgc.encoding.Barcode;
import se.digg.dgc.encoding.BarcodeCreator;
import se.digg.dgc.encoding.Base45;
import se.digg.dgc.encoding.DGCConstants;
import se.digg.dgc.encoding.Zlib;
import se.digg.dgc.encoding.impl.DefaultBarcodeCreator;
import se.digg.dgc.payload.v1.DGCSchemaVersion;
import se.digg.dgc.payload.v1.DigitalCovidCertificate;
import se.digg.dgc.payload.v1.PersonName;
import se.digg.dgc.payload.v1.RecoveryEntry;
import se.digg.dgc.payload.v1.TestEntry;
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

  private PkiCredential rsa;
  private PkiCredential ecdsaVacPurpose;
  private PkiCredential ecdsaAllPurposes;
  private PkiCredential ecdsaOther;

  private static final char[] password = "secret".toCharArray();

  public CreateTestDataTest() throws Exception {
    this.rsa = new KeyStoreCredential(new ClassPathResource("rsa.jks"), password, "signer", password);
    this.rsa.init();
    this.ecdsaVacPurpose = new KeyStoreCredential(new ClassPathResource("dgc-signer.jks"), password, "dgc-signer-test-vac-only", password);
    this.ecdsaVacPurpose.init();
    this.ecdsaAllPurposes = new KeyStoreCredential(new ClassPathResource("dgc-signer.jks"), password, "dgc-signer-test", password);
    this.ecdsaAllPurposes.init();
    this.ecdsaOther = new KeyStoreCredential(new ClassPathResource("ecdsa.jks"), password, "signer", password);
    this.ecdsaOther.init();
  }

  // One vac-entry
  @Test
  public void test1() throws Exception {
    final DigitalCovidCertificate dgc = readDgcFile("dgc-simple-one-entry.json", DigitalCovidCertificate.class);
    final Instant issueTime = Instant.now();
    final Instant expiration = issueTime.plus(Duration.ofDays(90));
    final DGCSigner signer = new DefaultDGCSigner(this.ecdsaVacPurpose);

    final TestStatement test = createTestStatement(dgc, dgc.encode(), issueTime, expiration, signer, null, null, this.ecdsaVacPurpose
      .getCertificate());
    test.getTestCtx().setDescription("1: One vaccination entry - Everything should verify fine");

    // Before we write the test we want to make sure that we can handle it ...
    DGCTestDataVerifier.validate("Test #1", test);

    writeTestFile("1", test);
  }

  // Two vac-entries
  @Test
  public void test2() throws Exception {
    final DigitalCovidCertificate dgc = readDgcFile("dgc-simple-two-entries.json", DigitalCovidCertificate.class);
    final Instant issueTime = Instant.now();
    final Instant expiration = issueTime.plus(Duration.ofDays(90));
    final DGCSigner signer = new DefaultDGCSigner(this.ecdsaAllPurposes);

    final TestStatement test = createTestStatement(dgc, dgc.encode(), issueTime, expiration, signer, null, null, this.ecdsaAllPurposes
      .getCertificate());
    test.getTestCtx().setDescription("2: Two vaccination entries - Everything should verify fine");

    // Before we write the test we want to make sure that we can handle it ...
    DGCTestDataVerifier.validate("Test #2", test);

    writeTestFile("2", test);
  }

  // One vac-entry, signed using an RSA key
  @Test
  public void test3() throws Exception {
    final DigitalCovidCertificate dgc = readDgcFile("dgc-simple-one-entry.json", DigitalCovidCertificate.class);
    final Instant issueTime = Instant.now();
    final Instant expiration = issueTime.plus(Duration.ofDays(90));
    final DGCSigner signer = new DefaultDGCSigner(this.rsa);

    final TestStatement test = createTestStatement(dgc, dgc.encode(), issueTime, expiration, signer, null, null, this.rsa.getCertificate());
    test.getTestCtx().setDescription("3: One vaccination entry - RSA signature. Everything should verify fine");

    // Before we write the test we want to make sure that we can handle it ...
    DGCTestDataVerifier.validate("Test #3", test);

    writeTestFile("3", test);
  }

  // One vac-entry, no COSE message tag included
  @Test
  public void test4() throws Exception {
    final DigitalCovidCertificate dgc = readDgcFile("dgc-simple-one-entry.json", DigitalCovidCertificate.class);
    final Instant issueTime = Instant.now();
    final Instant expiration = issueTime.plus(Duration.ofDays(90));
    final DefaultDGCSigner signer = new DefaultDGCSigner(this.ecdsaAllPurposes);
    signer.setIncludeCoseTag(false);

    final TestStatement test = createTestStatement(dgc, dgc.encode(), issueTime, expiration, signer, null, null, this.ecdsaAllPurposes
      .getCertificate());
    test.getTestCtx().setDescription("4: One vaccination entry - No tag for COSE object. Everything should verify fine.");

    // Before we write the test we want to make sure that we can handle it ...
    DGCTestDataVerifier.validate("Test #4", test);

    writeTestFile("4", test);
  }

  // One vac-entry, Both CWT and COSE message tags included
  @Test
  public void test5() throws Exception {
    final DigitalCovidCertificate dgc = readDgcFile("dgc-simple-one-entry.json", DigitalCovidCertificate.class);
    final Instant issueTime = Instant.now();
    final Instant expiration = issueTime.plus(Duration.ofDays(90));
    final DefaultDGCSigner signer = new DefaultDGCSigner(this.ecdsaAllPurposes);
    signer.setIncludeCoseTag(true);
    signer.setIncludeCwtTag(true);

    final TestStatement test = createTestStatement(dgc, dgc.encode(), issueTime, expiration, signer, null, null, this.ecdsaAllPurposes
      .getCertificate());
    test.getTestCtx().setDescription("5: One vaccination entry - Both CWT and Cose_Sign1 tags present. Everything should verify fine.");

    // Before we write the test we want to make sure that we can handle it ...
    DGCTestDataVerifier.validate("Test #5", test);

    writeTestFile("5", test);
  }

  // One vac-entry, Signature validation will fail
  @Test
  public void test6() throws Exception {
    final DigitalCovidCertificate dgc = readDgcFile("dgc-simple-one-entry.json", DigitalCovidCertificate.class);
    final Instant issueTime = Instant.now();
    final Instant expiration = issueTime.plus(Duration.ofDays(90));
    final DefaultDGCSigner signer = new DefaultDGCSigner(this.ecdsaOther);

    final TestStatement test = createTestStatement(dgc, dgc.encode(), issueTime, expiration, signer, null, null, this.ecdsaVacPurpose
      .getCertificate());
    test.getTestCtx().setDescription("6: One vaccination entry - Signature validation should fail.");

    test.getExpectedResults().expectedVerify = false;

    // Before we write the test we want to make sure that we can handle it ...
    DGCTestDataVerifier.validate("Test #6", test);

    writeTestFile("6", test);
  }

  @Test
  public void test7_testEntry() throws Exception {
    
    final Instant issueTime = Instant.now();
    
    final DigitalCovidCertificate dgc = (DigitalCovidCertificate) new DigitalCovidCertificate()
      .withVer(DGCSchemaVersion.DGC_SCHEMA_VERSION)
      .withNam(new PersonName().withGn("Oscar").withFn("Lövström"))
      .withDob("1958-11-11")
      .withT(Arrays.asList(new TestEntry()
        .withTg("840539006")
        .withTt("LP6464-4")
        .withMa("1218")
        .withSc(issueTime.minus(Duration.ofHours(4).minus(Duration.ofMinutes(34))))
        .withTr("260415000")
        .withTc("Arlanda Airport Covid Center 1")
        .withCo("SE")
        .withIs("Swedish eHealth Agency")
        .withCi("URN:UVCI:01:SE:ARN/89875439877")));
    
    final Instant expiration = issueTime.plus(Duration.ofHours(72));
    final Instant validationClock = issueTime.plus(Duration.ofHours(27));
    final DefaultDGCSigner signer = new DefaultDGCSigner(this.ecdsaAllPurposes);

    final TestStatement test = createTestStatement(dgc, dgc.encode(), issueTime, expiration, signer, null, validationClock, 
      this.ecdsaAllPurposes.getCertificate());
    test.getTestCtx().setDescription("7: One test entry - Everything should verify fine.");

    // Before we write the test we want to make sure that we can handle it ...
    DGCTestDataVerifier.validate("Test #7", test);

    writeTestFile("7", test);
  }
  
  @Test
  public void test8_recoveryEntry() throws Exception {
    
    final Instant issueTime = Instant.now();
    final Instant expiration = issueTime.plus(Duration.ofDays(90));
    
    final DigitalCovidCertificate dgc = (DigitalCovidCertificate) new DigitalCovidCertificate()
        .withVer(DGCSchemaVersion.DGC_SCHEMA_VERSION)
        .withNam(new PersonName().withGn("Oscar").withFn("Lövström"))
        .withDob("1958-11-11")
        .withR(Arrays.asList(new RecoveryEntry()
          .withTg("840539006")
          .withFr(issueTime.minus(Duration.ofDays(11)).atZone(ZoneOffset.UTC).toLocalDate())
          .withCo("SE")
          .withIs("Swedish eHealth Agency")
          .withDf(issueTime.atZone(ZoneOffset.UTC).toLocalDate())
          .withDu(expiration.atZone(ZoneOffset.UTC).toLocalDate())
          .withCi("URN:UVCI:01:SE:EHN/189005439PP7")));
    
    final DefaultDGCSigner signer = new DefaultDGCSigner(this.ecdsaAllPurposes);

    final TestStatement test = createTestStatement(dgc, dgc.encode(), issueTime, expiration, signer, null, issueTime.plus(Duration.ofDays(5)), 
      this.ecdsaAllPurposes.getCertificate());
    test.getTestCtx().setDescription("8: One recovery entry - Everything should verify fine.");

    // Before we write the test we want to make sure that we can handle it ...
    DGCTestDataVerifier.validate("Test #8", test);

    writeTestFile("8", test);
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
      test.setCompressed(compressed);
      base45 = Base45.getEncoder().encodeToString(compressed);
      test.setBase45(base45);
    }
    else {
      // This tests an error case, so set the compressed to the uncompressed cose
      test.setCompressed(cose);

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
    final File file = new File(TARGET_DIR + "/" + testFile + ".json");
    System.out.println("Writing testfile " + file.getAbsolutePath());
    try (FileOutputStream fos = new FileOutputStream(file, false)) {
      fos.write(testStatement.toJson().getBytes(StandardCharset.UTF_8));
      fos.flush();
    }
    try (FileOutputStream fos = new FileOutputStream(new File(TARGET_DIR + "/" + testFile + ".png"), false)) {
      fos.write(Base64.getDecoder().decode(testStatement.getBarCode()));
      fos.flush();
    }

  }

  private static <T> T readDgcFile(final String file, final Class<T> clazz) throws IOException {
    return DigitalCovidCertificate.getJSONMapper().readValue(new File(SRC_DIR + "/" + file), clazz);
  }

}
