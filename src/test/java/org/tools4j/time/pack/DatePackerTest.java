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
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tools4j.time.base.TimeFactors.MILLIS_PER_DAY;
import static org.tools4j.time.validate.ValidationMethod.INVALIDATE_RESULT;
import static org.tools4j.time.validate.ValidationMethod.THROW_EXCEPTION;

/**
 * Unit test for {@link DatePacker}.
 */
public class DatePackerTest {

    private static final DatePacker[] PACKERS = initPackers();

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

        private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

        @Parameter LocalDate localDate;

        @Test
        public void packDecimal() {
            final int packed = DatePacker.DECIMAL.pack(localDate);
            assertEquals(Integer.parseInt(localDate.format(YYYYMMDD)), packed);
        }

        @Test
        public void packBinary() {
            final int packed = DatePacker.BINARY.pack(localDate);
            assertEquals((localDate.getYear() << 9) | (localDate.getMonthValue() << 5) | localDate.getDayOfMonth(),
                    packed);
        }

        @Test
        public void packAndUnpackLocalDate() {
            for (final DatePacker packer : PACKERS) {
                final int packed = packer.pack(localDate);
                final LocalDate unpacked = packer.unpackLocalDate(packed);
                assertEquals(localDate, unpacked, packer + ": " + localDate + " -> " + packed);
            }
        }

        @Test
        public void packAndUnpackYearMonthDay() {
            for (final DatePacker packer : PACKERS) {
                final int packed = packer.pack(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
                final int year = packer.unpackYear(packed);
                final int month = packer.unpackMonth(packed);
                final int day = packer.unpackDay(packed);
                assertEquals(localDate.getYear(), year, packer + ": " + localDate + " -> " + packed + " [y]");
                assertEquals(localDate.getMonthValue(), month, packer + ": " + localDate + " -> " + packed + " [m]");
                assertEquals(localDate.getDayOfMonth(), day, packer + ": " + localDate + " -> " + packed + " [d]");
            }
        }

        @Test
        public void packEpochDay() {
            for (final DatePacker packer : PACKERS) {
                final int packed = packer.packEpochDay(localDate.toEpochDay());
                final LocalDate unpacked = packer.unpackLocalDate(packed);
                assertEquals(localDate, unpacked, packer + ": " + localDate + " -> " + packed);
            }
        }

        @Test
        public void unpackEpochDay() {
            for (final DatePacker packer : PACKERS) {
                final long epochDay = packer.unpackEpochDay(packer.pack(localDate));
                assertEquals(localDate.toEpochDay(), epochDay, packer + ": " + localDate);
            }
        }

        @Test
        public void packEpochMilli() {
            for (final DatePacker packer : PACKERS) {
                final int packed = packer.packEpochMilli(localDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli());
                final LocalDate unpacked = packer.unpackLocalDate(packed);
                assertEquals(localDate, unpacked, packer + ": " + localDate + " -> " + packed);
            }
        }

        @Test
        public void packFromPackedDateTime() {
            for (final DatePacker packer : PACKERS) {
                Packing.forEach(packing -> {
                    final long packedDateTime = DateTimePacker.valueOf(packing).pack(localDate.atStartOfDay());
                    final int packedDate = packer.packFromDateTime(packedDateTime, packing);
                    final LocalDate unpacked = packer.unpackLocalDate(packedDate);
                    assertEquals(localDate, unpacked, packer + "|" + packing + ": " + localDate + " -> " + packedDate);
                });
            }
        }

        @Test
        public void unpackEpochMilli() {
            for (final DatePacker packer : PACKERS) {
                final long epochMilli = packer.unpackEpochMilli(packer.pack(localDate));
                assertEquals(localDate.toEpochDay() * MILLIS_PER_DAY, epochMilli, packer + ": " + localDate);
            }
        }
    }

    @Nested
    @ParameterizedClass
    @CsvSource(delimiter = '|', value = {
          //"  year | month | day ",
            "     0 |    1  |   1 ",
            "    -1 |    1  |   1 ",
            " 10000 |    1  |   1 ",
            "  2017 |    0  |   1 ",
            "  2017 |   -1  |   1 ",
            "  2017 |   13  |   1 ",
            "  2017 |    1  |   0 ",
            "  2017 |    4  |  -1 ",//NOTE: day=-1 is equivalent to day=31
            "  2017 |    1  |  32 ",
            "  2017 |    2  |  29 ",
            "  2016 |    2  |  30 ",
            "  2000 |    2  |  30 ",
            "  1900 |    2  |  29 ",
            "  1900 |    4  |  31 ",
            "  1900 |    6  |  31 ",
            "  1900 |    9  |  31 ",
            "  1900 |   11  |  31 ",
    })
    public class Invalid {
        @Parameter(0) int year;
        @Parameter(1) int month;
        @Parameter(2) int day;

        @Test
        public void packIllegalYearMonthDayBinary() {
            assertThrowsExactly(DateTimeException.class,
                    () -> DatePacker.BINARY.forValidationMethod(THROW_EXCEPTION).pack(year, month, day)
            );
        }

        @Test
        public void packInvalidYearMonthDayBinary() {
            final int packed = DatePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).pack(year, month, day);
            assertEquals(DatePacker.INVALID, packed, "should be invalid");
        }

        @Test
        public void packIllegalYearMonthDayDecimal() {
            assertThrowsExactly(DateTimeException.class, () ->
                    DatePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).pack(year, month, day)
            );
        }

        @Test
        public void packInvalidYearMonthDayDecimal() {
            final int packed = DatePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).pack(year, month, day);
            assertEquals(DatePacker.INVALID, packed, "should be invalid");
        }

        @Test
        public void unpackIllegalYearMonthDayBinary() {
            final int packed = DatePacker.BINARY.pack(year, month, day);
            assertThrowsExactly(DateTimeException.class, () ->
                DatePacker.BINARY.forValidationMethod(THROW_EXCEPTION).unpackDay(packed)
            );
        }

        @Test
        public void unpackInvalidYearMonthDayBinary() {
            final int packed = DatePacker.BINARY.pack(year, month, day);
            assertNotEquals(DatePacker.INVALID, packed, "should not be invalid");
            final int invalid = DatePacker.BINARY.forValidationMethod(INVALIDATE_RESULT).unpackDay(packed);
            assertEquals(DatePacker.INVALID, invalid, "should be invalid");
        }

        @Test
        public void unpackIllegalYearMonthDayDecimal() {
            final int packed = DatePacker.DECIMAL.pack(year, month, day);
            assertThrowsExactly(DateTimeException.class, () ->
                    DatePacker.DECIMAL.forValidationMethod(THROW_EXCEPTION).unpackDay(packed)
            );
        }

        @Test
        public void unpackInvalidYearMonthDayDecimal() {
            final int packed = DatePacker.DECIMAL.pack(year, month, day);
            assertNotEquals(DatePacker.INVALID, packed, "should not be invalid");
            final int invalid = DatePacker.DECIMAL.forValidationMethod(INVALIDATE_RESULT).unpackDay(packed);
            assertEquals(DatePacker.INVALID, invalid, "should be invalid");
        }
    }

    @Nested
    @ParameterizedClass
    @EnumSource(Packing.class)
    public class Special {

        @Parameter Packing packing;

        @Test
        public void packAndUnpackNull() {
            final DatePacker packer = DatePacker.valueOf(packing);
            final int packed1 = packer.packNull();
            final int packed2 = packer.pack(null);
            final boolean isNull1 = packer.unpackNull(packed1);
            final boolean isNull2 = packer.unpackNull(packed2);
            assertEquals(0, packed1, packer + ".packNull()");
            assertEquals(0, packed2, packer + ".pack(null)");
            assertTrue(isNull1, packer + ":unpackNull(packNull())");
            assertTrue(isNull2, packer + ":unpackNull(pack(null))");
        }

        @Test
        public void packing() throws Exception {
            final DatePacker packer = DatePacker.valueOf(packing);
            assertEquals(packing, packer.packing());
            assertEquals(packer, DatePacker.class.getField(packing.name()).get(null));
            assertEquals(DatePacker.class.getSimpleName() + "." + packing, packer.toString());
        }
    }

    private static DatePacker[] initPackers() {
        final DatePacker[] packers = new DatePacker[Packing.values().length * ValidationMethod.values().length];
        int index = 0;
        for (final Packing packing : Packing.values()) {
            for (final ValidationMethod validationMethod : ValidationMethod.values()) {
                packers[index++] = DatePacker.valueOf(packing, validationMethod);
            }
        }
        return packers;
    }
}