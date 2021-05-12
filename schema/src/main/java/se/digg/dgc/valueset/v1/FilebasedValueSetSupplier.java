/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.valueset.v1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * A file based {@link ValueSet} {@link Supplier} that can be used for the {@link ReloadableValueSet} class.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class FilebasedValueSetSupplier extends AbstractValueSetSupplier<File> {

  /**
   * Constructor.
   * 
   * @param resource
   *          the path to the file containing the JSON representation of the value set
   * @param refreshRate
   *          the refresh rate (how often should the file be re-read?)
   * @throws IOException
   *           if the file can not be read or if the JSON can not be successfully parsed
   */
  public FilebasedValueSetSupplier(final File resource, final Duration refreshRate) throws IOException {
    super(resource, refreshRate);
  }

  /** {@inheritDoc} */
  @Override
  protected ValueSet getValueSet(final File resource) throws IOException {
    try (FileInputStream fis = new FileInputStream(resource)) {
      return new ValueSet(fis);
    }
  }

  /** {@inheritDoc} */
  @Override
  protected String getResourceString(final File resource) {
    return resource.getAbsolutePath();
  }
  
}
