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
import org.junit.jupiter.params.provider.ValueSource;
import org.tools4j.time.base.TimeFactors;
import org.tools4j.time.validate.ValidationMethod;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tools4j.time.base.TimeFactors.NANOS_PER_MILLI;
import static org.tools4j.time.validate.ValidationMethod.INVALIDATE_RESULT;
import static org.tools4j.time.validate.ValidationMethod.THROW_EXCEPTION;

/**
 * Unit test for {@link MilliTimePacker}.
 */
public class MilliTimePackerTest {

    private static final MilliTimePacker[] PACKERS = initPackers();
    private static final LocalDate[] DATES = {LocalDate.of(1, 1, 1), LocalDate.of(1969, 12, 31), LocalDate.of(1970, 1,1), LocalDate.of(2017,6, 6), LocalDate.of(9999, 12, 31)};

    @Nested
    @ParameterizedClass
    @ValueSource(strings = {
            "00:00:00.000",
            "23:59:59.999",
            "01:01:01.111",
            "10:11:12.123",
            "11:59:59.999",
            "12:59:59.999",
            "12:34:56.789",
    })
    class Valid {

        private static final DateTimeFormatter HHMMSSMMM = DateTimeFormatter.ofPattern("HHmmssSSS");

        @Parameter LocalTime localTime;

        @Test
        public void packDecimal() {
            final int packed = MilliTimePacker.DECIMAL.pack(localTime);
            assertEquals(Integer.parseInt(localTime.format(HHMMSSMMM)), packed);
        }

        @Test
        public void packBinary() {
            final int packed = MilliTimePacker.BINARY.pack(localTime);
            assertEquals((localTime.getHour() << 22) | (localTime.getMinute() << 16) | (localTime.getSecond() << 10) | (localTime.getNano() / TimeFactors.NANOS_PER_MILLI),
                    packed);
        }

        @Test
        public void packAndUnpackLocalTime() {
            for (final MilliTimePacker packer : PACKERS) {
                final int packed = packer.pack(localTime);
                final LocalTime unpacked = packer.unpackLocalTime(packed);
                assertEquals(localTime, unpacked, packer + ": " + localTime + " -> " + packed);
            }
        }

        @Test
        public void packAndUnpackHourMinuteSecondMilli() {
            for (final MilliTimePacker packer : PACKERS) {
                final int packed = packer.pack(localTime.getHour(), localTime.getMinute(), localTime.getSecond(), localTime.getNano() / TimeFactors.NANOS_PER_MILLI);
                final int hour = packer.unpackHour(packed);
                final int minute = packer.unpackMinute(packed);
                final int second = packer.unpackSecond(packed);
                final int milli = packer.unpackMilli(packed);
                assertEquals(localTime.getHour(), hour, packer + ": " + localTime + " -> " + packed + " [h]");
                assertEquals(localTime.getMinute(), minute, packer + ": " + localTime + " -> " + packed + " [m]");
                assertEquals(localTime.getSecond(), second, packer + ": " + localTime + " -> " + packed + " [s]");
                assertEquals(localTime.getNano() / TimeFactors.NANOS_PER_MILLI, milli, packer + ": " + localTime + " -> " + packed + " [S]");
            }
        }

        @Test
        public void packFromPackedDateTime() {
            for (final MilliTimePacker packer : PACKERS) {
                Packing.forEach(packing -> {
                    final long packedDateTime = DateTimePacker.valueOf(packing).pack(localTime.atDate(LocalDate.now()));
                    final int packedTime = packer.packFromDateTime(packedDateTime, packing);
                    final LocalTime unpacked = packer.unpackLocalTime(packedTime);
                    assertEquals(localTime, unpacked, packer + "|" + packing + ": " + localTime + " -> " + packedTime);
                });
            }
        }

        @Test
        public void packEpochMilli() {
            for (final LocalDate date : DATES) {
                final long epochMilli = localTime.atDate(date).toInstant(ZoneOffset.UTC).toEpochMilli();
                for (final MilliTimePacker packer : PACKERS) {
                    final int packed = packer.packEpochMilli(epochMilli);
                    final LocalTime unpacked = packer.unpackLocalTime(packed);
                    assertEquals(localTime, unpacked, packer + ": " + localTime + " -> " + packed);
                }
            }
        }

        @Test
        public void unpackMilliOfDay() {
            for (final MilliTimePacker packer : PACKERS) {
                final long epochMilli = packer.unpackMilliOfDay(packer.pack(localTime));
                assertEquals(localTime.toNanoOfDay() / NANOS_PER_MILLI, epochMilli, packer + ": " + localTime);
            }
        }
    }

    @Nested
    @ParameterizedClass
    @CsvSource(delimiter = '|', value = {
          //" hour | minute | second | milli ",
            "   -1 |     1  |      1 |     0 ",
            "    0 |    -1  |      1 |     0 ",
            "    0 |     0  |     -1 |     0 ",
            "    1 |     0  |      0 |    -1 ",
            "   24 |     0  |      1 |     0 ",
            "    0 |    60  |      1 |     0 ",
            "    0 |     1  |     60 |     0 ",
            "    0 |     0  |     59 |  1000 ",
    })
    class Invalid {
        @Parameter(0) int hour;
        @Parameter(1) int minute;
        @Parameter(2) int second;
        @Parameter(3) int milli;

        @Test
        public void packIllegalHourMinuteSecondMilliBinary() {
            assertThrowsExactly(DateTimeException.class, () ->
                MilliTimePacker.BINARY.forValidationMethod(THROW_EXCEPTION).pack(hour, minute, second, milli)
            );
        }

        @Test
        public void packInvalidHourMinuteSecondMilliBinary() {
            final int packed = MilliTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).pack(hour, minute, second, milli);
            assertEquals(MilliTimePacker.INVALID, packed, "should be invalid");
        }

        @Test
        public void packIllegalHourMinuteSecondMilliDecimal() {
            assertThrowsExactly(DateTimeException.class, () ->
                MilliTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).pack(hour, minute, second, milli)
            );
        }

        @Test
        public void packInvalidHourMinuteSecondMilliDecimal() {
            final int packed = MilliTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).pack(hour, minute, second, milli);
            assertEquals(MilliTimePacker.INVALID, packed, "should be invalid");
        }

        @Test
        public void unpackIllegalLocalTimeBinary() {
            assertThrowsExactly(DateTimeException.class, () -> {
                final int packed = MilliTimePacker.BINARY.pack(hour, minute, second, milli);
                assertNotEquals(MilliTimePacker.INVALID, packed, "should not be invalid");
                MilliTimePacker.BINARY.forValidationMethod(THROW_EXCEPTION).unpackLocalTime(packed);
            });
        }

        @Test
        public void unpackInvalidLocalTimeBinary() {
            assertThrowsExactly(DateTimeException.class, () -> {
                final int packed = MilliTimePacker.BINARY.pack(hour, minute, second, milli);
                assertNotEquals(MilliTimePacker.INVALID, packed, "should not be invalid");
                MilliTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackLocalTime(packed);
            });
        }

        @Test
        public void unpackIllegalLocalTimeDecimal() {
            assertThrowsExactly(DateTimeException.class, () -> {
                final int packed = MilliTimePacker.DECIMAL.pack(hour, minute, second, milli);
                assertNotEquals(MilliTimePacker.INVALID, packed, "should not be invalid");
                MilliTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackLocalTime(packed);
            });
        }

        @Test
        public void unpackInvalidLocalTimeDecimal() {
            assertThrowsExactly(DateTimeException.class, () -> {
                final int packed = MilliTimePacker.DECIMAL.pack(hour, minute, second, milli);
                assertNotEquals(MilliTimePacker.INVALID, packed, "should not be invalid");
                MilliTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackLocalTime(packed);
            });
        }

        @Test
        public void unpackIllegalHourMinuteSecondMilliBinary() {
            assertThrowsExactly(DateTimeException.class, () -> {
                final int packed = MilliTimePacker.BINARY.pack(hour, minute, second, milli);
                assertNotEquals(MilliTimePacker.INVALID, packed, "should not be invalid");
                MilliTimePacker.BINARY.forValidationMethod(THROW_EXCEPTION).unpackHour(packed);
                MilliTimePacker.BINARY.forValidationMethod(THROW_EXCEPTION).unpackMinute(packed);
                MilliTimePacker.BINARY.forValidationMethod(THROW_EXCEPTION).unpackSecond(packed);
                MilliTimePacker.BINARY.forValidationMethod(THROW_EXCEPTION).unpackMilli(packed);
            });
        }

        @Test
        public void unpackInvalidHourMinuteSecondMilliBinary() {
            final int packed = MilliTimePacker.BINARY.pack(hour, minute, second, milli);
            assertNotEquals(MilliTimePacker.INVALID, packed, "should not be invalid");
            final int h = MilliTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackHour(packed);
            final int m = MilliTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackMinute(packed);
            final int s = MilliTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackSecond(packed);
            final int l = MilliTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackMilli(packed);
            final int inv = MilliTimePacker.INVALID;
            assertTrue(h == inv || m == inv || s == inv || l == inv, "at least one should be invalid");
        }

        @Test
        public void unpackIllegalHourMinuteSecondMilliDecimal() {
            assertThrowsExactly(DateTimeException.class, () -> {
                final int packed = MilliTimePacker.DECIMAL.pack(hour, minute, second, milli);
                assertNotEquals(MilliTimePacker.INVALID, packed, "should not be invalid");
                MilliTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackHour(packed);
                MilliTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackMinute(packed);
                MilliTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackSecond(packed);
                MilliTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackMilli(packed);
            });
        }

        @Test
        public void unpackInvalidHourMinuteSecondMilliDecimal() {
            final int packed = MilliTimePacker.DECIMAL.pack(hour, minute, second, milli);
            assertNotEquals(MilliTimePacker.INVALID, packed, "should not be invalid");
            final int h = MilliTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackHour(packed);
            final int m = MilliTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackMinute(packed);
            final int s = MilliTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackSecond(packed);
            final int l = MilliTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackMilli(packed);
            final int inv = MilliTimePacker.INVALID;
            assertTrue(h == inv || m == inv || s == inv || l == inv, "at least one should be invalid");
        }
    }

    @Nested
    @ParameterizedClass
    @EnumSource(Packing.class)
    class Special {

        @Parameter Packing packing;

        @Test
        public void packAndUnpackNull() {
            final MilliTimePacker packer = MilliTimePacker.valueOf(packing);
            final int packed = packer.packNull();
            final boolean isNull = packer.unpackNull(packed);
            assertEquals(MilliTimePacker.NULL, packed, packer + ": pack null");
            assertTrue(isNull, packer + ": unpack null");
        }

        @Test
        public void packing() throws Exception {
            final MilliTimePacker packer = MilliTimePacker.valueOf(packing);
            assertEquals(packing, packer.packing());
            assertEquals(packer, MilliTimePacker.class.getField(packing.name()).get(null));
            assertEquals(MilliTimePacker.class.getSimpleName() + "." + packing, packer.toString());
        }
    }

    private static MilliTimePacker[] initPackers() {
        final MilliTimePacker[] packers = new MilliTimePacker[Packing.values().length * ValidationMethod.values().length];
        int index = 0;
        for (final Packing packing : Packing.values()) {
            for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                packers[index++] = MilliTimePacker.valueOf(packing, validationMethod);
            }
        }
        return packers;
    }
}