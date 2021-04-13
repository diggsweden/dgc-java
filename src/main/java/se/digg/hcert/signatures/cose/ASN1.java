/*
 * MIT License
 * 
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.hcert.signatures.cose;

import java.util.ArrayList;
import java.util.List;

/**
 * ASN.1 encoding support.
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class ASN1 {

  private static final byte[] SEQUENCE_TAG = new byte[] { 0x30 };

  /**
   * Converts the supplied bytes into the ASN.1 DER encoding for an unsigned integer.
   * 
   * @param i
   *          the byte array to convert
   * @return the DER encoding
   */
  public static byte[] toUnsignedInteger(final byte[] i) {
    int offset = 0;
    while (offset < i.length && i[offset] == 0) {
      offset++;
    }
    if (offset == i.length) {
      return new byte[] { 0x02, 0x01, 0x00 };
    }
    int pad = 0;
    if ((i[offset] & 0x80) != 0) {
      pad++;
    }

    final int length = i.length - offset;
    final byte[] der = new byte[2 + length + pad];
    der[0] = 0x02;
    der[1] = (byte) (length + pad);
    System.arraycopy(i, offset, der, 2 + pad, length);

    return der;
  }

  /**
   * Convert the supplied input to an ASN.1 Sequence.
   * 
   * @param seq
   *          the data in the sequence
   * @return the DER encoding
   */
  public static byte[] toSequence(final ArrayList<byte[]> seq) {
    final byte[] seqBytes = toBytes(seq);
    final List<byte[]> seqList = new ArrayList<>();
    seqList.add(SEQUENCE_TAG);
    if (seqBytes.length <= 127) {
      seqList.add(new byte[] { (byte) seqBytes.length });
    }
    else {
      seqList.add(new byte[] { (byte) 0x81, (byte) seqBytes.length });
    }
    seqList.add(seqBytes);

    return toBytes(seqList);
  }

  private static byte[] toBytes(final List<byte[]> bytes) {
    int len = 0;
    len = bytes.stream().map((r) -> r.length).reduce(len, Integer::sum);

    final byte[] b = new byte[len];
    len = 0;
    for (final byte[] r : bytes) {
      System.arraycopy(r, 0, b, len, r.length);
      len += r.length;
    }

    return b;
  }

}
