![Logo](https://docs.swedenconnect.se/technical-framework/latest/img/digg_centered.png)

# dgc-java

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/se.digg.dgc/dgc-create-validate/badge.svg)](https://maven-badges.herokuapp.com/maven-central/se.digg.dgc/dgc-create-validate) 

An implementation in Java for creating and validating a EU Digital Covid Certificate.

---

## About

This repository contains the Java libraries **dgc-schema** and **dgc-create-validate** for creating and validating EU Digital Covid Certificates. It is maintained by the [Swedish Agency for Digital Government](https://www.digg.se/en).

**Note:** The commission has decided on a late name change where the Digital Green Certificate was renamed to EU Digital Covid Certificate. Therefore, you'll find a lot of code in this library where the abbreviation DGC is used instead of DCC.

## Resources

- [Electronic Health Certificate Specification](https://github.com/ehn-digital-green-development/hcert-spec).

- [EU Digital Covid Certificate Schema](https://github.com/ehn-digital-green-development/ehn-dgc-schema).

- [Test data](https://github.com/eu-digital-green-certificates/dgc-testdata).

- Misc. implementations can be found at <https://github.com/ehn-digital-green-development>.

## Structure

The code is structured into the following parts:

- API - Java POJO:s generated from the [schema](https://github.com/ehn-digital-green-development/ehn-dgc-schema). **dgc-schema**

- CBOR, CWT/COSE - Support for representing the DGC in CBOR and to sign/validate it. **dgc-create-validate**

- Compresssion/decompression. **dgc-create-validate**

- Base45 implementation. **dgc-create-validate**

- QR code generation - Support for creating/reading QR-codes. **dgc-create-validate**

- UVCI generation including Luhn mod N checksum calculation. **dgc-create-validate**

- Service layer - A service that ties all the above components together and presents high-level methods for creating and validating EU Digital Covid Certificates. **dgc-create-validate**

## Maven

Include the following in your POM:

```
<dependency>
  <groupId>se.digg.dgc</groupId>
  <artifactId>dgc-schema</artifactId>
  <version>${dgc-java.version}</version>
</dependency>

<dependency>
  <groupId>se.digg.dgc</groupId>
  <artifactId>dgc-create-validate</artifactId>
  <version>${dgc-java.version}</version>
</dependency>
```

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

Note: Only the Java 11 build is published to Maven central (see Java 8 note below).

## For Java 8 users

On request, the libraries can also be built with Java 8. However, they are not published to Maven central.

To build:

```
>mvn clean install -P\!default,j8-build
```

Now, the artifacts `dgc-schema-java8` and `dgc-create-validate-java8` will be built.

## Documentation

Java API documentation of the library: 

- **dgc-schema** - https://diggsweden.github.io/dgc-java/javadoc/dgc-schema/
- **dgc-create-validate** - https://diggsweden.github.io/dgc-java/javadoc/dgc-create-validate/

## Acknowledgements

* A special thank you to [Peter Occil](https://github.com/peteroupc) for his excellent work on the CBOR library for Java - https://github.com/peteroupc/CBOR-Java.

-----

Copyright &copy; 2021, [Myndigheten för digital förvaltning - Swedish Agency for Digital Government (DIGG)](http://www.digg.se). Licensed under the MIT license.

