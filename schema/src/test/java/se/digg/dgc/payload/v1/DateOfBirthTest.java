/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.payload.v1;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for DateOfBirth.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class DateOfBirthTest {
  
  @Test
  public void testCompleteYear() {
    final String s = "1969-11-21";
    final DateOfBirth dob = new DateOfBirth(s);
    Assert.assertTrue(dob.isCompleteDate());
    Assert.assertEquals(LocalDate.parse(s), dob.asLocalDate());
    Assert.assertEquals(s, dob.toString());
    
    final DateOfBirth dob2 = new DateOfBirth(LocalDate.parse(s));
    Assert.assertTrue(dob2.isCompleteDate());
    Assert.assertEquals(LocalDate.parse(s), dob2.asLocalDate());
    Assert.assertEquals(s, dob2.toString());
  }
  
  @Test(expected = DateTimeException.class)
  public void testInvalidCompleteYear() {
    new DateOfBirth("1967-13-99");
  }

  @Test
  public void testYearOnly() {
    final DateOfBirth dob = new DateOfBirth("1969");
    Assert.assertFalse(dob.isCompleteDate());
    Assert.assertEquals(1969, dob.getYear().getValue());
    Assert.assertNull(dob.getMonth());
    Assert.assertNull(dob.getDayOfMonth());
    Assert.assertNull(dob.asLocalDate());
    Assert.assertEquals("1969", dob.toString());
  }
  
  @Test
  public void testYearOnlyWithXX() {
    final DateOfBirth dob = new DateOfBirth("1969-XX-XX");
    Assert.assertFalse(dob.isCompleteDate());
    Assert.assertEquals(1969, dob.getYear().getValue());
    Assert.assertNull(dob.getMonth());
    Assert.assertNull(dob.getDayOfMonth());
    Assert.assertNull(dob.asLocalDate());
    Assert.assertEquals("1969", dob.toString());
  }
  
  @Test
  public void testYearAndMonth() {
    final DateOfBirth dob = new DateOfBirth("1969-02");
    Assert.assertFalse(dob.isCompleteDate());
    Assert.assertEquals(1969, dob.getYear().getValue());
    Assert.assertEquals(Month.FEBRUARY, dob.getMonth());
    Assert.assertNull(dob.getDayOfMonth());
    Assert.assertNull(dob.asLocalDate());
    Assert.assertEquals("1969-02", dob.toString());
  }
  
  @Test
  public void testYearAndMonthWithXX() {
    final DateOfBirth dob = new DateOfBirth("1969-02-XX");
    Assert.assertFalse(dob.isCompleteDate());
    Assert.assertEquals(1969, dob.getYear().getValue());
    Assert.assertEquals(Month.FEBRUARY, dob.getMonth());
    Assert.assertNull(dob.getDayOfMonth());
    Assert.assertNull(dob.asLocalDate());
    Assert.assertEquals("1969-02", dob.toString());
  }
  
  @Test(expected = DateTimeException.class)
  public void testInvalidMonth() {
    new DateOfBirth("1967-13");
  }
  
  public void testUnknownDob() {
    final DateOfBirth dob = new DateOfBirth("");
    Assert.assertFalse(dob.isCompleteDate());
    Assert.assertNull(dob.getYear());
    Assert.assertNull(dob.getMonth());
    Assert.assertNull(dob.getDayOfMonth());
    Assert.assertNull(dob.asLocalDate());
    Assert.assertEquals("unknown", dob.toString());
  }
  
}
