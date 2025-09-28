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

import static org.tools4j.time.base.TimeFactors.HOURS_PER_DAY;
import static org.tools4j.time.base.TimeFactors.MICROS_PER_SECOND;
import static org.tools4j.time.base.TimeFactors.MILLIS_PER_SECOND;
import static org.tools4j.time.base.TimeFactors.MINUTES_PER_HOUR;
import static org.tools4j.time.base.TimeFactors.NANOS_PER_SECOND;
import static org.tools4j.time.base.TimeFactors.SECONDS_PER_MINUTE;

/**
 * Defines validators for time components.
 */
public enum TimeValidator {
    /**
     * No date validation is performed and inputs are returned unchecked.
     */
    UNVALIDATED {
        @Override
        public ValidationMethod validationMethod() {
            return ValidationMethod.UNVALIDATED;
        }

        @Override
        public int validateHour(final int hour) {
            return hour;
        }

        @Override
        public int validateMinute(final int minute) {
            return minute;
        }

        @Override
        public int validateSecond(final int second) {
            return second;
        }

        @Override
        public int validateMilli(final int milli) {
            return milli;
        }

        @Override
        public int validateMicro(final int micro) {
            return micro;
        }

        @Override
        public int validateNano(final int nano) {
            return nano;
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
        public int validateHour(final int hour) {
            return isValidHour(hour) ? hour : INVALID;
        }

        @Override
        public int validateMinute(final int minute) {
            return isValidMinute(minute) ? minute : INVALID;
        }

        @Override
        public int validateSecond(final int second) {
            return isValidSecond(second) ? second : INVALID;
        }

        @Override
        public int validateMilli(final int milli) {
            return isValidMilli(milli) ? milli : INVALID;
        }

        @Override
        public int validateMicro(final int micro) {
            return isValidMicro(micro) ? micro : INVALID;
        }

        @Override
        public int validateNano(final int nano) {
            return isValidNano(nano) ? nano : INVALID;
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
        public int validateHour(final int hour) {
            if (isValidHour(hour)) {
                return hour;
            }
            throw new DateTimeException("Invalid hour, must be in [0,23] but was: " + hour);
        }

        @Override
        public int validateMinute(final int minute) {
            if (isValidMinute(minute)) {
                return minute;
            }
            throw new DateTimeException("Invalid minute, must be in [0,59] but was: " + minute);
        }

        @Override
        public int validateSecond(final int second) {
            if (isValidSecond(second)) {
                return second;
            }
            throw new DateTimeException("Invalid second, must be in [0,59] but was: " + second);
        }

        @Override
        public int validateMilli(final int milli) {
            if (isValidMilli(milli)) {
                return milli;
            }
            throw new DateTimeException("Invalid milli second, must be in [0,999] but was: " + milli);
        }

        @Override
        public int validateMicro(final int micro) {
            if (isValidMicro(micro)) {
                return micro;
            }
            throw new DateTimeException("Invalid micro second, must be in [0,999999] but was: " + micro);
        }

        @Override
        public int validateNano(final int nano) {
            if (isValidNano(nano)) {
                return nano;
            }
            throw new DateTimeException("Invalid nano second, must be in [0,999999999] but was: " + nano);
        }
    };

    /** Special result value returned by {@link TimeValidator#INVALIDATE_RESULT} in case of invalid input values*/
    public static final int INVALID = -1;
    /**
     * Special result value returned by {@link TimeValidator#INVALIDATE_RESULT} in case of valid input values for
     * multi component validate methods such as {@link #validateTime(int, int, int)}.
     */
    public static final int VALID = 0;
    public static final int HOUR_MIN = 0;
    public static final int HOUR_MAX = HOURS_PER_DAY - 1;
    public static final int MINUTE_MIN = 0;
    public static final int MINUTE_MAX = MINUTES_PER_HOUR - 1;
    public static final int SECOND_MIN = 0;
    public static final int SECOND_MAX = SECONDS_PER_MINUTE - 1;
    public static final int MILLI_MIN = 0;
    public static final int MILLI_MAX = MILLIS_PER_SECOND - 1;
    public static final int MICRO_MIN = 0;
    public static final int MICRO_MAX = MICROS_PER_SECOND - 1;
    public static final int NANO_MIN = 0;
    public static final int NANO_MAX = NANOS_PER_SECOND - 1;

    public static TimeValidator valueOf(final ValidationMethod validationMethod) {
        return validationMethod.timeValidator();
    }

    public static boolean isValidHour(final int hour) {
        return HOUR_MIN <= hour & hour <= HOUR_MAX;
    }

    public static boolean isValidMinute(final int minute) {
        return MINUTE_MIN <= minute & minute <= MINUTE_MAX;
    }

    public static boolean isValidSecond(final int second) {
        return SECOND_MIN <= second & second <= SECOND_MAX;
    }

    public static boolean isValidMilli(final int milli) {
        return MILLI_MIN <= milli & milli <= MILLI_MAX;
    }

    public static boolean isValidMicro(final int micro) {
        return MICRO_MIN <= micro & micro <= MICRO_MAX;
    }

    public static boolean isValidNano(final int nano) {
        return NANO_MIN <= nano & nano <= NANO_MAX;
    }

    public static boolean isValidTime(final int hour, final int minute) {
        return isValidHour(hour) & isValidMinute(minute);
    }

    public static boolean isValidTime(final int hour, final int minute, final int second) {
        return isValidTime(hour, minute) & isValidSecond(second);
    }

    public static boolean isValidTimeWithMillis(final int hour, final int minute, final int second,
                                                final int milli) {
        return isValidTime(hour, minute, second) & isValidMilli(milli);
    }

    public static boolean isValidTimeWithMicros(final int hour, final int minute, final int second,
                                                final int micro) {
        return isValidTime(hour, minute, second) & isValidMicro(micro);
    }

    public static boolean isValidTimeWithNanos(final int hour, final int minute, final int second,
                                               final int nano) {
        return isValidTime(hour, minute, second) & isValidNano(nano);
    }

    abstract public ValidationMethod validationMethod();

    abstract public int validateHour(int hour);

    abstract public int validateMinute(int minute);

    abstract public int validateSecond(int second);

    abstract public int validateMilli(int milli);

    abstract public int validateMicro(int micro);

    abstract public int validateNano(int nano);

    public int validateTime(final int hour, final int minute) {
        return validateHour(hour) != INVALID & validateMinute(minute) != INVALID ? VALID : INVALID;
    }

    public int validateTime(final int hour, final int minute, final int second) {
        return validateTime(hour, minute) != INVALID & validateSecond(second) != INVALID ? VALID : INVALID;
    }

    public int validateTimeWithMillis(final int hour, final int minute, final int second,
                                      final int milli) {
        return validateTime(hour, minute, second) != INVALID & validateMilli(milli) != INVALID ? VALID : INVALID;
    }

    public int validateTimeWithMicros(final int hour, final int minute, final int second,
                                      final int micro) {
        return validateTime(hour, minute, second) != INVALID & validateMicro(micro) != INVALID ? VALID : INVALID;
    }

    public int validateTimeWithNanos(final int hour, final int minute, final int second,
                                             final int nano) {
        return validateTime(hour, minute, second) != INVALID & validateNano(nano) != INVALID ? VALID : INVALID;
    }

}
