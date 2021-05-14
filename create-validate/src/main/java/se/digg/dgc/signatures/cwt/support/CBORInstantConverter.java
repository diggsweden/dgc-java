/*
 * MIT License
 * 
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.signatures.cwt.support;

import java.time.Instant;
import java.util.Date;

import com.upokecenter.cbor.CBORDateConverter;
import com.upokecenter.cbor.CBORObject;
import com.upokecenter.cbor.ICBORToFromConverter;

/**
 * Converter for representation of NumericDate using Instant according to RFC8392.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class CBORInstantConverter implements ICBORToFromConverter<Instant> {
  
  /** Converter used to take an Instant to/from a CBORObject (untagged). */
  private static final CBORDateConverter untaggedDateConverter = CBORDateConverter.UntaggedNumber;
  
  /** Converter which we fall back on if the object is tagged. */
  private static final CBORDateConverter taggedDateConverter = CBORDateConverter.TaggedNumber;

  /** {@inheritDoc} */
  @Override
  public CBORObject ToCBORObject(final Instant obj) {
    if (obj == null) {
      return null;
    }
    return untaggedDateConverter.ToCBORObject(new Date(obj.getEpochSecond() * 1000L));
  }

  /** {@inheritDoc} */
  @Override
  public Instant FromCBORObject(final CBORObject obj) {
    if (obj == null) {
      return null;
    }
    
    final Date date;
    if (obj.HasMostOuterTag(0) || obj.HasMostOuterTag(1)) {
      // We are liberal. Really it should be a numeric untagged date.
      date = taggedDateConverter.FromCBORObject(obj);
    }
    else {
      date = untaggedDateConverter.FromCBORObject(obj);
    }
    return Instant.ofEpochMilli(date.getTime());
  }
  
}
