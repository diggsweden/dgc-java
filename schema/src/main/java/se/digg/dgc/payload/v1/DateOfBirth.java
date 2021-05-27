/*
 * MIT License
 *
 * Copyright 2021 Myndigheten för digital förvaltning (DIGG)
 */
package se.digg.dgc.payload.v1;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * A representation of a date of birth according to the schema (where YYYY-MM-DD, YYYY-MM and YYYY) is allowed.
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Henrik Bengtsson (extern.henrik.bengtsson@digg.se)
 * @author Henric Norlander (extern.henric.norlander@digg.se)
 */
public class DateOfBirth {

  /** A LocalDate is used for all cases where a complete date is used. */
  private LocalDate dob;

  /** If this object doesn't hold a complete date, this field holds the year. */
  private Year year;

  /** If this object doesn't hold a complete date, this field holds the month (if available). */
  private Month month;

  /**
   * Constructor taking a string representation of a date of birth.
   *
   * @param dob
   *          the string representation of the date of birth
   * @throws DateTimeException
   *           for parse errors
   */
  public DateOfBirth(final String dob) throws DateTimeException {

    try {
      this.dob = LocalDate.parse(dob);
    }
    catch (final DateTimeParseException e) {
      // OK, it's a odd dob that isn't complete.
      final String[] parts = dob.split("-");
      if (parts.length > 3) {
        throw new DateTimeException("Invalid date of birth - " + dob);
      }

      this.year = Year.parse(parts[0].trim());
      if (parts.length > 1) {
        if ("XX".equalsIgnoreCase(parts[1].trim())) {
          // OK, this is not according to the schema, but we are forgiving ...
          this.month = null;
        }
        else {
          try {
            this.month = Month.of(Integer.parseInt(parts[1]));
          }
          catch (final NumberFormatException n) {
            throw new DateTimeException("Invalid month", n);
          }
        }
        if (parts.length == 3 && !"XX".equalsIgnoreCase(parts[2].trim())) {
          throw new DateTimeException("Invalid date of birth - " + dob);
        }
      }
    }
  }

  /**
   * Constructor taking a {@link LocalDate}.
   *
   * @param dob
   *          the date of birth
   */
  public DateOfBirth(final LocalDate dob) {
    this.dob = Optional.ofNullable(dob).orElseThrow(() -> new IllegalArgumentException("dob must not be null"));
  }

  /**
   * A predicate that tells whether this date is "complete", meaning YYYY-MM-DD.
   *
   * @return true if this is a complete date and false otherwise
   */
  public boolean isCompleteDate() {
    return this.dob != null;
  }

  /**
   * Gets the date of birth as a {@link LocalDate} object.
   *
   * @return a LocalDate object or null if this object doesn't represent a complete date
   */
  public LocalDate asLocalDate() {
    return this.dob;
  }

  /**
   * Gets the year.
   *
   * @return the year
   */
  public Year getYear() {
    return this.dob != null ? Year.from(this.dob) : this.year;
  }

  /**
   * Gets the month (if available).
   *
   * @return the month, or null if this is not available
   */
  public Month getMonth() {
    return this.dob != null ? Month.from(this.dob) : this.month;
  }

  /**
   * Gets the day of month (if available).
   *
   * @return the day of month, or null if this is not available
   */
  public Integer getDayOfMonth() {
    return this.dob != null ? this.dob.getDayOfMonth() : null;
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (this.dob == null ? 0 : this.dob.hashCode());
    result = prime * result + (this.month == null ? 0 : this.month.hashCode());
    result = prime * result + (this.year == null ? 0 : this.year.hashCode());
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    return this.toString().equals(obj.toString());
  }

  /**
   * Returns the date representation (YYYY-MM-DD, YYYY-MM or YYYY).
   */
  @Override
  public String toString() {
    if (this.dob != null) {
      return this.dob.toString();
    }
    else if (this.year != null && this.month != null) {
      return String.format("%04d-%02d", this.year.getValue(), this.month.getValue());
    }
    else {
      return String.format("%04d", this.year.getValue());
    }
  }

}
