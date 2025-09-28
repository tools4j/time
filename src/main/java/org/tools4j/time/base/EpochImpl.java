/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017-2025 tools4j.org (Marco Terzer)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.tools4j.time.base;

import org.tools4j.time.base.Epoch.Default;
import org.tools4j.time.pack.DatePacker;
import org.tools4j.time.validate.DateValidator;
import org.tools4j.time.validate.ValidationMethod;

import java.time.chrono.IsoChronology;
import java.util.Objects;

import static java.lang.Math.floorDiv;
import static java.lang.Math.floorMod;
import static org.tools4j.time.base.Allocation.Type.RESULT;

record EpochImpl(ValidationMethod validationMethod) implements Default {

    /**
     * The number of days in a 400-year cycle.
     */
    private static final int DAYS_PER_CYCLE = 146097;

    /**
     * The number of days from year zero to year 1970.
     * There are five 400 year cycles from year zero to 2000.
     * There are 7 leap years from 1970 to 2000.
     */
    private static final long DAYS_0000_TO_1970 = (DAYS_PER_CYCLE * 5L) - (30L * 365L + 7L);

    private static final EpochImpl[] INSTANCES = initInstances();

    EpochImpl(final ValidationMethod validationMethod) {
        this.validationMethod = Objects.requireNonNull(validationMethod);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public long toEpochDay(final int year, final int month, final int day) {
        //see LocalDate.toEpochDay
        if (DateValidator.INVALID == validationMethod.dateValidator().validateDay(year, month, day)) {
            return INVALID_EPOCH;
        }
        final long y = year;
        final long m = month;
        long total = 0;
        total += 365 * y;
        if (y >= 0) {
            total += (y + 3) / 4 - (y + 99) / 100 + (y + 399) / 400;
        } else {
            total -= y / -4 - y / -100 + y / -400;
        }
        total += ((367 * m - 362) / 12);
        total += day - 1;
        if (m > 2) {
            total--;
            if (!IsoChronology.INSTANCE.isLeapYear(year)) {
                total--;
            }
        }
        return total - DAYS_0000_TO_1970;
    }

    public int fromEpochDay(final long daysSinceEpoch, final DatePacker datePacker) {
        //see LocalDate.ofEpochDay(..)
        long zeroDay = daysSinceEpoch + DAYS_0000_TO_1970;
        // find the march-based year
        zeroDay -= 60;  // adjust to 0000-03-01 so leap day is at end of four-year cycle
        long adjust = 0;
        if (zeroDay < 0) {
            // adjust negative years to positive for calculation
            long adjustCycles = (zeroDay + 1) / DAYS_PER_CYCLE - 1;
            adjust = adjustCycles * 400;
            zeroDay += -adjustCycles * DAYS_PER_CYCLE;
        }
        long yearEst = (400 * zeroDay + 591) / DAYS_PER_CYCLE;
        long doyEst = zeroDay - (365 * yearEst + yearEst / 4 - yearEst / 100 + yearEst / 400);
        if (doyEst < 0) {
            // fix estimate
            yearEst--;
            doyEst = zeroDay - (365 * yearEst + yearEst / 4 - yearEst / 100 + yearEst / 400);
        }
        yearEst += adjust;  // reset any negative year
        final int marchDoy0 = (int) doyEst;

        // convert march-based values back to january-based
        final int marchMonth0 = (marchDoy0 * 5 + 2) / 153;
        final int month = (marchMonth0 + 2) % 12 + 1;
        final int day = marchDoy0 - (marchMonth0 * 306 + 5) / 10 + 1;
        yearEst += marchMonth0 / 10;

        // check year now we are certain it is correct
        final int year = validationMethod().dateValidator().validateYear(yearEst);
        if (year == DateValidator.INVALID & validationMethod() == ValidationMethod.INVALIDATE_RESULT) {
            return DatePacker.INVALID;
        }
        return datePacker.pack(year, month, day);
    }

    @Allocation(RESULT)
    @Override
    public String toString() {
        return "Epoch." + validationMethod();
    }

    static EpochImpl valueOf(final ValidationMethod validationMethod) {
        return INSTANCES[validationMethod.ordinal()];
    }

    static int divMod(final int value, final int divisor, final int moduloDivisor) {
        return floorMod(floorDiv(value, divisor), moduloDivisor);
    }

    static int divMod(final long value, final long divisor, final int moduloDivisor) {
        return floorMod(floorDiv(value, divisor), moduloDivisor);
    }

    private static EpochImpl[] initInstances() {
        final EpochImpl[] instances = new EpochImpl[ValidationMethod.count()];
        for (int ordinal = 0; ordinal < instances.length; ordinal++) {
            instances[ordinal] = new EpochImpl(ValidationMethod.valueByOrdinal(ordinal));
        }
        return instances;
    }
}
