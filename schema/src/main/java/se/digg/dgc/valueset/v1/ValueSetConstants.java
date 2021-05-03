/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.valueset.v1;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Utility class that holds constant value sets for the different value sets.
 * <p>
 * The value set JSON files are stored in this library, so for a more flexible solution the value sets used should be
 * based on dynamic files that are continuously updated. See {@link ReloadableValueSet}.
 * </p>
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class ValueSetConstants {
  
  /** ID for the "disease agent targeted" value set. */
  public static final String DISEASE_AGENT_TARGETED_ID = "disease-agent-targeted";

  /** ID for "Covid-19 lab test manufacturer (and name)" value set. */
  public static final String TEST_MANUFACTURER_AND_NAME_ID = "covid-19-lab-test-manufacturer-and-name";
  
  /** ID for "Covid-19 lab result" value set. */
  public static final String LAB_RESULT_ID = "covid-19-lab-result";
  
  /** ID for "Covid-19 marketing authorization holders" value set. */
  public static final String MARKETING_AUTH_HOLDERS_ID = "vaccines-covid-19-auth-holders";
  
  /** ID for "Vaccine medical product" value set. */
  public static final String MEDICAL_PRODUCT_ID = "vaccines-covid-19-names";
  
  /** ID for "Vaccine Prophylaxis" value set. */
  public static final String VACCINE_PROPHYLAXIS = "sct-vaccines-covid-19";  
  
  /**
   * Gets a static representation of the <b>disease-agent-targeted</b> value set.
   * 
   * @return disease-agent-targeted
   */
  public static ValueSet diseaseAgentTargeted() {
    return createValueSet("/v1-valuesets/disease-agent-targeted.json");
  }

  /**
   * Gets a static representation of the <b>covid-19-lab-test-manufacturer-and-name</b> value set.
   * 
   * @return covid-19-lab-test-manufacturer-and-name
   */
  public static ValueSet testManufacturer() {
    return createValueSet("/v1-valuesets/test-manf.json");
  }

  /**
   * Gets a static representation of the <b>covid-19-lab-result</b> value set.
   * 
   * @return covid-19-lab-result
   */
  public static ValueSet testResult() {
    return createValueSet("/v1-valuesets/test-result.json");
  }

  /**
   * Gets a static representation of the <b>vaccines-covid-19-auth-holders</b> value set.
   * 
   * @return vaccines-covid-19-auth-holders
   */
  public static ValueSet marketingAuthorizationHolder() {
    return createValueSet("/v1-valuesets/vaccine-mah-manf.json");
  }

  /**
   * Gets a static representation of the <b>vaccines-covid-19-names</b> value set.
   * 
   * @return vaccines-covid-19-names
   */
  public static ValueSet medicalProduct() {
    return createValueSet("/v1-valuesets/vaccine-medicinal-product.json");
  }

  //
  /**
   * Gets a static representation of the <b>sct-vaccines-covid-19</b> value set.
   * 
   * @return sct-vaccines-covid-19
   */
  public static ValueSet vaccineProphylaxis() {
    return createValueSet("/v1-valuesets/vaccine-prophylaxis.json");
  }

  // Hidden constructor
  private ValueSetConstants() {
  }

  /**
   * Helper method that creates a value set from a JSON file on the classpath.
   * 
   * @param classpathResource
   *          the resource
   * @return the ValueSet
   */
  private static ValueSet createValueSet(final String classpathResource) {
    try {
      return new ValueSet(ValueSetConstants.class.getResourceAsStream(classpathResource));
    }
    catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

}
