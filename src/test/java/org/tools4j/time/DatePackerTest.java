/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 tools4j.org (Marco Terzer)
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
package org.tools4j.time;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.tools4j.spockito.Spockito;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link DatePacker}.
 */
public class DatePackerTest {

    private static final DatePacker[] PACKERS = {DatePacker.BINARY, DatePacker.DECIMAL};

    @RunWith(Spockito.class)
    @Spockito.Unroll({
            "|  localDate |",
            "| 2017-01-01 |",
            "| 2017-01-31 |",
            "| 2017-02-28 |",
            "| 2017-03-31 |",
            "| 2017-04-30 |",
            "| 2017-05-31 |",
            "| 2017-06-30 |",
            "| 2017-07-31 |",
            "| 2017-08-31 |",
            "| 2017-09-30 |",
            "| 2017-10-31 |",
            "| 2017-11-30 |",
            "| 2017-12-31 |",
            "| 2017-12-31 |",
            "| 2016-02-29 |",
            "| 2000-02-29 |",
            "| 1900-02-28 |",
            "| 1970-01-01 |",
            "| 1970-01-02 |",
            "| 1969-12-31 |",
            "| 1969-12-30 |",
            "| 1969-04-30 |",
            "| 1968-02-28 |",
            "| 1600-02-29 |",
            "| 0004-02-29 |",
            "| 0100-02-28 |",
            "| 0400-02-29 |",
            "| 0001-01-01 |",
            "| 9999-12-31 |",
    })
    public static class Valid {

        private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

        @Test
        public void packDecimal(final LocalDate localDate) throws Exception {
            final int packed = DatePacker.DECIMAL.pack(localDate);
            assertEquals(Integer.parseInt(localDate.format(YYYYMMDD)), packed);
        }

        @Test
        public void packBinary(final LocalDate localDate) throws Exception {
            final int packed = DatePacker.BINARY.pack(localDate);
            assertEquals((localDate.getYear() << 9) | (localDate.getMonthValue() << 5) | localDate.getDayOfMonth(),
                    packed);
        }

        @Test
        public void packAndUnpackLocalDate(final LocalDate localDate) throws Exception {
            for (final DatePacker packer : PACKERS) {
                final int packed = packer.pack(localDate);
                final LocalDate unpacked = packer.unpackLocalDate(packed);
                assertEquals(packer + ": " + localDate + " -> " + packed, localDate, unpacked);
            }
        }

        @Test
        public void packAndUnpackYearMonthDay(final LocalDate localDate) throws Exception {
            for (final DatePacker packer : PACKERS) {
                final int packed = packer.pack(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
                final int year = packer.unpackYear(packed);
                final int month = packer.unpackMonth(packed);
                final int day = packer.unpackDay(packed);
                assertEquals(packer + ": " + localDate + " -> " + packed + " [y]", localDate.getYear(), year);
                assertEquals(packer + ": " + localDate + " -> " + packed + " [m]", localDate.getMonthValue(), month);
                assertEquals(packer + ": " + localDate + " -> " + packed + " [d]", localDate.getDayOfMonth(), day);
            }
        }

        @Test
        public void packDaysSinceEpoch(final LocalDate localDate) throws Exception {
            for (final DatePacker packer : PACKERS) {
                final int packed = packer.packDaysSinceEpoch(localDate.toEpochDay());
                final LocalDate unpacked = packer.unpackLocalDate(packed);
                assertEquals(packer + ": " + localDate + " -> " + packed, localDate, unpacked);
            }
        }

        @Test
        public void packMillisSinceEpoch(final LocalDate localDate) throws Exception {
            for (final DatePacker packer : PACKERS) {
                final int packed = packer.packMillisSinceEpoch(localDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli());
                final LocalDate unpacked = packer.unpackLocalDate(packed);
                assertEquals(packer + ": " + localDate + " -> " + packed, localDate, unpacked);
            }
        }
    }

    @RunWith(Spockito.class)
    @Spockito.Unroll({
            "|  year | month | day |",
            "|     0 |    1  |   1 |",
            "|    -1 |    1  |   1 |",
            "| 10000 |    1  |   1 |",
            "|  2017 |    0  |   1 |",
            "|  2017 |   -1  |   1 |",
            "|  2017 |   13  |   1 |",
            "|  2017 |    1  |   0 |",
            "|  2017 |    1  |  -1 |",
            "|  2017 |    1  |  32 |",
            "|  2017 |    2  |  29 |",
            "|  2016 |    2  |  30 |",
            "|  2000 |    2  |  30 |",
            "|  1900 |    2  |  29 |",
            "|  1900 |    4  |  31 |",
            "|  1900 |    6  |  31 |",
            "|  1900 |    9  |  31 |",
            "|  1900 |   11  |  31 |",
    })
    @Spockito.Name("[{row}]: {year}/{month}/{day}")
    public static class Invalid {
        @Test(expected = IllegalArgumentException.class)
        public void packIllegalYearMonthDayBinary(final int year, final int month, final int day) {
            DatePacker.BINARY.pack(year, month, day);
        }

        @Test(expected = IllegalArgumentException.class)
        public void packIllegalYearMonthDayDecimal(final int year, final int month, final int day) {
            DatePacker.DECIMAL.pack(year, month, day);
        }

        @Test(expected = IllegalArgumentException.class)
        public void unpackIllegalYearMonthDayBinary(final int year, final int month, final int day) {
            final int packed = (year << 9) | (month << 5) | day;
            DatePacker.BINARY.unpackDay(packed);
        }

        @Test(expected = IllegalArgumentException.class)
        public void unpackIllegalYearMonthDayDecimal(final int year, final int month, final int day) {
            final int packed = year * 10000 + month * 100 + day;
            DatePacker.DECIMAL.unpackDay(packed);
        }
    }

    @RunWith(Spockito.class)
    @Spockito.Unroll({
            "| packing |",
            "|  BINARY |",
            "| DECIMAL |",
    })
    @Spockito.UseValueConverter
    public static class Special {
        @Test
        public void packAndUnpackNull(final Packing packing) throws Exception {
            final DatePacker packer = DatePacker.forPacking(packing);
            final int packed = packer.packNull();
            final boolean isNull = packer.unpackNull(packed);
            assertEquals(packer + ": pack null", 0, packed);
            assertTrue(packer + ": unpack null", isNull);
        }

        @Test
        public void packing(final Packing packing) throws Exception {
            final DatePacker packer = DatePacker.forPacking(packing);
            assertEquals(packing, packer.packing());
            assertEquals(packer, DatePacker.class.getField(packing.name()).get(null));
            assertEquals(DatePacker.class.getSimpleName() + "." + packing, packer.toString());
        }
    }
}