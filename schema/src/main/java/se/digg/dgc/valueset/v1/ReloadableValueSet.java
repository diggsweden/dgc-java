/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.valueset.v1;

import java.time.LocalDate;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Since value sets may change over time and we really don't want to have to re-compile our programs everytime a value
 * set changes, the {@code ReloadableValueSet} class can be useful. The class extends {@link ValueSet} but relies on a
 * {@link Supplier} to get the value set data.
 * <p>
 * This supplier typically reads the value set from a JSON configuration file holding the data, or from a resource
 * accessed over HTTP(S).
 * </p>
 * <p>
 * The implementation of the {@link Supplier} decides how often the value set data is reloaded, and the implementation
 * is responsible of caching the current set of data.
 * </p>
 * <p>
 * <b>Note:</b>The {@link Supplier#get()} method MUST NEVER return {@code null}. If a download fails it should return
 * data from its cache, and if the cache is empty (download has never succeeded) a {@link RuntimeException} must be
 * thrown.
 * </p>
 * 
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class ReloadableValueSet extends ValueSet {

  /** The value set supplier. */
  private final Supplier<ValueSet> supplier;

  /**
   * Constructor.
   * 
   * @param supplier
   *          the ValueSet supplier
   */
  public ReloadableValueSet(final Supplier<ValueSet> supplier) {
    this.supplier = supplier;
  }

  /** {@inheritDoc} */
  @Override
  public String getId() {
    return this.supplier.get().getId();
  }

  /**
   * Not allowed to call - will throw {@link IllegalArgumentException}.
   */
  @Override
  public void setId(final String id) {
    throw new IllegalArgumentException("setId not permitted for " + this.getClass().getSimpleName());
  }

  /** {@inheritDoc} */
  @Override
  public LocalDate getDate() {
    return this.supplier.get().getDate();
  }

  /**
   * Not allowed to call - will throw {@link IllegalArgumentException}.
   */
  @Override
  public void setDate(final LocalDate date) {
    throw new IllegalArgumentException("setDate not permitted for " + this.getClass().getSimpleName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, ValueSetValue> getValues() {
    return this.supplier.get().getValues();
  }

  /**
   * Not allowed to call - will throw {@link IllegalArgumentException}.
   */
  @Override
  public void setValues(final Map<String, ValueSetValue> values) {
    throw new IllegalArgumentException("setValues not permitted for " + this.getClass().getSimpleName());
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    final ValueSet v = this.supplier.get();
    return String.format("RelodableValueSet [id='%s', date='%s', values=%s]", v.getId(), v.getDate(), v.getValues());
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return this.supplier.get().hashCode();
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(final Object obj) {
    return this.supplier.get().equals(obj);
  }

}
