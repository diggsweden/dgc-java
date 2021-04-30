/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.cose;

import com.upokecenter.cbor.CBORObject;
import org.junit.Assert;
import org.junit.Test;
import se.digg.dgc.helper.PkiCredentialHelper;
import se.digg.dgc.signatures.cose.CoseSign1_Object;
import se.digg.dgc.signatures.cose.HeaderParameterKey;
import se.digg.dgc.signatures.cose.SignatureAlgorithm;
import se.digg.dgc.signatures.cwt.Cwt;
import se.digg.dgc.signatures.impl.PkiCredential;

import java.security.MessageDigest;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.time.Instant;

/**
 * Test cases for CoseSign1_Object.
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class CoseSign1_ObjectTest {

    private PkiCredential rsa;
    private PkiCredential ecdsa;

    private static final String password = "secret";

    public CoseSign1_ObjectTest() throws Exception {
        this.rsa = PkiCredentialHelper.getFromJKS("rsa.jks", password, "se");
        this.ecdsa = PkiCredentialHelper.getFromJKS("ecdsa.jks", password, "se");
    }

    @Test
    public void testSignVerifyEc() throws Exception {
        final Cwt cwt = Cwt.builder().issuer("SE").issuedAt(Instant.now()).build();
        final CoseSign1_Object sign = CoseSign1_Object.builder()
                .protectedAttribute(HeaderParameterKey.ALG.getCborObject(), SignatureAlgorithm.ES256.getCborObject())
                .protectedAttribute(HeaderParameterKey.KID.getCborObject(), CBORObject.FromObject(getKeyId(this.ecdsa.getCertificate())))
                .content(cwt.encode())
                .build();

        sign.sign(this.ecdsa.getPrivateKey(), null);

        final byte[] encoding = sign.encode();

        CoseSign1_Object object2 = CoseSign1_Object.decode(encoding);

        // In a real life scenario, this is where I would ask for the KID (in order to locate the public key to use).
        final byte[] kid = object2.getKeyIdentifier();
        Assert.assertTrue(kid.length == 8);

        object2.verifySignature(this.ecdsa.getPublicKey());

        final Cwt cwt2 = object2.getCwt();
        Assert.assertEquals("SE", cwt2.getIssuer());
    }

    @Test
    public void testSignVerifyRsa() throws Exception {

        final Cwt cwt = Cwt.builder().issuer("SE").issuedAt(Instant.now()).build();
        final CoseSign1_Object sign = CoseSign1_Object.builder()
                .protectedAttribute(HeaderParameterKey.ALG.getCborObject(), SignatureAlgorithm.PS256.getCborObject())
                .protectedAttribute(HeaderParameterKey.KID.getCborObject(), CBORObject.FromObject(getKeyId(this.rsa.getCertificate())))
                .content(cwt.encode())
                .build();

        sign.sign(this.rsa.getPrivateKey(), null);

        final byte[] encoding = sign.encode();

        CoseSign1_Object object2 = CoseSign1_Object.decode(encoding);
        object2.verifySignature(this.rsa.getPublicKey());

        final Cwt cwt2 = object2.getCwt();
        Assert.assertEquals("SE", cwt2.getIssuer());
    }

    @Test(expected = SignatureException.class)
    public void testFailedVerify() throws Exception {
        final Cwt cwt = Cwt.builder().issuer("SE").issuedAt(Instant.now()).build();
        final CoseSign1_Object sign = CoseSign1_Object.builder()
                .protectedAttribute(HeaderParameterKey.ALG.getCborObject(), SignatureAlgorithm.ES256.getCborObject())
                .protectedAttribute(HeaderParameterKey.KID.getCborObject(), CBORObject.FromObject(getKeyId(this.ecdsa.getCertificate())))
                .content(cwt.encode())
                .build();

        sign.sign(this.ecdsa.getPrivateKey(), null);

        final byte[] encoding = sign.encode();

        // Change the signature
        encoding[encoding.length - 1] = 0x00;

        CoseSign1_Object object2 = CoseSign1_Object.decode(encoding);
        object2.verifySignature(this.ecdsa.getPublicKey());
    }

    private static byte[] getKeyId(final X509Certificate cert) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        final byte[] sha256 = digest.digest(cert.getEncoded());
        final byte[] kid = new byte[8];
        System.arraycopy(sha256, 0, kid, 0, 8);
        return kid;
    }

}
