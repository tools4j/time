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
package org.tools4j.time.base;

import org.tools4j.time.pack.DatePacker;
import org.tools4j.time.pack.DateTimePacker;
import org.tools4j.time.pack.MilliTimePacker;
import org.tools4j.time.pack.NanoTimePacker;
import org.tools4j.time.pack.Packing;
import org.tools4j.time.pack.TimePacker;
import org.tools4j.time.validate.DateValidator;
import org.tools4j.time.validate.TimeValidator;
import org.tools4j.time.validate.ValidationMethod;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.tools4j.time.base.Allocation.Type.RESULT;
import static org.tools4j.time.base.EpochImpl.divMod;
import static org.tools4j.time.base.TimeFactors.HOURS_PER_DAY;
import static org.tools4j.time.base.TimeFactors.MILLIS_PER_DAY;
import static org.tools4j.time.base.TimeFactors.MILLIS_PER_HOUR;
import static org.tools4j.time.base.TimeFactors.MILLIS_PER_MINUTE;
import static org.tools4j.time.base.TimeFactors.MILLIS_PER_SECOND;
import static org.tools4j.time.base.TimeFactors.MINUTES_PER_HOUR;
import static org.tools4j.time.base.TimeFactors.NANOS_PER_DAY;
import static org.tools4j.time.base.TimeFactors.NANOS_PER_HOUR;
import static org.tools4j.time.base.TimeFactors.NANOS_PER_MILLI;
import static org.tools4j.time.base.TimeFactors.NANOS_PER_MINUTE;
import static org.tools4j.time.base.TimeFactors.NANOS_PER_SECOND;
import static org.tools4j.time.base.TimeFactors.SECONDS_PER_DAY;
import static org.tools4j.time.base.TimeFactors.SECONDS_PER_HOUR;
import static org.tools4j.time.base.TimeFactors.SECONDS_PER_MINUTE;

/**
 * Converts dates to days since epoch and vice versa.
 */
public interface Epoch {

    long INVALID_EPOCH = DateValidator.INVALID_EPOCH;

    ValidationMethod validationMethod();

    long toEpochDay(int year, int month, int day);
    long toEpochDay(int packedDate, DatePacker datePacker);
    long toEpochDay(final LocalDate localDate);
    long toEpochSecond(int year, int month, int day);
    long toEpochSecond(int year, int month, int day, int hour, int minute);
    long toEpochSecond(int year, int month, int day, int hour, int minute, int second);
    long toEpochSecond(int packedDate, DatePacker datePacker);
    long toEpochSecond(int packedDate, DatePacker datePacker, int packedTime, TimePacker timePacker);
    long toEpochSecond(final LocalDateTime localDateTime);
    long toEpochMilli(int year, int month, int day);
    long toEpochMilli(int year, int month, int day, int hour, int minute, int second, int milli);
    long toEpochMilli(int packedDate, DatePacker datePacker);
    long toEpochMilli(int packedDate, DatePacker datePacker, int packedMilliTime, MilliTimePacker milliTimePacker);
    long toEpochMilli(long packedDateTime, DateTimePacker dateTimePacker);
    long toEpochMilli(final LocalDateTime localDateTime);
    long toEpochNano(int year, int month, int day);
    long toEpochNano(int year, int month, int day, int hour, int minute, int second, int nano);
    long toEpochNano(int packedDate, DatePacker datePacker, long packedNanoTime, NanoTimePacker nanoTimePacker);
    long toEpochNano(final LocalDateTime localDateTime);

    int fromEpochDay(long daysSinceEpoch, DatePacker datePacker);
    @Allocation(RESULT)
    LocalDate fromEpochDay(long daysSinceEpoch);
    int fromEpochSecond(long secondsSinceEpoch, DatePacker datePacker);
    int fromEpochSecond(long secondsSinceEpoch, TimePacker timePacker);
    int fromEpochSecond(long secondsSinceEpoch, MilliTimePacker milliTimePacker);
    long fromEpochSecond(long secondsSinceEpoch, NanoTimePacker nanoTimePacker);
    long fromEpochSecond(long secondsSinceEpoch, DateTimePacker dateTimePacker);
    @Allocation(RESULT)
    LocalDateTime fromEpochSecond(long secondsSinceEpoch);
    int fromEpochMilli(long millisSinceEpoch, DatePacker datePacker);
    int fromEpochMilli(long millisSinceEpoch, MilliTimePacker milliTimePacker);
    long fromEpochMilli(long millisSinceEpoch, NanoTimePacker nanoTimePacker);
    long fromEpochMilli(long millisSinceEpoch, DateTimePacker dateTimePacker);
    @Allocation(RESULT)
    LocalDateTime fromEpochMilli(long millisSinceEpoch);
    int fromEpochNano(long nanosSinceEpoch, DatePacker datePacker);
    long fromEpochNano(long nanosSinceEpoch, NanoTimePacker nanoTimePacker);
    @Allocation(RESULT)
    LocalDateTime fromEpochNano(long nanosSinceEpoch);

    static Epoch valueOf(final ValidationMethod validationMethod) {
        return EpochImpl.valueOf(validationMethod);
    }

    interface Default extends Epoch {
        default long toEpochDay(final int packedDate, final DatePacker datePacker) {
            return toEpochDay(
                    datePacker.unpackYear(packedDate),
                    datePacker.unpackMonth(packedDate),
                    datePacker.unpackDay(packedDate)
            );
        }

        @Override
        default long toEpochDay(final LocalDate localDate) {
            return toEpochDay(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
        }

        default long toEpochSecond(final int year, final int month, final int day) {
            return toEpochDay(year, month, day) * SECONDS_PER_DAY;
        }

        default long toEpochSecond(final int year, final int month, final int day,
                                   final int hour, final int minute) {
            return toEpochSecond(year, month, day, hour, minute, 0);
        }

        default long toEpochSecond(final int year, final int month, final int day,
                                   final int hour, final int minute, final int second) {
            if (TimeValidator.INVALID == validationMethod().timeValidator().validateTime(hour, minute, second)) {
                return INVALID_EPOCH;
            }
            return toEpochSecond(year, month, day)
                    + hour * SECONDS_PER_HOUR + minute * SECONDS_PER_MINUTE + second;
        }

        default long toEpochSecond(final int packedDate, final DatePacker datePacker) {
            return toEpochDay(packedDate, datePacker) * SECONDS_PER_DAY;
        }

        default long toEpochSecond(final int packedDate, final DatePacker datePacker,
                                   final int packedTime, final TimePacker timePacker) {
            return toEpochSecond(
                    datePacker.unpackYear(packedDate),
                    datePacker.unpackMonth(packedDate),
                    datePacker.unpackDay(packedDate),
                    timePacker.unpackHour(packedTime),
                    timePacker.unpackMinute(packedTime),
                    timePacker.unpackSecond(packedTime)
            );
        }

        @Override
        default long toEpochSecond(final LocalDateTime localDateTime) {
            return toEpochSecond(
                    localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth(),
                    localDateTime.getHour(), localDateTime.getMinute(), localDateTime.getSecond()
            );
        }

        default long toEpochMilli(final int year, final int month, final int day) {
            return toEpochDay(year, month, day) * MILLIS_PER_DAY;
        }

        default long toEpochMilli(final int year, final int month, final int day,
                                  final int hour, final int minute, final int second, final int milli) {
            if (TimeValidator.INVALID == validationMethod().timeValidator().validateTimeWithMillis(hour, minute, second, milli)) {
                return INVALID_EPOCH;
            }
            return toEpochMilli(year, month, day)
                    + hour * MILLIS_PER_HOUR + minute * MILLIS_PER_MINUTE + second * MILLIS_PER_SECOND + milli;
        }

        default long toEpochMilli(final int packedDate, final DatePacker datePacker) {
            return toEpochDay(packedDate, datePacker) * MILLIS_PER_DAY;
        }

        default long toEpochMilli(final int packedDate, final DatePacker datePacker,
                                  final int packedMilliTime, final MilliTimePacker milliTimePacker) {
            return toEpochMilli(
                    datePacker.unpackYear(packedDate),
                    datePacker.unpackMonth(packedDate),
                    datePacker.unpackDay(packedDate),
                    milliTimePacker.unpackHour(packedMilliTime),
                    milliTimePacker.unpackMinute(packedMilliTime),
                    milliTimePacker.unpackSecond(packedMilliTime),
                    milliTimePacker.unpackMilli(packedMilliTime)
            );
        }

        default long toEpochMilli(final long packedDateTime, final DateTimePacker dateTimePacker) {
            return toEpochMilli(
                    dateTimePacker.unpackYear(packedDateTime),
                    dateTimePacker.unpackMonth(packedDateTime),
                    dateTimePacker.unpackDay(packedDateTime),
                    dateTimePacker.unpackHour(packedDateTime),
                    dateTimePacker.unpackMinute(packedDateTime),
                    dateTimePacker.unpackSecond(packedDateTime),
                    dateTimePacker.unpackMilli(packedDateTime)
            );
        }

        @Override
        default long toEpochMilli(final LocalDateTime localDateTime) {
            return toEpochMilli(
                    localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth(),
                    localDateTime.getHour(), localDateTime.getMinute(), localDateTime.getSecond(), localDateTime.getNano() / NANOS_PER_MILLI
            );
        }

        default long toEpochNano(final int year, final int month, final int day) {
            return toEpochDay(year, month, day) * NANOS_PER_DAY;
        }

        default long toEpochNano(final int year, final int month, final int day,
                                 final int hour, final int minute, final int second, final int nano) {
            if (TimeValidator.INVALID == validationMethod().timeValidator().validateTimeWithNanos(hour, minute, second, nano)) {
                return INVALID_EPOCH;
            }
            return toEpochNano(year, month, day)
                    + hour * NANOS_PER_HOUR + minute * NANOS_PER_MINUTE + second * NANOS_PER_SECOND + nano;
        }

        default long toEpochNano(final int packedDate, final DatePacker datePacker,
                                 final long packedNanoTime, final NanoTimePacker nanoTimePacker) {
            return toEpochNano(
                    datePacker.unpackYear(packedDate),
                    datePacker.unpackMonth(packedDate),
                    datePacker.unpackDay(packedDate),
                    nanoTimePacker.unpackHour(packedNanoTime),
                    nanoTimePacker.unpackMinute(packedNanoTime),
                    nanoTimePacker.unpackSecond(packedNanoTime),
                    nanoTimePacker.unpackNano(packedNanoTime)
            );
        }

        @Override
        default long toEpochNano(final LocalDateTime localDateTime) {
            return toEpochNano(
                    localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth(),
                    localDateTime.getHour(), localDateTime.getMinute(), localDateTime.getSecond(), localDateTime.getNano()
            );
        }

        @Override
        @Allocation(RESULT)
        default LocalDate fromEpochDay(final long daysSinceEpoch) {
            final DatePacker packer = DatePacker.BINARY;
            final int packed = fromEpochDay(daysSinceEpoch, packer);
            return packer.unpackLocalDate(packed);
        }

        default int fromEpochSecond(final long secondsSinceEpoch, final DatePacker datePacker) {
            return fromEpochDay(Math.floorDiv(secondsSinceEpoch, SECONDS_PER_DAY), datePacker);
        }

        default int fromEpochSecond(final long secondsSinceEpoch, final TimePacker timePacker) {
            final int timeInSeconds = (int) Math.floorMod(secondsSinceEpoch, SECONDS_PER_DAY);
            return timePacker.pack(
                    divMod(timeInSeconds, SECONDS_PER_HOUR, HOURS_PER_DAY),
                    divMod(timeInSeconds, SECONDS_PER_MINUTE, MINUTES_PER_HOUR),
                    Math.floorMod(timeInSeconds, SECONDS_PER_MINUTE)
            );
        }

        default int fromEpochSecond(final long secondsSinceEpoch, final MilliTimePacker milliTimePacker) {
            final int timeInSeconds = (int) Math.floorMod(secondsSinceEpoch, SECONDS_PER_DAY);
            return milliTimePacker.pack(
                    divMod(timeInSeconds, SECONDS_PER_HOUR, HOURS_PER_DAY),
                    divMod(timeInSeconds, SECONDS_PER_MINUTE, MINUTES_PER_HOUR),
                    Math.floorMod(timeInSeconds, SECONDS_PER_MINUTE),
                    0
            );
        }

        default long fromEpochSecond(final long secondsSinceEpoch, final NanoTimePacker nanoTimePacker) {
            final int timeInSeconds = (int) Math.floorMod(secondsSinceEpoch, SECONDS_PER_DAY);
            return nanoTimePacker.pack(
                    divMod(timeInSeconds, SECONDS_PER_HOUR, HOURS_PER_DAY),
                    divMod(timeInSeconds, SECONDS_PER_MINUTE, MINUTES_PER_HOUR),
                    Math.floorMod(timeInSeconds, SECONDS_PER_MINUTE),
                    0
            );
        }

        default long fromEpochSecond(final long secondsSinceEpoch, final DateTimePacker dateTimePacker) {
            final DatePacker datePacker = DatePacker.valueOf(Packing.BINARY, dateTimePacker.validationMethod());
            final int packedDate = fromEpochSecond(secondsSinceEpoch, datePacker);
            final int timeInSeconds = (int) Math.floorMod(secondsSinceEpoch, SECONDS_PER_DAY);
            return dateTimePacker.pack(
                    datePacker.unpackYear(packedDate),
                    datePacker.unpackMonth(packedDate),
                    datePacker.unpackDay(packedDate),
                    divMod(timeInSeconds, SECONDS_PER_HOUR, HOURS_PER_DAY),
                    divMod(timeInSeconds, SECONDS_PER_MINUTE, MINUTES_PER_HOUR),
                    Math.floorMod(timeInSeconds, SECONDS_PER_MINUTE)
            );
        }

        @Override
        @Allocation(RESULT)
        default LocalDateTime fromEpochSecond(final long secondsSinceEpoch) {
            final DateTimePacker packer = DateTimePacker.BINARY;
            final long packed = fromEpochSecond(secondsSinceEpoch, packer);
            return packer.unpackLocalDateTime(packed);
        }

        default int fromEpochMilli(final long millisSinceEpoch, final DatePacker datePacker) {
            return fromEpochDay(Math.floorDiv(millisSinceEpoch, MILLIS_PER_DAY), datePacker);
        }

        default int fromEpochMilli(final long millisSinceEpoch, final MilliTimePacker milliTimePacker) {
            final int timeInMillis = (int) Math.floorMod(millisSinceEpoch, MILLIS_PER_DAY);
            return milliTimePacker.pack(
                    divMod(timeInMillis, MILLIS_PER_HOUR, HOURS_PER_DAY),
                    divMod(timeInMillis, MILLIS_PER_MINUTE, MINUTES_PER_HOUR),
                    divMod(timeInMillis, MILLIS_PER_SECOND, SECONDS_PER_MINUTE),
                    Math.floorMod(timeInMillis, MILLIS_PER_SECOND)
            );
        }

        default long fromEpochMilli(final long millisSinceEpoch, final NanoTimePacker nanoTimePacker) {
            final int timeInMillis = (int) Math.floorMod(millisSinceEpoch, MILLIS_PER_DAY);
            return nanoTimePacker.pack(
                    divMod(timeInMillis, MILLIS_PER_HOUR, HOURS_PER_DAY),
                    divMod(timeInMillis, MILLIS_PER_MINUTE, MINUTES_PER_HOUR),
                    divMod(timeInMillis, MILLIS_PER_SECOND, SECONDS_PER_MINUTE),
                    Math.floorMod(timeInMillis, MILLIS_PER_SECOND) * NANOS_PER_MILLI
            );
        }

        default long fromEpochMilli(final long millisSinceEpoch, final DateTimePacker dateTimePacker) {
            final DatePacker datePacker = DatePacker.valueOf(Packing.BINARY, dateTimePacker.validationMethod());
            final int packedDate = fromEpochMilli(millisSinceEpoch, datePacker);
            final int timeInMillis = (int) Math.floorMod(millisSinceEpoch, MILLIS_PER_DAY);
            return dateTimePacker.pack(
                    datePacker.unpackYear(packedDate),
                    datePacker.unpackMonth(packedDate),
                    datePacker.unpackDay(packedDate),
                    divMod(timeInMillis, MILLIS_PER_HOUR, HOURS_PER_DAY),
                    divMod(timeInMillis, MILLIS_PER_MINUTE, MINUTES_PER_HOUR),
                    divMod(timeInMillis, MILLIS_PER_SECOND, SECONDS_PER_MINUTE),
                    Math.floorMod(timeInMillis, MILLIS_PER_SECOND)
            );
        }

        @Override
        @Allocation(RESULT)
        default LocalDateTime fromEpochMilli(final long millisSinceEpoch) {
            final DateTimePacker packer = DateTimePacker.BINARY;
            final long packed = fromEpochMilli(millisSinceEpoch, packer);
            return packer.unpackLocalDateTime(packed);
        }

        default long fromEpochNano(final long nanosSinceEpoch, final NanoTimePacker nanoTimePacker) {
            final int timeInSeconds = divMod(nanosSinceEpoch, NANOS_PER_SECOND, SECONDS_PER_DAY);
            return nanoTimePacker.pack(
                    divMod(timeInSeconds, SECONDS_PER_HOUR, HOURS_PER_DAY),
                    divMod(timeInSeconds, SECONDS_PER_MINUTE, MINUTES_PER_HOUR),
                    Math.floorMod(timeInSeconds, SECONDS_PER_MINUTE),
                    (int) Math.floorMod(nanosSinceEpoch, NANOS_PER_SECOND)
            );
        }

        @Override
        default int fromEpochNano(final long nanosSinceEpoch, final DatePacker datePacker) {
            return fromEpochDay(Math.floorDiv(nanosSinceEpoch, NANOS_PER_DAY), datePacker);
        }

        @Override
        @Allocation(RESULT)
        default LocalDateTime fromEpochNano(final long nanosSinceEpoch) {
            final DatePacker datePacker = DatePacker.BINARY;
            final NanoTimePacker timePacker = NanoTimePacker.BINARY;
            final int packedDate = fromEpochNano(nanosSinceEpoch, datePacker);
            final long packedTime = fromEpochNano(nanosSinceEpoch, timePacker);
            return datePacker.unpackNull(packedDate) & timePacker.unpackNull(packedTime) ? null :
                    LocalDateTime.of(
                            datePacker.unpackYear(packedDate),
                            datePacker.unpackMonth(packedDate),
                            datePacker.unpackDay(packedDate),
                            timePacker.unpackHour(packedTime),
                            timePacker.unpackMinute(packedTime),
                            timePacker.unpackSecond(packedTime),
                            timePacker.unpackNano(packedTime)
                    );
        }
    }

}
