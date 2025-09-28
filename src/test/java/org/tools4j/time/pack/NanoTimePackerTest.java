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
import java.time.Instant;
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
 * Unit test for {@link NanoTimePacker}.
 */
public class NanoTimePackerTest {

    private static final NanoTimePacker[] PACKERS = initPackers();
    private static final LocalDate[] DATES = {LocalDate.of(1931, 1, 1), LocalDate.of(1969, 12, 31), LocalDate.of(1970, 1,1), LocalDate.of(2017,6, 6), LocalDate.of(2036, 12, 31)};

    @Nested
    @ParameterizedClass
    @ValueSource(strings = {
            "00:00:00.000000000",
            "23:59:59.999999999",
            "01:01:01.111111111",
            "10:11:12.123456789",
            "11:59:59.999999999",
            "12:59:59.999999999",
            "12:34:56.789012345",
    })
    class Valid {

        private static final DateTimeFormatter HHMMSSNNNNNNNNN = DateTimeFormatter.ofPattern("HHmmssnnnnnnnnn");

        @Parameter LocalTime localTime;

        @Test
        public void packDecimal() {
            final long packed = NanoTimePacker.DECIMAL.pack(localTime);
            assertEquals(Long.parseLong(localTime.format(HHMMSSNNNNNNNNN)), packed);
        }

        @Test
        public void packBinary() {
            final long packed = NanoTimePacker.BINARY.pack(localTime);
            assertEquals((((long)localTime.getHour()) << 42) | (((long)localTime.getMinute()) << 36) | (((long)localTime.getSecond()) << 30) | localTime.getNano(),
                    packed);
        }

        @Test
        public void packAndUnpackLocalTime() {
            for (final NanoTimePacker packer : PACKERS) {
                final long packed = packer.pack(localTime);
                final LocalTime unpacked = packer.unpackLocalTime(packed);
                assertEquals(localTime, unpacked, packer + ": " + localTime + " -> " + packed);
            }
        }

        @Test
        public void packAndUnpackHourMinuteSecondNano() {
            for (final NanoTimePacker packer : PACKERS) {
                final long packed = packer.pack(localTime.getHour(), localTime.getMinute(), localTime.getSecond(), localTime.getNano());
                final int hour = packer.unpackHour(packed);
                final int minute = packer.unpackMinute(packed);
                final int second = packer.unpackSecond(packed);
                final int nano = packer.unpackNano(packed);
                assertEquals(localTime.getHour(), hour, packer + ": " + localTime + " -> " + packed + " [h]");
                assertEquals(localTime.getMinute(), minute, packer + ": " + localTime + " -> " + packed + " [m]");
                assertEquals(localTime.getSecond(), second, packer + ": " + localTime + " -> " + packed + " [s]");
                assertEquals(localTime.getNano(), nano, packer + ": " + localTime + " -> " + packed + " [n]");
            }
        }

        @Test
        public void packEpochNano() {
            for (final LocalDate date : DATES) {
                for (final NanoTimePacker packer : PACKERS) {
                    final Instant instant = localTime.atDate(date).toInstant(ZoneOffset.UTC);
                    final long packed = packer.packEpochNano(instant.toEpochMilli()
                            * TimeFactors.NANOS_PER_MILLI + (instant.getNano() % TimeFactors.NANOS_PER_MILLI));
                    final LocalTime unpacked = packer.unpackLocalTime(packed);
                    assertEquals(localTime, unpacked, packer + ": " + localTime + " -> " + packed);
                }
            }
        }

        @Test
        public void unpackMilliOfDay() {
            for (final NanoTimePacker packer : PACKERS) {
                final long epochMilli = packer.unpackMilliOfDay(packer.pack(localTime));
                assertEquals(localTime.toNanoOfDay() / NANOS_PER_MILLI, epochMilli, packer + ": " + localTime);
            }
        }

        @Test
        public void packEpochMilli() {
            final int milliInNanos = (localTime.getNano() / TimeFactors.NANOS_PER_MILLI) * TimeFactors.NANOS_PER_MILLI;
            final LocalTime milliTime = localTime.withNano(milliInNanos);
            for (final LocalDate date : DATES) {
                for (final NanoTimePacker packer : PACKERS) {
                    final long packed = packer.packEpochMilli(localTime.atDate(date).toInstant(ZoneOffset.UTC).toEpochMilli());
                    final LocalTime unpacked = packer.unpackLocalTime(packed);
                    assertEquals(milliTime, unpacked, packer + ": " + localTime + " -> " + packed);
                }
            }
        }

        @Test
        public void unpackNanoOfDay() {
            for (final NanoTimePacker packer : PACKERS) {
                final long epochNano = packer.unpackNanoOfDay(packer.pack(localTime));
                assertEquals(localTime.toNanoOfDay(), epochNano, packer + ": " + localTime);
            }
        }
    }

    @Nested
    @ParameterizedClass
    @CsvSource(delimiter = '|', value = {
          //" hour | minute | second |       nano ",
            "   -1 |     1  |      1 |          0 ",
            "    0 |    -1  |      1 |          0 ",
            "    0 |     0  |     -1 |          0 ",
            "    0 |     0  |      0 |         -2 ",
            "   24 |     0  |      1 |          0 ",
            "    0 |    60  |      1 |          0 ",
            "    0 |     1  |     60 |          0 ",
            "    0 |     0  |     59 | 1000000000 ",
    })
    class Invalid {
        @Parameter(0) int hour;
        @Parameter(1) int minute;
        @Parameter(2) int second;
        @Parameter(3) int nano;
        
        @Test
        public void packIllegalHourMinuteSecondNanoBinary() {
            assertThrowsExactly(DateTimeException.class, () ->
                NanoTimePacker.BINARY.forValidationMethod(THROW_EXCEPTION).pack(hour, minute, second, nano)
            );
        }

        @Test
        public void packInvalidHourMinuteSecondNanoBinary() {
            final long packed = NanoTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).pack(hour, minute, second, nano);
            assertEquals(NanoTimePacker.INVALID, packed, "should be invalid");
        }

        @Test
        public void packIllegalHourMinuteSecondNanoDecimal() {
            assertThrowsExactly(DateTimeException.class, () ->
                NanoTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).pack(hour, minute, second, nano)
            );
        }

        @Test
        public void packInvalidHourMinuteSecondNanoDecimal() {
            final long packed = NanoTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).pack(hour, minute, second, nano);
            assertEquals(NanoTimePacker.INVALID, packed, "should be invalid");
        }

        @Test
        public void unpackIllegalLocalTimeBinary() {
            assertThrowsExactly(DateTimeException.class, () -> {
                final long packed = NanoTimePacker.BINARY.pack(hour, minute, second, nano);
                assertNotEquals(NanoTimePacker.INVALID, packed, "should not be invalid");
                NanoTimePacker.BINARY.forValidationMethod(THROW_EXCEPTION).unpackLocalTime(packed);
            });
        }

        @Test
        public void unpackInvalidLocalTimeBinary() {
            assertThrowsExactly(DateTimeException.class, () -> {
                final long packed = NanoTimePacker.BINARY.pack(hour, minute, second, nano);
                assertNotEquals(NanoTimePacker.INVALID, packed, "should not be invalid");
                NanoTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackLocalTime(packed);
            });
        }

        @Test
        public void unpackIllegalLocalTimeDecimal() {
            assertThrowsExactly(DateTimeException.class, () -> {
                final long packed = NanoTimePacker.DECIMAL.pack(hour, minute, second, nano);
                assertNotEquals(NanoTimePacker.INVALID, packed, "should not be invalid");
                NanoTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackLocalTime(packed);
            });
        }

        @Test
        public void unpackInvalidLocalTimeDecimal() {
            assertThrowsExactly(DateTimeException.class, () -> {
                final long packed = NanoTimePacker.DECIMAL.pack(hour, minute, second, nano);
                assertNotEquals(NanoTimePacker.INVALID, packed, "should not be invalid");
                NanoTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackLocalTime(packed);
            });
        }

        @Test
        public void unpackIllegalHourMinuteSecondMilliBinary() {
            assertThrowsExactly(DateTimeException.class, () -> {
                final long packed = NanoTimePacker.BINARY.pack(hour, minute, second, nano);
                assertNotEquals(NanoTimePacker.INVALID, packed, "should not be invalid");
                NanoTimePacker.BINARY.forValidationMethod(THROW_EXCEPTION).unpackHour(packed);
                NanoTimePacker.BINARY.forValidationMethod(THROW_EXCEPTION).unpackMinute(packed);
                NanoTimePacker.BINARY.forValidationMethod(THROW_EXCEPTION).unpackSecond(packed);
                NanoTimePacker.BINARY.forValidationMethod(THROW_EXCEPTION).unpackNano(packed);
            });
        }

        @Test
        public void unpackInvalidHourMinuteSecondMilliBinary() {
            final long packed = NanoTimePacker.BINARY.pack(hour, minute, second, nano);
            assertNotEquals(NanoTimePacker.INVALID, packed, "should not be invalid");
            final int h = NanoTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackHour(packed);
            final int m = NanoTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackMinute(packed);
            final int s = NanoTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackSecond(packed);
            final int n = NanoTimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackNano(packed);
            final long inv = NanoTimePacker.INVALID;
            assertTrue(h == inv || m == inv || s == inv || n == inv, "at least one should be invalid");
        }

        @Test
        public void unpackIllegalHourMinuteSecondMilliDecimal() {
            assertThrowsExactly(DateTimeException.class, () -> {
                final long packed = NanoTimePacker.DECIMAL.pack(hour, minute, second, nano);
                assertNotEquals(NanoTimePacker.INVALID, packed, "should not be invalid");
                NanoTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackHour(packed);
                NanoTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackMinute(packed);
                NanoTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackSecond(packed);
                NanoTimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackNano(packed);
            });
        }

        @Test
        public void unpackInvalidHourMinuteSecondMilliDecimal() {
            final long packed = NanoTimePacker.DECIMAL.pack(hour, minute, second, nano);
            assertNotEquals(NanoTimePacker.INVALID, packed, "should not be invalid");
            final int h = NanoTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackHour(packed);
            final int m = NanoTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackMinute(packed);
            final int s = NanoTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackSecond(packed);
            final int l = NanoTimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackNano(packed);
            final long inv = NanoTimePacker.INVALID;
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
            final NanoTimePacker packer = NanoTimePacker.valueOf(packing);
            final long packed = packer.packNull();
            final boolean isNull = packer.unpackNull(packed);
            assertEquals(NanoTimePacker.NULL, packed, packer + ": pack null");
            assertTrue(isNull, packer + ": unpack null");
        }

        @Test
        public void packing() throws Exception {
            final NanoTimePacker packer = NanoTimePacker.valueOf(packing);
            assertEquals(packing, packer.packing());
            assertEquals(packer, NanoTimePacker.class.getField(packing.name()).get(null));
            assertEquals(NanoTimePacker.class.getSimpleName() + "." + packing, packer.toString());
        }
    }

    private static NanoTimePacker[] initPackers() {
        final NanoTimePacker[] packers = new NanoTimePacker[Packing.values().length * ValidationMethod.values().length];
        int index = 0;
        for (final Packing packing : Packing.values()) {
            for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                packers[index++] = NanoTimePacker.valueOf(packing, validationMethod);
            }
        }
        return packers;
    }
}