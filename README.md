![Logo](https://docs.swedenconnect.se/technical-framework/latest/img/digg_centered.png)

# hcert-impl

An implementation in Java for creating and validating a Digital Green Certificate.

---

## About

This repository contains code for creating and validating a Digital Green Certificate. 

## Resources

- [Electronic Health Certificate Specification](https://github.com/ehn-digital-green-development/hcert-spec).

- [Electronic Health Certificate Schema](https://github.com/ehn-digital-green-development/hcert-schema).

- [Test data](https://github.com/ehn-digital-green-development/hcert-testdata).

- Misc. implementations can be found at <https://github.com/ehn-digital-green-development>.

## Structure

The code is structured into the following parts:

- API - Java POJO:s generated from the [hcert-schema](https://github.com/ehn-digital-green-development/hcert-schema).

- CBOR, CWT/COSE - Support for representing the HCERT in CBOR and to sign/validate it.

- Compresssion/decompression

- Base45 implementation

- QR code generation - Support for creating/reading QR-codes.

- Service layer - A service that ties all the above components together and presents high-level methods for creating and validating HCERT:s.

-----

Copyright &copy; 2021, [Myndigheten för digital förvaltning (DIGG)](http://www.digg.se). Licensed under the MIT license.

