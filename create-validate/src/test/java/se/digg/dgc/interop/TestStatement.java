/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.interop;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

import se.digg.dgc.payload.v1.DigitalGreenCertificate;

/**
 * A representation of a test statement, see
 * <a href="https://github.com/eu-digital-green-certificates/dgc-testdata/blob/main/README.md">DGC Test Data Repository
 * for Test Automation</a>
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestStatement {

  /** The JSON holding the DGC payload. */
  @JsonProperty("JSON")
  private Object json;

  /** The CBOR-encoding of the DGC payload (as a hex-encoded string). */
  @JsonProperty("CBOR")
  private String cbor;

  /** The signed DGC represented as a Cose_Sign1 object (as a hex-encoded string). */
  @JsonProperty("COSE")
  private String cose;

  /** Compressed CWT (as a hex-encoded string). */
  @JsonProperty("COMPRESSED")
  private String compressed;

  /** The compressed Cose_Sign1 object in its Base45-encoding. */
  @JsonProperty("BASE45")
  private String base45;

  /** The Base45 with the HC prefix. */
  @JsonProperty("PREFIX")
  private String prefix;

  /** The PNG 2D code (in base64). */
  @JsonProperty("2DCODE")
  private String barCode;

  /** The test context. */
  @JsonProperty("TESTCTX")
  private TestCtx testCtx;

  /** The expected results. */
  @JsonProperty("EXPECTEDRESULTS")
  private ExpectedResults expectedResults;

  /**
   * Default constructor.
   */
  public TestStatement() {
  }

  /**
   * Returns this object as a JSON string.
   * 
   * @return a JSON string
   * @throws JsonProcessingException
   *           for JSON processing errors
   */
  public String toJson() throws JsonProcessingException {
    return DigitalGreenCertificate.getJSONMapper().writeValueAsString(this);
  }

  /**
   * Gets the JSON holding the DGC payload.
   * 
   * @return the DGC payload as a JSON object
   */
  public Object getJson() {
    return this.json;
  }

  /**
   * Gets the JSON string representing the DGC payload.
   * 
   * @return a JSON string
   * @throws JsonProcessingException
   *           for JSON processing errors
   */
  @JsonIgnore
  public String getJsonString() throws JsonProcessingException {
    return this.json != null
        ? DigitalGreenCertificate.getJSONMapper().writeValueAsString(this.json)
        : null;
  }

  /**
   * Sets the JSON object holding the DGC payload.
   * 
   * @param json
   *          the DGC payload as a JSON object
   */
  public void setJson(final Object json) {
    this.json = json;
  }

  /**
   * Gets the CBOR-encoding of the DGC payload (as a hex-encoded string).
   * 
   * @return the CBOR encoding of the DGC payload
   */
  public String getCbor() {
    return this.cbor;
  }

  /**
   * Gets the CBOR-encoding of the DGC payload.
   * 
   * @return the CBOR encoding of the DGC payload
   */
  @JsonIgnore
  public byte[] getCborBytes() {
    try {
      return this.cbor != null
          ? Hex.decodeHex(this.cbor)
          : null;
    }
    catch (DecoderException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Sets the CBOR-encoding of the DGC payload (as a hex-encoded string).
   * 
   * @param cbor
   *          the CBOR encoding of the DGC payload
   */
  public void setCbor(final String cbor) {
    this.cbor = cbor;
  }

  /**
   * Sets the CBOR-encoding of the DGC payload.
   * 
   * @param cbor
   *          the CBOR encoding of the DGC payload
   */
  @JsonIgnore
  public void setCbor(final byte[] cbor) {
    this.cbor = Hex.encodeHexString(cbor);
  }

  /**
   * Gets the signed DGC represented as a Cose_Sign1 object (as a hex-encoded string).
   * 
   * @return the Cose_Sign1 object (in hex-encoding)
   */
  public String getCose() {
    return this.cose;
  }

  /**
   * Gets the signed DGC represented as a Cose_Sign1 object.
   * 
   * @return the Cose_Sign1 object
   */
  @JsonIgnore
  public byte[] getCoseBytes() {
    try {
      return this.cose != null
          ? Hex.decodeHex(this.cose)
          : null;
    }
    catch (DecoderException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Sets the signed DGC represented as a Cose_Sign1 object (as a hex-encoded string).
   * 
   * @param cose
   *          the Cose_Sign1 object (in hex-encoding)
   */
  public void setCose(final String cose) {
    this.cose = cose;
  }

  /**
   * Sets the signed DGC represented as a Cose_Sign1 object.
   * 
   * @param cose
   *          the Cose_Sign1 object
   */
  @JsonIgnore
  public void setCose(final byte[] cose) {
    this.cose = Hex.encodeHexString(cose);
  }

  /**
   * Gets the compressed CWT.
   * 
   * @return the compressed CWT
   */
  public String getCompressed() {
    return this.compressed;
  }

  /**
   * Gets the compressed CWT in byte representation.
   * 
   * @return the compressed CWT
   */
  @JsonIgnore
  public byte[] getCompressedBytes() {
    try {
      return this.compressed != null
          ? Hex.decodeHex(this.compressed)
          : null;
    }
    catch (DecoderException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Sets the compressed CWT.
   * 
   * @param compressed
   *          the compressed CWT
   */
  public void setCompressed(final String compressed) {
    this.compressed = compressed;
  }

  /**
   * Sets the compressed CWT.
   * 
   * @param compressed
   *          the compressed CWT
   */
  @JsonIgnore
  public void setCompressed(final byte[] compressed) {
    this.compressed = Hex.encodeHexString(compressed);
  }

  /**
   * Gets the compressed Cose_Sign1 object in its Base45-encoding.
   * 
   * @return the Base45-encoding
   */
  public String getBase45() {
    return this.base45;
  }

  /**
   * Sets the compressed Cose_Sign1 object in its Base45-encoding.
   * 
   * @param base45
   *          the Base45-encoding
   */
  public void setBase45(final String base45) {
    this.base45 = base45;
  }

  /**
   * Gets the Base45 with the HC prefix.
   * 
   * @return the full test data
   */
  public String getPrefix() {
    return this.prefix;
  }

  /**
   * Sets the Base45 with the HC prefix.
   * 
   * @param prefix
   *          the full test data
   */
  public void setPrefix(final String prefix) {
    this.prefix = prefix;
  }

  /**
   * Gets the PNG 2D code (in base64).
   * 
   * @return the qr-code image
   */
  public String getBarCode() {
    return this.barCode;
  }

  /**
   * Sets the PNG 2D code (in base64).
   * 
   * @param barCode
   *          the qr-code image
   */
  public void setBarCode(final String barCode) {
    this.barCode = barCode;
  }

  /**
   * Sets the PNG 2D code.
   * 
   * @param barCode
   *          the qr-code image
   */
  @JsonIgnore
  public void setBarCode(final byte[] barCode) {
    this.barCode = Base64.getEncoder().encodeToString(barCode);
  }

  /**
   * Gets the test context.
   * 
   * @return the test context
   */
  public TestCtx getTestCtx() {
    return this.testCtx;
  }

  /**
   * Sets the test context.
   * 
   * @param testCtx
   *          the test context
   */
  public void setTestCtx(final TestCtx testCtx) {
    this.testCtx = testCtx;
  }

  /**
   * Gets the expected results.
   * 
   * @return the expected results
   */
  public ExpectedResults getExpectedResults() {
    return this.expectedResults;
  }

  /**
   * Sets the expected results.
   * 
   * @param expectedResults
   *          the expected results
   */
  public void setExpectedResults(final ExpectedResults expectedResults) {
    this.expectedResults = expectedResults;
  }

  /**
   * Test context.
   */
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class TestCtx {

    /** The version (of what?). */
    @JsonProperty("VERSION")
    private int version;

    /** The DGC schema version. */
    @JsonProperty("SCHEMA")
    private String schema;

    /** The certificate used to sign (in Base64). */
    @JsonProperty("CERTIFICATE")
    private String certificate;

    /** The time to simulate when validating this entry. */
    @JsonProperty("VALIDATIONCLOCK")
    private String validationClock;

    /** The description of the test. */
    @JsonProperty("DESCRIPTION")
    private String description;

    public int getVersion() {
      return this.version;
    }

    public void setVersion(final int version) {
      this.version = version;
    }

    public String getSchema() {
      return this.schema;
    }

    public void setSchema(final String schema) {
      this.schema = schema;
    }

    public String getCertificate() {
      return this.certificate;
    }

    @JsonIgnore
    public X509Certificate getCertificateObject() throws CertificateException {
      if (this.certificate != null) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode(this.certificate))) {
          return (X509Certificate) CertificateFactory.getInstance("X.509")
            .generateCertificate(bis);
        }
        catch (IOException e) {
          throw new CertificateException(e);
        }
      }
      else {
        return null;
      }
    }

    public void setCertificate(final String certificate) {
      this.certificate = certificate;
    }

    @JsonIgnore
    public void setCertificate(final X509Certificate certificate) throws CertificateEncodingException {
      this.certificate = Base64.getEncoder().encodeToString(certificate.getEncoded());
    }

    public String getValidationClock() {
      return this.validationClock;
    }

    public Instant getValidationClockInstant() {
      if (this.validationClock == null) {
        return null;
      }
      // We are expected an Instant, but can accept a day also.
      try {
        return Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(this.validationClock));
      }
      catch (DateTimeParseException e) {
        try {
          LocalDate date = LocalDate.parse(this.validationClock);
          return date.atStartOfDay().toInstant(ZoneOffset.UTC);
        }
        catch (Exception e2) {
          throw e;
        }
      }
    }

    public void setValidationClock(final String validationClock) {
      this.validationClock = validationClock;
    }

    public void setValidationClock(final Instant validationClock) {
      this.validationClock = validationClock.toString();
    }

    public String getDescription() {
      return this.description;
    }

    public void setDescription(final String description) {
      this.description = description;
    }

  }

  /**
   * Expected results.
   */
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ExpectedResults {

    @JsonProperty("EXPECTEDSCHEMAVALIDATION")
    public Boolean expectedSchemaValidation;

    @JsonProperty("EXPECTEDDECODE")
    public Boolean expectedDecode;    
    
    @JsonProperty("EXPECTEDVERIFY")
    public Boolean expectedVerify;

    @JsonProperty("EXPECTEDUNPREFIX")
    public Boolean expectedUnprefix;

    @JsonProperty("EXPECTEDVALIDJSON")
    public Boolean expectedValidJson;
    
    @JsonProperty("EXPECTEDCOMPRESSION")
    public Boolean expectedCompression;

    @JsonProperty("EXPECTEDB45DECODE")
    public Boolean expectedBase45Decode;

    @JsonProperty("EXPECTEDPICTUREDECODE")
    public Boolean expectedPictureDecode;

    @JsonProperty("EXPECTEDEXPIRATIONCHECK")
    public Boolean expectedExpirationCheck;
    
    // EXPECTEDKEYUSAGE

    @JsonIgnore
    public void setAllPositive() {
      this.expectedValidJson = true;
      this.expectedSchemaValidation = true;
      this.expectedDecode = true;
      this.expectedVerify = true;
      this.expectedUnprefix = true;
      this.expectedValidJson = true;
      this.expectedCompression = true;
      this.expectedBase45Decode = true;
      this.expectedPictureDecode = true;
      this.expectedExpirationCheck = true;
    }
  }

}
