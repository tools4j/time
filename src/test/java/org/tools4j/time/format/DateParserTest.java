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
import org.tools4j.time.pack.DatePacker;
import org.tools4j.time.pack.Packing;
import org.tools4j.time.validate.DateValidator;
import org.tools4j.time.validate.ValidationMethod;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit test for {@link DateParser}.
 */
public class DateParserTest {

    private static final char[] SEPARATORS = {DateParser.NO_SEPARATOR, '-', '/', '.', '_'};
    private static final char BAD_SEPARATOR = ':';
    private static final Map<DateFormat, String> PATTERN_BY_FORMAT = patternByFormat();
    private static final DateParser[] PARSERS = initParsers();

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
        public void parseYear() {
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, localDate);
                assertEquals(localDate.getYear(), parser.parseYear(input), "input=" + input);
                assertEquals(localDate.getYear(), parser.parseYear(input, AsciiReader.CHAR_SEQUENCE), "input=" + input);
                assertEquals(localDate.getYear(), parser.parseYear("BLA" + input, 3), "input=BLA" + input);
            }
        }

        @Test
        public void parseMonth() {
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, localDate);
                assertEquals(localDate.getMonthValue(), parser.parseMonth(input), "input=" + input);
                assertEquals(localDate.getMonthValue(), parser.parseMonth(input, AsciiReader.CHAR_SEQUENCE), "input=" + input);
                assertEquals(localDate.getMonthValue(), parser.parseMonth("BLA" + input, 3), "input=BLA" + input);
            }
        }

        @Test
        public void parseDay() {
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, localDate);
                assertEquals(localDate.getDayOfMonth(), parser.parseDay(input), "input=" + input);
                assertEquals(localDate.getDayOfMonth(), parser.parseDay(input, AsciiReader.CHAR_SEQUENCE), "input=" + input);
                assertEquals(localDate.getDayOfMonth(), parser.parseDay("BLA" + input, 3), "input=BLA" + input);
            }
        }

        @Test
        public void parseAsPackedDate() {
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, localDate);
                for (final Packing packing : Packing.values()) {
                    final int expected = DatePacker.valueOf(packing).pack(localDate);
                    assertEquals(expected, parser.parseAsPackedDate(input, packing), "input=" + input);
                    assertEquals(expected, parser.parseAsPackedDate(input, AsciiReader.CHAR_SEQUENCE, packing), "input=" + input);
                    assertEquals(expected, parser.parseAsPackedDate("BLA" + input, 3, packing), "input=BLA" + input);
                }
            }
        }

        @Test
        public void parseAsEpochDay() {
            for (final DateParser parser : PARSERS) {
                final long epochDay = localDate.toEpochDay();
                final String input = formatInput(parser, localDate);
                assertEquals(epochDay, parser.parseAsEpochDay(input), "input=" + input);
                assertEquals(epochDay, parser.parseAsEpochDay(input, AsciiReader.CHAR_SEQUENCE), "input=" + input);
                assertEquals(epochDay, parser.parseAsEpochDay("BLA" + input, 3), "input=BLA" + input);
            }
        }

        @Test
        public void parseAsEpochMilli() {
            for (final DateParser parser : PARSERS) {
                final long epochMilli = localDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
                final String input = formatInput(parser, localDate);
                assertEquals(epochMilli, parser.parseAsEpochMilli(input), "input=" + input);
                assertEquals(epochMilli, parser.parseAsEpochMilli(input, AsciiReader.CHAR_SEQUENCE), "input=" + input);
                assertEquals(epochMilli, parser.parseAsEpochMilli("BLA" + input, 3), "input=BLA" + input);
                assertEquals(epochMilli, parser.parseAsEpochMilli("BLABLA" + input, AsciiReader.CHAR_SEQUENCE, 6), "input=BLABLA" + input);
            }
        }

        @Test
        public void parseAsLocalDate() {
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, localDate);
                assertEquals(localDate, parser.parseAsLocalDate(input), "input=" + input);
                assertEquals(localDate, parser.parseAsLocalDate(input, AsciiReader.CHAR_SEQUENCE), "input=" + input);
                assertEquals(localDate, parser.parseAsLocalDate("BLA" + input, 3), "input=BLA" + input);
            }
        }

        @Test
        public void parseSeparator() {
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, localDate);
                for (int sep = 0; sep <= 1; sep++) {
                    assertEquals(parser.separator(), parser.parseSeparator(input, sep), "input=" + input);
                    assertEquals(parser.separator(), parser.parseSeparator(input, AsciiReader.CHAR_SEQUENCE, sep), "input=" + input);
                    assertEquals(parser.separator(), parser.parseSeparator("BLA" + input, 3, sep), "input=BLA" + input);
                }
            }
        }

        @Test
        public void isValid() {
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, localDate);
                assertTrue(parser.isValid(input), "input=" + input);
                assertTrue(parser.isValid(input, AsciiReader.CHAR_SEQUENCE), "input=" + input);
                assertTrue(parser.isValid("BLA" + input, 3), "input=BLA" + input);
            }
        }

        private static String formatInput(final DateParser parser, final LocalDate localDate) {
            final String standardPattern = PATTERN_BY_FORMAT.get(parser.format());
            final String currentPattern = standardPattern.replace(DateParser.DEFAULT_SEPARATOR, parser.separator());
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(currentPattern);
            return formatter.format(localDate);
        }
    }

    @SuppressWarnings("unused")
    enum InvalidPart {
        YEAR, MONTH, DAY, SEPARATOR
    }

    @Nested
    @ParameterizedClass
    @CsvSource(delimiter = '|', value = {
          //"  year | month | day | invalidPart ",
            "     0 |    1  |   1 |     YEAR    ",
            "    -1 |    1  |   1 |     YEAR    ",
            "  -999 |    1  |   1 |     YEAR    ",
            "  2017 |    0  |   1 |    MONTH    ",
            "  2017 |   -1  |   1 |    MONTH    ",
            "  2017 |   13  |   1 |    MONTH    ",
            "  2017 |    1  |   0 |     DAY     ",
            "  2017 |    4  |  -1 |     DAY     |",//NOTE: day=-1 is equivalent to day31
            "  2017 |    1  |  32 |     DAY     ",
            "  2017 |    2  |  29 |     DAY     ",
            "  2016 |    2  |  30 |     DAY     ",
            "  2000 |    2  |  30 |     DAY     ",
            "  1900 |    2  |  29 |     DAY     ",
            "  1900 |    4  |  31 |     DAY     ",
            "  1900 |    6  |  31 |     DAY     ",
            "  1900 |    9  |  31 |     DAY     ",
            "  1900 |   11  |  31 |     DAY     ",
            "  1900 |   11  |  30 |  SEPARATOR  ",
    })
    class Invalid {

        @Parameter(0) int year;
        @Parameter(1) int month;
        @Parameter(2) int day;
        @Parameter(3) InvalidPart invalidPart;

        @Test
        public void parseYear() {
            final boolean isInvalid = invalidPart == InvalidPart.YEAR;
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, year, month, day, invalidPart);
                DateTimeException exception = null;
                int result = DateValidator.YEAR_MAX + 1;
                try {
                    result = parser.parseYear(input);
                } catch (final DateTimeException e) {
                    exception = e;
                }
                if (isInvalid) {
                    assertValue(parser, input, exception, result);
                } else {
                    assertEquals(year, result, "Wrong year for input=" + input);
                }
            }
        }

        @Test
        public void parseMonth() {
            final boolean isInvalid = invalidPart == InvalidPart.MONTH;
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, year, month, day, invalidPart);
                DateTimeException exception = null;
                int result = DateValidator.YEAR_MAX + 1;
                try {
                    result = parser.parseMonth(input);
                } catch (final DateTimeException e) {
                    exception = e;
                }
                if (isInvalid) {
                    assertValue(parser, input, exception, result);
                } else {
                    assertEquals(month, result, "Wrong month for input=" + input);
                }
            }
        }

        @Test
        public void parseDay() {
            final boolean isInvalid = invalidPart != InvalidPart.SEPARATOR;
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, year, month, day, invalidPart);
                DateTimeException exception = null;
                int result = DateValidator.YEAR_MAX + 1;
                try {
                    result = parser.parseDay(input);
                } catch (final DateTimeException e) {
                    exception = e;
                }
                if (isInvalid) {
                    assertValue(parser, input, exception, result);
                } else {
                    assertEquals(day, result, "Wrong day for input=" + input);
                }
            }
        }

        @Test
        public void parseAsPackedDate() {
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, year, month, day, invalidPart);
                for (final Packing packing : Packing.values()) {
                    DateTimeException exception = null;
                    int result = DateValidator.YEAR_MAX + 1;
                    try {
                        result = parser.parseAsPackedDate(input, packing);
                    } catch (final DateTimeException e) {
                        exception = e;
                    }
                    if (!isValid(parser, invalidPart)) {
                        assertValue(parser, input, exception, result);
                    }
                }
            }
        }

        @Test
        public void parseAsEpochDay() {
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, year, month, day, invalidPart);
                DateTimeException exception = null;
                long result = DateValidator.YEAR_MAX + 1;
                try {
                    result = parser.parseAsEpochDay(input);
                } catch (final DateTimeException e) {
                    exception = e;
                }
                if (!isValid(parser, invalidPart)) {
                    assertEpoch(parser, input, exception, result);
                }
            }
        }

        @Test
        public void parseAsEpochMilli() {
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, year, month, day, invalidPart);
                DateTimeException exception = null;
                long result = DateValidator.YEAR_MAX + 1;
                try {
                    result = parser.parseAsEpochMilli(input);
                } catch (final DateTimeException e) {
                    exception = e;
                }
                if (!isValid(parser, invalidPart)) {
                    assertEpoch(parser, input, exception, result);
                }
            }
        }

        @Test
        public void parseAsLocalDate() {
            for (final DateParser parser : PARSERS) {
                final boolean shouldFail =
                        invalidPart != InvalidPart.SEPARATOR ||
                        parser.validationMethod() != ValidationMethod.UNVALIDATED &&
                                (parser.format().hasSeparators() && parser.separator() != DateParser.NO_SEPARATOR);

                final String input = formatInput(parser, year, month, day, invalidPart);
                try {
                    parser.parseAsLocalDate(input);
                    if (shouldFail) {
                        fail("toLocalDate should ALWAYS throw exception for parser=" + parser + " and input=" + input);
                    }
                } catch (final DateTimeException e) {
                    if (!shouldFail) {
                        throw new AssertionError(
                                "toLocalDate should NEVER throw exception for parser=" + parser +
                                        " and input=" + input + " but we caught exception=" + e, e);
                    }
                }
            }
        }

        @Test
        public void parseSeparator() {
            for (final DateParser parser : PARSERS) {
                final String input = formatInput(parser, year, month, day, invalidPart);
                for (int sep = 0; sep <= 1; sep++) {
                    DateTimeException exception = null;
                    char result = (char)(Byte.MAX_VALUE + 1);
                    try {
                        result = parser.parseSeparator(input, sep);
                    } catch (final DateTimeException e) {
                        exception = e;
                    }
                    if (!isValid(parser, invalidPart) && invalidPart == InvalidPart.SEPARATOR) {
                        assertSeparator(parser, input, exception, result);
                    } else {
                        final char expected = parser.format().hasSeparators() ?
                                (invalidPart == InvalidPart.SEPARATOR ? BAD_SEPARATOR : parser.separator()) :
                                DateParser.NO_SEPARATOR;
                        assertEquals(expected, result, "input=" + input + ", parser=" + parser);
                    }
                }
            }
        }

        @Test
        public void isValid() {
            for (final DateParser parser : PARSERS) {
                final boolean isValid = isValid(parser, invalidPart);
                final String input = formatInput(parser, year, month, day, invalidPart);
                assertEquals(isValid, parser.isValid(input), "input=" + input + ", parser=" + parser);
                assertEquals(isValid, parser.isValid(input, AsciiReader.CHAR_SEQUENCE), "input=" + input + ", parser=" + parser);
                assertEquals(isValid, parser.isValid("BLA" + input, 3), "input=BLA" + input + ", parser=" + parser);
            }
        }

        private static boolean isValid(final DateParser parser, final InvalidPart invalidPart) {
            return invalidPart == InvalidPart.SEPARATOR &&
                    (!parser.format().hasSeparators() || parser.separator() == DateParser.NO_SEPARATOR);
        }

        private static void assertEpoch(final DateParser parser, final String input,
                                        final DateTimeException exception, final long result) {
            assertResult(parser, input, exception, result, DateParser.INVALID_EPOCH);
        }
        private static void assertSeparator(final DateParser parser, final String input,
                                            final DateTimeException exception, final char result) {
            assertResult(parser, input, exception, (byte)result, DateParser.INVALID_SEPARATOR);
        }
        private static void assertValue(final DateParser parser, final String input,
                                        final DateTimeException exception, final int result) {
            assertResult(parser, input, exception, result, DateParser.INVALID);
        }
        private static void assertResult(final DateParser parser, final String input,
                                         final DateTimeException exception,
                                         final long result, final long invalidValue) {
            switch (parser.validationMethod()) {
                case UNVALIDATED:
                    assertNull(exception, "Unvalidating parser should not throw an exception for input='" +
                            input + "' and parser=" + parser);
                    assertNotEquals(invalidValue, result, "Unvalidating parser should not return INVALID for input='" +
                            input + "' and parser=" + parser);
                    break;
                case INVALIDATE_RESULT:
                    assertNull(exception, "Invalidate-result parser should not throw an exception for input='" +
                            input + "' and parser=" + parser);
                    assertEquals(invalidValue, result, "Invalidate-result parser should return INVALID for input='" +
                            input + "' and parser=" + parser);
                    break;
                case THROW_EXCEPTION:
                    assertNotNull(exception, "Throw-exception parser should throw an exception for input='" +
                            input + "' and parser=" + parser);
                    break;
                default:
                    throw new RuntimeException("Unsupported validation method: " + parser.validationMethod());
            }
        }

        private static String formatInput(final DateParser parser,
                                          final int year, final int month, final int day, final InvalidPart invalidPart) {
            final char separator = invalidPart == InvalidPart.SEPARATOR ? BAD_SEPARATOR : parser.separator();
            final String standardPattern = PATTERN_BY_FORMAT.get(parser.format());
            final String currentPattern = standardPattern.replace(DateParser.DEFAULT_SEPARATOR, separator);
            return currentPattern
                    .replace("yyyy", toFixedLength(4, year))
                    .replace("MM", toFixedLength(2, month))
                    .replace("dd", toFixedLength(2, day));
        }

        private static String toFixedLength(final int length, final int value) {
            final StringBuilder sb = new StringBuilder(length);
            sb.append(value);
            while (sb.length() < length) {
                sb.insert(0, '0');
            }
            return sb.substring(0, length);
        }
    }

    @Nested
    @ParameterizedClass
    @EnumSource(DateFormat.class)
    class Special {

        @Parameter DateFormat format;

        @Test
        public void format() {
            assertSame(format, DateParser.valueOf(format).format());
            for (final char separator : SEPARATORS) {
                assertSame(format, DateParser.valueOf(format, separator).format());
                for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                    assertSame(format, DateParser.valueOf(format, validationMethod).format());
                    assertSame(format, DateParser.valueOf(format, separator, validationMethod).format());
                }
            }
        }

        @Test
        public void separator() {
            char expected = format.hasSeparators() ? DateParser.DEFAULT_SEPARATOR : DateParser.NO_SEPARATOR;
            assertEquals(DateParser.valueOf(format).separator(), expected);
            for (final char separator : SEPARATORS) {
                expected = format.hasSeparators() ? separator : DateParser.NO_SEPARATOR;
                assertEquals(DateParser.valueOf(format, separator).separator(), expected);
                for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                    expected = format.hasSeparators() ? DateParser.DEFAULT_SEPARATOR : DateParser.NO_SEPARATOR;
                    assertEquals(DateParser.valueOf(format, validationMethod).separator(), expected);
                    expected = format.hasSeparators() ? separator : DateParser.NO_SEPARATOR;
                    assertEquals(DateParser.valueOf(format, separator, validationMethod).separator(), expected);
                }
            }
        }

        @Test
        public void validationMethod() {
            assertSame(ValidationMethod.UNVALIDATED, DateParser.valueOf(format).validationMethod());
            for (final char separator : SEPARATORS) {
                assertSame(ValidationMethod.UNVALIDATED, DateParser.valueOf(format, separator).validationMethod());
                for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                    assertSame(validationMethod, DateParser.valueOf(format, validationMethod).validationMethod());
                    assertSame(validationMethod, DateParser.valueOf(format, separator, validationMethod).validationMethod());
                }
            }
        }

        @Test
        public void to_String() {
            String expected;
            expected = "SimpleDateParser[format=" + format + ", separator=" + separatorString(format) + "]";
            assertEquals(DateParser.valueOf(format).toString(), expected);
            for (final char separator : SEPARATORS) {
                expected = "SimpleDateParser[format=" + format + ", separator=" + separatorString(format, separator) + "]";
                assertEquals(DateParser.valueOf(format, separator).toString(), expected);
                for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                    final String master = validationMethod == ValidationMethod.UNVALIDATED
                            ? "SimpleDateParser[format=%s, separator=%s]"
                            : "ValidatingDateParser[format=%s, separator=%s, validationMethod=%s]";
                    expected = String.format(master, format, separatorString(format), validationMethod);
                    assertEquals(DateParser.valueOf(format, validationMethod).toString(), expected);
                    expected = String.format(master, format, separatorString(format, separator), validationMethod);
                    assertEquals(DateParser.valueOf(format, separator, validationMethod).toString(), expected);
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

    private static DateParser[] initParsers() {
        final DateParser[] parsers = new DateParser[DateFormat.values().length * SEPARATORS.length * ValidationMethod.values().length];
        int index = 0;
        for (final DateFormat format : DateFormat.values()) {
            for (final char separator : SEPARATORS) {
                for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                    parsers[index++] = DateParser.valueOf(format, separator, validationMethod);
                }
            }
        }
        return parsers;
    }

    static String separatorString(final DateFormat format) {
        return separatorString(format, DateFormatter.DEFAULT_SEPARATOR);
    }

    static String separatorString(final DateFormat format, final char separator) {
        return format.hasSeparators() && separator != DateFormatter.NO_SEPARATOR ?
                "'" + separator + "'" : "<none>";
    }
}