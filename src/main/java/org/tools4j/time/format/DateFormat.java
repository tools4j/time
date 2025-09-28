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
 * Constants for common date formats of fixed length. The format defines the ordering
 * of the date components and the position of the separator characters (if any), but not
 * the type of separator character.
 */
public enum DateFormat {
    /** Big-endian date format without separator chars such as 20170328 */
    YYYYMMDD(0, 4, 6, -1, -1),
    /** Middle-endian date format without separator chars such as 03282017 */
    MMDDYYYY(4, 0, 2, -1, -1),
    /** Little-endian date format without separator chars such as 28032017 */
    DDMMYYYY(4, 2, 0, -1, -1),
    /** Big-endian date format with separator chars such as 2017-03-28 */
    YYYY_MM_DD(0, 5, 8, 4, 7),
    /** Middle-endian date format with separator chars such as 03-28-2017 */
    MM_DD_YYYY(6, 0, 3, 2, 5),
    /** Little-endian date format with separator chars such as 28-03-2017 */
    DD_MM_YYYY(6, 3, 0, 2, 5);

    private final int offsetYear;
    private final int offsetMonth;
    private final int offsetDay;
    private final int offsetSeparatorOne;
    private final int offsetSeparatorTwo;

    DateFormat(final int offsetYear,
               final int offsetMonth,
               final int offsetDay,
               final int offsetSeparatorOne,
               final int offsetSeparatorTwo) {
        this.offsetYear = offsetYear;
        this.offsetMonth = offsetMonth;
        this.offsetDay = offsetDay;
        this.offsetSeparatorOne = offsetSeparatorOne;
        this.offsetSeparatorTwo = offsetSeparatorTwo;
    }

    /** @return the start position of the year in the date string (zero based) */
    public final int offsetYear() {
        return offsetYear;
    }

    /** @return the start position of the month in the date string (zero based) */
    public final int offsetMonth() {
        return offsetMonth;
    }

    /** @return the start position of the day in the date string (zero based) */
    public final int offsetDay() {
        return offsetDay;
    }

    /**
     * @return  the position of the first separator character in the date string (zero based),
     *          or -1 if the format has no separator characters
     */
    public final int offsetSeparatorOne() {
        return offsetSeparatorOne;
    }

    /**
     * @return  the position of the second separator character in the date string (zero based),
     *          or -1 if the format has no separator characters
     */
    public final int offsetSeparatorTwo() {
        return offsetSeparatorTwo;
    }

    /** @return true if this date format has separator characters and false otherwise */
    public final boolean hasSeparators() {
        return offsetSeparatorOne >= 0;
    }

    /** @return the length of a string formatted according to this date format */
    public final int length() {
        return hasSeparators() ? 10 : 8;
    }

    private static final DateFormat[] VALUES = values();

    /** @return the number of date formats available */
    public static int count() {
        return VALUES.length;
    }

    /**
     * @param ordinal the zero based format index or {@link #ordinal()}
     * @return the constant for the specified ordinal
     * @throws ArrayIndexOutOfBoundsException if ordinal is out of bounds
     * @see #count()
     */
    public static DateFormat valueByOrdinal(final int ordinal) {
        return VALUES[ordinal];
    }

    /**
     * Invokes the specified consumer for each of the constants.
     * @param consumer the consumer to invoke for every constant
     */
    public static void forEach(final Consumer<? super DateFormat> consumer) {
        for (final DateFormat packing : VALUES) {
            consumer.accept(packing);
        }
    }
}
