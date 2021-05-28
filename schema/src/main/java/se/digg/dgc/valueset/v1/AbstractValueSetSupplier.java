/*
 * Copyright 2021 Litsec AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.digg.dgc.valueset.v1;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract {@link ValueSet} {@link Supplier} that can be used for the {@link ReloadableValueSet} class.
 * 
 * @param <T>
 *          the type of resource being handled
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public abstract class AbstractValueSetSupplier<T> implements Supplier<ValueSet> {

  /** Logger */
  private static final Logger log = LoggerFactory.getLogger(AbstractValueSetSupplier.class);

  /** The resource that contains the JSON value set. */
  private final T resource;

  /** Tells how often the value set should be updated (by re-reading the resource). */
  private final Duration refreshRate;

  /** The cached value set. */
  private ValueSet cache;

  /** The time the cache was last updated. */
  private Instant lastUpdate;

  /**
   * Constructor.
   * 
   * @param resource
   *          the resource holding the value set
   * @param refreshRate
   *          the refresh rate (how often should the file be re-read?)
   * @throws IOException
   *           if the file can not be read or if the JSON can not be successfully parsed
   */
  public AbstractValueSetSupplier(final T resource, final Duration refreshRate) throws IOException {
    this.resource = resource;
    this.refreshRate = refreshRate;
    this.update();
  }

  /** {@inheritDoc} */
  @Override
  public final synchronized ValueSet get() {
    if (this.needsUpdate()) {
      try {
        this.update();
      }
      catch (final IOException e) {
        log.error("Failed to update DCC ValueSet '{}' from {} - Using cache",
          this.cache.getId(), this.getResourceString(this.resource), e);
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
    final ValueSet valueSet = this.getValueSet(this.resource);
    if (valueSet != null) {
      this.cache = valueSet;
      this.lastUpdate = Instant.now();
      log.debug("ValueSet '{}' was updated - date/version is: {}", this.cache.getId(), this.cache.getDate());
    }
    else {
      throw new IOException("No ValueSet available");
    }
  }

  /**
   * Reads the value set from the resource.
   * 
   * @return the value set
   * @throws IOException
   *           for read errors
   */
  protected abstract ValueSet getValueSet(final T resource) throws IOException;

  /**
   * Returns a string representation of the resource being handled.
   * 
   * @return the resource string
   */
  protected abstract String getResourceString(final T resource);

}
