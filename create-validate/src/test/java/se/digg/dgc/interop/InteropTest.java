/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.interop;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Executes test cases collected from <a href=
 * "https://github.com/eu-digital-green-certificates/dgc-testdata">https://github.com/eu-digital-green-certificates/dgc-testdata</a>.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
@RunWith(Parameterized.class)
public class InteropTest {

  /** Directory where we have the test files. */
  private static final String BASEDIR = "src/test/resources/interop/eu";

  /** Name of test. */
  private final String testName;

  /** Path to test file. */
  private final String testFile;

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {

        // Bulgaria
        { "BG-1", "BG/1.json" },

        // Romania
        { "RO-1", "RO/1.json" },
        { "RO-2", "RO/2.json" },

        // Austria
        { "AT-1", "AT/1.json" },
        { "AT-2", "AT/2.json" },
        // AT has a bug where they use millis instead of seconds for timestamps ...
        // { "AT-3", "AT/3.json" },
        // { "AT-4", "AT/4.json" },

        // Spain
        { "ES-1101", "ES/1101.json" },

        // Bad testdata - Base64 instead of Hex
        // { "ES-1201", "ES/1201.json" },
        // { "ES-1202", "ES/1202.json" },
        // { "ES-1203", "ES/1203.json" },
        { "ES-2001", "ES/2001.json" },
        { "ES-2002", "ES/2002.json" },
        { "ES-201", "ES/201.json" },
        { "ES-202", "ES/202.json" },
        { "ES-501", "ES/501.json" },
        { "ES-502", "ES/502.json" },
        { "ES-503", "ES/503.json" },

        // Croatia
        // Fails because the expiration time is missing from the CWT
        // Also, the iss-field of the CWT is wrong.
        // { "HR-1", "HR/1.json" },
        // { "HR-2", "HR/2.json" },
        // { "HR-3", "HR/3.json" },

        // Iceland
        { "IS-1", "IS/1.json" },

        // Greece
        // Fails because the expiration time is missing from the CWT
        { "GR-1", "GR/1.json" },
        { "GR-2", "GR/2.json" },

        // Sweden (this library)
        { "SE-1", "SE/1.json" },
        { "SE-2", "SE/2.json" },
        { "SE-3", "SE/3.json" },
        { "SE-4", "SE/4.json" },
        { "SE-5", "SE/5.json" },
        { "SE-6", "SE/6.json" }
    });
  }

  /**
   * Constructor.
   * 
   * @param testName
   *          name of test
   * @param testFile
   *          test file
   */
  public InteropTest(final String testName, final String testFile) {
    this.testName = testName;
    this.testFile = BASEDIR + "/" + testFile;
  }

  /**
   * Executes the test.
   * 
   * @throws Exception
   *           for errors
   */
  @Test
  public void validate() throws Exception {
    final TestStatement testStatement = DGCTestDataVerifier.getTestStatement(this.testFile);
    DGCTestDataVerifier.validate(this.testName, testStatement);
  }

}
