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
import org.tools4j.time.validate.TimeValidator;
import org.tools4j.time.validate.ValidationMethod;

import java.time.LocalTime;
import java.util.Objects;

import static org.tools4j.time.base.Allocation.Type.RESULT;
import static org.tools4j.time.base.TimeFactors.MILLIS_PER_HOUR;
import static org.tools4j.time.base.TimeFactors.MILLIS_PER_MINUTE;
import static org.tools4j.time.base.TimeFactors.MILLIS_PER_SECOND;
import static org.tools4j.time.base.TimeFactors.NANOS_PER_HOUR;
import static org.tools4j.time.base.TimeFactors.NANOS_PER_MILLI;
import static org.tools4j.time.base.TimeFactors.NANOS_PER_MINUTE;
import static org.tools4j.time.base.TimeFactors.NANOS_PER_SECOND;
import static org.tools4j.time.validate.TimeValidator.isValidTimeWithNanos;

/**
 * Packs a time value (hour, minute, second, nano) into a long.  Packing and unpacking can be done with or without time
 * validation using different {@link #validationMethod() validation methods}.  A {@link #DECIMAL} and a {@link #BINARY}
 * packing is supported and both packings preserve the natural date ordering, that is, if the packed longs are sorted
 * then the corresponding time values are also sorted.  Packing and unpacking of null values is supported via
 * {@link #packNull()} and {@link #unpackNull(long)}.
 * <p>
 * <i>Examples:</i>
 * <ul>
 *     <li>{@link #DECIMAL} packing for a time value 14:15:16.171819200 is 141516171819200</li>
 *     <li>{@link #BINARY} packing uses shifts to pack the time parts which is more efficient but the result is not
 *     easily human-readable</li>
 * </ul>
 * @see #valueOf(Packing, ValidationMethod)
 * @see #BINARY
 * @see #DECIMAL
 */
public interface NanoTimePacker {
    long INVALID = -1;
    long NULL = Long.MAX_VALUE;
    Packing packing();
    ValidationMethod validationMethod();
    NanoTimePacker forValidationMethod(ValidationMethod validationMethod);
    long pack(int hour, int minute, int second, int nano);
    long packFromTime(int packedTime, Packing packing, int nano);
    long packFromMilliTime(int packedMilliTime, Packing packing);
    long packFromDateTime(long packedDateTime, Packing packing);
    long packFromNanoTime(long packedNanoTime, Packing packing);
    int unpackHour(long packed);
    int unpackMinute(long packed);
    int unpackSecond(long packed);
    int unpackNano(long packed);
    long packNull();
    boolean unpackNull(long packed);
    long pack(LocalTime localTime);
    @Allocation(RESULT)
    LocalTime unpackLocalTime(long packed);
    long packEpochMilli(long millisSinceEpoch);
    long unpackMilliOfDay(long packed);
    long packEpochNano(long nanosSinceEpoch);
    long unpackNanoOfDay(long packed);

    boolean isValid(long packed);
    long validate(long packed);
    long validate(long packed, ValidationMethod validationMethod);

    /**
     * Returns a nano-time packer that performs no validation.
     * @param packing the packing type for the returned packer
     * @return a cached packer instance
     */
    static NanoTimePacker valueOf(final Packing packing) {
        return Instances.valueOf(packing, ValidationMethod.UNVALIDATED);
    }

    /**
     * Returns a nano-time packer that performs validation using the specified validation method.
     * @param packing the packing type for the returned packer
     * @param validationMethod validation method to perform during packing and unpacking operations
     * @return a cached packer instance
     */
    static NanoTimePacker valueOf(final Packing packing, final ValidationMethod validationMethod) {
        return Instances.valueOf(packing, validationMethod);
    }

    /**
     * Provides common default implementations for nano-time packer.
     */
    interface Default extends NanoTimePacker {
        @Override
        default long packNull() {
            return NULL;
        }

        @Override
        default boolean unpackNull(final long packed) {
            return packed == NULL;
        }

        @Override
        default long packFromTime(final int packedTime, final Packing packing, final int nano) {
            final TimePacker tp = TimePacker.valueOf(packing, validationMethod());
            return pack(tp.unpackHour(packedTime), tp.unpackMinute(packedTime), tp.unpackSecond(packedTime), nano);
        }

        @Override
        default long packFromMilliTime(final int packedMilliTime, final Packing packing) {
            final MilliTimePacker mtp = MilliTimePacker.valueOf(packing, validationMethod());
            return pack(mtp.unpackHour(packedMilliTime), mtp.unpackMinute(packedMilliTime),
                    mtp.unpackSecond(packedMilliTime), mtp.unpackMilli(packedMilliTime) * NANOS_PER_MILLI);
        }

        @Override
        default long packFromDateTime(final long packedDateTime, final Packing packing) {
            final DateTimePacker dtp = DateTimePacker.valueOf(packing, validationMethod());
            return pack(dtp.unpackHour(packedDateTime), dtp.unpackMinute(packedDateTime),
                    dtp.unpackSecond(packedDateTime), dtp.unpackMilli(packedDateTime) * NANOS_PER_MILLI);
        }

        @Override
        default long packFromNanoTime(final long packedNanoTime, final Packing packing) {
            if (packing == packing() && validationMethod() == ValidationMethod.UNVALIDATED) {
                return packedNanoTime;
            }
            final NanoTimePacker ntp = NanoTimePacker.valueOf(packing, validationMethod());
            return pack(ntp.unpackHour(packedNanoTime), ntp.unpackMinute(packedNanoTime),
                    ntp.unpackSecond(packedNanoTime), ntp.unpackNano(packedNanoTime));
        }

        @Override
        default long pack(final LocalTime localTime) {
            return localTime == null ? packNull() :
                    pack(localTime.getHour(), localTime.getMinute(), localTime.getSecond(), localTime.getNano());
        }

        @Override
        @Allocation(RESULT)
        default LocalTime unpackLocalTime(final long packed) {
            return unpackNull(packed) ? null :
                    LocalTime.of(unpackHour(packed), unpackMinute(packed), unpackSecond(packed), unpackNano(packed));
        }

        @Override
        default long packEpochMilli(final long millisSinceEpoch) {
            return Epoch.valueOf(validationMethod()).fromEpochMilli(millisSinceEpoch, this);
        }

        @Override
        default long unpackMilliOfDay(final long packed) {
            return unpackHour(packed) * (long)MILLIS_PER_HOUR +
                    unpackMinute(packed) * (long)MILLIS_PER_MINUTE +
                    unpackSecond(packed) * (long)MILLIS_PER_SECOND +
                    unpackNano(packed) / NANOS_PER_MILLI;
        }

        @Override
        default long packEpochNano(final long nanosSinceEpoch) {
            return Epoch.valueOf(validationMethod()).fromEpochNano(nanosSinceEpoch, this);
        }

        @Override
        default long unpackNanoOfDay(final long packed) {
            return unpackHour(packed) * NANOS_PER_HOUR +
                    unpackMinute(packed) * NANOS_PER_MINUTE +
                    unpackSecond(packed) * (long)NANOS_PER_SECOND +
                    unpackNano(packed);
        }

        @Override
        default boolean isValid(final long packed) {
            final NanoTimePacker ntp = forValidationMethod(ValidationMethod.UNVALIDATED);
            return packed != INVALID && (packed == NULL || isValidTimeWithNanos(
                    ntp.unpackHour(packed), ntp.unpackMinute(packed), ntp.unpackSecond(packed), ntp.unpackNano(packed)
            ));
        }

        @Override
        default long validate(final long packed) {
            return validate(packed, validationMethod());
        }

        @Override
        default long validate(final long packed, final ValidationMethod validationMethod) {
            if (packed == NULL || validationMethod == ValidationMethod.UNVALIDATED) {
                return packed;
            }
            final NanoTimePacker ntp = forValidationMethod(ValidationMethod.UNVALIDATED);
            final TimeValidator tv = validationMethod.timeValidator();
            return tv.validateTimeWithNanos(
                    ntp.unpackHour(packed), ntp.unpackMinute(packed), ntp.unpackSecond(packed), ntp.unpackNano(packed)
            ) != TimeValidator.INVALID ? packed : INVALID;
        }

        @Override
        default NanoTimePacker forValidationMethod(final ValidationMethod validationMethod) {
            return valueOf(packing(), validationMethod);
        }
    }

    /**
     * Non-validating binary packing method.  This packing method uses bit shifting and other bitwise logical operations
     * and is very efficient; resulting packed dates are not easily .
     */
    NanoTimePacker BINARY = new Default() {
        @Override
        public Packing packing() {
            return Packing.BINARY;
        }

        @Override
        public ValidationMethod validationMethod() {
            return ValidationMethod.UNVALIDATED;
        }

        @Override
        public long pack(final int hour, final int minute, final int second, final int nano) {
            return ((hour & 0x1fL) << 42) | ((minute & 0x3fL) << 36) | ((second & 0x3fL)  << 30) | (nano & 0x3fffffffL);
        }

        @Override
        public int unpackHour(final long packed) {
            return (int)((packed >>> 42) & 0x1fL);
        }

        @Override
        public int unpackMinute(final long packed) {
            return (int)((packed >>> 36) & 0x3fL);
        }

        @Override
        public int unpackSecond(final long packed) {
            return (int)((packed >> 30) & 0x3fL);
        }

        @Override
        public int unpackNano(final long packed) {
            return (int)(packed & 0x3fffffffL);
        }

        @Override
        public String toString() {
            return "NanoTimePacker.BINARY";
        }
    };

    /**
     * Non-validating decimal packing method.  This packing method uses multiplications, divisions and modulo operations
     * which means it is less efficient than binary packing but results in  packed longs.  For instance
     * the time 14:15:16.171819200 is packed into the long value 141516171819200.
     */
    NanoTimePacker DECIMAL = new Default() {
        @Override
        public Packing packing() {
            return Packing.DECIMAL;
        }

        @Override
        public ValidationMethod validationMethod() {
            return ValidationMethod.UNVALIDATED;
        }

        @Override
        public long pack(final int hour, final int minute, final int second, final int nano) {
            return hour * (100_00L * NANOS_PER_SECOND) + minute * (100L * NANOS_PER_SECOND) + second * ((long)NANOS_PER_SECOND) + nano;
        }

        @Override
        public int unpackHour(final long packed) {
            return (int)(packed / (100_00L * NANOS_PER_SECOND));
        }

        @Override
        public int unpackMinute(final long packed) {
            return (int)((packed / (100L * NANOS_PER_SECOND)) % 100L);
        }

        @Override
        public int unpackSecond(final long packed) {
            return (int)((packed / NANOS_PER_SECOND) % 100L);
        }

        @Override
        public int unpackNano(final long packed) {
            return (int)(packed % NANOS_PER_SECOND);
        }

        @Override
        public String toString() {
            return "NanoTimePacker.DECIMAL";
        }
    };

    /**
     * Implementation that performs validation before packing and after unpacking a time value.  Instances can be
     * accessed via {@link #valueOf(Packing, ValidationMethod)}.
     */
    @SuppressWarnings("ClassCanBeRecord")
    class Validated implements Default {
        private final NanoTimePacker packer;
        private final TimeValidator validator;

        public Validated(final NanoTimePacker packer, final ValidationMethod validationMethod) {
            this(packer, TimeValidator.valueOf(validationMethod));
        }

        public Validated(final NanoTimePacker packer, final TimeValidator validator) {
            this.packer = Objects.requireNonNull(packer);
            this.validator = Objects.requireNonNull(validator);
        }

        @Override
        public Packing packing() {
            return packer.packing();
        }

        @Override
        public ValidationMethod validationMethod() {
            return validator.validationMethod();
        }

        @Override
        public long pack(final int hour, final int minute, final int second, final int nano) {
            if (validator.validateTimeWithNanos(hour, minute, second, nano) != TimeValidator.INVALID) {
                return packer.pack(hour, minute, second, nano);
            }
            return INVALID;
        }

        @Override
        public int unpackHour(final long packed) {
            return validator.validateHour(packer.unpackHour(packed));
        }

        @Override
        public int unpackMinute(final long packed) {
            return validator.validateMinute(packer.unpackMinute(packed));
        }

        @Override
        public int unpackSecond(final long packed) {
            return validator.validateSecond(packer.unpackSecond(packed));
        }

        @Override
        public int unpackNano(final long packed) {
            return validator.validateNano(packer.unpackNano(packed));
        }

        @Override
        public String toString() {
            return "NanoTimePacker.Validated." + packer.packing();
        }
    }

    /**
     * Helper class that manages instances of nano-time packers.
     */
    final class Instances {
        private static final NanoTimePacker[][] BY_PACKING_AND_VALIDATION_METHOD = instancesByPackingAndValidationMethod();

        private static NanoTimePacker valueOf(final Packing packing, final ValidationMethod validationMethod) {
            return BY_PACKING_AND_VALIDATION_METHOD[packing.ordinal()][validationMethod.ordinal()];
        }

        private static NanoTimePacker[][] instancesByPackingAndValidationMethod() {
            final NanoTimePacker[][] instances = new NanoTimePacker[Packing.count()][ValidationMethod.count()];
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
