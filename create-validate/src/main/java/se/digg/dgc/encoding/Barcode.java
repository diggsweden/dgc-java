/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.encoding;

import java.util.Base64;
import java.util.Optional;

/**
 * The representation of a 2D barcode.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class Barcode {

  /** The type of barcode. */
  private final BarcodeType type;

  /** The image format of the barcode. */
  private final ImageFormat imageFormat;

  /** The byte array representing the barcode image according to the selected image format. */
  private final byte[] image;

  /** The width of the barcode in pixels. */
  private final int width;

  /** The height of the barcode in pixels. */
  private final int height;

  /** The content of the barcode, i.e., the data "in" the barcode. */
  private final String payload;

  /**
   * Constructor.
   * 
   * @param type
   *          the type of barcode
   * @param image
   *          byte array representing the barcode image according to the selected image format
   * @param imageFormat
   *          the image format of the code
   * @param width
   *          the width in pixels
   * @param height
   *          the height in pixels
   * @param payload
   *          the barcode payload (content)
   */
  public Barcode(final BarcodeType type, final byte[] image, final ImageFormat imageFormat, 
      final int width, final int height, final String payload) {
    this.type = Optional.ofNullable(type).orElseThrow(() -> new IllegalArgumentException("type must not be null"));
    this.image = Optional.ofNullable(image)
      .filter(c -> c.length > 0)
      .orElseThrow(() -> new IllegalArgumentException("code must not be null or empty"));
    this.imageFormat = Optional.ofNullable(imageFormat).orElseThrow(() -> new IllegalArgumentException("imageFormat must not be null"));
    this.width = width;
    this.height = height;
    this.payload = Optional.ofNullable(payload).orElseThrow(() -> new IllegalArgumentException("payload must not be null"));

    if (this.width <= 0) {
      throw new IllegalArgumentException("width must be a positive integer");
    }
    if (this.height <= 0) {
      throw new IllegalArgumentException("height must be a positive integer");
    }
  }

  /**
   * Gets the barcode type.
   * 
   * @return the barcode type
   */
  public BarcodeType getType() {
    return this.type;
  }

  /**
   * Gets the image format of the barcode.
   * 
   * @return the image format
   */
  public ImageFormat getImageFormat() {
    return this.imageFormat;
  }

  /**
   * Gets the contents that is the barcode image according to the selected image format
   * 
   * @return the image contents representing the barcode
   */
  public byte[] getImage() {
    return this.image;
  }

  /**
   * Gets the width of the barcode in pixels.
   * 
   * @return the width
   */
  public int getWidth() {
    return this.width;
  }

  /**
   * Gets the height of the barcode in pixels.
   * 
   * @return the height
   */
  public int getHeight() {
    return this.height;
  }

  /**
   * Gets the payload/contents of the barcode.
   * 
   * @return the payload
   */
  public String getPayload() {
    return this.payload;
  }

  /**
   * Returns a representation of the barcode as a Base64 image.
   * 
   * <p>
   * For example:
   * </p>
   * 
   * <pre>
   * {@code data:image/png;base64, iVBORw0KGgoAAAANSUhEUgAAAAUA
   * AAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO
   * 9TXL0Y4OHwAAAABJRU5ErkJggg==
   * }
   * </pre>
   * 
   * <p>
   * The image may then be directly inserted in HTML code as:
   * </p>
   * 
   * <pre>
   * {@code <img src="data:image/png;base64, iVBORw0KGgoAAAANSUhEUgAAAAUA
   * AAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO
   * 9TXL0Y4OHwAAAABJRU5ErkJggg==" scale="0">
   * }
   * </pre>
   * 
   * @return
   */
  public String toBase64Image() {
    return String.format("data:image/%s;base64, %s",
      this.imageFormat.getName().toLowerCase(), Base64.getEncoder().encodeToString(this.image));
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return String.format("%s barcode in %s format (%d x %d) - %s",
      this.type, this.imageFormat.getName(), this.width, this.height,
      Base64.getEncoder().encodeToString(this.image));
  }

  /**
   * Barcode type.
   */
  public static enum BarcodeType {
    /** Aztec code. */
    AZTEC("AZTEC"),

    /** QR code. */
    QR("QR");

    /**
     * Gets the name of the type.
     * 
     * @return type name
     */
    public String getName() {
      return this.name;
    }

    /**
     * Parses a barcode type name into an {@code BarcodeType} instance.
     * 
     * @param name
     *          the string to parse
     * @return a BarcodeType
     */
    public static BarcodeType parse(final String name) {
      for (BarcodeType t : BarcodeType.values()) {
        if (t.getName().equalsIgnoreCase(name)) {
          return t;
        }
      }
      throw new IllegalArgumentException("Unsupported Barcode type - " + name);
    }

    /**
     * Constructor.
     * 
     * @param name
     *          the name of the type
     */
    private BarcodeType(final String name) {
      this.name = name;
    }

    /** The name. */
    private final String name;
  }

  /**
   * Enum representing an image format.
   */
  public static enum ImageFormat {
    JPG("jpg", "image/jpg"), PNG("png", "image/png");

    /**
     * Returns the image format in text format.
     * 
     * @return the image format
     */
    public String getName() {
      return this.name;
    }

    /**
     * Gets the image MIME type.
     * 
     * @return the MIME type
     */
    public String getMimeType() {
      return this.mimeType;
    }

    /**
     * Parses an image format string into an {@code ImageFormat} instance.
     * 
     * @param name
     *          the string to parse
     * @return an ImageFormat
     */
    public static ImageFormat parse(final String name) {
      for (ImageFormat i : ImageFormat.values()) {
        if (i.getName().equalsIgnoreCase(name)) {
          return i;
        }
      }
      throw new IllegalArgumentException("Unsupported image format - " + name);
    }

    /**
     * Parses an image MIME type into an {@code ImageFormat} instance.
     * 
     * @param mimeType
     *          the MIME type
     * @return an ImageFormat
     */
    public static ImageFormat parseFromMimeType(final String mimeType) {
      for (ImageFormat i : ImageFormat.values()) {
        if (i.getMimeType().equalsIgnoreCase(mimeType)) {
          return i;
        }
      }
      throw new IllegalArgumentException("Unsupported image MIME type - " + mimeType);
    }

    /**
     * Constructor.
     * 
     * @param name
     *          the image format name
     * @param mimeType
     *          the image mime type
     */
    private ImageFormat(final String name, final String mimeType) {
      this.name = name;
      this.mimeType = mimeType;
    }

    /** The image format name. */
    private final String name;

    /** The image MIME type. */
    private final String mimeType;
  }

}
