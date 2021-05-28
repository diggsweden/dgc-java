/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.interop;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipException;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upokecenter.cbor.CBORDateConverter;
import com.upokecenter.cbor.CBORObject;

import se.digg.dgc.encoding.Barcode;
import se.digg.dgc.encoding.BarcodeDecoder;
import se.digg.dgc.encoding.BarcodeException;
import se.digg.dgc.encoding.Base45;
import se.digg.dgc.encoding.DGCConstants;
import se.digg.dgc.encoding.Zlib;
import se.digg.dgc.encoding.impl.DefaultBarcodeDecoder;
import se.digg.dgc.interop.DGCPayloadValidator.Report;
import se.digg.dgc.payload.v1.DigitalGreenCertificate;
import se.digg.dgc.signatures.CertificateProvider;
import se.digg.dgc.signatures.DGCSignatureVerifier;
import se.digg.dgc.signatures.cose.CoseSign1_Object;
import se.digg.dgc.signatures.cwt.Cwt;
import se.digg.dgc.signatures.impl.DefaultDGCSignatureVerifier;

/**
 * Verifier support for testdata collected from <a href=
 * "https://github.com/eu-digital-green-certificates/dgc-testdata">https://github.com/eu-digital-green-certificates/dgc-testdata</a>.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class DGCTestDataVerifier {

  private static ObjectMapper jsonMapper = DigitalGreenCertificate.getJSONMapper();

  static {
    jsonMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    jsonMapper.setSerializationInclusion(Include.USE_DEFAULTS);
    jsonMapper.setSerializationInclusion(Include.NON_NULL);
  }

  /** Logger */
  private static final Logger log = LoggerFactory.getLogger(DGCTestDataVerifier.class);

  /**
   * Validates the data from the test statement.
   * 
   * @param testName
   *          the name of the test
   * @param test
   *          the test statement
   * @throws Exception
   *           for assertion errors
   */
  public static void validate(final String testName, final TestStatement test) throws Exception {

    // Step 0: Some countries supply the CBOR encoding of the entire CWT instead of just the CBOR encoding of the
    // payload.
    // Cover up for that ...
    //
    if (test.getCborBytes() != null) {
      try {
        CBORObject _cbor = CBORObject.DecodeFromBytes(test.getCborBytes());
        CBORObject hcert = _cbor.get(Cwt.HCERT_CLAIM_KEY);
        if (hcert != null && hcert.get(Cwt.EU_DGC_V1_MESSAGE_TAG) != null) {
          test.setCbor(hcert.get(Cwt.EU_DGC_V1_MESSAGE_TAG).EncodeToBytes());
          log.warn("[{}]: Bad testdata CBOR field contains CBOR encoding of CWT - not just the payload", testName);
        }
      }
      catch (Exception e) {
      }
    }

    // 1. Load the picture and extract the prefixed BASE45content.
    //
    if (test.getExpectedResults().expectedPictureDecode != null) {
      validateQrCode(testName, test.getExpectedResults().expectedPictureDecode, test.getBarCode(), test.getPrefix());
    }

    // 2. Load Prefix Object from RAW Content and remove the prefix. Validate against the BASE45 raw content.
    //
    if (test.getExpectedResults().expectedUnprefix != null) {
      validatePrefix(testName, test.getExpectedResults().expectedUnprefix, test.getPrefix(), test.getBase45());
    }
    
    if (test.getExpectedResults().expectedCompression != null) {
      validateDecompress(testName, test.getExpectedResults().expectedCompression, test.getCompressedBytes(), test.getCoseBytes());
    }

    // 3. Decode the BASE45 RAW Content and validate the COSE content against the RAW content.
    //
    if (test.getExpectedResults().expectedBase45Decode != null) {
      validateBase45DecodeAndDecompress(testName, test.getExpectedResults().expectedBase45Decode,
        test.getBase45(), test.getCoseBytes(), test.getCompressedBytes());
    }

    // 4. Check the EXP Field for expiring against the VALIDATIONCLOCK time.
    // 5. Verify the signature of the COSE Object against the JWK Public Key.
    //
    if (test.getExpectedResults().expectedExpirationCheck != null
        || test.getExpectedResults().expectedVerify != null) {
      validateExpirationAndSignature(testName, test.getExpectedResults().expectedExpirationCheck,
        test.getExpectedResults().expectedVerify, test.getCoseBytes(),
        test.getTestCtx().getValidationClockInstant(), test.getTestCtx().getCertificateObject());
    }

    // 6. Extract the CBOR content and validate the CBOR content against the RAW CBOR content field.
    //
    if (test.getExpectedResults().expectedDecode != null) {
      validateCoseDecode(testName, test.getExpectedResults().expectedDecode, test.getCoseBytes(), test.getCborBytes());
    }

    // 7. Transform CBOR into JSON and validate against the RAW JSON content.
    //
    if (test.getExpectedResults().expectedValidJson != null) {
      validateCborDecode(testName, test.getExpectedResults().expectedValidJson, test.getCborBytes(), test.getJsonString());
    }

    // 8. Validate the extracted JSON against the schema defined in the test context.
    //

    // Own test. Validate the CBOR data
    if (test.getCborBytes() != null) {
      List<DGCPayloadValidator.Report> report = DGCPayloadValidator.validateDgcPayload(test.getCborBytes());
      if (!report.isEmpty()) {
        final boolean errors = report.stream().filter(r -> r.getType() == Report.Type.ERROR).findAny().isPresent();
        if (errors) {
          Assert.fail(String.format("[%s]: %s", testName, report));
        }
//        else {
//          System.out.println(testName + ": " + report);
//        }
      }
    }

  }

  /**
   * Validates test number 1: Load the picture and extract the prefixed BASE45content.
   * 
   * @param testName
   *          name of test
   * @param expectedResult
   *          true/false
   * @param qrCode
   *          the QR-code image
   * @param prefixedContents
   *          the expected contents
   * @throws Exception
   *           for test errors
   */
  public static void validateQrCode(final String testName, final boolean expectedResult,
      final String qrCode, final String prefixedContents) {
    Assert.assertNotNull(String.format("[%s]: Can not execute Barcode decode test - Missing 2DCode", testName), qrCode);
    Assert.assertNotNull(String.format("[%s]: Can not execute Barcode decode test - Missing PREFIX", testName), prefixedContents);

    try {
      final BarcodeDecoder decoder = new DefaultBarcodeDecoder();
      final String payload = decoder.decodeToString(Base64.getDecoder().decode(qrCode),
        Barcode.BarcodeType.QR, StandardCharsets.US_ASCII);

      if (expectedResult) {
        Assert.assertEquals(String.format("[%s]: Barcode decode test failed - Decoded data does not match expected", testName),
          prefixedContents, payload);
      }
      else {
        Assert.assertNotEquals(String.format("[%s]: Barcode decode test failed - Decoded data match, but expected error", testName),
          prefixedContents, payload);
      }
    }
    catch (BarcodeException e) {
      if (expectedResult) {
        Assert.fail(String.format("[%s]: Barcode decode test failed - %s", testName, e.getMessage()));
      }
    }
  }

  /**
   * Validates test number 2: Load Prefix Object from RAW Content and remove the prefix. Validate against the BASE45 raw
   * content.
   * 
   * @param testName
   *          name of test
   * @param expectedResult
   *          true/false
   * @param prefixedContents
   *          prefixed contents of barcode image
   * @param base45
   *          resulting Base45-encoding
   */
  public static void validatePrefix(final String testName, final boolean expectedResult,
      final String prefixedContents, final String base45) {
    Assert.assertNotNull(String.format("[%s]: Can not execute Prefix test - Missing PREFIX", testName), prefixedContents);
    Assert.assertNotNull(String.format("[%s]: Can not execute Prefix test - Missing BASE45", testName), base45);

    if (expectedResult) {
      Assert.assertTrue(
        String.format("[%s]: Prefix test failed - Data does not start with '%s'", testName, DGCConstants.DGC_V1_HEADER),
        prefixedContents.startsWith(DGCConstants.DGC_V1_HEADER));
    }
    final String unprefixed = prefixedContents.substring(DGCConstants.DGC_V1_HEADER.length());
    if (expectedResult) {
      Assert.assertEquals(
        String.format("[%s]: Prefix test failed - Data does not match after removing prefix", testName),
        base45, unprefixed);
    }
    else {
      Assert.assertNotEquals(
        String.format("[%s]: Prefix test failed - Data do match after removing prefix, but expected error", testName),
        base45, unprefixed);
    }
  }

  /**
   * Validates that decompression works.
   * 
   * @param testName
   *          name of test
   * @param expectedResult
   *          true/false
   * @param compressed
   *          compressed CWT
   * @param cose
   *          the signed CWT (COSE)
   */
  public static void validateDecompress(final String testName, final boolean expectedResult,
      final byte[] compressed, final byte[] cose) {
    Assert.assertNotNull(String.format("[%s]: Can not execute decompress test - Missing COMPRESSED", testName), compressed);
    Assert.assertNotNull(String.format("[%s]: Can not execute decompress test - Missing COSE", testName), cose);

    try {
      final byte[] decompressed = Zlib.decompress(compressed, true);

      Assert.assertArrayEquals(String.format("[%s]: Decompress test failed", testName),
        cose, decompressed);

      if (!expectedResult) {
        Assert.fail(String.format("[%s]: Decompress test failed - match but expected failure", testName));
      }
    }
    catch (IllegalArgumentException | ZipException e) {
      if (expectedResult) {
        Assert.fail(String.format("[%s]: Decompress test failed - %s", testName, e.getMessage()));
      }
    }
  }

  /**
   * Validates test number 3: Decode the BASE45 RAW Content and validate the COSE content against the RAW content.
   * TODO: will change
   * 
   * @param testName
   *          name of test
   * @param expectedResult
   *          true/false
   * @param base45
   *          the Base45 data
   * @param cose
   *          the COSE bytes
   */
  public static void validateBase45DecodeAndDecompress(final String testName, final boolean expectedResult,
      final String base45, final byte[] cose, final byte[] compressed) {
    Assert.assertNotNull(String.format("[%s]: Can not execute Base45 decode test - Missing BASE45", testName), base45);
    if (cose == null && compressed == null) {
      Assert.fail(String.format("[%s]: Can not execute Base45 decode test - Missing both COSE and COMPRESSED", testName));
    }    

    try {
      final byte[] decoded = Base45.getDecoder().decode(base45);
      
      if (compressed != null) {
        Assert.assertArrayEquals(String.format("[%s]: Base45 decode and test failed", testName),
          compressed, decoded);
      }
      else if (cose != null) {      
        final byte[] decompressed = Zlib.decompress(decoded, true);

        Assert.assertArrayEquals(String.format("[%s]: Base45 decode and decompress test failed", testName),
          cose, decompressed);
      }

      if (!expectedResult) {
        Assert.fail(String.format("[%s]: Base45 decode and decompress test failed - match but expected failure", testName));
      }
    }
    catch (IllegalArgumentException | ZipException e) {
      if (expectedResult) {
        Assert.fail(String.format("[%s]: Base45 decode test failed - %s", testName, e.getMessage()));
      }
    }
  }

  /**
   * Validates test number 4: Check the EXP Field for expiring against the VALIDATIONCLOCK time and test number 5:
   * Verify the signature of the COSE Object against the JWK Public Key.
   * 
   * @param testName
   *          test name
   * @param expectedExpirationCheck
   *          should expiration work?
   * @param expectedSignatureCheck
   *          should signature validation work?
   * @param cose
   *          Cose_Sign1 object
   * @param validationClock
   *          simulated validation time
   * @param signerCertificate
   *          signer's certificate
   */
  public static void validateExpirationAndSignature(final String testName, final Boolean expectedExpirationCheck,
      final Boolean expectedSignatureCheck, final byte[] cose, final Instant validationClock, final X509Certificate signerCertificate) {

    Assert.assertNotNull(String.format("[%s]: Can not execute COSE verify test - Missing COSE", testName), cose);

    // We can't test the combination failed signature check, but passed expiration check using the
    // DefaultDGCSignatureVerifier. In those cases we must decode the COSE

    if (expectedSignatureCheck != null) {
      final DefaultDGCSignatureVerifier verifier = new DefaultDGCSignatureVerifier();
      verifier.setTestValidationTime(validationClock);

      final CertificateProvider certProvider = (c, k) -> signerCertificate != null ? Arrays.asList(signerCertificate)
          : Collections.emptyList();

      try {
        final DGCSignatureVerifier.Result vResult = verifier.verify(cose, certProvider);
        
        if (!expectedSignatureCheck.booleanValue()) {
          Assert.fail(String.format("[%s]: Signature validation was successful but expected failure", testName));
        }
        if (expectedExpirationCheck != null && !expectedExpirationCheck.booleanValue()) {
          Assert.fail(String.format("[%s]: Expiration check was successful but expected failure", testName));
        }
        if (expectedExpirationCheck != null && vResult.getExpires() == null) {
          Assert.fail(String.format("[%s]: Expiration check failed - Missing expiration field in CWT", testName));
        }
        
        // Check kid
        final byte[] kid = vResult.getKid();
        if (kid != null) {
          final byte[] expectedKid = calculateKid(vResult.getSignerCertificate());
          Assert.assertArrayEquals(String.format("[%s]: KID does not correspond with certificate", testName),
            expectedKid, kid);
        }
      }
      catch (CertificateExpiredException e) {
        if (expectedExpirationCheck != null && expectedExpirationCheck.booleanValue()) {
          Assert.fail(String.format("[%s]: Expiration check failed", testName));
        }
      }
      catch (SignatureException e) {
        if (expectedSignatureCheck.booleanValue()) {
          Assert.fail(String.format("[%s]: Signature validation check failed - %s", testName, e.getMessage()));
        }
      }
    }
    else if (expectedExpirationCheck != null && validationClock != null) {
      // No, signature check made. Fix this manually
      final CoseSign1_Object coseObject = CoseSign1_Object.decode(cose);
      final Cwt cwt = coseObject.getCwt();

      final Instant expiration = cwt.getExpiration();
      if (expiration == null) {
        Assert.fail(String.format("[%s]: Expiration check failed - Missing expiration field in CWT", testName));
      }
      else {
        if (validationClock.isAfter(expiration)) {
          if (expectedExpirationCheck.booleanValue()) {
            Assert.fail(String.format("[%s]: Expiration check failed - Object has expired", testName));
          }
        }
        else {
          if (!expectedExpirationCheck.booleanValue()) {
            Assert.fail(String.format("[%s]: Expiration check failed - Expected error, but check passed", testName));
          }
        }
      }
    }
  }

  /**
   * Validates test number 6: Extract the CBOR content and validate the CBOR content against the RAW CBOR content field.
   * 
   * @param testName
   *          test name
   * @param expected
   *          true/false
   * @param cose
   *          the COSE encoding
   * @param cbor
   *          the payload CBOR
   */
  public static void validateCoseDecode(final String testName, final boolean expected,
      final byte[] cose, final byte[] cbor) {
    Assert.assertNotNull(String.format("[%s]: Can not execute COSE decode test - Missing COSE", testName), cose);
    Assert.assertNotNull(String.format("[%s]: Can not execute COSE decode test - Missing CBOR", testName), cbor);

    final byte[] payload = CoseSign1_Object.decode(cose).getCwt().getDgcV1();

    // First compare arrays directly ...
    if (expected) {
      if (Arrays.equals(cbor, payload)) {
        // OK, exact encoding
        return;
      }

      // Otherwise, use the same CBOR decoder/encoder to make sure the data is the same.
      final byte[] normalizedPayload = CBORObject.DecodeFromBytes(payload).EncodeToBytes();
      final byte[] normalizedCbor = CBORObject.DecodeFromBytes(cbor).EncodeToBytes();

      Assert.assertArrayEquals(String.format("[%s]: Failed COSE decode test", testName),
        normalizedCbor, normalizedPayload);
    }
    else {
      if (Arrays.equals(cbor, payload)) {
        Assert.fail(String.format("[%s]: Failed COSE decode test - Encodings match but expected failure", testName));
      }
    }
  }

  /**
   * Validates test number 7: Transform CBOR into JSON and validate against the RAW JSON content.
   * 
   * @param testName
   *          test name
   * @param expected
   *          true/false
   * @param cbor
   *          CBOR encoding
   * @param json
   *          JSON string
   */
  public static void validateCborDecode(final String testName, final boolean expected, final byte[] cbor, final String json) {
    Assert.assertNotNull(String.format("[%s]: Can not execute CBOR decode test - Missing CBOR", testName), cbor);
    Assert.assertNotNull(String.format("[%s]: Can not execute CBOR decode test - Missing JSON", testName), json);

    // Normalize
    //

    // Since the CBOR encoding may contain timestamps encoded as ints, we see if need to
    // take care of that as part of the normalization process.
    final String cborNormalizedJson;
    {
      final CBORObject cborObj = CBORObject.DecodeFromBytes(cbor);
      if (cborObj.get("t") != null) {
        final CBORObject tArr = cborObj.get("t");
        for (int i = 0; i < tArr.size(); i++) {
          final CBORObject tObj = tArr.get(i);
          final CBORObject scObj = tObj.get("sc");
          if (scObj != null) {
            if (scObj.HasMostOuterTag(1)) {
              final Date date = CBORDateConverter.TaggedString.FromCBORObject(scObj);
              tObj.set("sc", CBORDateConverter.TaggedString.ToCBORObject(date));
            }
            else if (scObj.isNumber()) {
              final Date date = CBORDateConverter.UntaggedNumber.FromCBORObject(scObj);
              tObj.set("sc", CBORDateConverter.TaggedString.ToCBORObject(date));
            }
          }
          final CBORObject drObj = tObj.get("dr");
          if (drObj != null) {
            if (drObj.HasMostOuterTag(1)) {
              final Date date = CBORDateConverter.TaggedString.FromCBORObject(drObj);
              tObj.set("dr", CBORDateConverter.TaggedString.ToCBORObject(date));
            }
            else if (drObj.isNumber()) {
              final Date date = CBORDateConverter.UntaggedNumber.FromCBORObject(drObj);
              tObj.set("dr", CBORDateConverter.TaggedString.ToCBORObject(date));
            }
          }
        }
      }
      cborNormalizedJson = cborObj.ToJSONString();
    }

    final String jsonNormalized = CBORObject.FromJSONString(json).ToJSONString();

    // This may still fail, since the CBOR may use ints for timestamps ...

    if (expected) {
      Assert.assertEquals(String.format("[%s]: CBOR decode test failed", testName),
        jsonNormalized, cborNormalizedJson);
    }
    else {
      Assert.assertNotEquals(String.format("[%s]: CBOR decode test failed - Match but expected error", testName),
        jsonNormalized, cborNormalizedJson);
    }
  }
  
  /**
   * Given a certificate a 8-byte KID is calculated that is the SHA-256 digest over the certificate DER-encoding.
   * 
   * @param cert
   *          the certificate
   * @return a 8 byte KID
   */  
  private static byte[] calculateKid(final X509Certificate cert) {

    try {
      final MessageDigest digest = MessageDigest.getInstance("SHA-256");

      final byte[] sha256 = digest.digest(cert.getEncoded());
      final byte[] kid = new byte[8];
      System.arraycopy(sha256, 0, kid, 0, 8);
      return kid;
    }
    catch (NoSuchAlgorithmException | CertificateEncodingException e) {
      throw new SecurityException(e);
    }
  }

  /**
   * Reads a test file.
   * 
   * @param file
   *          the test file
   * @return a TestStatement
   * @throws IOException
   *           for parsing errors
   */
  public static TestStatement getTestStatement(final String file) throws IOException {
    return jsonMapper.readValue(new File(file), TestStatement.class);
  }

  // Hidden
  private DGCTestDataVerifier() {
  }

}
