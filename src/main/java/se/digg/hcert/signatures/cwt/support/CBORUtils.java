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
package se.digg.hcert.signatures.cwt.support;

import java.time.Instant;

import com.upokecenter.cbor.CBORException;
import com.upokecenter.numbers.EDecimal;
import com.upokecenter.numbers.EInteger;
import com.upokecenter.numbers.ERounding;

/**
 * The CBOR library we are using have a lot of package private classes that we really need to access. Until the library
 * is fixed, we "borrow" some code from the CBOR library.
 * 
 * @author Martin Lindström (martin@litsec.se)
 */
public class CBORUtils {

  private CBORUtils() {
  }

  public static void parseAtomDateTimeString(
      String str,
      EInteger[] bigYearArray,
      int[] lf) {
    int[] d = ParseAtomDateTimeString(str);
    bigYearArray[0] = EInteger.FromInt32(d[0]);
    System.arraycopy(d, 1, lf, 0, 7);
  }

  private static int[] ParseAtomDateTimeString(String str) {
    boolean bad = false;
    if (str.length() < 19) {
      throw new IllegalArgumentException("Invalid date/time");
    }
    for (int i = 0; i < 19 && !bad; ++i) {
      if (i == 4 || i == 7) {
        bad |= str.charAt(i) != '-';
      }
      else if (i == 13 || i == 16) {
        bad |= str.charAt(i) != ':';
      }
      else if (i == 10) {
        bad |= str.charAt(i) != 'T';
        /*
         * lowercase t not used to separate date/time, following RFC 4287 sec. 3.3
         */ }
      else {
        bad |= str.charAt(i) < '0' || str.charAt(i) > '9';
      }
    }
    if (bad) {
      throw new IllegalArgumentException("Invalid date/time");
    }
    int year = ((str.charAt(0) - '0') * 1000) + ((str.charAt(1) - '0') * 100) +
        ((str.charAt(2) - '0') * 10) + (str.charAt(3) - '0');
    int month = ((str.charAt(5) - '0') * 10) + (str.charAt(6) - '0');
    int day = ((str.charAt(8) - '0') * 10) + (str.charAt(9) - '0');
    int hour = ((str.charAt(11) - '0') * 10) + (str.charAt(12) - '0');
    int minute = ((str.charAt(14) - '0') * 10) + (str.charAt(15) - '0');
    int second = ((str.charAt(17) - '0') * 10) + (str.charAt(18) - '0');
    int index = 19;
    int nanoSeconds = 0;
    if (index <= str.length() && str.charAt(index) == '.') {
      int icount = 0;
      ++index;
      while (index < str.length()) {
        if (str.charAt(index) < '0' || str.charAt(index) > '9') {
          break;
        }
        if (icount < 9) {
          nanoSeconds = (nanoSeconds * 10) + (str.charAt(index) - '0');
          ++icount;
        }
        ++index;
      }
      while (icount < 9) {
        nanoSeconds *= 10;
        ++icount;
      }
    }
    int utcToLocal = 0;
    if (index + 1 == str.length() && str.charAt(index) == 'Z') {
      /*
       * lowercase z not used to indicate UTC, following RFC 4287 sec. 3.3
       */
      utcToLocal = 0;
    }
    else if (index + 6 == str.length()) {
      bad = false;
      for (int i = 0; i < 6 && !bad; ++i) {
        if (i == 0) {
          bad |= str.charAt(index + i) != '-' && str.charAt(index + i) != '+';
        }
        else if (i == 3) {
          bad |= str.charAt(index + i) != ':';
        }
        else {
          bad |= str.charAt(index + i) < '0' || str.charAt(index + i) > '9';
        }
      }
      if (bad) {
        throw new IllegalArgumentException("Invalid date/time");
      }
      boolean neg = str.charAt(index) == '-';
      int tzhour = ((str.charAt(index + 1) - '0') * 10) + (str.charAt(index + 2) - '0');
      int tzminute = ((str.charAt(index + 4) - '0') * 10) + (str.charAt(index + 5) - '0');
      if (tzminute >= 60) {
        throw new IllegalArgumentException("Invalid date/time");
      }
      utcToLocal = ((neg ? -1 : 1) * (tzhour * 60)) + tzminute;
    }
    else {
      throw new IllegalArgumentException("Invalid date/time");
    }
    int[] dt = {
        year, month, day, hour, minute, second,
        nanoSeconds, utcToLocal,
    };
    if (!IsValidDateTime(dt)) {
      throw new IllegalArgumentException("Invalid date/time");
    }
    return dt;
  }

  private static boolean IsValidDateTime(int[] dateTime) {
    if (dateTime == null || dateTime.length < 8) {
      return false;
    }
    if (dateTime[1] < 1 || dateTime[1] > 12 || dateTime[2] < 1) {
      return false;
    }
    boolean leap = IsLeapYear(dateTime[0]);
    if (dateTime[1] == 4 || dateTime[1] == 6 || dateTime[1] == 9 ||
        dateTime[1] == 11) {
      if (dateTime[2] > 30) {
        return false;
      }
    }
    else if (dateTime[1] == 2) {
      if (dateTime[2] > (leap ? 29 : 28)) {
        return false;
      }
    }
    else {
      if (dateTime[2] > 31) {
        return false;
      }
    }
    return !(dateTime[3] < 0 || dateTime[4] < 0 || dateTime[5] < 0 ||
        dateTime[3] >= 24 || dateTime[4] >= 60 || dateTime[5] >= 61 ||
        dateTime[6] < 0 ||
        dateTime[6] >= 1000000000 || dateTime[7] <= -1440 ||
        dateTime[7] >= 1440);
  }

  private static boolean IsLeapYear(int yr) {
    yr %= 400;
    if (yr < 0) {
      yr += 400;
    }
    return (((yr % 4) == 0) && ((yr % 100) != 0)) || ((yr % 400) == 0);
  }

  public static EInteger GetNumberOfDaysProlepticGregorian(
      EInteger year,
      int month,
      int mday) {
    // NOTE: month = 1 is January, year = 1 is year 1
    if (month <= 0 || month > 12) {
      throw new IllegalArgumentException("month");
    }
    if (mday <= 0 || mday > 31) {
      throw new IllegalArgumentException("mday");
    }
    EInteger numDays = EInteger.FromInt32(0);
    int startYear = 1970;
    if (year.compareTo(startYear) < 0) {
      EInteger currentYear = EInteger.FromInt32(startYear - 1);
      EInteger diff = currentYear.Subtract(year);

      if (diff.compareTo(401) > 0) {
        EInteger blocks = diff.Subtract(401).Divide(400);
        numDays = numDays.Subtract(blocks.Multiply(146097));
        diff = diff.Subtract(blocks.Multiply(400));
        currentYear = currentYear.Subtract(blocks.Multiply(400));
      }

      numDays = numDays.Subtract(diff.Multiply(365));
      int decrement = 1;
      for (; currentYear.compareTo(year) > 0; currentYear = currentYear.Subtract(decrement)) {
        if (decrement == 1 && currentYear.Remainder(4).signum() == 0) {
          decrement = 4;
        }
        if (!(currentYear.Remainder(4).signum() != 0 || (currentYear.Remainder(100).signum() == 0 &&
            currentYear.Remainder(400).signum() != 0))) {
          numDays = numDays.Subtract(1);
        }
      }
      if (year.Remainder(4).signum() != 0 || (year.Remainder(100).signum() == 0 && year.Remainder(400).signum() != 0)) {
        numDays = numDays.Subtract(365 - ValueNormalToMonth[month])
          .Subtract(ValueNormalDays[month] - mday + 1);
      }
      else {
        numDays = numDays
          .Subtract(366 - ValueLeapToMonth[month])
          .Subtract(ValueLeapDays[month] - mday + 1);
      }
    }
    else {
      boolean isNormalYear = year.Remainder(4).signum() != 0 ||
          (year.Remainder(100).signum() == 0 && year.Remainder(400).signum() != 0);

      EInteger currentYear = EInteger.FromInt32(startYear);
      if (currentYear.Add(401).compareTo(year) < 0) {
        EInteger y2 = year.Subtract(2);
        numDays = numDays.Add(
          y2.Subtract(startYear).Divide(400).Multiply(146097));
        currentYear = y2.Subtract(
          y2.Subtract(startYear).Remainder(400));
      }

      EInteger diff = year.Subtract(currentYear);
      numDays = numDays.Add(diff.Multiply(365));
      EInteger eileap = currentYear;
      if (currentYear.Remainder(4).signum() != 0) {
        eileap = eileap.Add(4 - eileap.Remainder(4).ToInt32Checked());
      }
      numDays = numDays.Add(year.Subtract(eileap).Add(3).Divide(4));
      if (currentYear.Remainder(100).signum() != 0) {
        currentYear = currentYear.Add(100 -
            currentYear.Remainder(100).ToInt32Checked());
      }
      while (currentYear.compareTo(year) < 0) {
        if (currentYear.Remainder(400).signum() != 0) {
          numDays = numDays.Subtract(1);
        }
        currentYear = currentYear.Add(100);
      }
      int yearToMonth = isNormalYear ? ValueNormalToMonth[month - 1] : ValueLeapToMonth[month - 1];
      numDays = numDays.Add(yearToMonth)
        .Add(mday - 1);
    }
    return numDays;
  }

  private static EInteger FloorDiv(EInteger a, EInteger n) {
    return a.signum() >= 0 ? a.Divide(n)
        : EInteger.FromInt32(-1)
          .Subtract(
            EInteger.FromInt32(-1).Subtract(a).Divide(n));
  }

  private static EInteger FloorMod(EInteger a, EInteger n) {
    return a.Subtract(FloorDiv(a, n).Multiply(n));
  }

  public static void breakDownSecondsSinceEpoch(
      EDecimal edec,
      EInteger[] year,
      int[] lesserFields) {
    EInteger integerPart = edec.Quantize(0, ERounding.Floor)
      .ToEInteger();
    EDecimal fractionalPart = edec.Abs()
      .Subtract(EDecimal.FromEInteger(integerPart).Abs());
    int nanoseconds = fractionalPart.Multiply(1000000000)
      .ToInt32Checked();
    EInteger[] normPart = new EInteger[3];
    EInteger days = FloorDiv(
      integerPart,
      EInteger.FromInt32(86400)).Add(1);
    int secondsInDay = FloorMod(
      integerPart,
      EInteger.FromInt32(86400)).ToInt32Checked();
    GetNormalizedPartProlepticGregorian(
      EInteger.FromInt32(1970),
      1,
      days,
      normPart);
    lesserFields[0] = normPart[1].ToInt32Checked();
    lesserFields[1] = normPart[2].ToInt32Checked();
    lesserFields[2] = secondsInDay / 3600;
    lesserFields[3] = (secondsInDay % 3600) / 60;
    lesserFields[4] = secondsInDay % 60;
    lesserFields[5] = nanoseconds / 100;
    lesserFields[6] = 0;
    year[0] = normPart[0];
  }

  public static void GetNormalizedPartProlepticGregorian(
      EInteger year,
      int month,
      EInteger day,
      EInteger[] dest) {
    // NOTE: This method assumes month is 1 to 12
    if (month <= 0 || month > 12) {
      throw new IllegalArgumentException("month");
    }
    int[] dayArray = (year.Remainder(4).signum() != 0 || (year.Remainder(100).signum() == 0 && year.Remainder(400).signum() != 0))
        ? ValueNormalDays
        : ValueLeapDays;
    if (day.compareTo(100) > 0) {
      // Number of days in a 400-year block
      EInteger count = day.Divide(146097);
      day = day.Subtract(count.Multiply(146097));
      year = year.Add(count.Multiply(400));
    }
    if (day.compareTo(-101) < 0) {
      // Number of days in a 400-year block
      EInteger count = day.Abs().Divide(146097);
      day = day.Add(count.Multiply(146097));
      year = year.Subtract(count.Multiply(400));
    }
    while (true) {
      EInteger days = EInteger.FromInt32(dayArray[month]);
      if (day.signum() > 0 && day.compareTo(days) <= 0) {
        break;
      }
      if (day.compareTo(days) > 0) {
        day = day.Subtract(days);
        if (month == 12) {
          month = 1;
          year = year.Add(1);
          dayArray = (year.Remainder(4).signum() != 0 || (year.Remainder(100).signum() == 0 &&
              year.Remainder(400).signum() != 0)) ? ValueNormalDays : ValueLeapDays;
        }
        else {
          ++month;
        }
      }
      if (day.signum() <= 0) {
        --month;
        if (month <= 0) {
          year = year.Add(-1);
          month = 12;
        }
        dayArray = (year.Remainder(4).signum() != 0 || (year.Remainder(100).signum() == 0 &&
            year.Remainder(400).signum() != 0)) ? ValueNormalDays : ValueLeapDays;
        day = day.Add(dayArray[month]);
      }
    }
    dest[0] = year;
    dest[1] = EInteger.FromInt32(month);
    dest[2] = day;
  }

  public static Instant buildUpInstant(EInteger year, int[] dt) {
    EInteger dateMS = GetNumberOfDaysProlepticGregorian(
      year, dt[0], dt[1]).Multiply(EInteger.FromInt32(86400000));
    EInteger frac = EInteger.FromInt32(0);
    // System.out.println(dt[2]+","+dt[3]+","+dt[4]+","+dt[5]);
    frac = frac.Add(EInteger.FromInt32(dt[2] * 3600000 + dt[3] * 60000 + dt[4] * 1000));
    // Milliseconds
    frac = frac.Add(EInteger.FromInt32(dt[5] / 1000000));
    // Time zone offset in minutes
    frac = frac.Subtract(EInteger.FromInt32(dt[6] * 60000));
    dateMS = dateMS.Add(frac);
    if (!dateMS.CanFitInInt64()) {
      throw new CBORException("Value too big or too small for Java Instant");
    }
    return Instant.ofEpochMilli(dateMS.ToInt64Checked());
  }

  private static final int[] ValueNormalDays = {
      0, 31, 28, 31, 30, 31, 30,
      31, 31, 30,
      31, 30, 31,
  };

  private static final int[] ValueLeapDays = {
      0, 31, 29, 31, 30, 31, 30,
      31, 31, 30,
      31, 30, 31,
  };

  private static final int[] ValueNormalToMonth = {
      0, 0x1f, 0x3b, 90, 120,
      0x97, 0xb5,
      0xd4, 0xf3, 0x111, 0x130, 0x14e, 0x16d,
  };

  private static final int[] ValueLeapToMonth = {
      0, 0x1f, 60, 0x5b, 0x79,
      0x98, 0xb6,
      0xd5, 0xf4, 0x112, 0x131, 0x14f, 0x16e,
  };
}
