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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.upokecenter.cbor.CBORException;
import com.upokecenter.cbor.CBORObject;
import com.upokecenter.cbor.CBORType;

/**
 * A utility class that is used during interop testing.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class DGCPayloadValidator {

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
    validateDob(dgc.get("dob"), errors);
    
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
          validateTestEntry(v.get(i), "t[" + i + "]", errors, uvcis);
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
  
  private static void validateDob(final CBORObject dob, final List<Report> errors) {    
    final String d = assertString(dob, "dob", true, errors); 
    try {
      LocalDate.parse(d);
    }
    catch (Exception e) {
      errors.add(Report.error("dob", String.format("Bad format for date of birth - %s", dob.AsString())));
    }
  }
  
  private static void validateVaccinationEntry(final CBORObject v, final String item, final List<Report> errors, final List<String> uvcis) {
    
  }
  
  private static void validateTestEntry(final CBORObject v, final String item, final List<Report> errors, final List<String> uvcis) {
    
  }
  
  private static void validateRecoveryEntry(final CBORObject v, final String item, final List<Report> errors, final List<String> uvcis) {
    
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
    if (required && s.isBlank()) {
      errors.add(Report.error(item, "Field is required but is empty, or contains only blanks"));
    }
    return s;
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
