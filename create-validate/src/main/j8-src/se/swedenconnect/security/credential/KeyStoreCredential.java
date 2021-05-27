package se.swedenconnect.security.credential;

import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import se.swedenconnect.security.credential.PkiCredential;

/**
 * Simple copy of the KeyStoreCredential in credentials-support. For Java 8.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Stefan Santesson (stefan@idsec.se)
 */
public class KeyStoreCredential implements PkiCredential {

  /** The resource holding the keystore. */
  private Resource resource;

  /** The password needed to unlock the KeyStore. */
  private char[] password;

  /** The alias to the entry holding the key pair. */
  private String alias;

  /** the password to unlock the private key. */
  private char[] keyPassword;

  /** Whether the credential has been loaded? */
  private boolean loaded = false;

  /** The private key. */
  private PrivateKey privateKey;

  /** The certificate. */
  private X509Certificate certificate;

  public KeyStoreCredential(final Resource resource, final char[] password, final String alias, final char[] keyPassword) {
    this.resource = resource;
    Optional.ofNullable(password).map(p -> Arrays.copyOf(p, p.length)).orElse(null);
    this.alias = alias;
    this.keyPassword = Optional.ofNullable(keyPassword).map(p -> Arrays.copyOf(p, p.length)).orElse(null);
  }

  /** {@inheritDoc} */
  @Override
  public void init() throws Exception {
    this.load();
  }

  /**
   * Loads the KeyStore (if needed) and loads the private key and certificate.
   * 
   * @throws Exception
   *           for errors loading the credential
   */
  private synchronized void load() throws Exception {
    if (this.loaded) {
      return;
    }

    this.loaded = true;

    KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());

    Assert.notNull(this.resource, "Property 'resource' must be set");
    try (InputStream is = this.resource.getInputStream()) {
      keystore.load(is, this.password);
    }

    // Load the private key and certificate ...
    //
    Assert.hasText(this.alias, "Property 'alias' must be set");
    if (this.keyPassword == null) {
      if (this.password != null) {
        this.keyPassword = this.password;
      }
      else {
        throw new IllegalArgumentException("No key password assigned");
      }
    }
    final Key key = keystore.getKey(this.alias, this.keyPassword);
    if (PrivateKey.class.isInstance(key)) {
      this.privateKey = PrivateKey.class.cast(key);
    }
    else {
      throw new KeyStoreException("No private key found at entry " + this.alias);
    }

    this.certificate = (X509Certificate) keystore.getCertificate(this.alias);
    if (this.certificate == null) {
      throw new CertificateException("No certificate found at entry " + this.alias);
    }
  }

  /** {@inheritDoc} */
  @Override
  public PublicKey getPublicKey() {
    final X509Certificate cert = this.getCertificate();
    return cert != null ? cert.getPublicKey() : null;
  }

  /** {@inheritDoc} */
  @Override
  public synchronized X509Certificate getCertificate() {
    if (!this.loaded) {
      try {
        this.load();
      }
      catch (Exception e) {
        throw new SecurityException("Failed to load KeyStoreCredential - " + e.getMessage(), e);
      }
    }
    return this.certificate;
  }

  /** {@inheritDoc} */
  @Override
  public synchronized PrivateKey getPrivateKey() {
    if (!this.loaded) {
      try {
        this.load();
      }
      catch (Exception e) {
        throw new SecurityException("Failed to load KeyStoreCredential - " + e.getMessage(), e);
      }
    }
    return this.privateKey;
  }

  @Override
  public String getName() {
    return this.alias; 
  }

}
