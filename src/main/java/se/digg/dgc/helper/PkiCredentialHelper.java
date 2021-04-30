package se.digg.dgc.helper;

import com.nimbusds.jose.util.X509CertUtils;
import org.bouncycastle.asn1.x500.style.RFC4519Style;
import org.bouncycastle.jce.PrincipalUtil;
import se.digg.dgc.signatures.impl.PkiCredential;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

/**
 * A simple helper class for loading X509Certificates
 *
 * @author Johannes Marchart, johannes.marchart@itsv.at
 */
public class PkiCredentialHelper {

    /**
     * @param jksFileName the path to the JKS keystore
     * @param jksPassword the password for the JKS
     * @param countryCode     the two letter ISO-3166 country code
     * @throws KeyStoreException
     */
    public static PkiCredential getFromJKS(String jksFileName, String jksPassword, String countryCode) throws KeyStoreException {
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(getResourceInputStream(jksFileName), jksPassword.toCharArray());
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                Certificate certificate = keyStore.getCertificate(alias);
                if (certificate instanceof X509Certificate) {
                    String c = (String) PrincipalUtil.getSubjectX509Principal((X509Certificate) certificate).getValues(RFC4519Style.c).get(0);
                    if (c.equalsIgnoreCase(countryCode)) {
                        PkiCredential pkiCredential = new PkiCredential();
                        pkiCredential.setCertificate((X509Certificate) certificate);
                        pkiCredential.setPrivateKey((PrivateKey) keyStore.getKey(alias, jksPassword.toCharArray()));
                        pkiCredential.setPublicKey(certificate.getPublicKey());
                        return pkiCredential;
                    }
                }
            }
        } catch (Exception e) {
            throw new KeyStoreException(e.getMessage(), e);
        }
        throw new KeyStoreException("No such certificate found in " + jksFileName + " with country " + countryCode);
    }

    /**
     * @param pemFilenName the path to the PEM encoded certificate
     * @return
     * @throws Exception
     */
    public static X509Certificate getFromPEM(String pemFilenName) throws Exception {
        String pem = new String(getResourceInputStream(pemFilenName).readAllBytes());
        return X509CertUtils.parse(pem);
    }

    public static InputStream getResourceInputStream(String path) {
        return PkiCredentialHelper.class.getClassLoader().getResourceAsStream(path);
    }


}
