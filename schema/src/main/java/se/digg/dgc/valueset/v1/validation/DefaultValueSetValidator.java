/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.valueset.v1.validation;

import java.util.List;
import java.util.Optional;

import se.digg.dgc.payload.v1.Eudcc;
import se.digg.dgc.payload.v1.RecoveryEntry;
import se.digg.dgc.payload.v1.TestEntry;
import se.digg.dgc.payload.v1.VaccinationEntry;
import se.digg.dgc.valueset.v1.ValueSet;
import se.digg.dgc.valueset.v1.ValueSetConstants;
import se.digg.dgc.valueset.v1.ValueSetValue;

/**
 * Default implementation of the {@link ValueSetValidator} interface.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class DefaultValueSetValidator implements ValueSetValidator {

  /** The value sets that are configured for this validator. */
  private final List<ValueSet> valueSets;

  /**
   * Constructor.
   * 
   * @param valueSets
   *          the value sets that this validator should use
   */
  public DefaultValueSetValidator(final List<ValueSet> valueSets) {
    this.valueSets = Optional.ofNullable(valueSets).orElseThrow(() -> new IllegalArgumentException("valueSets must not be null"));
  }

  /** {@inheritDoc} */
  @Override
  public ValueSetValidationResult validate(final Eudcc dcc) {
    final ValueSetValidationResult result = new ValueSetValidationResult();
    if (dcc.getV() != null) {
      for (int i = 0; i < dcc.getV().size(); i++) {
        result.addChildResult(this.validate(dcc.getV().get(i), "v[" + i + "]"));
      }
    }
    if (dcc.getT() != null) {
      for (int i = 0; i < dcc.getT().size(); i++) {
        result.addChildResult(this.validate(dcc.getT().get(i), "t[" + i + "]"));
      }
    }
    if (dcc.getR() != null) {
      for (int i = 0; i < dcc.getR().size(); i++) {
        result.addChildResult(this.validate(dcc.getR().get(i), "r[" + i + "]"));
      }
    }
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public ValueSetValidationResult validate(final VaccinationEntry vaccination) {
    return this.validate(vaccination, null);
  }

  /**
   * Validates a {@link VaccinationEntry} against the value sets configured for this validator.
   * 
   * @param vaccination
   *          the entry to validate
   * @param propertyName
   *          the property name
   * @return validation result
   */
  private ValueSetValidationResult validate(final VaccinationEntry vaccination, final String propertyName) {
    final ValueSetValidationResult result = new ValueSetValidationResult(propertyName);
    result.addChildResult(this.checkValue(vaccination.getTg(), "tg", ValueSetConstants.DISEASE_AGENT_TARGETED_ID));
    result.addChildResult(this.checkValue(vaccination.getVp(), "vp", ValueSetConstants.VACCINE_PROPHYLAXIS));
    result.addChildResult(this.checkValue(vaccination.getMp(), "mp", ValueSetConstants.MEDICAL_PRODUCT_ID));
    result.addChildResult(this.checkValue(vaccination.getMa(), "ma", ValueSetConstants.MARKETING_AUTH_HOLDERS_ID));

    return result;
  }

  /** {@inheritDoc} */
  @Override
  public ValueSetValidationResult validate(final TestEntry testEntry) {
    return this.validate(testEntry, null);
  }

  /**
   * Validates a {@link TestEntry} against the value sets configured for this validator.
   * 
   * @param testEntry
   *          the test entry to validate
   * @param propertyName
   *          the property name
   * @return validation result
   */
  private ValueSetValidationResult validate(final TestEntry testEntry, final String propertyName) {
    final ValueSetValidationResult result = new ValueSetValidationResult(propertyName);
    result.addChildResult(this.checkValue(testEntry.getTg(), "tg", ValueSetConstants.DISEASE_AGENT_TARGETED_ID));
    result.addChildResult(this.checkValue(testEntry.getTt(), "tt", ValueSetConstants.TEST_TYPE));    
    result.addChildResult(this.checkValue(testEntry.getMa(), "ma", ValueSetConstants.TEST_MANUFACTURER_AND_NAME_ID));
    result.addChildResult(this.checkValue(testEntry.getTr(), "tr", ValueSetConstants.LAB_RESULT_ID));

    return result;
  }

  /** {@inheritDoc} */
  @Override
  public ValueSetValidationResult validate(final RecoveryEntry recoveryEntry) {
    return this.validate(recoveryEntry, null);
  }

  /**
   * Validates a {@link RecoveryEntry} against the value sets configured for this validator.
   * 
   * @param recoveryEntry
   *          the recovery entry to validate
   * @param propertyName
   *          the property name
   * @return validation result
   */
  private ValueSetValidationResult validate(final RecoveryEntry recoveryEntry, final String propertyName) {
    final ValueSetValidationResult result = new ValueSetValidationResult(propertyName);
    result.addChildResult(this.checkValue(recoveryEntry.getTg(), "tg", ValueSetConstants.DISEASE_AGENT_TARGETED_ID));

    return result;
  }

  /**
   * Checks the supplied value against the supplied value set.
   * 
   * @param value
   *          the value to check
   * @param propertyName
   *          the property name (for reporting)
   * @param valueSetId
   *          the ID of the value set
   * @return a ValueSetValidationResult
   */
  private ValueSetValidationResult checkValue(final String value, final String propertyName, final String valueSetId) {
    if (value == null) {
      return null;
    }
    final ValueSet valueSet = this.valueSets.stream()
      .filter(vs -> valueSetId.equals(vs.getId()))
      .findFirst()
      .orElse(null);

    if (valueSet == null) {
      return new ValueSetValidationResult(propertyName, ValueSetValidationResult.Status.UNDETERMINE,
        String.format("No '%s' value-set installed - cannot perform check of '%s'", valueSetId, propertyName));
    }
    final ValueSetValue vsv = valueSet.getValue(value);
    if (vsv == null) {
      return new ValueSetValidationResult(propertyName, ValueSetValidationResult.Status.ERROR,
        String.format("Value for '%s' (%s) is not valid according to value-set '%s'",
          propertyName, value, valueSetId));
    }
    else {
      return new ValueSetValidationResult(propertyName, ValueSetValidationResult.Status.SUCCESS,
        String.format("Value for '%s' is in value-set '%s'", propertyName, valueSetId));
    }
  }

}
