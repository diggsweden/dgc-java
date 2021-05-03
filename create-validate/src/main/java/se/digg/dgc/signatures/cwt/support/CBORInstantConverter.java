/*
 * MIT License
 * 
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.signatures.cwt.support;

import java.time.Instant;

import com.upokecenter.cbor.CBORException;
import com.upokecenter.cbor.CBORNumber;
import com.upokecenter.cbor.CBORObject;
import com.upokecenter.cbor.ICBORToFromConverter;
import com.upokecenter.numbers.EDecimal;
import com.upokecenter.numbers.EInteger;

/**
 * Converter for representation of NumericDate using Instant according to RFC8392.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class CBORInstantConverter implements ICBORToFromConverter<Instant> {

  /** {@inheritDoc} */
  @Override
  public CBORObject ToCBORObject(final Instant obj) {
    if (obj == null) {
      return null;
    }
    return CBORObject.FromObject(obj.getEpochSecond());
  }

  /** {@inheritDoc} */
  @Override
  public Instant FromCBORObject(final CBORObject obj) {
    if (obj == null) {
      return null;
    }

    CBORObject untaggedObject = obj;

    if (obj.HasMostOuterTag(0)) {
      // We are liberal. Really it should be a numeric date.
      try {
        return stringToInstant(obj.AsString());
      }
      catch (ArithmeticException | IllegalStateException | IllegalArgumentException e) {
        throw new CBORException(e.getMessage(), e);
      }
    }
    else if (obj.HasMostOuterTag(1)) {
      // Section 2 of RFC8392 states that the leading 1 tag MUST be omitted, but it is present here
      untaggedObject = obj.UntagOne();
    }

    if (!untaggedObject.isNumber()) {
      throw new CBORException("Expected number for representation of date");
    }
    final CBORNumber num = untaggedObject.AsNumber();
    if (!num.IsFinite()) {
      throw new CBORException("Not a finite number");
    }
    if (num.compareTo(Long.MIN_VALUE) < 0 || num.compareTo(Long.MAX_VALUE) > 0) {
      throw new CBORException("Date can not be represented as Instant (too small or large)");
    }
    
    final EDecimal dec = (EDecimal)untaggedObject.ToObject(EDecimal.class);
    final int[] lesserFields = new int[7];
    final EInteger[] year = new EInteger[1];
    CBORUtils.breakDownSecondsSinceEpoch(dec, year, lesserFields);
    return CBORUtils.buildUpInstant(year[0], lesserFields);
  }

  private static Instant stringToInstant(final String str) {
    int[] lesserFields = new int[7];
    EInteger[] year = new EInteger[1];
    CBORUtils.parseAtomDateTimeString(str, year, lesserFields);
    return CBORUtils.buildUpInstant(year[0], lesserFields);
  }
}
