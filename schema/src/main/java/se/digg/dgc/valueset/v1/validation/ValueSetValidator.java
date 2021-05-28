/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.valueset.v1.validation;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import se.digg.dgc.payload.v1.Eudcc;
import se.digg.dgc.payload.v1.RecoveryEntry;
import se.digg.dgc.payload.v1.TestEntry;
import se.digg.dgc.payload.v1.VaccinationEntry;

/**
 * An interface describing a validator that can be used to ensure that data present in a DCC is in accordance with the
 * configured value sets.
 * <p>
 * Note: This type of validator only validates present data against value sets. For a validation against the schema, use
 * {@link ValidatorFactory} and {@link Validator}.
 * </p>
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public interface ValueSetValidator {

  /**
   * Validates a DCC against the value sets configured for this validator.
   * 
   * @param dcc
   *          the DCC to validate
   * @return validation result
   */
  ValueSetValidationResult validate(final Eudcc dcc);

  /**
   * Validates a {@link VaccinationEntry} against the value sets configured for this validator.
   * 
   * @param vaccination
   *          the vaccination entry to validate
   * @return validation result
   */
  ValueSetValidationResult validate(final VaccinationEntry vaccination);

  /**
   * Validates a {@link TestEntry} against the value sets configured for this validator.
   * 
   * @param testEntry
   *          the test entry to validate
   * @return validation result
   */
  ValueSetValidationResult validate(final TestEntry testEntry);

  /**
   * Validates a {@link RecoveryEntry} against the value sets configured for this validator.
   * 
   * @param recoveryEntry
   *          the recovery entry to validate
   * @return validation result
   */
  ValueSetValidationResult validate(final RecoveryEntry recoveryEntry);

}
