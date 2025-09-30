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
package org.tools4j.time.validate;

import java.time.DateTimeException;

/**
 * Defines validators for date components.
 */
public enum DateValidator {
    /**
     * No date validation is performed and inputs are returned unchecked.
     */
    UNVALIDATED {
        @Override
        public ValidationMethod validationMethod() {
            return ValidationMethod.UNVALIDATED;
        }

        @Override
        public int validateYear(final long year) {
            return (int)year;
        }

        @Override
        public int validateYear(final int year) {
            return year;
        }

        @Override
        public int validateMonth(final int month) {
            return month;
        }

        @Override
        public int validateDay(final int year, final int month, final int day) {
            return day;
        }
    },
    /**
     * Date validation is performed and failure is signalled by the special {@link #INVALID} result value.
     */
    INVALIDATE_RESULT {
        @Override
        public ValidationMethod validationMethod() {
            return ValidationMethod.INVALIDATE_RESULT;
        }

        @Override
        public int validateYear(final long year) {
            return DateValidator.isValidYear(year) ? (int)year : INVALID;
        }

        @Override
        public int validateYear(final int year) {
            return DateValidator.isValidYear(year) ? year : INVALID;
        }

        @Override
        public int validateMonth(final int month) {
            return DateValidator.isValidMonth(month) ? month : INVALID;
        }

        @Override
        public int validateDay(final int year, final int month, final int day) {
            return DateValidator.isValidDate(year, month, day) ? day : INVALID;
        }
    },
    /**
     * Date validation is performed and failure is signalled through an {@link DateTimeException}.
     */
    THROW_EXCEPTION {
        @Override
        public ValidationMethod validationMethod() {
            return ValidationMethod.THROW_EXCEPTION;
        }

        @Override
        public int validateYear(final long year) {
            if (DateValidator.isValidYear(year)) {
                return (int)year;
            }
            throw new DateTimeException("Invalid year, must be in [1,9999] but was: " + year);
        }

        @Override
        public int validateYear(final int year) {
            if (DateValidator.isValidYear(year)) {
                return year;
            }
            throw new DateTimeException("Invalid year, must be in [1,9999] but was: " + year);
        }

        @Override
        public int validateMonth(final int month) {
            if (DateValidator.isValidMonth(month)) {
                return month;
            }
            throw new DateTimeException("Invalid month, must be in [1,12] but was: " + month);
        }

        @Override
        public int validateDay(final int year, final int month, final int day) {
            validateYear(year);
            validateMonth(month);
            if (isValidDay(year, month, day)) {
                return day;
            }
            throw new DateTimeException("Invalid day in date: " + year + "/" + month + "/" + day);
        }
    };

    /** Special result value returned by {@link DateValidator#INVALIDATE_RESULT} in case of invalid input values*/
    public static final int INVALID = -1;
    public static final long INVALID_EPOCH = Long.MIN_VALUE;
    public static final int YEAR_MIN = 1;
    public static final int YEAR_MAX = 9999;
    public static final int MONTH_MIN = 1;
    public static final int MONTH_MAX = 12;
    public static final int DAY_MIN = 1;
    public static final int DAY_MAX = 31;

    public static DateValidator valueOf(final ValidationMethod validationMethod) {
        return validationMethod.dateValidator();
    }

    public static boolean isLeapYear(final int year) {
        //see IsoChronology.INSTANCE.isLeapYear
        return ((year & 3) == 0) && ((year % 100) != 0 || (year % 400) == 0);
    }

    public static boolean isValidYear(final long year) {
        return YEAR_MIN <= year & year <= YEAR_MAX;
    }

    public static boolean isValidYear(final int year) {
        return YEAR_MIN <= year & year <= YEAR_MAX;
    }

    public static boolean isValidMonth(final int month) {
        return MONTH_MIN <= month & month <= MONTH_MAX;
    }

    public static boolean isValidDate(final int year, final int month, final int day) {
        return isValidYear(year) && isValidMonth(month) && isValidDay(year, month, day);
    }

    abstract public ValidationMethod validationMethod();

    abstract public int validateYear(long year);

    abstract public int validateYear(int year);

    abstract public int validateMonth(int month);

    abstract public int validateDay(int year, int month, int day);

    //PRECONDITION: valid year and month
    private static boolean isValidDay(final int year, final int month, final int day) {
        if (day >= DAY_MIN) {
            if (day <= 28) {
                return true;
            }
            if (day <= DAY_MAX) {
                if (month != 2) {
                    return (day <= 30) || ((month < 8) ^ ((month & 0x1) == 0));
                }
                return day == 29 && isLeapYear(year);
            }
        }
        return false;
    }
}
