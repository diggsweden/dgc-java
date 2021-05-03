/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.valueset.v1.validation;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import se.digg.dgc.payload.v1.Eudgc;
import se.digg.dgc.payload.v1.RecoveryEntry;
import se.digg.dgc.payload.v1.TestEntry;
import se.digg.dgc.payload.v1.VaccinationEntry;
import se.digg.dgc.valueset.v1.ValueSetConstants;

/**
 * Test cases for DefaultValueSetValidator.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class DefaultValueSetValidatorTest {

  @Test
  public void testVaccinationEntryOK() throws Exception {    
    final DefaultValueSetValidator v = new DefaultValueSetValidator(Arrays.asList(
      ValueSetConstants.diseaseAgentTargeted(),
      ValueSetConstants.vaccineProphylaxis(),
      ValueSetConstants.medicalProduct(),
      ValueSetConstants.marketingAuthorizationHolder()));
    
    final VaccinationEntry e = new VaccinationEntry()
        .withTg("840539006")
        .withVp("1119349007")
        .withMp("EU/1/20/1507")
        .withMa("ORG-100030215");
    
    ValueSetValidationResult res = v.validate(e);
    Assert.assertTrue(res.isSuccess());
    Assert.assertEquals(4, res.getChildren().size());
    
    Eudgc dgc = new Eudgc();
    dgc.setV(Arrays.asList(e));
    res = v.validate(dgc);

    Assert.assertTrue(res.isSuccess());    
    Assert.assertEquals(1, res.getChildren().size());
    Assert.assertEquals("v[0]", res.getChildren().get(0).getPropertyName());
  }
  
  @Test
  public void testVaccinationEntryUndetermine() throws Exception {    
    final DefaultValueSetValidator v = new DefaultValueSetValidator(Arrays.asList(
      ValueSetConstants.vaccineProphylaxis(),
      ValueSetConstants.medicalProduct(),
      ValueSetConstants.marketingAuthorizationHolder()));
    
    final VaccinationEntry e = new VaccinationEntry()
        .withTg("840539006")
        .withVp("1119349007")
        .withMp("EU/1/20/1507")
        .withMa("ORG-100030215");
    
    ValueSetValidationResult res = v.validate(e);
    
    Assert.assertEquals(ValueSetValidationResult.Status.UNDETERMINE, res.getResult()); 
    Assert.assertEquals(4, res.getChildren().size());
    Assert.assertEquals(ValueSetValidationResult.Status.UNDETERMINE, res.getChildren().get(0).getResult());
    Assert.assertTrue(res.getChildren().get(1).isSuccess());
    Assert.assertTrue(res.getChildren().get(2).isSuccess());
    Assert.assertTrue(res.getChildren().get(3).isSuccess());
    
    Eudgc dgc = new Eudgc();
    dgc.setV(Arrays.asList(e));
    res = v.validate(dgc);

    Assert.assertEquals(ValueSetValidationResult.Status.UNDETERMINE, res.getResult());
    Assert.assertEquals(1, res.getChildren().size());
    Assert.assertEquals("v[0]", res.getChildren().get(0).getPropertyName());
  }
  
  @Test
  public void testVaccinationEntryError() throws Exception {    
    final DefaultValueSetValidator v = new DefaultValueSetValidator(Arrays.asList(
      ValueSetConstants.diseaseAgentTargeted(),
      ValueSetConstants.vaccineProphylaxis(),
      ValueSetConstants.medicalProduct(),
      ValueSetConstants.marketingAuthorizationHolder()));
    
    final VaccinationEntry e = new VaccinationEntry()
        .withTg("8405390089")
        .withVp("111934900700909")
        .withMp("EU/1/20/1507990")
        .withMa("ORG-100030215JKJKJ");
    
    ValueSetValidationResult res = v.validate(e);
    Assert.assertEquals(ValueSetValidationResult.Status.ERROR, res.getResult());
  }
  
  @Test
  public void testTestEntryOK() throws Exception {    
    final DefaultValueSetValidator v = new DefaultValueSetValidator(Arrays.asList(
      ValueSetConstants.diseaseAgentTargeted(),
      ValueSetConstants.testResult(),
      ValueSetConstants.marketingAuthorizationHolder()));
    
    final TestEntry e = new TestEntry()        
        .withTg("840539006")
        .withMa("ORG-100030215")
        .withTr("260373001");
    
    ValueSetValidationResult res = v.validate(e);
    Assert.assertTrue(res.isSuccess());
    Assert.assertEquals(3, res.getChildren().size());
    
    Eudgc dgc = new Eudgc();
    dgc.setT(Arrays.asList(e));
    res = v.validate(dgc);

    Assert.assertTrue(res.isSuccess());    
    Assert.assertEquals(1, res.getChildren().size());
    Assert.assertEquals("t[0]", res.getChildren().get(0).getPropertyName());
  }
  
  @Test
  public void testRecoveryEntryOK() throws Exception {    
    final DefaultValueSetValidator v = new DefaultValueSetValidator(Arrays.asList(
      ValueSetConstants.diseaseAgentTargeted()));
    
    final RecoveryEntry e = new RecoveryEntry()        
        .withTg("840539006");
    
    ValueSetValidationResult res = v.validate(e);
    Assert.assertTrue(res.isSuccess());
    Assert.assertEquals(1, res.getChildren().size());
    
    Eudgc dgc = new Eudgc();
    dgc.setR(Arrays.asList(e));
    res = v.validate(dgc);

    Assert.assertTrue(res.isSuccess());    
    Assert.assertEquals(1, res.getChildren().size());
    Assert.assertEquals("r[0]", res.getChildren().get(0).getPropertyName());
  }
}
