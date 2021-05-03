/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.valueset.v1;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A file based {@link ValueSet} {@link Supplier} that can be used for the {@link ReloadableValueSet} class.
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class FilebasedValueSetSupplier implements Supplier<ValueSet> {

  /** Logger */
  private static final Logger log = LoggerFactory.getLogger(FilebasedValueSetSupplier.class);

  /** Path to the file that contains the value set (in JSON). */
  private final String filePath;

  /** Tells how often the value set should be updated (by re-reading the file). */
  private final Duration refreshRate;

  /** The cached value set. */
  private ValueSet cache;

  /** The time the cache was last updated. */
  private Instant lastUpdate;

  /**
   * Constructor.
   * 
   * @param filePath
   *          the path to the file containing the JSON representation of the value set
   * @param refreshRate
   *          the refresh rate (how often should the file be re-read?)
   * @throws IOException
   *           if the file can not be read or if the JSON can not be successfully parsed
   */
  public FilebasedValueSetSupplier(final String filePath, final Duration refreshRate) throws IOException {
    this.filePath = filePath;
    this.refreshRate = refreshRate;
    this.update();
  }

  /** {@inheritDoc} */
  @Override
  public synchronized ValueSet get() {
    if (this.needsUpdate()) {
      try {
        this.update();
      }
      catch (final IOException e) {
        log.error("Failed to update DGC ValueSet '{}' from {} - Using cache",
          this.cache.getId(), this.filePath, e);
      }
    }
    return this.cache;
  }

  /**
   * Predicate that tells whether it is time to update the cache.
   * 
   * @return true for update and false otherwise
   */
  private boolean needsUpdate() {
    if (this.lastUpdate == null) {
      return true;
    }
    return Instant.now().minus(this.refreshRate).isAfter(this.lastUpdate);
  }

  /**
   * Updates the cached {@link ValueSet} by re-reading the file.
   * 
   * @throws IOException
   *           for read or parse errors
   */
  private void update() throws IOException {
    try (FileInputStream fis = new FileInputStream(this.filePath)) {
      final ValueSet valueSet = new ValueSet(fis);
      this.cache = valueSet;
      this.lastUpdate = Instant.now();
      log.debug("ValueSet '{}' was updated - date/version is: {}", this.cache.getId(), this.cache.getDate());
    }
  }
}
