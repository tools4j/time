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
import org.tools4j.time.validate.ValidationMethod;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tools4j.time.base.TimeFactors.NANOS_PER_MILLI;
import static org.tools4j.time.validate.ValidationMethod.INVALIDATE_RESULT;
import static org.tools4j.time.validate.ValidationMethod.THROW_EXCEPTION;

/**
 * Unit test for {@link TimePacker}.
 */
public class TimePackerTest {

    private static final TimePacker[] PACKERS = initPackers();
    private static final LocalDate[] DATES = {LocalDate.of(1, 1, 1), LocalDate.of(1969, 12, 31), LocalDate.of(1970, 1,1), LocalDate.of(2017,6, 6), LocalDate.of(9999, 12, 31)};

    @Nested
    @ParameterizedClass
    @ValueSource(strings = {
            "00:00:00",
            "23:59:59",
            "01:01:01",
            "10:11:12",
            "11:59:59",
            "12:59:59",
            "12:34:56",
    })
    class Valid {

        private static final DateTimeFormatter HHMMSS = DateTimeFormatter.ofPattern("HHmmss");
        
        @Parameter LocalTime localTime;

        @Test
        public void packDecimal() {
            final int packed = TimePacker.DECIMAL.pack(localTime);
            assertEquals(Integer.parseInt(localTime.format(HHMMSS)), packed);
        }

        @Test
        public void packBinary() {
            final int packed = TimePacker.BINARY.pack(localTime);
            assertEquals((localTime.getHour() << 12) | (localTime.getMinute() << 6) | localTime.getSecond(),
                    packed);
        }

        @Test
        public void packAndUnpackLocalTime() {
            for (final TimePacker packer : PACKERS) {
                final int packed = packer.pack(localTime);
                final LocalTime unpacked = packer.unpackLocalTime(packed);
                assertEquals(localTime, unpacked, packer + ": " + localTime + " -> " + packed);
            }
        }

        @Test
        public void packAndUnpackHourMinuteSecond() {
            for (final TimePacker packer : PACKERS) {
                final int packed = packer.pack(localTime.getHour(), localTime.getMinute(), localTime.getSecond());
                final int hour = packer.unpackHour(packed);
                final int minute = packer.unpackMinute(packed);
                final int second = packer.unpackSecond(packed);
                assertEquals(localTime.getHour(), hour, packer + ": " + localTime + " -> " + packed + " [h]");
                assertEquals(localTime.getMinute(), minute, packer + ": " + localTime + " -> " + packed + " [m]");
                assertEquals(localTime.getSecond(), second, packer + ": " + localTime + " -> " + packed + " [s]");
            }
        }

        @Test
        public void packEpochSecond() {
            for (final LocalDate date : DATES) {
                for (final TimePacker packer : PACKERS) {
                    final int packed = packer.packEpochSecond(localTime.atDate(date).toEpochSecond(ZoneOffset.UTC));
                    final LocalTime unpacked = packer.unpackLocalTime(packed);
                    assertEquals(localTime, unpacked, packer + ": " + localTime + " -> " + packed);
                }
            }
        }

        @Test
        public void unpackSecondOfDay() {
            for (final TimePacker packer : PACKERS) {
                final long epochMilli = packer.unpackSecondOfDay(packer.pack(localTime));
                assertEquals(localTime.toSecondOfDay(), epochMilli, packer + ": " + localTime);
            }
        }

        @Test
        public void packFromPackedDateTime() {
            for (final TimePacker packer : PACKERS) {
                Packing.forEach(packing -> {
                    final long packedDateTime = DateTimePacker.valueOf(packing).pack(localTime.atDate(LocalDate.now()));
                    final int packedTime = packer.pack(packedDateTime, packing);
                    final LocalTime unpacked = packer.unpackLocalTime(packedTime);
                    assertEquals(localTime, unpacked, packer + "|" + packing + ": " + localTime + " -> " + packedTime);
                });
            }
        }

        @Test
        public void packEpochMilli() {
            for (final LocalDate date : DATES) {
                for (final TimePacker packer : PACKERS) {
                    final int packed = packer.packEpochMilli(localTime.atDate(date).toInstant(ZoneOffset.UTC).toEpochMilli());
                    final LocalTime unpacked = packer.unpackLocalTime(packed);
                    assertEquals(localTime, unpacked, packer + ": " + localTime + " -> " + packed);
                }
            }
        }

        @Test
        public void unpackMilliOfDay() {
            for (final TimePacker packer : PACKERS) {
                final long epochMilli = packer.unpackMilliOfDay(packer.pack(localTime));
                assertEquals(localTime.toNanoOfDay() / NANOS_PER_MILLI, epochMilli, packer + ": " + localTime);
            }
        }

        @Test
        public void isValidBinary() {
            isValid(TimePacker.BINARY);
        }

        @Test
        public void isValidDecimal() {
            isValid(TimePacker.DECIMAL);
        }

        private void isValid(final TimePacker packer) {
            final int packed = packer.pack(localTime);
            assertNotEquals(TimePacker.INVALID, packed, "should not be invalid");
            for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                final TimePacker validatingPacker = packer.forValidationMethod(validationMethod);
                assertTrue(validatingPacker.isValid(packed), localTime + " should be valid");
            }
        }

        @Test
        public void validateBinary() {
            validate(TimePacker.BINARY);
        }

        @Test
        public void validateDecimal() {
            validate(TimePacker.DECIMAL);
        }

        private void validate(final TimePacker packer) {
            final int packed = packer.pack(localTime);
            assertNotEquals(TimePacker.INVALID, packed, "should not be invalid");
            for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                final TimePacker validatingPacker = packer.forValidationMethod(validationMethod);
                assertEquals(packed, validatingPacker.validate(packed),
                        "validate(..) for " + localTime);
                assertEquals(packed, packer.validate(packed, validationMethod),
                        "validate(.., " + validationMethod + ") for " + localTime);
            }
        }
    }

    @Nested
    @ParameterizedClass
    @CsvSource(delimiter = '|', value = {
          //" hour | minute | second ",
            "   -1 |     1  |      1 ",
            "    0 |    -1  |      1 ",
            "    0 |     0  |     -2 ",
            "   24 |     0  |      1 ",
            "    0 |    60  |      1 ",
            "    0 |     1  |     60 ",
    })
    class Invalid {

        @Parameter(0) int hour;
        @Parameter(1) int minute;
        @Parameter(2) int second;

        @Test
        public void packIllegalHourMinuteSecondBinary() {
            assertThrowsExactly(DateTimeException.class, () ->
                TimePacker.BINARY.forValidationMethod(THROW_EXCEPTION).pack(hour, minute, second)
            );
        }

        @Test
        public void packInvalidHourMinuteSecondBinary() {
            final int packed = TimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).pack(hour, minute, second);
            assertEquals(TimePacker.INVALID, packed, "should be invalid");
        }

        @Test
        public void packIllegalHourMinuteSecondDecimal() {
            assertThrowsExactly(DateTimeException.class, () ->
                TimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).pack(hour, minute, second)
            );
        }

        @Test
        public void packInvalidHourMinuteSecondDecimal() {
            final int packed = TimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).pack(hour, minute, second);
            assertEquals(TimePacker.INVALID, packed, "should be invalid");
        }

        @Test
        public void unpackIllegalLocalTimeBinary() {
            assertThrowsExactly(DateTimeException.class, () -> {
                final int packed = TimePacker.BINARY.pack(hour, minute, second);
                assertNotEquals(TimePacker.INVALID, packed, "should not be invalid");
                TimePacker.BINARY.forValidationMethod(THROW_EXCEPTION).unpackLocalTime(packed);
            });
        }

        @Test
        public void unpackInvalidLocalTimeBinary() {
            assertThrowsExactly(DateTimeException.class, () -> {
                final int packed = TimePacker.BINARY.pack(hour, minute, second);
                assertNotEquals(TimePacker.INVALID, packed, "should not be invalid");
                TimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackLocalTime(packed);
            });
        }

        @Test
        public void unpackIllegalLocalTimeDecimal() {
            assertThrowsExactly(DateTimeException.class, () -> {
                final int packed = TimePacker.DECIMAL.pack(hour, minute, second);
                assertNotEquals(TimePacker.INVALID, packed, "should not be invalid");
                TimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackLocalTime(packed);
            });
        }

        @Test
        public void unpackInvalidLocalTimeDecimal() {
            assertThrowsExactly(DateTimeException.class, () -> {
                final int packed = TimePacker.DECIMAL.pack(hour, minute, second);
                assertNotEquals(TimePacker.INVALID, packed, "should not be invalid");
                TimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackLocalTime(packed);
            });
        }

        @Test
        public void unpackIllegalHourMinuteSecondMilliBinary() {
            assertThrowsExactly(DateTimeException.class, () -> {
                final int packed = TimePacker.BINARY.pack(hour, minute, second);
                assertNotEquals(TimePacker.INVALID, packed, "should not be invalid");
                TimePacker.BINARY.forValidationMethod(THROW_EXCEPTION).unpackHour(packed);
                TimePacker.BINARY.forValidationMethod(THROW_EXCEPTION).unpackMinute(packed);
                TimePacker.BINARY.forValidationMethod(THROW_EXCEPTION).unpackSecond(packed);
            });
        }

        @Test
        public void unpackInvalidHourMinuteSecondMilliBinary() {
            final int packed = TimePacker.BINARY.pack(hour, minute, second);
            assertNotEquals(TimePacker.INVALID, packed, "should not be invalid");
            final int h = TimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackHour(packed);
            final int m = TimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackMinute(packed);
            final int s = TimePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackSecond(packed);
            final int inv = TimePacker.INVALID;
            assertTrue(h == inv || m == inv || s == inv, "at least one should be invalid");
        }

        @Test
        public void unpackIllegalHourMinuteSecondMilliDecimal() {
            assertThrowsExactly(DateTimeException.class, () -> {
                final int packed = TimePacker.DECIMAL.pack(hour, minute, second);
                assertNotEquals(TimePacker.INVALID, packed, "should not be invalid");
                TimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackHour(packed);
                TimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackMinute(packed);
                TimePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackSecond(packed);
            });
        }

        @Test
        public void unpackInvalidHourMinuteSecondMilliDecimal() {
            final int packed = TimePacker.DECIMAL.pack(hour, minute, second);
            assertNotEquals(TimePacker.INVALID, packed, "should not be invalid");
            final int h = TimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackHour(packed);
            final int m = TimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackMinute(packed);
            final int s = TimePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackSecond(packed);
            final int inv = TimePacker.INVALID;
            assertTrue(h == inv || m == inv || s == inv, "at least one should be invalid");
        }

        @Test
        public void isValidBinary() {
            isValid(TimePacker.BINARY);
        }

        @Test
        public void isValidDecimal() {
            isValid(TimePacker.DECIMAL);
        }

        private void isValid(final TimePacker packer) {
            final int packed = packer.pack(hour, minute, second);
            assertNotEquals(TimePacker.INVALID, packed, "should not be invalid");
            for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                final TimePacker validatingPacker = packer.forValidationMethod(validationMethod);
                assertFalse(validatingPacker.isValid(packed));
            }
        }

        @Test
        public void validateBinary() {
            validate(TimePacker.BINARY);
        }

        @Test
        public void validateDecimal() {
            validate(TimePacker.DECIMAL);
        }

        private void validate(final TimePacker packer) {
            final int packed = packer.pack(hour, minute, second);
            assertNotEquals(TimePacker.INVALID, packed, "should not be invalid");
            for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                final TimePacker validatingPacker = packer.forValidationMethod(validationMethod);
                switch (validationMethod) {
                    case UNVALIDATED:
                        assertEquals(packed, validatingPacker.validate(packed));
                        assertEquals(packed, packer.validate(packed, validationMethod));
                        break;
                    case INVALIDATE_RESULT:
                        assertEquals(TimePacker.INVALID, validatingPacker.validate(packed));
                        assertEquals(TimePacker.INVALID, packer.validate(packed, validationMethod));
                        break;
                    case THROW_EXCEPTION:
                        assertThrowsExactly(DateTimeException.class, () -> validatingPacker.validate(packed));
                        assertThrowsExactly(DateTimeException.class, () -> packer.validate(packed, validationMethod));
                        break;
                    default:
                        throw new RuntimeException("Unsupported validation method: " + validationMethod);
                }
            }
        }
    }

    @Nested
    @ParameterizedClass
    @EnumSource(Packing.class)
    class Special {
        
        @Parameter Packing packing;
        
        @Test
        public void packAndUnpackNull() {
            final TimePacker packer = TimePacker.valueOf(packing);
            final int packed = packer.packNull();
            final boolean isNull = packer.unpackNull(packed);
            assertEquals(TimePacker.NULL, packed, packer + ": pack null");
            assertTrue(isNull, packer + ": unpack null");
        }

        @Test
        public void validateNull() {
            final TimePacker packer = TimePacker.valueOf(packing);
            assertTrue(packer.isValid(TimePacker.NULL));
            assertEquals(TimePacker.NULL, packer.validate(packer.packNull()));
            for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                assertEquals(TimePacker.NULL, packer.validate(packer.packNull(), validationMethod));
            }
        }

        @Test
        public void validateInvalid() {
            final TimePacker packer = TimePacker.valueOf(packing);
            assertFalse(packer.isValid(TimePacker.INVALID));
            assertEquals(TimePacker.INVALID, packer.validate(TimePacker.INVALID));
            for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                if (validationMethod == THROW_EXCEPTION) {
                    assertThrowsExactly(DateTimeException.class, () ->
                            packer.validate(TimePacker.INVALID, validationMethod)
                    );
                } else {
                    assertEquals(TimePacker.INVALID, packer.validate(TimePacker.INVALID, validationMethod));
                }
            }
        }

        @Test
        public void packing() throws Exception {
            final TimePacker packer = TimePacker.valueOf(packing);
            assertEquals(packing, packer.packing());
            assertEquals(packer, TimePacker.class.getField(packing.name()).get(null));
            assertEquals(TimePacker.class.getSimpleName() + "." + packing, packer.toString());
        }
    }

    private static TimePacker[] initPackers() {
        final TimePacker[] packers = new TimePacker[Packing.values().length * ValidationMethod.values().length];
        int index = 0;
        for (final Packing packing : Packing.values()) {
            for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                packers[index++] = TimePacker.valueOf(packing, validationMethod);
            }
        }
        return packers;
    }
}