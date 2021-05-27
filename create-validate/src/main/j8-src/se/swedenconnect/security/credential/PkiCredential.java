/*
 * Copyright 2020-2021 Sweden Connect
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.swedenconnect.security.credential;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

/**
 * A representation of a PKI key pair that holds a private key and a X.509 certificate (or just a public key).
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Stefan Santesson (stefan@idsec.se)
 */
public interface PkiCredential {

  /**
   * Gets the public key.
   * 
   * @return the public key
   */
  PublicKey getPublicKey();

  /**
   * Gets the certificate holding the public key of the key pair. May be null depending on whether certificates are
   * handled by the implementing class.
   * 
   * @return the certificate, or null if no certificate is configured for the credential
   */
  X509Certificate getCertificate();

  /**
   * Gets the private key.
   * 
   * @return the private key
   */
  PrivateKey getPrivateKey();

  /**
   * Gets the name of the credential.
   * 
   * @return the name
   */
  String getName();
  
  /**
   * Initializes the credential.
   * 
   * @throws Exception
   *           for init errors
   */
  void init() throws Exception;
  
}
