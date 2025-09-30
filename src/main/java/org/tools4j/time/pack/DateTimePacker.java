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

import org.tools4j.time.base.Allocation;
import org.tools4j.time.base.Epoch;
import org.tools4j.time.validate.DateValidator;
import org.tools4j.time.validate.TimeValidator;
import org.tools4j.time.validate.ValidationMethod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import static org.tools4j.time.base.Allocation.Type.RESULT;
import static org.tools4j.time.base.TimeFactors.MILLIS_PER_SECOND;
import static org.tools4j.time.base.TimeFactors.NANOS_PER_MILLI;
import static org.tools4j.time.validate.DateValidator.isValidDate;
import static org.tools4j.time.validate.TimeValidator.isValidTimeWithMillis;

/**
 * Packs a date/time value (year, month, day, hour, minute, second, millis) into a single long.  Packing and unpacking
 * can be done with or without date/time validation using different {@linkplain #validationMethod() validation methods}.
 * A {@linkplain #DECIMAL} and a {@linkplain #BINARY} packing is supported and both packings preserve the natural date
 * ordering, that is, if the packed longs are sorted then the corresponding date/time values are also sorted.  Packing
 * and unpacking of null values is supported via {@link #packNull()} and {@link #unpackNull(long)}.
 * <p>
 * <i>Examples:</i>
 * <ul>
 *     <li>{@linkplain #DECIMAL} packing for a date/time value 21-Jan-2017 14:15:16.170 is 20170121141516170</li>
 *     <li>{@linkplain #BINARY} packing uses shifts to pack the date/time parts which is more efficient but the result
 *     is not easily human-readable</li>
 * </ul>
 * @see #valueOf(Packing, ValidationMethod)
 * @see #BINARY
 * @see #DECIMAL
 */
public interface DateTimePacker {
    long INVALID = -1;
    long NULL = Long.MAX_VALUE;
    Packing packing();
    ValidationMethod validationMethod();
    DateTimePacker forValidationMethod(ValidationMethod validationMethod);
    long pack(int year, int month, int day);
    long pack(int year, int month, int day, int hour, int minute, int second);
    long pack(int year, int month, int day, int hour, int minute, int second, int milli);
    long packFromDateAndTime(int packedDate, Packing datePacking, int packedTime, Packing timePacking);
    long packFromDateAndMilliTime(int packedDate, Packing datePacking, int packedMilliTime, Packing milliTimePacking);
    long packFromDateTime(long packedDateTime, Packing dateTimePacking);
    int unpackYear(long packed);
    int unpackMonth(long packed);
    int unpackDay(long packed);
    int unpackHour(long packed);
    int unpackMinute(long packed);
    int unpackSecond(long packed);
    int unpackMilli(long packed);
    long packNull();
    boolean unpackNull(long packed);
    long pack(LocalDateTime localDateTime);
    long pack(LocalDate localDate, LocalTime localTime);
    @Allocation(RESULT)
    LocalDateTime unpackLocalDateTime(long packed);
    long packEpochMilli(long millisSinceEpoch);
    long unpackEpochMilli(long packed);

    boolean isValid(long packed);
    long validate(long packed);
    long validate(long packed, ValidationMethod validationMethod);

    /**
     * Returns a date/time packer that performs no validation.
     * @param packing the packing type for the returned packer
     * @return a cached packer instance
     */
    static DateTimePacker valueOf(final Packing packing) {
        return Instances.valueOf(packing, ValidationMethod.UNVALIDATED);
    }

    /**
     * Returns a date/time packer that performs validation using the specified validation method.
     * @param packing the packing type for the returned packer
     * @param validationMethod validation method to perform during packing and unpacking operations
     * @return a cached packer instance
     */
    static DateTimePacker valueOf(final Packing packing, final ValidationMethod validationMethod) {
        return Instances.valueOf(packing, validationMethod);
    }

    /**
     * Provides common default implementations for date/time packer.
     */
    interface Default extends DateTimePacker {
        @Override
        default long packNull() {
            return NULL;
        }

        @Override
        default boolean unpackNull(final long packed) {
            return packed == NULL;
        }

        @Override
        default long pack(final int year, final int month, final int day) {
            return pack(year, month, day, 0, 0, 0, 0);
        }

        @Override
        default long pack(final int year, final int month, final int day,
                          final int hour, final int minute, final int second) {
            return pack(year, month, day, hour, minute, second, 0);
        }

        @Override
        default long packFromDateAndTime(final int packedDate, final Packing datePacking,
                                         final int packedTime, final Packing timePacking) {
            final DatePacker dp = DatePacker.valueOf(datePacking, validationMethod());
            final TimePacker tp = TimePacker.valueOf(timePacking, validationMethod());
            return pack(
                    dp.unpackYear(packedDate), dp.unpackMonth(packedDate), dp.unpackDay(packedDate),
                    tp.unpackHour(packedTime), tp.unpackMinute(packedTime), tp.unpackSecond(packedTime)
            );
        }

        @Override
        default long packFromDateAndMilliTime(final int packedDate, final Packing datePacking,
                                              final int packedMilliTime, final Packing milliTimePacking) {
            final DatePacker dp = DatePacker.valueOf(datePacking, validationMethod());
            final MilliTimePacker mtp = MilliTimePacker.valueOf(milliTimePacking, validationMethod());
            return pack(
                    dp.unpackYear(packedDate), dp.unpackMonth(packedDate), dp.unpackDay(packedDate),
                    mtp.unpackHour(packedMilliTime), mtp.unpackMinute(packedMilliTime),
                    mtp.unpackSecond(packedMilliTime), mtp.unpackMilli(packedMilliTime)
            );
        }

        @Override
        default long packFromDateTime(final long packedDateTime, final Packing dateTimePacking) {
            if (dateTimePacking == packing() && validationMethod() == ValidationMethod.UNVALIDATED) {
                return packedDateTime;
            }
            final DateTimePacker dtp = DateTimePacker.valueOf(dateTimePacking, validationMethod());
            return pack(
                    dtp.unpackYear(packedDateTime), dtp.unpackMonth(packedDateTime), dtp.unpackDay(packedDateTime),
                    dtp.unpackHour(packedDateTime), dtp.unpackMinute(packedDateTime),
                    dtp.unpackSecond(packedDateTime), dtp.unpackMilli(packedDateTime)
            );
        }

        @Override
        default long pack(final LocalDateTime localDateTime) {
            return localDateTime == null ? packNull() :
                    pack(localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth(),
                            localDateTime.getHour(), localDateTime.getMinute(), localDateTime.getSecond(),
                            localDateTime.getNano() / NANOS_PER_MILLI);
        }

        @Override
        default long pack(final LocalDate localDate, final LocalTime localTime) {
            return localDate == null ? packNull() :
                    localTime == null ?
                            pack(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth()) :
                            pack(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(),
                                    localTime.getHour(), localTime.getMinute(), localTime.getSecond(),
                                    localTime.getNano() / NANOS_PER_MILLI);
        }

        @Override
        @Allocation(RESULT)
        default LocalDateTime unpackLocalDateTime(final long packed) {
            return unpackNull(packed) ? null :
                    LocalDateTime.of(unpackYear(packed), unpackMonth(packed), unpackDay(packed),
                            unpackHour(packed), unpackMinute(packed), unpackSecond(packed),
                            unpackMilli(packed) * NANOS_PER_MILLI);
        }

        @Override
        default long packEpochMilli(final long millisSinceEpoch) {
            return Epoch.valueOf(validationMethod()).fromEpochMilli(millisSinceEpoch, this);
        }

        @Override
        default long unpackEpochMilli(final long packed) {
            return Epoch.valueOf(validationMethod()).toEpochMilli(packed, this);
        }

        @Override
        default boolean isValid(final long packed) {
            return packed != INVALID && (packed == NULL || (
                    isValidDate(unpackDay(packed), unpackMonth(packed), unpackDay(packed)) &&
                    isValidTimeWithMillis(unpackHour(packed), unpackMinute(packed), unpackSecond(packed), unpackMilli(packed)))
            );
        }

        @Override
        default long validate(final long packed) {
            return validate(packed, validationMethod());
        }

        @Override
        default long validate(final long packed, final ValidationMethod validationMethod) {
            if (packed == NULL || packed == INVALID || validationMethod == ValidationMethod.UNVALIDATED) {
                return packed;
            }
            final DateValidator dv = validationMethod.dateValidator();
            final TimeValidator tv = validationMethod.timeValidator();
            return dv.validateDay(
                    unpackYear(packed), unpackMonth(packed), unpackDay(packed)
            ) != DateValidator.INVALID && tv.validateTimeWithMillis(
                    unpackHour(packed), unpackMinute(packed), unpackSecond(packed), unpackMilli(packed)
            ) != TimeValidator.INVALID ? packed : INVALID;
        }

        @Override
        default DateTimePacker forValidationMethod(final ValidationMethod validationMethod) {
            return valueOf(packing(), validationMethod);
        }
    }

    /**
     * Non-validating binary packing method.  This packing method uses bit shifting and other bitwise logical operations
     * and is very efficient; resulting packed dates are not easily human-readable.
     */
    DateTimePacker BINARY = new Default() {
        @Override
        public Packing packing() {
            return Packing.BINARY;
        }

        @Override
        public ValidationMethod validationMethod() {
            return ValidationMethod.UNVALIDATED;
        }

        @Override
        public long pack(final int year, final int month, final int day) {
            return ((year & 0x3fffL) << 36) | ((month & 0xfL) << 32) | ((day & 0x1fL) << 27);
        }

        @Override
        public long pack(final int year, final int month, final int day,
                         final int hour, final int minute, final int second, final int milli) {
            return ((year & 0x3fffL) << 36) | ((month & 0xfL) << 32) | ((day & 0x1fL) << 27) |
                    ((hour & 0x1fL) << 22) | ((minute & 0x3fL) << 16) | ((second & 0x3fL)  << 10) | (milli & 0x3ffL);
        }

        @Override
        public int unpackYear(final long packed) {
            return (int)((packed >>> 36) & 0x3fffL);
        }

        @Override
        public int unpackMonth(final long packed) {
            return (int)((packed >>> 32) & 0xfL);
        }

        @Override
        public int unpackDay(final long packed) {
            return (int)((packed >>> 27) & 0x1fL);
        }

        @Override
        public int unpackHour(final long packed) {
            return (int)((packed >>> 22) & 0x1fL);
        }

        @Override
        public int unpackMinute(final long packed) {
            return (int)((packed >>> 16) & 0x3fL);
        }

        @Override
        public int unpackSecond(final long packed) {
            return (int)((packed >> 10) & 0x3fL);
        }

        @Override
        public int unpackMilli(final long packed) {
            return (int)(packed & 0x3ffL);
        }

        @Override
        public String toString() {
            return "DateTimePacker.BINARY";
        }
    };

    /**
     * Non-validating decimal packing method.  This packing method uses multiplications, divisions and modulo operations
     * which means it is less efficient than binary packing but results in human-readable packed longs.  For instance
     * the date/time value 21-Jan-2017 14:15:16.170 is packed into the long value 20170121141516170.
     */
    DateTimePacker DECIMAL = new Default() {
        @Override
        public Packing packing() {
            return Packing.DECIMAL;
        }

        @Override
        public ValidationMethod validationMethod() {
            return ValidationMethod.UNVALIDATED;
        }

        @Override
        public long pack(final int year, final int month, final int day) {
            return (year * 100_00_00_00_00_000L) + (month * 100_00_00_00_000L) + (day * 100_00_00_000L);
        }

        @Override
        public long pack(final int year, final int month, final int day,
                         final int hour, final int minute, final int second, final int milli) {
            return (year * 100_00_00_00_00_000L) + (month * 100_00_00_00_000L) + (day * 100_00_00_000L) +
                    (hour * 100_00_000L) + (minute * 100_000L) + (second * 1000L) + milli;
        }

        @Override
        public int unpackYear(final long packed) {
            return (int)(packed / 100_00_00_00_00_000L);
        }

        @Override
        public int unpackMonth(final long packed) {
            return (int)((packed / 100_00_00_00_000L) % 100L);
        }

        @Override
        public int unpackDay(final long packed) {
            return (int)((packed / 100_00_00_000L) % 100L);
        }

        @Override
        public int unpackHour(final long packed) {
            return (int)((packed / 100_00_000L) % 100L);
        }

        @Override
        public int unpackMinute(final long packed) {
            return (int)((packed / 100_000L) % 100L);
        }

        @Override
        public int unpackSecond(final long packed) {
            return (int)((packed / 1_000L) % 100L);
        }

        @Override
        public int unpackMilli(final long packed) {
            return (int)(packed % MILLIS_PER_SECOND);
        }

        @Override
        public String toString() {
            return "DateTimePacker.DECIMAL";
        }
    };

    /**
     * Implementation that performs validation before packing and after unpacking a date.  Instances can be accessed
     * via {@link #valueOf(Packing, ValidationMethod)}.
     */
    @SuppressWarnings("ClassCanBeRecord")
    class Validated implements Default {
        private final DateTimePacker packer;
        private final DateValidator dateValidator;
        private final TimeValidator timeValidator;

        protected Validated(final DateTimePacker packer, final ValidationMethod validationMethod) {
            this(packer, DateValidator.valueOf(validationMethod), TimeValidator.valueOf(validationMethod));
        }

        protected Validated(final DateTimePacker packer, final DateValidator dateValidator, final TimeValidator timeValidator) {
            this.packer = Objects.requireNonNull(packer);
            this.dateValidator = Objects.requireNonNull(dateValidator);
            this.timeValidator = Objects.requireNonNull(timeValidator);
        }

        @Override
        public Packing packing() {
            return packer.packing();
        }

        @Override
        public ValidationMethod validationMethod() {
            return timeValidator.validationMethod();
        }

        @Override
        public long pack(final int year, final int month, final int day) {
            if (dateValidator.validateDay(year, month, day) != DateValidator.INVALID) {
                return packer.pack(year, month, day);
            }
            return INVALID;
        }

        @Override
        public long pack(final int year, final int month, final int day, final int hour, final int minute, final int second, final int milli) {
            if (dateValidator.validateDay(year, month, day) != DateValidator.INVALID &
                    timeValidator.validateTimeWithMillis(hour, minute, second, milli) != TimeValidator.INVALID) {
                return packer.pack(year, month, day, hour, minute, second, milli);
            }
            return INVALID;
        }

        @Override
        public int unpackYear(final long packed) {
            return dateValidator.validateYear(packer.unpackYear(packed));
        }

        @Override
        public int unpackMonth(final long packed) {
            return dateValidator.validateMonth(packer.unpackMonth(packed));
        }

        @Override
        public int unpackDay(final long packed) {
            final int year = packer.unpackYear(packed);
            final int month = packer.unpackMonth(packed);
            final int day = packer.unpackDay(packed);
            return dateValidator.validateDay(year, month, day);
        }

        @Override
        public int unpackHour(final long packed) {
            return timeValidator.validateHour(packer.unpackHour(packed));
        }

        @Override
        public int unpackMinute(final long packed) {
            return timeValidator.validateMinute(packer.unpackMinute(packed));
        }

        @Override
        public int unpackSecond(final long packed) {
            return timeValidator.validateSecond(packer.unpackSecond(packed));
        }

        @Override
        public int unpackMilli(final long packed) {
            return timeValidator.validateMilli(packer.unpackMilli(packed));
        }

        @Override
        public String toString() {
            return "DateTimePacker.Validated." + packer.packing();
        }
    }

    /**
     * Helper class that manages instances of date/time packers.
     */
    final class Instances {
        private static final DateTimePacker[][] BY_PACKING_AND_VALIDATION_METHOD = instancesByPackingAndValidationMethod();

        private static DateTimePacker valueOf(final Packing packing, final ValidationMethod validationMethod) {
            return BY_PACKING_AND_VALIDATION_METHOD[packing.ordinal()][validationMethod.ordinal()];
        }

        private static DateTimePacker[][] instancesByPackingAndValidationMethod() {
            final DateTimePacker[][] instances = new DateTimePacker[Packing.count()][ValidationMethod.count()];
            final int vOrdUnvalidated = ValidationMethod.UNVALIDATED.ordinal();
            instances[Packing.BINARY.ordinal()][vOrdUnvalidated] = BINARY;
            instances[Packing.DECIMAL.ordinal()][vOrdUnvalidated] = DECIMAL;
            for (int pOrd = 0; pOrd < Packing.count(); pOrd++) {
                for (int vOrd = 0; vOrd < ValidationMethod.count(); vOrd++) {
                    if (vOrd != vOrdUnvalidated) {
                        instances[pOrd][vOrd] = new Validated(instances[pOrd][vOrdUnvalidated], ValidationMethod.valueByOrdinal(vOrd));
                    }
                }
            }
            return instances;
        }
    }
}
