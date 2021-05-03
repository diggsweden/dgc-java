/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.valueset.v1.validation;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import se.digg.dgc.payload.v1.Eudgc;
import se.digg.dgc.payload.v1.RecoveryEntry;
import se.digg.dgc.payload.v1.TestEntry;
import se.digg.dgc.payload.v1.VaccinationEntry;

/**
 * An interface describing a validator that can be used to ensure that data present in a DGC is in accordance with the
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
   * Validates a DGC against the value sets configured for this validator.
   * 
   * @param dgc
   *          the DGC to validate
   * @return validation result
   */
  ValueSetValidationResult validate(final Eudgc dgc);

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
