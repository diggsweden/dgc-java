/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.encoding.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import se.digg.dgc.encoding.BarcodeDecoder;
import se.digg.dgc.encoding.BarcodeException;

/**
 * Default implementation of the {@link BarcodeDecoder} interface using the ZXing library.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class DefaultBarcodeDecoder implements BarcodeDecoder {

  /** Decoding hints. */
  private static Map<DecodeHintType, Object> HINTS = new HashMap<>();

  static {
    HINTS.put(DecodeHintType.POSSIBLE_FORMATS, Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.AZTEC));
  }

  /** {@inheritDoc} */
  @Override
  public byte[] decode(final byte[] image) throws BarcodeException {

    BufferedImage bufferedImage = null;
    try (final ByteArrayInputStream imageStream = new ByteArrayInputStream(image)) {
      bufferedImage = ImageIO.read(imageStream);
    }
    catch (IOException e) {
      throw new BarcodeException("Failed to read supplied image bytes into a valid image", e);
    }

    try {
      final MultiFormatReader reader = new MultiFormatReader();
      final Result result = reader.decode(new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(bufferedImage))), HINTS);
      if (result.getText() != null) {
        return result.getText().getBytes();
      }
      else {
        throw new BarcodeException("No contents found inside of supplied image");
      }
    }
    catch (final NotFoundException e) {
      throw new BarcodeException("No barcode found inside supplied image", e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public String decodeToString(final byte[] image, final Charset characterSet) throws BarcodeException {
    final byte[] bytes = this.decode(image);
    return new String(bytes, characterSet != null ? characterSet : Charset.defaultCharset());
  }

}
