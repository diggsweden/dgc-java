package se.digg.dgc.signatures.impl;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

/**
 * A simple class, holding a X509Certificate and it's keys
 *
 * @author Johannes Marchart, johannes.marchart@itsv.at
 */
public class PkiCredential {

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private X509Certificate certificate;

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(X509Certificate certificate) {
        this.certificate = certificate;
    }

}
