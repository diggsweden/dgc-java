/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.encoding.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.aztec.AztecWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitArray;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import se.digg.dgc.encoding.Barcode;
import se.digg.dgc.encoding.BarcodeCreator;
import se.digg.dgc.encoding.BarcodeException;

/**
 * A bean implementing the {@link BarcodeCreator} interface using the ZXing library.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class DefaultBarcodeCreator implements BarcodeCreator {

  /** The default barcode type for this implementation is QR. */
  public static final Barcode.BarcodeType DEFAULT_TYPE = Barcode.BarcodeType.QR;

  /** The default image format to use when creating barcodes. */
  public static final Barcode.ImageFormat DEFAULT_IMAGE_FORMAT = Barcode.ImageFormat.PNG;

  /** The default width and height to be used for the created barcodes. */
  public static final int DEFAULT_WIDTH_AND_HEIGHT = 300;

  /** The type of barcodes to create. */
  private Barcode.BarcodeType type = DEFAULT_TYPE;

  /** The image format to use when creating barcodes. */
  private Barcode.ImageFormat imageFormat = DEFAULT_IMAGE_FORMAT;

  /** The width and height (in pixels) to use when creating barcodes. */
  private int widthAndHeight = DEFAULT_WIDTH_AND_HEIGHT;

  /** {@inheritDoc} */
  @Override
  public Barcode create(final String contents) throws BarcodeException {
    return this.create(contents, null);
  }

  /** {@inheritDoc} */
  @Override
  public Barcode create(final String contents, final Charset characterSet) throws BarcodeException {
    if (contents == null || contents.trim().isEmpty()) {
      throw new IllegalArgumentException("contents is not set");
    }

    Writer writer = null;
    final Map<EncodeHintType, Object> hints = new HashMap<>();
    if (characterSet != null) {
      hints.put(EncodeHintType.CHARACTER_SET, characterSet.name());
    }

    if (this.type == Barcode.BarcodeType.QR) {
      writer = new QRCodeWriter();
      hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);
    }
    else { // AZTEC
      writer = new AztecWriter();
      hints.put(EncodeHintType.ERROR_CORRECTION, Integer.valueOf(23));
    }
    try {
      final BitMatrix bitMatrix = writer.encode(contents, this.zxingBarcodeFormat(), this.widthAndHeight, this.widthAndHeight, hints);

      try (final ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
        MatrixToImageWriter.writeToStream(bitMatrix, imageFormat.getName(), stream);
        byte[] bytes = stream.toByteArray();
        return new Barcode(this.type, bytes, this.imageFormat, this.toSvgImage(bitMatrix), 
          bitMatrix.getWidth(), bitMatrix.getHeight(), contents);
      }
      catch (final IOException e) {
        throw new BarcodeException("Failed to create barcode - " + e.getMessage(), e);
      }

    }
    catch (final WriterException e) {
      throw new BarcodeException("Failed to create barcode - " + e.getMessage(), e);
    }
  }

  /**
   * Transforms the supplied bit matrix into an SVG image.
   * 
   * @param image
   *          the bit matrix
   * @return SVG image
   */
  private String toSvgImage(final BitMatrix image) {
    final int width = image.getWidth();
    final int height = image.getHeight();
    StringBuilder sb = new StringBuilder();
    sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\"0 0 ")
      .append(width)
      .append(" ")
      .append(height)
      .append("\"")
      .append(" shape-rendering=\"crispEdges\"")
      .append(">")
      .append(System.lineSeparator());

    BitArray row = new BitArray(width);
    for (int y = 0; y < height; ++y) {
      StringBuilder sbPath = new StringBuilder();
      row = image.getRow(y, row);
      for (int x = 0; x < width; ++x) {
        if (row.get(x)) {
          sbPath.append(" M" + x + "," + y + "h1v1h-1z");
        }
      }
      if (sbPath.length() > 0) {
        sb.append("<path d=\"")
          .append(sbPath.toString())
          .append("\"></path>")
          .append(System.lineSeparator());
      }
    }
    sb.append("</svg>").append(System.lineSeparator());

    return sb.toString();
  }

  /**
   * Transforms to the ZXING format from our own type representation.
   * 
   * @return a BarcodeFormat
   */
  private BarcodeFormat zxingBarcodeFormat() {
    if (this.type == Barcode.BarcodeType.QR) {
      return BarcodeFormat.QR_CODE;
    }
    else {
      return BarcodeFormat.AZTEC;
    }
  }

  /**
   * Sets the type of barcodes to create.
   * <p>
   * {@value #DEFAULT_TYPE} is the default.
   * </p>
   * 
   * @param type
   *          barcode type
   */
  public void setType(final Barcode.BarcodeType type) {
    this.type = Optional.ofNullable(type).orElse(DEFAULT_TYPE);
  }

  /**
   * Sets the image format for barcodes created.
   * <p>
   * {@value #DEFAULT_IMAGE_FORMAT} is the default.
   * </p>
   * 
   * @param imageFormat
   *          the image format
   */
  public void setImageFormat(final Barcode.ImageFormat imageFormat) {
    this.imageFormat = Optional.ofNullable(imageFormat).orElse(DEFAULT_IMAGE_FORMAT);
  }

  /**
   * Sets the width and height (in pixels) to use for creating barcodes.
   * <p>
   * {@value #DEFAULT_WIDTH_AND_HEIGHT} is the default.
   * </p>
   * <p>
   * If 0 is supplied the smallest possible size will be choosen for the generated code.
   * </p>
   * 
   * @param widthAndHeight
   *          the width/height
   */
  public void setWidthAndHeight(final int widthAndHeight) {
    if (widthAndHeight < 0) {
      throw new IllegalArgumentException("widthAndHeight must not be less than 0");
    }
    this.widthAndHeight = widthAndHeight;
  }

}
