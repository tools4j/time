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
package org.tools4j.time.pack;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.tools4j.time.base.TimeFactors;
import org.tools4j.time.validate.ValidationMethod;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tools4j.time.validate.ValidationMethod.INVALIDATE_RESULT;
import static org.tools4j.time.validate.ValidationMethod.THROW_EXCEPTION;

/**
 * Unit test for {@link DateTimePacker}.
 */
public class DateTimePackerTest {

    private static final DateTimePacker[] PACKERS = initPackers();

    @Nested
    @ParameterizedClass
    @CsvSource(delimiter = '|', value = {
          //"  localDate |  localTime   ",
            " 2017-01-01 | 00:00:00.000 ",
            " 2017-01-31 | 23:59:59.999 ",
            " 2017-02-28 | 01:01:01.111 ",
            " 2017-03-31 | 10:11:12.123 ",
            " 2017-04-30 | 11:59:59.999 ",
            " 2017-05-31 | 12:59:59.999 ",
            " 2017-06-30 | 12:34:56.789 ",
            " 2017-07-31 | 00:00:00.000 ",
            " 2017-08-31 | 23:59:59.999 ",
            " 2017-09-30 | 01:01:01.111 ",
            " 2017-10-31 | 10:11:12.123 ",
            " 2017-11-30 | 11:59:59.999 ",
            " 2017-12-31 | 12:59:59.999 ",
            " 2017-12-31 | 12:34:56.789 ",
            " 2016-02-29 | 00:00:00.000 ",
            " 2000-02-29 | 23:59:59.999 ",
            " 1900-02-28 | 01:01:01.111 ",
            " 1970-01-01 | 10:11:12.123 ",
            " 1970-01-02 | 11:59:59.999 ",
            " 1969-12-31 | 12:59:59.999 ",
            " 1969-12-30 | 12:34:56.789 ",
            " 1969-04-30 | 00:00:00.000 ",
            " 1968-02-28 | 23:59:59.999 ",
            " 1600-02-29 | 01:01:01.111 ",
            " 0004-02-29 | 10:11:12.123 ",
            " 0100-02-28 | 11:59:59.999 ",
            " 0400-02-29 | 12:59:59.999 ",
            " 0001-01-01 | 00:00:00.000 ",
            " 9999-12-31 | 23:59:59.999 ",
    })
    class Valid {

        private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");
        private static final DateTimeFormatter YYYYMMDDHHMMSS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        private static final DateTimeFormatter YYYYMMDDHHMMSSMMM = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

        @Parameter(0) LocalDate localDate;
        @Parameter(1) LocalTime localTime;

        @Test
        public void packDecimal() {
            final LocalDateTime localDateTime = localDate.atTime(localTime);
            final long packedDateOnly = DateTimePacker.DECIMAL.pack(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
            final long packedDateTimeWithMillis = DateTimePacker.DECIMAL.pack(localDate, localTime);
            final long packedDateTimeNoMillis = DateTimePacker.DECIMAL.pack(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(),
                    localTime.getHour(), localTime.getMinute(), localTime.getSecond());
            assertEquals(Long.parseLong(localDateTime.format(YYYYMMDD)) * 100_00_00_000L, packedDateOnly);
            assertEquals(Long.parseLong(localDateTime.format(YYYYMMDDHHMMSS)) * 1000L, packedDateTimeNoMillis);
            assertEquals(Long.parseLong(localDateTime.format(YYYYMMDDHHMMSSMMM)), packedDateTimeWithMillis);
        }

        @Test
        public void packBinary() {
            final long packed = DateTimePacker.BINARY.pack(localDate, localTime);
            final int datePart = (localDate.getYear() << 9) | (localDate.getMonthValue() << 5) | localDate.getDayOfMonth();
            final int timePart = (localTime.getHour() << 22) | (localTime.getMinute() << 16) | (localTime.getSecond() << 10) | (localTime.getNano() / TimeFactors.NANOS_PER_MILLI);
            final long expected = ((0xffffffffL & datePart) << 27) | (0xffffffffL & timePart);
            assertEquals(expected, packed);
        }

        @Test
        public void packAndUnpackLocalDateTime() {
            final LocalDateTime expected = localDate.atTime(localTime);
            for (final DateTimePacker packer : PACKERS) {
                final long packed = packer.pack(localDate, localTime);
                final LocalDateTime unpacked = packer.unpackLocalDateTime(packed);
                assertEquals(expected, unpacked, packer + ": " + localDate + " " + localTime + " -> " + packed);
            }
        }

        @Test
        public void packAndUnpackYearMonthDay() {
            for (final DateTimePacker packer : PACKERS) {
                final long packed = packer.pack(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
                final int year = packer.unpackYear(packed);
                final int month = packer.unpackMonth(packed);
                final int day = packer.unpackDay(packed);
                assertEquals(localDate.getYear(), year, packer + ": " + localDate + " -> " + packed + " [y]");
                assertEquals(localDate.getMonthValue(), month, packer + ": " + localDate + " -> " + packed + " [m]");
                assertEquals(localDate.getDayOfMonth(), day, packer + ": " + localDate + " -> " + packed + " [d]");
            }
        }

        @Test
        public void packAndUnpackYearMonthDayHourMinuteSecond() {
            for (final DateTimePacker packer : PACKERS) {
                final long packed = packer.pack(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(),
                        localTime.getHour(), localTime.getMinute(), localTime.getSecond());
                final int year = packer.unpackYear(packed);
                final int month = packer.unpackMonth(packed);
                final int day = packer.unpackDay(packed);
                final int hour = packer.unpackHour(packed);
                final int minute = packer.unpackMinute(packed);
                final int second = packer.unpackSecond(packed);
                assertEquals(localDate.getYear(), year, packer + ": " + localDate + " " + localTime + " -> " + packed + " [y]");
                assertEquals(localDate.getMonthValue(), month, packer + ": " + localDate + " " + localTime + " -> " + packed + " [m]");
                assertEquals(localDate.getDayOfMonth(), day, packer + ": " + localDate + " " + localTime + " -> " + packed + " [d]");
                assertEquals(localTime.getHour(), hour, packer + ": " + localDate + " " + localTime + " -> " + packed + " [h]");
                assertEquals(localTime.getMinute(), minute, packer + ": " + localDate + " " + localTime + " -> " + packed + " [m]");
                assertEquals(localTime.getSecond(), second, packer + ": " + localDate + " " + localTime + " -> " + packed + " [s]");
            }
        }

        @Test
        public void packAndUnpackYearMonthDayHourMinuteSecondMilli() {
            for (final DateTimePacker packer : PACKERS) {
                final long packed = packer.pack(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(),
                        localTime.getHour(), localTime.getMinute(), localTime.getSecond(), localTime.getNano() / TimeFactors.NANOS_PER_MILLI);
                final int year = packer.unpackYear(packed);
                final int month = packer.unpackMonth(packed);
                final int day = packer.unpackDay(packed);
                final int hour = packer.unpackHour(packed);
                final int minute = packer.unpackMinute(packed);
                final int second = packer.unpackSecond(packed);
                final int milli = packer.unpackMilli(packed);
                assertEquals(localDate.getYear(), year, packer + ": " + localDate + " " + localTime + " -> " + packed + " [y]");
                assertEquals(localDate.getMonthValue(), month, packer + ": " + localDate + " " + localTime + " -> " + packed + " [m]");
                assertEquals(localDate.getDayOfMonth(), day, packer + ": " + localDate + " " + localTime + " -> " + packed + " [d]");
                assertEquals(localTime.getHour(), hour, packer + ": " + localDate + " " + localTime + " -> " + packed + " [h]");
                assertEquals(localTime.getMinute(), minute, packer + ": " + localDate + " " + localTime + " -> " + packed + " [m]");
                assertEquals(localTime.getSecond(), second, packer + ": " + localDate + " " + localTime + " -> " + packed + " [s]");
                assertEquals(localTime.getNano() / TimeFactors.NANOS_PER_MILLI, milli, packer + ": " + localDate + " " + localTime + " -> " + packed + " [S]");
            }
        }

        @Test
        public void packFromPackedDateAndTime() {
            for (final DateTimePacker packer : PACKERS) {
                Packing.forEach(packing -> {
                    final int packedDate = DatePacker.valueOf(packing).pack(localDate);
                    final int packedTime = TimePacker.valueOf(packing).pack(localTime);
                    final long packedDateTime = packer.pack(packedDate, packing, packedTime, TimePacker.valueOf(packing));
                    final LocalDateTime unpacked = packer.unpackLocalDateTime(packedDateTime);
                    assertEquals(localDate, unpacked.toLocalDate(), packer + "|" + packing  + ": " + localDate + " " + localTime + " -> " + packedDateTime);
                    assertEquals(localTime.withNano(0), unpacked.toLocalTime(), packer + "|" + packing  + ": " + localDate + " " + localTime + " -> " + unpacked.toLocalTime());
                });
            }
        }

        @Test
        public void packFromPackedDateAndMilliTime() {
            for (final DateTimePacker packer : PACKERS) {
                Packing.forEach(packing -> {
                    final int packedDate = DatePacker.valueOf(packing).pack(localDate);
                    final int packedTime = MilliTimePacker.valueOf(packing).pack(localTime);
                    final long packedDateTime = packer.pack(packedDate, packing, packedTime, MilliTimePacker.valueOf(packing));
                    final LocalDateTime unpacked = packer.unpackLocalDateTime(packedDateTime);
                    assertEquals(localDate, unpacked.toLocalDate(), packer + "|" + packing  + ": " + localDate + " " + localTime + " -> " + packedDateTime);
                    assertEquals(localTime, unpacked.toLocalTime(), packer + "|" + packing  + ": " + localDate + " " + localTime + " -> " + unpacked.toLocalTime());
                });
            }
        }

        @Test
        public void packEpochMilli() {
            final long epochMilli = localDate.atTime(localTime).toInstant(ZoneOffset.UTC).toEpochMilli();
            for (final DateTimePacker packer : PACKERS) {
                final long packed = packer.packEpochMilli(epochMilli);
                final LocalDateTime localDateTime = packer.unpackLocalDateTime(packed);
                assertEquals(localDate, localDateTime.toLocalDate(), packer + ": " + localDate + " " + localTime + " -> " + packed);
                assertEquals(localTime, localDateTime.toLocalTime(), packer + ": " + localDate + " " + localTime + " -> " + packed);
            }
        }

        @Test
        public void unpackEpochMilli() {
            final LocalDateTime localDateTime = localDate.atTime(localTime);
            for (final DateTimePacker packer : PACKERS) {
                final long epochMilli = packer.unpackEpochMilli(packer.pack(localDateTime));
                assertEquals(localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli(), epochMilli, packer + ": " + localDate);
            }
        }
    }

    @Nested
    @ParameterizedClass
    @CsvSource(delimiter = '|', value = {
          //"  year | month | day | hour | minute | second | milli ",
            "     0 |    1  |   1 |    0 |     0  |      0 |     0 ",
            "    -1 |    1  |   1 |    0 |     0  |      0 |     0 ",
            " 10000 |    1  |   1 |    0 |     0  |      0 |     0 ",
            "  2017 |    0  |   1 |    0 |     0  |      0 |     0 ",
            "  2017 |   -1  |   1 |    0 |     0  |      0 |     0 ",
            "  2017 |   13  |   1 |    0 |     0  |      0 |     0 ",
            "  2017 |    1  |   0 |    0 |     0  |      0 |     0 ",
            "  2017 |    4  |  -1 |    0 |     0  |      0 |     0 |",//NOTE: day=-1 is equivalent to day31
            "  2017 |    1  |  32 |    0 |     0  |      0 |     0 ",
            "  2017 |    2  |  29 |    0 |     0  |      0 |     0 ",
            "  2016 |    2  |  30 |    0 |     0  |      0 |     0 ",
            "  2000 |    2  |  30 |    0 |     0  |      0 |     0 ",
            "  1900 |    2  |  29 |    0 |     0  |      0 |     0 ",
            "  1900 |    4  |  31 |    0 |     0  |      0 |     0 ",
            "  1900 |    6  |  31 |    0 |     0  |      0 |     0 ",
            "  1900 |    9  |  31 |    0 |     0  |      0 |     0 ",
            "  1900 |   11  |  31 |    0 |     0  |      0 |     0 ",
            "  2017 |    1  |   1 |   -1 |     1  |      1 |     0 ",
            "  2017 |    1  |   1 |    0 |    -1  |      1 |     0 ",
            "  2017 |    1  |   1 |    0 |     0  |     -1 |     0 ",
            "  2017 |    1  |   1 |    0 |     0  |      0 |    -1 ",
            "  2017 |    1  |   1 |   24 |     0  |      1 |     0 ",
            "  2017 |    1  |   1 |    0 |    60  |      1 |     0 ",
            "  2017 |    1  |   1 |    0 |     1  |     60 |     0 ",
    })
    class Invalid {
        @Parameter(0) int year;
        @Parameter(1) int month;
        @Parameter(2) int day;
        @Parameter(3) int hour;
        @Parameter(4) int minute;
        @Parameter(5) int second;
        @Parameter(6) int milli;

        @Test
        public void packIllegalYearMonthDayHourMinSecMilliBinary() {
            assertThrowsExactly(DateTimeException.class,
                    () -> DateTimePacker.BINARY.forValidationMethod(THROW_EXCEPTION).pack(year, month, day, hour, minute, second, milli)
            );
        }

        @Test
        public void packInvalidYearMonthDayHourMinSecMilliBinary() {
            final long packed = DateTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).pack(year, month, day, hour, minute, second, milli);
            assertEquals(DateTimePacker.INVALID, packed, "should be invalid");
        }

        @Test
        public void packIllegalYearMonthDayHourMinSecMilliDecimal() {
            assertThrowsExactly(DateTimeException.class,
                    () -> DateTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).pack(year, month, day, hour, minute, second, milli)
            );
        }

        @Test
        public void packInvalidYearMonthDayHourMinSecMilliDecimal() {
            final long packed = DateTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).pack(year, month, day, hour, minute, second, milli);
            assertEquals(DateTimePacker.INVALID, packed, "should be invalid");
        }

        @Test
        public void unpackIllegalYearMonthDayHourMinSecMilliBinary() {
            final long packed = DateTimePacker.BINARY.pack(year, month, day, hour, minute, second, milli);
            assertNotEquals(DateTimePacker.INVALID, packed, "should not be invalid");
            assertThrowsExactly(DateTimeException.class, () -> {
                DateTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackDay(packed);
                DateTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackHour(packed);
                DateTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackMinute(packed);
                DateTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackSecond(packed);
                DateTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackMilli(packed);
            });
        }

        @Test
        public void unpackInvalidYearMonthDayHourMinSecMilliBinary() {
            final long packed = DateTimePacker.BINARY.pack(year, month, day, hour, minute, second, milli);
            assertNotEquals(DateTimePacker.INVALID, packed, "should not be invalid");
            final int d = DateTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackDay(packed);
            final int h = DateTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackHour(packed);
            final int m = DateTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackMinute(packed);
            final int s = DateTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackSecond(packed);
            final int l = DateTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackMilli(packed);
            final long inv = DateTimePacker.INVALID;
            assertTrue(d == inv || h == inv || m == inv || s == inv || l == inv, "at least one should be invalid");
        }

        @Test
        public void unpackIllegalYearMonthDayHourMinSecMilliDecimal() {
            final long packed = DateTimePacker.DECIMAL.pack(year, month, day, hour, minute, second, milli);
            assertThrowsExactly(DateTimeException.class, () -> {
                DateTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackDay(packed);
                DateTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackHour(packed);
                DateTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackMinute(packed);
                DateTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackSecond(packed);
                DateTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackMilli(packed);
            });
        }

        @Test
        public void unpackInvalidYearMonthDayourMinSecMilliDecimal() {
            final long packed = DateTimePacker.DECIMAL.pack(year, month, day, hour, minute, second, milli);
            assertNotEquals(DateTimePacker.INVALID, packed, "should not be invalid");
            final int d = DateTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackDay(packed);
            final int h = DateTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackHour(packed);
            final int m = DateTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackMinute(packed);
            final int s = DateTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackSecond(packed);
            final int l = DateTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackMilli(packed);
            final long inv = DateTimePacker.INVALID;
            assertTrue(d == inv || h == inv || m == inv || s == inv || l == inv, "at least one should be invalid");
        }

        @Test
        public void unpackIllegalLocalDateTimeBinary() {
            final long packed = DateTimePacker.BINARY.pack(year, month, day, hour, minute, second, milli);
            assertNotEquals(DateTimePacker.INVALID, packed, "should not be invalid");
            assertThrowsExactly(DateTimeException.class, () ->
                DateTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackLocalDateTime(packed)
            );
        }

        @Test
        public void unpackInvalidLocalDateTimeBinary() {
            final long packed = DateTimePacker.BINARY.pack(year, month, day, hour, minute, second, milli);
            assertNotEquals(DateTimePacker.INVALID, packed, "should not be invalid");
            assertThrowsExactly(DateTimeException.class, () ->
                DateTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackLocalDateTime(packed)
            );
        }

        @Test
        public void unpackIllegalLocalDateTimeDecimal() {
            final long packed = DateTimePacker.DECIMAL.pack(year, month, day, hour, minute, second, milli);
            assertThrowsExactly(DateTimeException.class, () ->
                DateTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackLocalDateTime(packed)
            );
        }

        @Test
        public void unpackInvalidLocalDateTimeDecimal() {
            final long packed = DateTimePacker.DECIMAL.pack(year, month, day, hour, minute, second, milli);
            assertNotEquals(DateTimePacker.INVALID, packed, "should not be invalid");
            assertThrowsExactly(DateTimeException.class, () ->
                DateTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackLocalDateTime(packed)
            );
        }
    }

    @Nested
    @ParameterizedClass
    @EnumSource(Packing.class)
    class Special {

        @Parameter Packing packing;

        @Test
        public void packAndUnpackNull() {
            final DateTimePacker packer = DateTimePacker.valueOf(packing);
            final long packed1 = packer.packNull();
            final long packed2 = packer.pack(null);
            final boolean isNull1 = packer.unpackNull(packed1);
            final boolean isNull2 = packer.unpackNull(packed2);
            assertEquals(DateTimePacker.NULL, packed1, packer + ".packNull()");
            assertEquals(DateTimePacker.NULL, packed2, packer + ".pack(null)");
            assertTrue(isNull1, packer + ":unpackNull(packNull())");
            assertTrue(isNull2, packer + ":unpackNull(pack(null))");
        }

        @Test
        public void packing() throws Exception {
            final DateTimePacker packer = DateTimePacker.valueOf(packing);
            assertEquals(packing, packer.packing());
            assertEquals(packer, DateTimePacker.class.getField(packing.name()).get(null));
            assertEquals(DateTimePacker.class.getSimpleName() + "." + packing, packer.toString());
        }
    }

    private static DateTimePacker[] initPackers() {
        final DateTimePacker[] packers = new DateTimePacker[Packing.values().length * ValidationMethod.values().length];
        int index = 0;
        for (final Packing packing : Packing.values()) {
            for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                packers[index++] = DateTimePacker.valueOf(packing, validationMethod);
            }
        }
        return packers;
    }
}