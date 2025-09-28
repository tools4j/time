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
package org.tools4j.time.format;

import java.util.function.Consumer;

/**
 * Constants for common time formats of fixed length. The format defines the ordering
 * of the time components and the position of the separator characters (if any), but not
 * the type of separator character.
 */
public enum TimeFormat {
    /** Time format without separator char such as 1315 for a quarter past 1pm */
    HHMM(0, 2, -1, -1, -1, -1, -1, 0, 4),
    /** Time format without separator chars such as 131501 for 15 minutes and one second past 1pm */
    HHMMSS(0, 2, 4, -1, -1, -1, -1, 0, 6),
    /** Time format without separator chars such as 131501999 for 15 minutes, one second and 999 milliseconds past 1pm */
    HHMMSSMMM(0, 2, 4, 6, -1, -1, -1, 3, 9),
    /** Time format without separator chars such as 131501999999 for 15 minutes, one second and 999999 microseconds past 1pm */
    HHMMSSUUUUUU(0, 2, 4, 6, -1, -1, -1, 6, 12),
    /** Time format without separator chars such as 131501999999999 for 15 minutes, one second and 999999999 nanoseconds past 1pm */
    HHMMSSNNNNNNNNN(0, 2, 4, 6, -1, -1, -1, 9, 15),
    /** Time format with separator char such as 13:15 for a quarter past 1pm */
    HH_MM(0, 3, -1, -1, 2, -1, -1, 0, 5),
    /** Time format with separator chars such as 13:15:01 for 15 minutes and one second past 1pm */
    HH_MM_SS(0, 3, 6, -1, 2, 5, -1, 0, 8),
    /** Time format with separator chars such as 13:15:01.999 for 15 minutes, one second and 999 milliseconds past 1pm */
    HH_MM_SS_MMM(0, 3, 6, 9, 2, 5, 8, 3, 12),
    /** Time format with separator chars such as 13:15:01.999999 for 15 minutes, one second and 999999 microseconds past 1pm */
    HH_MM_SS_UUUUUU(0, 3, 6, 9, 2, 5, 8, 6, 15),
    /** Time format with separator chars such as 13:15:01.999999999 for 15 minutes, one second and 999999999 nanoseconds past 1pm */
    HH_MM_SS_NNNNNNNNN(0, 3, 6, 9, 2, 5, 8, 9, 18),
    /** Time format without separator chars but with a fraction separator such as 131501.999 for 15 minutes, one second and 999 milliseconds past 1pm */
    HHMMSS_MMM(0, 2, 4, 7, -1, -1, 6, 3, 10),
    /** Time format without separator chars but with a fraction separator such as 131501.999999 for 15 minutes, one second and 999999 microseconds past 1pm */
    HHMMSS_UUUUUU(0, 2, 4, 7, -1, -1, 6, 6, 13),
    /** Time format without separator chars but with a fraction separator such as 131501.999999999 for 15 minutes, one second and 999999999 nanoseconds past 1pm */
    HHMMSS_NNNNNNNNN(0, 2, 4, 7, -1, -1, 6, 9, 16);

    private final int offsetHour;
    private final int offsetMinute;
    private final int offsetSecond;
    private final int offsetFraction;
    private final int offsetSeparatorMinute;
    private final int offsetSeparatorSecond;
    private final int offsetSeparatorFraction;
    private final int fractionLength;
    private final int length;

    TimeFormat(final int offsetHour,
               final int offsetMinute,
               final int offsetSecond,
               final int offsetFraction,
               final int offsetSeparatorMinute,
               final int offsetSeparatorSecond,
               final int offsetSeparatorFraction,
               final int fractionLength,
               final int length) {
        this.offsetHour = offsetHour;
        this.offsetMinute = offsetMinute;
        this.offsetSecond = offsetSecond;
        this.offsetFraction = offsetFraction;
        this.offsetSeparatorMinute = offsetSeparatorMinute;
        this.offsetSeparatorSecond = offsetSeparatorSecond;
        this.offsetSeparatorFraction = offsetSeparatorFraction;
        this.fractionLength = fractionLength;
        this.length = length;
    }

    /** @return the start position of the hour in the time String (zero based) */
    public final int offsetHour() {
        return offsetHour;
    }

    /** @return the start position of the minute in the time String (zero based) */
    public final int offsetMinute() {
        return offsetMinute;
    }

    /** @return the start position of the second in the time String (zero based) */
    public final int offsetSecond() {
        return offsetSecond;
    }

    /** @return the start position of the fractional part in the time String (zero based) */
    public final int offsetFraction() {
        return offsetFraction;
    }

    /**
     * @return  the position of the minute separator character in the time String (zero based),
     *          or -1 if the format has no minute separator character
     */
    public final int offsetSeparatorMinute() {
        return offsetSeparatorMinute;
    }

    /**
     * @return  the position of the second separator character in the time String (zero based),
     *          or -1 if the format has no second separator character
     */
    public final int offsetSeparatorSecond() {
        return offsetSeparatorSecond;
    }

    /**
     * @return  the position of the fraction separator character in the time String (zero based),
     *          or -1 if the format has no fraction separator character
     */
    public final int offsetSeparatorFraction() {
        return offsetSeparatorFraction;
    }

    /** @return true if this time format has minute or second separator characters and false otherwise */
    public final boolean hasSeparators() {
        return offsetSeparatorMinute >= 0 | offsetSeparatorSecond >= 0;
    }

    /** @return true if this time format has a second part and false otherwise */
    public final boolean hasSeconds() {
        return offsetSeparatorSecond >= 0;
    }

    /** @return true if this time format has a fractional second part and false otherwise */
    public final boolean hasFraction() {
        return fractionLength > 0;
    }

    /**
     * @return  the length of the fractional second part, or 0 if this time format has no
     *          fractional second part
     */
    public final int fractionLength() {
        return fractionLength;
    }

    /** @return true if this time format has a fractional second part and false otherwise */
    public final boolean hasFractionSeparator() {
        return offsetSeparatorFraction >= 0;
    }

    /** @return the length of a string formatted according to this time format */
    public final int length() {
        return length;
    }

    private static final TimeFormat[] VALUES = values();

    /** @return the number of time formats available */
    public static int count() {
        return VALUES.length;
    }

    /**
     * @param ordinal the zero based format index or {@link #ordinal()}
     * @return the constant for the specified ordinal
     * @throws ArrayIndexOutOfBoundsException if ordinal is out of bounds
     * @see #count()
     */
    public static TimeFormat valueByOrdinal(final int ordinal) {
        return VALUES[ordinal];
    }

    /**
     * Invokes the specified consumer for each of the constants.
     * @param consumer the consumer to invoke for every constant
     */
    public static void forEach(final Consumer<? super TimeFormat> consumer) {
        for (final TimeFormat packing : VALUES) {
            consumer.accept(packing);
        }
    }
}
