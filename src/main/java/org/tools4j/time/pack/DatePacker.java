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
import org.tools4j.time.validate.ValidationMethod;

import java.time.LocalDate;
import java.util.Objects;

import static org.tools4j.time.base.Allocation.Type.RESULT;

/**
 * Packs a date (year, month, day) into an integer.  Packing and unpacking can be done with or without date validation
 * using different {@link #validationMethod() validation methods}.  A {@link #DECIMAL} and a {@link #BINARY} packing is
 * supported and both packings preserve the natural date ordering, that is, if the packed integers are sorted then the
 * corresponding dates are also sorted.  Packing and unpacking of null values is supported via {@link #packNull()} and
 * {@link #unpackNull(int)}.
 * <p>
 * <i>Examples:</i>
 * <ul>
 *     <li>{@link #DECIMAL} packing for a date 21-Jan-2017 is 20170121</li>
 *     <li>{@link #BINARY} packing uses shifts to pack the date parts which is more efficient but the result is not
 *     easily human-readable</li>
 * </ul>
 * @see #valueOf(Packing, ValidationMethod)
 * @see #BINARY
 * @see #DECIMAL
 */
public interface DatePacker {
    int INVALID = -1;
    int NULL = 0;

    Packing packing();
    ValidationMethod validationMethod();
    DatePacker forValidationMethod(ValidationMethod validationMethod);
    int pack(int year, int month, int day);
    int pack(long packedDateTime, Packing packing);
    int unpackYear(int packed);
    int unpackMonth(int packed);
    int unpackDay(int packed);
    int packNull();
    boolean unpackNull(int packed);
    int pack(LocalDate localDate);
    @Allocation(RESULT)
    LocalDate unpackLocalDate(int packed);
    int packEpochDay(long daysSinceEpoch);
    long unpackEpochDay(int packed);
    int packEpochMilli(long millisSinceEpoch);
    long unpackEpochMilli(int packed);

    /**
     * Returns a date packer that performs no validation.
     * @param packing the packing type for the returned packer
     * @return a cached packer instance
     */
    static DatePacker valueOf(final Packing packing) {
        return Instances.valueOf(packing, ValidationMethod.UNVALIDATED);
    }

    /**
     * Returns a date packer that performs validation using the specified validation method.
     * @param packing the packing type for the returned packer
     * @param validationMethod validation method to perform during packing and unpacking operations
     * @return a cached packer instance
     */
    static DatePacker valueOf(final Packing packing, final ValidationMethod validationMethod) {
        return Instances.valueOf(packing, validationMethod);
    }

    /**
     * Provides common default implementations for date packer.
     */
    interface Default extends DatePacker {
        @Override
        default int packNull() {
            return NULL;
        }

        @Override
        default boolean unpackNull(final int packed) {
            return packed == NULL;
        }

        @Override
        default int pack(final LocalDate localDate) {
            return localDate == null ? packNull() : pack(
                    localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth()
            );
        }

        @Override
        default int pack(final long packedDateTime, final Packing packing) {
            final DateTimePacker unpacker = DateTimePacker.valueOf(packing, validationMethod());
            return pack(
                    unpacker.unpackYear(packedDateTime),
                    unpacker.unpackMonth(packedDateTime),
                    unpacker.unpackDay(packedDateTime)
            );
        }

        @Override
        @Allocation(RESULT)
        default LocalDate unpackLocalDate(final int packed) {
            return unpackNull(packed) ? null : LocalDate.of(
                    unpackYear(packed), unpackMonth(packed), unpackDay(packed)
            );
        }

        @Override
        default int packEpochDay(final long daysSinceEpoch) {
            return Epoch.valueOf(validationMethod()).fromEpochDay(daysSinceEpoch, this);
        }

        @Override
        default long unpackEpochDay(final int packed) {
            return Epoch.valueOf(validationMethod()).toEpochDay(packed, this);
        }

        @Override
        default int packEpochMilli(final long millisSinceEpoch) {
            return Epoch.valueOf(validationMethod()).fromEpochMilli(millisSinceEpoch, this);
        }

        @Override
        default long unpackEpochMilli(final int packed) {
            return Epoch.valueOf(validationMethod()).toEpochMilli(packed, this);
        }

        @Override
        default DatePacker forValidationMethod(final ValidationMethod validationMethod) {
            return valueOf(packing(), validationMethod);
        }
    }

    /**
     * Non-validating binary packing method.  This packing method uses bit shifting and other bitwise logical operations
     * and is very efficient; resulting packed dates are not easily .
     */
    DatePacker BINARY = new DatePacker.Default() {
        @Override
        public Packing packing() {
            return Packing.BINARY;
        }

        @Override
        public ValidationMethod validationMethod() {
            return ValidationMethod.UNVALIDATED;
        }

        @Override
        public int pack(final int year, final int month, final int day) {
            return ((year & 0x3fff) << 9) | ((month & 0xf) << 5) | (day & 0x1f);
        }

        @Override
        public int unpackYear(final int packed) {
            return (packed >>> 9) & 0x3fff;
        }

        @Override
        public int unpackMonth(final int packed) {
            return (packed >>> 5) & 0xf;
        }

        @Override
        public int unpackDay(final int packed) {
            return packed & 0x1f;
        }

        @Override
        public String toString() {
            return "DatePacker.BINARY";
        }
    };

    /**
     * Non-validating decimal packing method.  This packing method uses multiplications, divisions and modulo operations
     * which means it is less efficient than binary packing but results in  packed integers.  For instance
     * the date 21-Jan-2017 is packed into the integer value 20170121.
     */
    DatePacker DECIMAL = new DatePacker.Default() {
        @Override
        public Packing packing() {
            return Packing.DECIMAL;
        }

        @Override
        public ValidationMethod validationMethod() {
            return ValidationMethod.UNVALIDATED;
        }

        @Override
        public int pack(final int year, final int month, final int day) {
            return (year * 100_00) + (month * 100) + day;
        }

        @Override
        public int unpackYear(final int packed) {
            return packed / 100_00;
        }

        @Override
        public int unpackMonth(final int packed) {
            return (packed / 100) % 100;
        }

        @Override
        public int unpackDay(final int packed) {
            return packed % 100;
        }

        @Override
        public String toString() {
            return "DatePacker.DECIMAL";
        }
    };

    /**
     * Implementation that performs validation before packing and after unpacking a date.  Instances can be accessed
     * via {@link #valueOf(Packing, ValidationMethod)}.
     */
    @SuppressWarnings("ClassCanBeRecord")
    class Validated implements DatePacker.Default {
        private final DatePacker packer;
        private final DateValidator validator;

        protected Validated(final DatePacker packer, final ValidationMethod validationMethod) {
            this(packer, DateValidator.valueOf(validationMethod));
        }

        protected Validated(final DatePacker packer, final DateValidator validator) {
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
        public int pack(final int year, final int month, final int day) {
            if (validator.validateDay(year, month, day) != DateValidator.INVALID) {
                return packer.pack(year, month, day);
            }
            return INVALID;
        }

        @Override
        public int unpackYear(final int packed) {
            return validator.validateYear(packer.unpackYear(packed));
        }

        @Override
        public int unpackMonth(final int packed) {
            return validator.validateMonth(packer.unpackMonth(packed));
        }

        @Override
        public int unpackDay(final int packed) {
            final int year = packer.unpackYear(packed);
            final int month = packer.unpackMonth(packed);
            final int day = packer.unpackDay(packed);
            return validator.validateDay(year, month, day);
        }

        @Override
        public String toString() {
            return "DatePacker.Validated." + packer.packing();
        }

    }

    /**
     * Helper class that manages instances of date packers.
     */
    final class Instances {
        private static final DatePacker[][] BY_PACKING_AND_VALIDATION_METHOD = instancesByPackingAndValidationMethod();

        private static DatePacker valueOf(final Packing packing, final ValidationMethod validationMethod) {
            return BY_PACKING_AND_VALIDATION_METHOD[packing.ordinal()][validationMethod.ordinal()];
        }

        private static DatePacker[][] instancesByPackingAndValidationMethod() {
            final DatePacker[][] instances = new DatePacker[Packing.count()][ValidationMethod.count()];
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
