/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.valueset.v1;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


/**
 * Test cases for FilebasedValueSetSupplier.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class FilebasedValueSetSupplierTest {

  /** The JSON mapper. */
  private static ObjectMapper jsonMapper = new ObjectMapper();

  static {
    jsonMapper.registerModule(new JavaTimeModule());
    jsonMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }

  @Test
  public void testSupplier() throws Exception {
    final Path path = Files.createTempFile("", ".tmp");

    try {
      final ValueSetValue vsv = new ValueSetValue();
      vsv.setDisplay("Display");
      vsv.setLang("en");
      vsv.setVersion("1");
      vsv.setSystem("System");
      vsv.setActive(true);
      Map<String, ValueSetValue> values = new HashMap<>();
      values.put("1234", vsv);
      
      final LocalDate firstDate = LocalDate.parse("2021-01-01");
      final LocalDate secondDate = LocalDate.parse("2021-02-02");
      final ValueSet valueSet = new ValueSet("value-set", firstDate, values);
      
      final File file = path.toFile();
      
      jsonMapper.writeValue(file, valueSet);
      
      final FilebasedValueSetSupplier supplier = new FilebasedValueSetSupplier(file, Duration.ofMillis(500L));
      
      // Should return initial value (no re-loading has been done).
      Assert.assertEquals(firstDate, supplier.get().getDate());
      Assert.assertEquals(firstDate, supplier.get().getDate());

      // Update file
      valueSet.setDate(secondDate);
      jsonMapper.writeValue(file, valueSet);
      
      // Wait for reload
      Thread.sleep(500L);
      
      // Should get new value
      Assert.assertEquals(secondDate, supplier.get().getDate());
      
      // Remove file
      Files.deleteIfExists(path);
      
      // No reloading has occured
      Assert.assertEquals(secondDate, supplier.get().getDate());
      
      // Wait for reload
      Thread.sleep(500L);
      
      // File can't be read, so we should get the cache
      Assert.assertEquals(secondDate, supplier.get().getDate());
    }
    finally {
      if (path != null) {
        Files.deleteIfExists(path);
      }
    }
  }

}
