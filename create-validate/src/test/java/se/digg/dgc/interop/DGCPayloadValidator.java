/*
 * Copyright 2021 Litsec AB
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
package se.digg.dgc.interop;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.upokecenter.cbor.CBORDateConverter;
import com.upokecenter.cbor.CBORException;
import com.upokecenter.cbor.CBORObject;
import com.upokecenter.cbor.CBORType;

import se.digg.dgc.valueset.v1.ValueSet;
import se.digg.dgc.valueset.v1.ValueSetConstants;

/**
 * A utility class that is used during interop testing.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class DGCPayloadValidator {

  private static final ValueSet diseaseAgentTargetedVS = ValueSetConstants.diseaseAgentTargeted();
  private static final ValueSet testManufacturerVS = ValueSetConstants.testManufacturer();
  private static final ValueSet testResultVS = ValueSetConstants.testResult();
  private static final ValueSet testTypeVS = ValueSetConstants.testType();
  private static final ValueSet marketingAuthorizationHolderVS = ValueSetConstants.marketingAuthorizationHolder();
  private static final ValueSet medicalProductVS = ValueSetConstants.medicalProduct();
  private static final ValueSet vaccineProphylaxisVS = ValueSetConstants.vaccineProphylaxis();

  public static List<Report> validateDgcPayload(final byte[] payload) {
    List<Report> errors = new ArrayList<>();

    // First decode the bytes into a CBOR object and make initial checks ...
    //
    final CBORObject dgc;
    try {
      dgc = CBORObject.DecodeFromBytes(payload);
    }
    catch (CBORException e) {
      errors.add(Report.error("dgc", String.format("Failed to decode - %s", e.getMessage())));
      return errors;
    }
    if (dgc.getType() != CBORType.Map) {
      errors.add(Report.error("dgc", "Payload is not encoded as a CBOR map"));
      return errors;
    }

    // ver
    //
    validateVersion(dgc.get("ver"), errors);

    // name
    CBORObject nam = dgc.get("nam");
    if (nam == null) {
      errors.add(Report.error("nam", "Required field 'nam' is missing"));
    }
    else {
      validateName(nam, errors);
    }

    // date-of-birth
    //
    assertDate(dgc.get("dob"), "dob", true, errors);

    // OK, now check the different entries.
    //

    // Vaccination entries
    //
    final CBORObject v = dgc.get("v");
    if (v != null) {
      if (v.getType() != CBORType.Array) {
        errors.add(Report.error("v", "Object 'v' is expected to be an array"));
      }
      else if (v.size() == 0) {
        errors.add(Report.error("v", "Empty array in 'v' - not allowed"));
      }
      else {
        final List<String> uvcis = new ArrayList<>();
        for (int i = 0; i < v.size(); i++) {
          validateVaccinationEntry(v.get(i), "v[" + i + "]", errors, uvcis);
        }
      }
    }

    // Test entries
    //
    final CBORObject t = dgc.get("t");
    if (t != null) {
      if (t.getType() != CBORType.Array) {
        errors.add(Report.error("t", "Object 't' is expected to be an array"));
      }
      else if (t.size() == 0) {
        errors.add(Report.error("t", "Empty array in 't' - not allowed"));
      }
      else {
        final List<String> uvcis = new ArrayList<>();
        for (int i = 0; i < t.size(); i++) {
          validateTestEntry(t.get(i), "t[" + i + "]", errors, uvcis);
        }
      }
    }

    // Recovery entries
    //
    final CBORObject r = dgc.get("r");
    if (r != null) {
      if (r.getType() != CBORType.Array) {
        errors.add(Report.error("r", "Object 'r' is expected to be an array"));
      }
      else if (r.size() == 0) {
        errors.add(Report.error("r", "Empty array in 'r' - not allowed"));
      }
      else {
        final List<String> uvcis = new ArrayList<>();
        for (int i = 0; i < r.size(); i++) {
          validateRecoveryEntry(r.get(i), "r[" + i + "]", errors, uvcis);
        }
      }
    }

    // Make additional checks

    return errors;
  }

  private static void validateVersion(final CBORObject ver, final List<Report> errors) {
    final String version = assertString(ver, "ver", true, errors);
    if (version != null && !version.matches("^\\d+.\\d+.\\d+$")) {
      errors.add(Report.error("ver", String.format("Version is set to '%s' - Does not match regexp '^\\\\d+.\\\\d+.\\\\d+$'", version)));
      return;
    }
  }

  private static void validateName(final CBORObject nam, final List<Report> errors) {
    if (nam.getType() != CBORType.Map) {
      errors.add(Report.error("nam", "Field 'nam' should be a CBOR map but is not"));
      return;
    }
    final CBORObject fn = nam.get("fn");
    if (fn == null) {
      errors.add(Report.warning("nam.fn", "Field 'fn' is missing - Is this really intentional?"));
    }
    else {
      validateNameComponent(fn, "nam.fn", errors);
    }
    final CBORObject fnt = nam.get("fnt");
    if (fnt == null) {
      errors.add(Report.error("nam.fnt", "Required field 'fnt' is missing"));
    }
    else {
      final String s = validateNameComponent(fnt, "nam.fnt", errors);
      if (s != null && !s.matches("^[A-Z<]*$")) {
        errors.add(Report.error("nam.fnt", String.format("Illegal transliteration: %s", s)));
      }
    }
    final CBORObject gn = nam.get("gn");
    if (gn == null) {
      errors.add(Report.warning("nam.gn", "Field 'gn' is missing - Is this really intentional?"));
    }
    else {
      validateNameComponent(gn, "nam.gn", errors);
    }
    final CBORObject gnt = nam.get("gnt");
    if (gnt == null) {
      errors.add(Report.warning("nam.gnt", "Field 'gnt' is missing - Is this really intentional?"));
    }
    else {
      final String s = validateNameComponent(gnt, "nam.gnt", errors);
      if (s != null && !s.matches("^[A-Z<]*$")) {
        errors.add(Report.error("nam.gnt", String.format("Illegal transliteration: %s", s)));
      }
    }
  }

  private static String validateNameComponent(final CBORObject obj, final String item, final List<Report> errors) {
    final String s = assertString(obj, item, true, errors);
    if (s != null && s.length() > 50) {
      errors.add(Report.error(item, "Exceeds maxLength limitation of 50 characters"));
    }
    return s;
  }

  private static void validateVaccinationEntry(final CBORObject v, final String item, final List<Report> errors, final List<String> uvcis) {
    if (v.getType() != CBORType.Map) {
      errors.add(Report.error(item, "Object is expected to be a map, but is not"));
      return;
    }
    final String tg = assertString(v.get("tg"), item + ".tg", true, errors);
    validateAgainstValueset(tg, item + ".tg", diseaseAgentTargetedVS, errors);

    final String vp = assertString(v.get("vp"), item + ".vp", true, errors);
    validateAgainstValueset(vp, item + ".vp", vaccineProphylaxisVS, errors);

    final String mp = assertString(v.get("mp"), item + ".mp", true, errors);
    validateAgainstValueset(mp, item + ".mp", medicalProductVS, errors);

    final String ma = assertString(v.get("ma"), item + ".ma", true, errors);
    validateAgainstValueset(ma, item + ".ma", marketingAuthorizationHolderVS, errors);

    int dnInt = -1;
    final CBORObject dn = v.get("dn");
    if (dn == null) {
      errors.add(Report.error(item + ".dn", "Required field is not present"));
    }
    else if (dn.getType() != CBORType.Integer) {
      errors.add(Report.error(item + ".dn", "Expected integer"));
    }
    else {
      dnInt = dn.AsInt32Value();
      if (dnInt < 1 || dnInt > 9) {
        errors.add(Report.error(item + ".dn", "dn not within range 1-9"));
      }
    }

    int sdInt = -1;
    final CBORObject sd = v.get("sd");
    if (sd == null) {
      errors.add(Report.error(item + ".sd", "Required field is not present"));
    }
    else if (sd.getType() != CBORType.Integer) {
      errors.add(Report.error(item + ".sd", "Expected integer"));
    }
    else {
      sdInt = sd.AsInt32Value();
      if (sdInt < 1 || sdInt > 9) {
        errors.add(Report.error(item + ".sd", "sd not within range 1-9"));
      }
    }

    if (dnInt != -1 && sdInt != -1 && dnInt > sdInt) {
      errors.add(Report.warning(item, "dn is larger than sd. This doesn't make sense"));
    }

    assertDate(v.get("dt"), item + ".dt", true, errors);

    final String co = assertString(v.get("co"), item + ".co", true, errors);
    if (co != null && !co.matches("[A-Z]{1,10}")) {
      errors.add(Report.error(item + ".co", "Invalid country code"));
    }

    final String is = assertString(v.get("is"), item + ".is", true, errors);
    if (is != null && is.length() > 50) {
      errors.add(Report.error(item + ".is", "Exceeds length restriction of 50 characters"));
    }

    final String ci = assertString(v.get("ci"), item + ".ci", true, errors);
    if (ci != null) {
      if (ci.length() > 50) {
        errors.add(Report.error(item + ".ci", "Exceeds length restriction of 50 characters"));
      }
      if (uvcis.contains(ci)) {
        errors.add(Report.warning(item + ".ci", "UVCI is the same in several vaccinations entries!"));
      }
      uvcis.add(ci);
      // TODO: check format ...
    }

  }

  private static void validateTestEntry(final CBORObject t, final String item, final List<Report> errors, final List<String> uvcis) {
    if (t.getType() != CBORType.Map) {
      errors.add(Report.error(item, "Object is expected to be a map, but is not"));
      return;
    }
    final String tg = assertString(t.get("tg"), item + ".tg", true, errors);
    validateAgainstValueset(tg, item + ".tg", diseaseAgentTargetedVS, errors);

    final String tt = assertString(t.get("tt"), item + ".tt", true, errors);
    validateAgainstValueset(tt, item + ".tt", testTypeVS, errors);
    
    assertString(t.get("nm"), item + ".nm", false, errors);

    final String ma = assertString(t.get("ma"), item + ".ma", false, errors);
    validateAgainstValueset(ma, item + ".ma", testManufacturerVS, errors);

    Instant sc = assertDateTime(t.get("sc"), item + ".sc", true, errors);
    
    Instant dr = assertDateTime(t.get("dr"), item + ".dr", false, errors);
    
    if (sc != null && dr != null) {
      if (sc.isAfter(dr)) {
        errors.add(Report.warning(item, "sc (sample collection) is after dr (date of result). This doesn't make sense"));
      }
    }
    
    final String tr = assertString(t.get("tr"), item + ".tr", true, errors);
    validateAgainstValueset(tr, item + ".tr", testResultVS, errors);
    
    final String tc = assertString(t.get("tc"), item + ".tc", true, errors);
    if (tc != null && tc.length() > 50) {
      errors.add(Report.error(item + ".tc", "Exceeds length restriction of 50 characters"));
    }
    
    final String co = assertString(t.get("co"), item + ".co", true, errors);
    if (co != null && !co.matches("[A-Z]{1,10}")) {
      errors.add(Report.error(item + ".co", "Invalid country code"));
    }
    
    final String is = assertString(t.get("is"), item + ".is", true, errors);
    if (is != null && is.length() > 50) {
      errors.add(Report.error(item + ".is", "Exceeds length restriction of 50 characters"));
    }

    final String ci = assertString(t.get("ci"), item + ".ci", true, errors);
    if (ci != null) {
      if (ci.length() > 50) {
        errors.add(Report.error(item + ".ci", "Exceeds length restriction of 50 characters"));
      }
      if (uvcis.contains(ci)) {
        errors.add(Report.warning(item + ".ci", "UVCI is the same in several test entries!"));
      }
      uvcis.add(ci);
      // TODO: check format ...
    }
  }

  private static void validateRecoveryEntry(final CBORObject r, final String item, final List<Report> errors, final List<String> uvcis) {
    if (r.getType() != CBORType.Map) {
      errors.add(Report.error(item, "Object is expected to be a map, but is not"));
      return;
    }
  }

  private static void validateAgainstValueset(final String v, final String item, final ValueSet valueSet, final List<Report> errors) {
    if (v == null) {
      return;
    }
    if (v.isBlank()) {
      return;
    }
    if (valueSet.getValue(v) == null) {
      errors.add(Report.error(item, String.format("Value '%s' is not allowed according to the '%s' value-set", v, valueSet.getId())));
    }
  }

  private static String assertString(final CBORObject obj, final String item, final boolean required, final List<Report> errors) {
    if (obj == null) {
      if (required) {
        errors.add(Report.error(item, "Required field is not present"));
      }
      return null;
    }
    if (obj.getType() != CBORType.TextString) {
      errors.add(Report.error(item, "Field is not a string"));
    }
    final String s = obj.AsString();
    if (s.isBlank()) {
      if (required) {
        errors.add(Report.error(item, "Field is required but is empty, or contains only blanks"));
      }
      else {
        errors.add(Report.warning(item, "Optional field is empty string (or blanks only) - better to set as null"));
      }
    }
    return s;
  }

  private static Instant assertDateTime(final CBORObject obj, final String item, final boolean required, final List<Report> errors) {
    if (obj == null) {
      if (required) {
        errors.add(Report.error(item, "Required field is not present"));
      }
      return null;
    }
    try {
      Date date = null;
      if (obj.HasMostOuterTag(0)) {
        // This is the correct way ...
        date = CBORDateConverter.TaggedString.FromCBORObject(obj);
      }
      else if (obj.HasMostOuterTag(1)) {
        errors.add(Report.warning(item, "DateTime represented as tagged numeric (should be string)"));
        date = CBORDateConverter.TaggedString.FromCBORObject(obj);
      }
      else if (obj.isNumber()) {
        errors.add(Report.error(item, "DateTime represented as un-tagged numeric (should be string)"));
        date = CBORDateConverter.UntaggedNumber.FromCBORObject(obj);
      }
      else if (obj.getType() == CBORType.TextString) {
        errors.add(Report.warning(item, "DateTime represented as un-tagged string (should be tagged with 0)"));
  
        try {
          return Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(obj.AsString()));
        }
        catch (DateTimeParseException e) {
          errors.add(Report.warning(item, String.format("DateTime is not represented in ISO Zulu-time - %s", obj.AsString())));
          return Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(obj.AsString()));
        }        
      }

      if (date != null) {
        return Instant.ofEpochMilli(date.getTime());
      }
    }
    catch (DateTimeParseException | CBORException e) {
      errors.add(Report.error(item, "Invalid format for DateTime"));
    }
    return null;
  }

  private static void assertDate(final CBORObject obj, final String item, final boolean required, final List<Report> errors) {
    final String s = assertString(obj, item, required, errors);
    if (s != null) {
      try {
        LocalDate.parse(s);
      }
      catch (Exception e) {
        errors.add(Report.error(item, String.format("Bad format for date - %s", s)));
      }
    }
  }

  public static class Report {

    public enum Type {
      WARNING, ERROR;
    }

    /** The type. */
    private final Type type;

    /** The item that has an error. */
    private final String item;

    /** The error message. */
    private final String msg;

    /**
     * Constructor.
     * 
     * @param type
     *          the type
     * @param item
     *          the item that has an error
     * @param msg
     *          the error message
     */
    public Report(final Type type, final String item, final String msg) {
      this.type = type;
      this.item = item;
      this.msg = msg;
    }

    /**
     * Creates a warning report.
     * 
     * @param item
     *          the item
     * @param msg
     *          the message
     * @return a Report object
     */
    public static Report warning(final String item, final String msg) {
      return new Report(Type.WARNING, item, msg);
    }

    /**
     * Creates an error report.
     * 
     * @param item
     *          the item
     * @param msg
     *          the message
     * @return a Report object
     */
    public static Report error(final String item, final String msg) {
      return new Report(Type.ERROR, item, msg);
    }

    /**
     * Gets the type.
     * 
     * @return the type
     */
    public Type getType() {
      return this.type;
    }

    /**
     * Gets the item that has an error.
     * 
     * @return the item name
     */
    public String getItem() {
      return this.item;
    }

    /**
     * Gets the error message.
     * 
     * @return the error message
     */
    public String getMsg() {
      return this.msg;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
      return String.format("[%s] %s=\"%s\"", this.type, this.item, this.msg);
    }

  }

  // Hidden constructor
  private DGCPayloadValidator() {
  }

}
