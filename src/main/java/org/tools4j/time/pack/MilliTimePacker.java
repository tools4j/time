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
import static org.tools4j.time.base.TimeFactors.NANOS_PER_MILLI;

/**
 * Packs a time value (hour, minute, second, millis) into an integer.  Packing and unpacking can be done with or without
 * time validation using different {@link #validationMethod() validation methods}.  A {@link #DECIMAL} and a
 * {@link #BINARY} packing is supported and both packings preserve the natural date ordering, that is, if the packed
 * integers are sorted then the corresponding time values are also sorted.  Packing and unpacking of null values is
 * supported via {@link #packNull()} and {@link #unpackNull(int)}.
 * <p>
 * <i>Examples:</i>
 * <ul>
 *     <li>{@link #DECIMAL} packing for a time value 14:15:16.170 is 141516170</li>
 *     <li>{@link #BINARY} packing uses shifts to pack the time parts which is more efficient but the result is not
 *     easily human-readable</li>
 * </ul>
 * @see #valueOf(Packing, ValidationMethod)
 * @see #BINARY
 * @see #DECIMAL
 */
public interface MilliTimePacker {
    int INVALID = -1;
    int NULL = Integer.MAX_VALUE;
    Packing packing();
    ValidationMethod validationMethod();
    MilliTimePacker forValidationMethod(ValidationMethod validationMethod);
    int pack(int hour, int minute, int second, int milli);
    int pack(long packedDateTime, Packing packing);
    int unpackHour(int packed);
    int unpackMinute(int packed);
    int unpackSecond(int packed);
    int unpackMilli(int packed);
    int packNull();
    boolean unpackNull(int packed);
    int pack(LocalTime localTime);
    @Allocation(RESULT)
    LocalTime unpackLocalTime(int packed);
    int packEpochMilli(long millisSinceEpoch);
    long unpackMilliOfDay(int packed);

    /**
     * Returns a milli-time packer that performs no validation.
     * @param packing the packing type for the returned packer
     * @return a cached packer instance
     */
    static MilliTimePacker valueOf(final Packing packing) {
        return Instances.valueOf(packing, ValidationMethod.UNVALIDATED);
    }

    /**
     * Returns a milli-time packer that performs validation using the specified validation method.
     * @param packing the packing type for the returned packer
     * @param validationMethod validation method to perform during packing and unpacking operations
     * @return a cached packer instance
     */
    static MilliTimePacker valueOf(final Packing packing, final ValidationMethod validationMethod) {
        return Instances.valueOf(packing, validationMethod);
    }

    /**
     * Provides common default implementations for milli-time packer.
     */
    interface Default extends MilliTimePacker {
        @Override
        default int packNull() {
            return NULL;
        }

        @Override
        default boolean unpackNull(final int packed) {
            return packed == NULL;
        }

        @Override
        default int pack(final LocalTime localTime) {
            return localTime == null ? packNull() :
                    pack(localTime.getHour(), localTime.getMinute(), localTime.getSecond(),
                            localTime.getNano() / NANOS_PER_MILLI);
        }

        @Override
        default int pack(final long packedDateTime, final Packing packing) {
            final DateTimePacker unpacker = DateTimePacker.valueOf(packing, validationMethod());
            return pack(unpacker.unpackHour(packedDateTime), unpacker.unpackMinute(packedDateTime),
                    unpacker.unpackSecond(packedDateTime), unpacker.unpackMilli(packedDateTime));
        }

        @Override
        @Allocation(RESULT)
        default LocalTime unpackLocalTime(final int packed) {
            return unpackNull(packed) ? null :
                    LocalTime.of(unpackHour(packed), unpackMinute(packed), unpackSecond(packed),
                            unpackMilli(packed) * NANOS_PER_MILLI);
        }

        @Override
        default int packEpochMilli(final long millisSinceEpoch) {
            return Epoch.valueOf(validationMethod()).fromEpochMilli(millisSinceEpoch, this);
        }

        @Override
        default long unpackMilliOfDay(final int packed) {
            return unpackHour(packed) * MILLIS_PER_HOUR +
                    unpackMinute(packed) * MILLIS_PER_MINUTE +
                    unpackSecond(packed) * MILLIS_PER_SECOND +
                    unpackMilli(packed);
        }

        @Override
        default MilliTimePacker forValidationMethod(final ValidationMethod validationMethod) {
            return valueOf(packing(), validationMethod);
        }
    }

    /**
     * Non-validating binary packing method.  This packing method uses bit shifting and other bitwise logical operations
     * and is very efficient; resulting packed dates are not easily human readable.
     */
    MilliTimePacker BINARY = new Default() {
        @Override
        public Packing packing() {
            return Packing.BINARY;
        }

        @Override
        public ValidationMethod validationMethod() {
            return ValidationMethod.UNVALIDATED;
        }

        @Override
        public int pack(final int hour, final int minute, final int second, final int milli) {
            return ((hour & 0x1f) << 22) | ((minute & 0x3f) << 16) | ((second & 0x3f)  << 10) | (milli & 0x3ff);
        }

        @Override
        public int unpackHour(final int packed) {
            return (packed >>> 22) & 0x1f;
        }

        @Override
        public int unpackMinute(final int packed) {
            return (packed >>> 16) & 0x3f;
        }

        @Override
        public int unpackSecond(final int packed) {
            return (packed >> 10) & 0x3f;
        }

        @Override
        public int unpackMilli(final int packed) {
            return packed & 0x3ff;
        }

        @Override
        public String toString() {
            return "MilliTimePacker.BINARY";
        }
    };

    /**
     * Non-validating decimal packing method.  This packing method uses multiplications, divisions and modulo operations
     * which means it is less efficient than binary packing but results in human readable packed integers.  For instance
     * the time 14:15:16.170 is packed into the integer value 141516170.
     */
    MilliTimePacker DECIMAL = new Default() {
        @Override
        public Packing packing() {
            return Packing.DECIMAL;
        }

        @Override
        public ValidationMethod validationMethod() {
            return ValidationMethod.UNVALIDATED;
        }

        @Override
        public int pack(final int hour, final int minute, final int second, final int milli) {
            return hour * 100_00_000 + minute * 100_000 + second * 1000 + milli;
        }

        @Override
        public int unpackHour(final int packed) {
            return packed / 100_00_000;
        }

        @Override
        public int unpackMinute(final int packed) {
            return (packed / 100_000) % 100;
        }

        @Override
        public int unpackSecond(final int packed) {
            return (packed / 1000) % 100;
        }

        @Override
        public int unpackMilli(final int packed) {
            return packed % MILLIS_PER_SECOND;
        }

        @Override
        public String toString() {
            return "MilliTimePacker.DECIMAL";
        }
    };

    /**
     * Implementation that performs validation before packing and after unpacking a time value.  Instances can be
     * accessed via {@link #valueOf(Packing, ValidationMethod)}.
     */
    class Validated implements Default {
        private final MilliTimePacker packer;
        private final TimeValidator validator;

        public Validated(final MilliTimePacker packer, final ValidationMethod validationMethod) {
            this(packer, TimeValidator.valueOf(validationMethod));
        }

        public Validated(final MilliTimePacker packer, final TimeValidator validator) {
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
        public int pack(final int hour, final int minute, final int second, final int milli) {
            if (validator.validateTimeWithMillis(hour, minute, second, milli) != TimeValidator.INVALID) {
                return packer.pack(hour, minute, second, milli);
            }
            return INVALID;
        }

        @Override
        public int unpackHour(final int packed) {
            return validator.validateHour(packer.unpackHour(packed));
        }

        @Override
        public int unpackMinute(final int packed) {
            return validator.validateMinute(packer.unpackMinute(packed));
        }

        @Override
        public int unpackSecond(final int packed) {
            return validator.validateSecond(packer.unpackSecond(packed));
        }

        @Override
        public int unpackMilli(final int packed) {
            return validator.validateMilli(packer.unpackMilli(packed));
        }

        @Override
        public String toString() {
            return "MilliTimePacker.Validated." + packer.packing();
        }
    }

    /**
     * Helper class that manages instances of milli-time packers.
     */
    final class Instances {
        private static final MilliTimePacker[][] BY_PACKING_AND_VALIDATION_METHOD = instancesByPackingAndValidationMethod();

        private static MilliTimePacker valueOf(final Packing packing, final ValidationMethod validationMethod) {
            return BY_PACKING_AND_VALIDATION_METHOD[packing.ordinal()][validationMethod.ordinal()];
        }

        private static MilliTimePacker[][] instancesByPackingAndValidationMethod() {
            final MilliTimePacker[][] instances = new MilliTimePacker[Packing.count()][ValidationMethod.count()];
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
