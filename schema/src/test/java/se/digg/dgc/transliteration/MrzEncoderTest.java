/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.transliteration;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for MrzEncoder.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class MrzEncoderTest {

  @Test
  public void testMrzEncoder() throws Exception {    
        
    Assert.assertEquals("LINDSTROEM", MrzEncoder.encode("Lindström"));
    Assert.assertEquals("KARL<AAKE", MrzEncoder.encode("Karl Åke"));
    Assert.assertEquals("KARL<AAKE", MrzEncoder.encode("Karl-Åke"));
    Assert.assertEquals("ANDRE", MrzEncoder.encode("André"));
    Assert.assertEquals("OLEARY", MrzEncoder.encode("O'Leary"));
    Assert.assertEquals("AERGE", MrzEncoder.encode("Àérge"));
    Assert.assertEquals("BOESSE", MrzEncoder.encode("BŒße"));
    Assert.assertEquals("BOESSE", MrzEncoder.encode("Bœße"));
    Assert.assertEquals("DAN", MrzEncoder.encode("Ðan"));
    Assert.assertEquals("ZLATAN", MrzEncoder.encode("Žlatan"));
  }
  
  @Test
  public void testTruncating() throws Exception {
    Assert.assertEquals("CHAYAPA<DEJTHAMRONG<KRASUANG<NILAVADHANANANDA<WARRANDYTE<WARNAMBOOL<PAPANDROPOUL", 
      MrzEncoder.encode("Chayapa Dejthamrong Krasuang Nilavadhanananda Warrandyte Warnambool Papandropoulous"));
  }
}
