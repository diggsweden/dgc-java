/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.valueset.v1;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for ValueSetConstants.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class ValueSetConstantsTest {

  @Test
  public void testConstants() throws Exception {
    assertValueSet(ValueSetConstants.diseaseAgentTargeted());
    assertValueSet(ValueSetConstants.marketingAuthorizationHolder());
    assertValueSet(ValueSetConstants.medicalProduct());
    assertValueSet(ValueSetConstants.testManufacturer());
    assertValueSet(ValueSetConstants.testResult());
    assertValueSet(ValueSetConstants.vaccineProphylaxis());
  }
  
  private static void assertValueSet(final ValueSet vs) throws Exception {
    Assert.assertNotNull(vs);
    Assert.assertNotNull(vs.getId());
    Assert.assertNotNull(vs.getDate());
    Assert.assertNotNull(vs.getValues());
    Assert.assertTrue(vs.getValues().size() > 0);
  }

}
