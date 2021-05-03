/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.service.interop;

import com.upokecenter.cbor.CBORObject;
import org.junit.Assert;
import org.junit.Test;
import se.digg.dgc.encoding.Base45;
import se.digg.dgc.encoding.Zlib;
import se.digg.dgc.encoding.impl.DefaultBarcodeDecoder;
import se.digg.dgc.signatures.DGCSignatureVerifier;
import se.digg.dgc.signatures.impl.DefaultDGCSignatureVerifier;

import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import static se.digg.dgc.helper.PkiCredentialHelper.getFromPEM;
import static se.digg.dgc.helper.PkiCredentialHelper.getResourceInputStream;

/**
 * Interoperability tests with other implementations
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class InteropConsumeTest {

  @Test
  public void testDecodeFromBarcode() throws Exception {
    final byte[] image = getResourceInputStream("interop/hcert-testdata/test1.png").readAllBytes();
    final X509Certificate cert = getFromPEM("interop/hcert-testdata/issuer1.crt");

    // Decode barcode into the Base45 string ...
    final DefaultBarcodeDecoder decoder = new DefaultBarcodeDecoder();
    final String base45 = decoder.decodeToString(image, StandardCharsets.US_ASCII);

    // Assert the the prefix is there.
    Assert.assertTrue(base45.startsWith("HC1"));

    // Base45 decode
    final byte[] compressedCwt = Base45.getDecoder().decode(base45.substring(3));

    // Decompress
    final byte[] cwt = Zlib.decompress(compressedCwt, true);

    // Verify signature
    final DefaultDGCSignatureVerifier verifier = new DefaultDGCSignatureVerifier();
    DGCSignatureVerifier.Result result = verifier.verify(cwt, (c, k) -> Arrays.asList(cert));

    System.out.println(String.format("CWT: issuing-country='%s', issued-at='%s', expires='%s',signer-cert='%s'",
      result.getCountry(), result.getIssuedAt(), result.getExpires(), result.getSignerCertificate().getSubjectX500Principal()));

    // Dump HCERT contents in JSON
    final CBORObject dgcObject = CBORObject.DecodeFromBytes(result.getDgcPayload());
    System.out.println(dgcObject.ToJSONString());

    // Deserialize into our Java representation ...
//    final ObjectMapper cborMapper = new CBORMapper();
//    final DigitalGreenCertificate dgc = cborMapper.readValue(result.getHcert(), DigitalGreenCertificate.class);
  }


}
