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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.tools4j.time.base.Epoch;
import org.tools4j.time.base.TimeFactors;
import org.tools4j.time.pack.DatePacker;
import org.tools4j.time.pack.Packing;
import org.tools4j.time.validate.DateValidator;
import org.tools4j.time.validate.ValidationMethod;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.tools4j.time.format.AsciiWriter.STRING_BUILDER;
import static org.tools4j.time.format.DateParserTest.separatorString;

/**
 * Unit test for {@link DateFormatter}.
 */
public class DateFormatterTest {

    private static final char[] SEPARATORS = {DateFormatter.NO_SEPARATOR, '-', '/', '.', '_'};
    private static final Map<DateFormat, String> PATTERN_BY_FORMAT = patternByFormat();
    private static final DateFormatter[] FORMATTERS = initFormatters();

    @Nested
    @ParameterizedClass
    @ValueSource(strings = {
            "2017-01-01",
            "2017-01-31",
            "2017-02-28",
            "2017-03-31",
            "2017-04-30",
            "2017-05-31",
            "2017-06-30",
            "2017-07-31",
            "2017-08-31",
            "2017-09-30",
            "2017-10-31",
            "2017-11-30",
            "2017-12-31",
            "2017-12-31",
            "2016-02-29",
            "2000-02-29",
            "1900-02-28",
            "1970-01-01",
            "1970-01-02",
            "1969-12-31",
            "1969-12-30",
            "1969-04-30",
            "1968-02-28",
            "1600-02-29",
            "0004-02-29",
            "0100-02-28",
            "0400-02-29",
            "0001-01-01",
            "9999-12-31",
    })
    class Valid {

        @Parameter LocalDate localDate;

        @Test
        public void format() {
            for (final DateFormatter formatter : FORMATTERS) {
                final String expected = expected(formatter, localDate);
                final StringBuilder actual1 = new StringBuilder();
                formatter.format(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(),
                        actual1);
                assertEquals(expected, actual1.toString(), "input=" + localDate);
                final StringBuilder actual2 = new StringBuilder("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
                formatter.format(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(),
                        actual2, STRING_BUILDER);
                assertEquals(expected + "ABCDEFGHIJKLMNOPQRSTUVWXYZ".substring(expected.length()),
                        actual2.toString(), "input=" + localDate);
                final StringBuilder actual3 = new StringBuilder("ABCDE");
                formatter.format(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(),
                        actual3, STRING_BUILDER, 3);
                assertEquals("ABC" + expected, actual3.toString(), "input=" + localDate);
            }
        }

        @Test
        public void formatPackedDate() {
            for (final DateFormatter formatter : FORMATTERS) {
                final int len = formatter.format().length();
                for (final Packing packing : Packing.values()) {
                    final String expected = expected(formatter, localDate);
                    final int packed = DatePacker.valueOf(packing).pack(localDate);
                    final StringBuilder actual1 = new StringBuilder();
                    assertEquals(len, formatter.formatPackedDate(packed, packing, actual1),
                            "format=" + formatter.format());
                    assertEquals(expected, actual1.toString(), "input=" + localDate);
                    final StringBuilder actual2 = new StringBuilder("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
                    assertEquals(len, formatter.formatPackedDate(packed, packing, actual2, STRING_BUILDER),
                            "format=" + formatter.format());
                    assertEquals(expected + "ABCDEFGHIJKLMNOPQRSTUVWXYZ".substring(expected.length()),
                            actual2.toString(), "input=" + localDate);
                    final StringBuilder actual3 = new StringBuilder("ABCDE");
                    assertEquals(len,
                            formatter.formatPackedDate(packed, packing, actual3, STRING_BUILDER, 3), "format=" + formatter.format());
                    assertEquals("ABC" + expected, actual3.toString(), "input=" + localDate);
                }
            }
        }

        @Test
        public void formatEpochDay() {
            for (final DateFormatter formatter : FORMATTERS) {
                final int len = formatter.format().length();
                final String expected = expected(formatter, localDate);
                final StringBuilder actual1 = new StringBuilder();
                assertEquals(len, formatter.formatEpochDay(localDate.toEpochDay(), actual1),
                        "format=" + formatter.format());
                assertEquals(expected, actual1.toString(), "input=" + localDate);
                final StringBuilder actual2 = new StringBuilder("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
                assertEquals(len, formatter.formatEpochDay(localDate.toEpochDay(), actual2, STRING_BUILDER),
                        "format=" + formatter.format());
                assertEquals(expected + "ABCDEFGHIJKLMNOPQRSTUVWXYZ".substring(expected.length()),
                        actual2.toString(), "input=" + localDate);
                final StringBuilder actual3 = new StringBuilder("ABCDE");
                assertEquals(len, formatter.formatEpochDay(localDate.toEpochDay(), actual3, STRING_BUILDER, 3),
                        "format=" + formatter.format());
                assertEquals("ABC" + expected, actual3.toString(), "input=" + localDate);
            }
        }

        @Test
        public void formatEpochMilli() {
            for (final DateFormatter formatter : FORMATTERS) {
                final int len = formatter.format().length();
                final String expected = expected(formatter, localDate);
                final StringBuilder actual1 = new StringBuilder();
                assertEquals(len, formatter.formatEpochMilli(localDate.toEpochDay() * TimeFactors.MILLIS_PER_DAY, actual1), "format=" + formatter.format());
                assertEquals(expected, actual1.toString(), "input=" + localDate);
                final StringBuilder actual2 = new StringBuilder("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
                assertEquals(len,
                        formatter.formatEpochMilli(localDate.toEpochDay() * TimeFactors.MILLIS_PER_DAY,
                                actual2, STRING_BUILDER), "format=" + formatter.format());
                assertEquals(expected + "ABCDEFGHIJKLMNOPQRSTUVWXYZ".substring(expected.length()),
                        actual2.toString(), "input=" + localDate);
                final StringBuilder actual3 = new StringBuilder("ABCDE");
                final int randomMillis = (int)(TimeFactors.MILLIS_PER_DAY * Math.random());
                assertEquals(len,
                        formatter.formatEpochMilli(localDate.toEpochDay() * TimeFactors.MILLIS_PER_DAY + randomMillis,
                                actual3, STRING_BUILDER, 3), "format=" + formatter.format());
                assertEquals("ABC" + expected, actual3.toString(), "input=" + localDate);
            }
        }

        @Test
        public void formatLocalDate() {
            for (final DateFormatter formatter : FORMATTERS) {
                final int len = formatter.format().length();
                final String expected = expected(formatter, localDate);
                final StringBuilder actual1 = new StringBuilder();
                assertEquals(len,
                        formatter.formatLocalDate(localDate, actual1), "format=" + formatter.format());
                assertEquals(expected, actual1.toString(), "input=" + localDate);
                final StringBuilder actual2 = new StringBuilder("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
                assertEquals(len,
                        formatter.formatLocalDate(localDate, actual2, STRING_BUILDER), "format=" + formatter.format());
                assertEquals(expected + "ABCDEFGHIJKLMNOPQRSTUVWXYZ".substring(expected.length()),
                        actual2.toString(), "input=" + localDate);
                final StringBuilder actual3 = new StringBuilder("ABCDE");
                assertEquals(len, formatter.formatLocalDate(localDate, actual3, STRING_BUILDER, 3), "format=" + formatter.format());
                assertEquals("ABC" + expected, actual3.toString(), "input=" + localDate);
            }
        }

        private static String expected(final DateFormatter formatter, final LocalDate localDate) {
            final String standardPattern = PATTERN_BY_FORMAT.get(formatter.format());
            final String currentPattern = standardPattern.replace(DateParser.DEFAULT_SEPARATOR, formatter.separator());
            final DateTimeFormatter javaFormatter = DateTimeFormatter.ofPattern(currentPattern);
            return javaFormatter.format(localDate);
        }
    }

    @Nested
    @ParameterizedClass
    @CsvSource(delimiter = '|', value = {
          //"  year | month | day ",
            "     0 |    1  |   1 ",
            "    -1 |    1  |   1 ",
            "  -999 |    1  |   1 ",
            "  2017 |    0  |   1 ",
            "  2017 |   -1  |   1 ",
            "  2017 |   13  |   1 ",
            "  2017 |    1  |   0 ",
            "  2017 |    2  |  -2 |",//NOTE: day=-2 is equivalent to day30
            "  2017 |    1  |  32 ",
            "  2017 |    2  |  29 ",
            "  2016 |    2  |  30 ",
            "  2000 |    2  |  30 ",
            "  1900 |    2  |  29 ",
            "  1900 |    4  |  31 ",
            "  1900 |    6  |  31 ",
            "  1900 |    9  |  31 ",
            "  1900 |   11  |  31 ",
            " 10000 |    1  |   1 ",
    })
    class Invalid {

        @Parameter(0) int year;
        @Parameter(1) int month;
        @Parameter(2) int day;
        
        @Test
        public void format() {
            for (final DateFormatter formatter : FORMATTERS) {
                DateTimeException exception = null;
                int result = 0;
                try {
                    result = formatter.format(year, month, day, new StringBuilder());
                } catch (final DateTimeException e) {
                    exception = e;
                }
                assertResult(formatter, String.format("%d/%d/%d", year, month, day),
                        exception, result);
            }
        }

        @Test
        public void formatPackedDate() {
            for (final DateFormatter formatter : FORMATTERS) {
                for (final Packing packing : Packing.values()) {
                    final int packedDate = DatePacker.valueOf(packing).pack(year, month, day);
                    DateTimeException exception = null;
                    int result = 0;
                    try {
                        result = formatter.formatPackedDate(packedDate, packing, new StringBuilder());
                    } catch (final DateTimeException e) {
                        exception = e;
                    }
                    assertResult(formatter, String.format("%d/%d/%d", year, month, day),
                            exception, result);
                }
            }
        }

        @Test
        public void formatEpochDay() {
            if (!DateValidator.isValidYear(year)) {
                final long epochDays = Epoch.valueOf(ValidationMethod.UNVALIDATED).toEpochDay(year, month, day);
                for (final DateFormatter formatter : FORMATTERS) {
                    DateTimeException exception = null;
                    int result = 0;
                    try {
                        result = formatter.formatEpochDay(epochDays, new StringBuilder());
                    } catch (final DateTimeException e) {
                        exception = e;
                    }
                    assertResult(formatter, String.format("%d/%d/%d", year, month, day),
                            exception, result);
                }
            }
        }

        @Test
        public void formatEpochMilli() {
            if (!DateValidator.isValidYear(year)) {
                final long epochDays = Epoch.valueOf(ValidationMethod.UNVALIDATED).toEpochMilli(year, month, day);
                for (final DateFormatter formatter : FORMATTERS) {
                    DateTimeException exception = null;
                    int result = 0;
                    try {
                        result = formatter.formatEpochMilli(epochDays, new StringBuilder());
                    } catch (final DateTimeException e) {
                        exception = e;
                    }
                    assertResult(formatter, String.format("%d/%d/%d", year, month, day),
                            exception, result);
                }
            }
        }

        @Test
        public void formatLocalDate() {
            if (!DateValidator.isValidYear(year)) {
                final LocalDate localDate = LocalDate.of(year, month, day);
                for (final DateFormatter formatter : FORMATTERS) {
                    DateTimeException exception = null;
                    int result = 0;
                    try {
                        result = formatter.formatLocalDate(localDate, new StringBuilder());
                    } catch (final DateTimeException e) {
                        exception = e;
                    }
                    assertResult(formatter, String.format("%d/%d/%d", year, month, day),
                            exception, result);
                }
            }
        }

        private static void assertResult(final DateFormatter formatter,
                                         final Object input,
                                         final DateTimeException exception,
                                         final int result) {
            switch (formatter.validationMethod()) {
                case UNVALIDATED:
                    assertNull(exception, "Unvalidating formatter should not throw an exception for input='" +
                            input + "' and formatter=" + formatter);
                    assertNotEquals(DateFormatter.INVALID, result, "Unvalidating parser should not return INVALID for input='" +
                            input + "' and formatter=" + formatter);
                    break;
                case INVALIDATE_RESULT:
                    assertNull(exception, "Invalidate-result formatter should not throw an exception for input='" +
                            input + "' and formatter=" + formatter);
                    assertEquals(DateFormatter.INVALID, result, "Invalidate-result formatter should return INVALID for input='" +
                            input + "' and formatter=" + formatter);
                    break;
                case THROW_EXCEPTION:
                    assertNotNull(exception, "Throw-exception formatter should throw an exception for input='" +
                            input + "' and formatter=" + formatter);
                    break;
                default:
                    throw new RuntimeException("Unsupported validation method: " + formatter.validationMethod());
            }
        }
    }

    @Nested
    @ParameterizedClass
    @EnumSource(DateFormat.class)
    class Special {

        @Parameter DateFormat format;

        @Test
        public void format() {
            assertSame(format, DateFormatter.valueOf(format).format());
            for (final char separator : SEPARATORS) {
                assertSame(format, DateFormatter.valueOf(format, separator).format());
                for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                    assertSame(format, DateFormatter.valueOf(format, validationMethod).format());
                    assertSame(format, DateFormatter.valueOf(format, separator, validationMethod).format());
                }
            }
        }

        @Test
        public void separator() {
            char expected = format.hasSeparators() ? DateFormatter.DEFAULT_SEPARATOR : DateFormatter.NO_SEPARATOR;
            assertEquals(expected, DateFormatter.valueOf(format).separator());
            for (final char separator : SEPARATORS) {
                expected = format.hasSeparators() ? separator : DateFormatter.NO_SEPARATOR;
                assertEquals(expected, DateFormatter.valueOf(format, separator).separator());
                for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                    expected = format.hasSeparators() ? DateFormatter.DEFAULT_SEPARATOR : DateFormatter.NO_SEPARATOR;
                    assertEquals(expected, DateFormatter.valueOf(format, validationMethod).separator());
                    expected = format.hasSeparators() ? separator : DateFormatter.NO_SEPARATOR;
                    assertEquals(expected, DateFormatter.valueOf(format, separator, validationMethod).separator());
                }
            }
        }

        @Test
        public void to_String() {
            String expected = "SimpleDateFormatter[format=" + format + ", separator=" + separatorString(format) + "]";
            assertEquals(expected, DateFormatter.valueOf(format).toString());
            for (final char separator : SEPARATORS) {
                expected = "SimpleDateFormatter[format=" + format + ", separator=" + separatorString(format, separator) + "]";
                assertEquals(expected, DateFormatter.valueOf(format, separator).toString());
                for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                    expected = "SimpleDateFormatter[format=" + format + ", separator=" + separatorString(format) + "]";
                    assertEquals(expected, DateFormatter.valueOf(format, validationMethod).toString());
                    expected = "SimpleDateFormatter[format=" + format + ", separator=" + separatorString(format, separator) + "]";
                    assertEquals(expected, DateFormatter.valueOf(format, separator, validationMethod).toString());
                }
            }
        }
    }

    private static Map<DateFormat, String> patternByFormat() {
        final Map<DateFormat, String> formatters = new EnumMap<>(DateFormat.class);
        formatters.put(DateFormat.YYYYMMDD, "yyyyMMdd");
        formatters.put(DateFormat.MMDDYYYY, "MMddyyyy");
        formatters.put(DateFormat.DDMMYYYY, "ddMMyyyy");
        formatters.put(DateFormat.YYYY_MM_DD, "yyyy-MM-dd");
        formatters.put(DateFormat.MM_DD_YYYY, "MM-dd-yyyy");
        formatters.put(DateFormat.DD_MM_YYYY, "dd-MM-yyyy");
        return formatters;
    }

    private static DateFormatter[] initFormatters() {
        final DateFormatter[] formatters = new DateFormatter[DateFormat.values().length * SEPARATORS.length * ValidationMethod.values().length];
        int index = 0;
        for (final DateFormat format : DateFormat.values()) {
            for (final char separator : SEPARATORS) {
                for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                    formatters[index++] = DateFormatter.valueOf(format, separator, validationMethod);
                }
            }
        }
        return formatters;
    }
}