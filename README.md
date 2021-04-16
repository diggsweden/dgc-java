![Logo](https://docs.swedenconnect.se/technical-framework/latest/img/digg_centered.png)

# dgc-java

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

An implementation in Java for creating and validating a Digital Green Certificate.

---

## About

This repository contains the Java library **dgc-create-validate** for creating and validating Digital Green Certificates. It is maintained by the [Swedish Agency for Digital Government](https://www.digg.se/en).

## Resources

- [Electronic Health Certificate Specification](https://github.com/ehn-digital-green-development/hcert-spec).

- [Electronic Health Certificate Schema](https://github.com/ehn-digital-green-development/hcert-schema).

- [Test data](https://github.com/ehn-digital-green-development/hcert-testdata).

- Misc. implementations can be found at <https://github.com/ehn-digital-green-development>.

## Structure

The code is structured into the following parts:

- API - Java POJO:s generated from the [schema](https://github.com/ehn-digital-green-development/hcert-schema).

- CBOR, CWT/COSE - Support for representing the DGC in CBOR and to sign/validate it.

- Compresssion/decompression

- Base45 implementation

- QR code generation - Support for creating/reading QR-codes.

- Service layer - A service that ties all the above components together and presents high-level methods for creating and validating Digital Green Certificates.

## Maven

Include the following in your POM:

```
<dependency>
  <groupId>se.digg.dgc</groupId>
  <artifactId>dgc-create-validate</artifactId>
  <version>${dgc-java.version}</version>
</dependency>
```

> Note: The artifact will be published to Maven central once the DGC schema is stable.

The **dgc-create-validate** library offers a barcode (QR) implementation using the [ZXing](https://github.com/zxing/zxing) library, but the [BarcodeCreator](https://github.com/DIGGSweden/dgc-java/blob/main/src/main/java/se/digg/dgc/encoding/BarcodeCreator.java) and [BarcodeDecoder](https://github.com/DIGGSweden/dgc-java/blob/main/src/main/java/se/digg/dgc/encoding/BarcodeDecoder.java) interfaces make it possible to implement your own barcode support using the library of your own choice. Therefore the dependencies to the ZXing jars are marked as optional in the **dgc-create-validate** POM. If you want to use the default implementation you need to include the following in your POM:

```
<dependency>
  <groupId>com.google.zxing</groupId>
  <artifactId>core</artifactId>
  <version>3.4.1</version>
</dependency>
    
<dependency>
  <groupId>com.google.zxing</groupId>
  <artifactId>javase</artifactId>
  <version>3.4.1</version>
</dependency>
```

> At least version 3.4.1 of the ZXing libraries are required.

## Documentation

- Java API documentation of the library: https://diggsweden.github.io/dgc-java/javadoc/

## Acknowledgements

* A special thank you to [Peter Occil](https://github.com/peteroupc) for his excellent work on the CBOR library for Java - https://github.com/peteroupc/CBOR-Java.

-----

Copyright &copy; 2021, [Myndigheten för digital förvaltning - Swedish Agency for Digital Government (DIGG)](http://www.digg.se). Licensed under the MIT license.

