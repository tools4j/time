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
package org.tools4j.time.format;

import org.tools4j.time.base.Allocation;
import org.tools4j.time.base.Epoch;
import org.tools4j.time.pack.DatePacker;
import org.tools4j.time.pack.Packing;
import org.tools4j.time.validate.DateValidator;
import org.tools4j.time.validate.ValidationMethod;
import org.tools4j.time.zone.Zone;

import java.time.LocalDate;

import static org.tools4j.time.base.Allocation.Type.RESULT;
import static org.tools4j.time.base.TimeFactors.MILLIS_PER_DAY;
import static org.tools4j.time.base.TimeFactors.MILLIS_PER_SECOND;
import static org.tools4j.time.base.TimeFactors.SECONDS_PER_DAY;

public interface DateFormatter {
    int INVALID = DateValidator.INVALID;
    char DEFAULT_SEPARATOR = '-';
    char NO_SEPARATOR = Ascii.NO_SEPARATOR;
    DateFormat format();
    char separator();
    ValidationMethod validationMethod();

    int format(int year, int month, int day, Appendable appendable);
    <T> int format(int year, int month, int day, T target, AsciiWriter<? super T> writer);
    <T> int format(int year, int month, int day, T target, AsciiWriter<? super T> writer, int offset);
    int formatPackedDate(int packedDate, Packing packing, Appendable appendable);
    <T> int formatPackedDate(int packedDate, Packing packing, T target, AsciiWriter<? super T> writer);
    <T> int formatPackedDate(int packedDate, Packing packing, T target, AsciiWriter<? super T> writer, int offset);

    int formatEpochDay(long daysSinceEpoch, Appendable appendable);
    <T> int formatEpochDay(long daysSinceEpoch, T target, AsciiWriter<? super T> writer);
    <T> int formatEpochDay(long daysSinceEpoch, T target, AsciiWriter<? super T> writer, int offset);
    int formatEpochSecond(long secondsSinceEpoch, Appendable appendable);
    <T> int formatEpochSecond(long secondsSinceEpoch, T target, AsciiWriter<? super T> writer);
    <T> int formatEpochSecond(long secondsSinceEpoch, T target, AsciiWriter<? super T> writer, int offset);
    int formatEpochSecond(long secondsSinceEpoch, Zone zone, Appendable appendable);
    <T> int formatEpochSecond(long secondsSinceEpoch, Zone zone, T target, AsciiWriter<? super T> writer);
    <T> int formatEpochSecond(long secondsSinceEpoch, Zone zone, T target, AsciiWriter<? super T> writer, int offset);
    int formatEpochMilli(long millisSinceEpoch, Appendable appendable);
    <T> int formatEpochMilli(long millisSinceEpoch, T target, AsciiWriter<? super T> writer);
    <T> int formatEpochMilli(long millisSinceEpoch, T target, AsciiWriter<? super T> writer, int offset);
    int formatEpochMilli(long millisSinceEpoch, Zone zone, Appendable appendable);
    <T> int formatEpochMilli(long millisSinceEpoch, Zone zone, T target, AsciiWriter<? super T> writer);
    <T> int formatEpochMilli(long millisSinceEpoch, Zone zone, T target, AsciiWriter<? super T> writer, int offset);
    int formatLocalDate(LocalDate localDate, Appendable appendable);
    <T> int formatLocalDate(LocalDate localDate, T target, AsciiWriter<? super T> writer);
    <T> int formatLocalDate(LocalDate localDate, T target, AsciiWriter<? super T> writer, int offset);

    /**
     * Returns a date formatter for the specified format which performs no validation.  If the date format contains a
     * separator character, '-' is used.
     * @param format the date format
     * @return a cached formatter instance
     */
    static DateFormatter valueOf(final DateFormat format) {
        return valueOf(format, ValidationMethod.UNVALIDATED);
    }

    /**
     * Returns a date formatter for the specified format and separator charactor which performs no validation.
     * @param format the date format
     * @param separatorChar the character separating date parts, e.g. '-' in '20170-07-31';
     *                      ignored if format has no separator characters
     * @return a cached formatter instance for {@link #NO_SEPARATOR} or standard separators '-', '/' and '.'; and otherwise
     *         a newly created formatter instance
     */
    @Allocation(value = RESULT, rare = true, text="new instance only for format with non-standard separator char")
    static DateFormatter valueOf(final DateFormat format, final char separatorChar) {
        return valueOf(format, separatorChar, ValidationMethod.UNVALIDATED);
    }

    /**
     * Returns a date formatter for the specified format and validation method.  If the date format contains a separator
     * character, '-' is used.
     * @param format the date format
     * @param validationMethod the type of date validation to perform
     * @return a cached formatter instance
     */
    static DateFormatter valueOf(final DateFormat format, final ValidationMethod validationMethod) {
        return valueOf(format, DEFAULT_SEPARATOR, validationMethod);
    }

    /**
     * Returns a date formatter for the specified format, separator charactor and validation method
     * @param format the date format
     * @param separatorChar the character separating date parts, e.g. '-' in '20170-07-31';
     *                      ignored if format has no separator characters
     * @param validationMethod the type of date validation to perform
     * @return a cached formatter instance for {@link #NO_SEPARATOR} or standard separators '-', '/' and '.'; and otherwise
     *         a newly created formatter instance
     */
    @Allocation(value = RESULT, rare = true, text="new instance only for format with non-standard separator char")
    static DateFormatter valueOf(final DateFormat format, final char separatorChar, final ValidationMethod validationMethod) {
        return Instances.valueOf(format, separatorChar, validationMethod);
    }

    interface Default extends DateFormatter {
        @Override
        default int format(final int year, final int month, final int day, final Appendable appendable) {
            final StringBuilder temp = Appendables.acquireStringBuilder(appendable);
            try {
                return format(year, month, day, temp, AsciiWriter.STRING_BUILDER);
            } finally {
                Appendables.appendAndReleaseStringBuilder(appendable, temp);
            }
        }

        @Override
        default <T> int format(final int year, final int month, final int day,
                               final T target, final AsciiWriter<? super T> writer) {
            return format(year, month, day, target, writer, 0);
        }

        @Override
        default int formatEpochDay(final long daysSinceEpoch, final Appendable appendable) {
            final DatePacker packer = DatePacker.BINARY;
            final int packed = Epoch.valueOf(validationMethod()).fromEpochDay(daysSinceEpoch, packer);
            return format(packer.unpackYear(packed), packer.unpackMonth(packed), packer.unpackDay(packed), appendable);
        }

        @Override
        default <T> int formatEpochDay(final long daysSinceEpoch,
                                       final T target, final AsciiWriter<? super T> writer) {
            return formatEpochDay(daysSinceEpoch, target, writer, 0);
        }

        @Override
        default <T> int formatEpochDay(final long daysSinceEpoch,
                                       final T target, final AsciiWriter<? super T> writer, final int offset) {
            final int packedDate = Epoch.valueOf(validationMethod()).fromEpochDay(daysSinceEpoch, DatePacker.BINARY);
            return formatPackedDate(packedDate, Packing.BINARY, target, writer, offset);
        }

        @Override
        default int formatEpochSecond(final long secondsSinceEpoch, final Appendable appendable) {
            return formatEpochDay(Math.floorDiv(secondsSinceEpoch, SECONDS_PER_DAY), appendable);
        }

        @Override
        default <T> int formatEpochSecond(final long secondsSinceEpoch,
                                          final T target, final AsciiWriter<? super T> writer) {
            return formatEpochSecond(secondsSinceEpoch, target, writer, 0);
        }

        @Override
        default <T> int formatEpochSecond(final long secondsSinceEpoch,
                                          final T target, final AsciiWriter<? super T> writer, final int offset) {
            return formatEpochDay(Math.floorDiv(secondsSinceEpoch, SECONDS_PER_DAY), target, writer, offset);
        }

        @Override
        default int formatEpochSecond(final long secondsSinceEpoch, final Zone zone, final Appendable appendable) {
            return formatEpochSecond(secondsSinceEpoch + zone.offsetSeconds(secondsSinceEpoch), appendable);
        }

        @Override
        default <T> int formatEpochSecond(final long secondsSinceEpoch, final Zone zone,
                                          final T target, final AsciiWriter<? super T> writer) {
            return formatEpochSecond(secondsSinceEpoch, zone, target, writer, 0);
        }

        @Override
        default <T> int formatEpochSecond(final long secondsSinceEpoch, final Zone zone,
                                          final T target, final AsciiWriter<? super T> writer, final int offset) {
            return formatEpochSecond(secondsSinceEpoch + zone.offsetSeconds(secondsSinceEpoch), target, writer, offset);
        }

        @Override
        default int formatEpochMilli(final long millisSinceEpoch, final Appendable appendable) {
            return formatEpochDay(Math.floorDiv(millisSinceEpoch, MILLIS_PER_DAY), appendable);
        }

        @Override
        default <T> int formatEpochMilli(final long millisSinceEpoch,
                                         final T target, final AsciiWriter<? super T> writer) {
            return formatEpochMilli(millisSinceEpoch, target, writer, 0);
        }

        @Override
        default <T> int formatEpochMilli(final long millisSinceEpoch,
                                         final T target, final AsciiWriter<? super T> writer, final int offset) {
            return formatEpochDay(Math.floorDiv(millisSinceEpoch, MILLIS_PER_DAY), target, writer, offset);
        }

        @Override
        default int formatEpochMilli(final long millisSinceEpoch, final Zone zone, final Appendable appendable) {
            return formatEpochSecond(Math.floorDiv(millisSinceEpoch, MILLIS_PER_SECOND), zone, appendable);
        }

        @Override
        default <T> int formatEpochMilli(final long millisSinceEpoch, final Zone zone,
                                         final T target, final AsciiWriter<? super T> writer) {
            return formatEpochMilli(millisSinceEpoch, zone, target, writer, 0);
        }

        @Override
        default <T> int formatEpochMilli(final long millisSinceEpoch, final Zone zone,
                                         final T target, final AsciiWriter<? super T> writer, final int offset) {
            return formatEpochSecond(Math.floorDiv(millisSinceEpoch, MILLIS_PER_SECOND), zone, target, writer, offset);
        }

        @Override
        default int formatPackedDate(final int packedDate, final Packing packing,
                                     final Appendable appendable) {
            final DatePacker packer = DatePacker.valueOf(packing, validationMethod());
            return format(packer.unpackYear(packedDate), packer.unpackMonth(packedDate), packer.unpackDay(packedDate),
                    appendable);
        }

        @Override
        default <T> int formatPackedDate(final int packedDate, final Packing packing,
                                         final T target, final AsciiWriter<? super T> writer) {
            return formatPackedDate(packedDate, packing, target, writer, 0);
        }

        @Override
        default <T> int formatPackedDate(final int packedDate, final Packing packing,
                                         final T target, final AsciiWriter<? super T> writer, final int offset) {
            final DatePacker packer = DatePacker.valueOf(packing, validationMethod());
            return format(packer.unpackYear(packedDate), packer.unpackMonth(packedDate), packer.unpackDay(packedDate),
                    target, writer, offset);
        }

        @Override
        default int formatLocalDate(final LocalDate localDate, final Appendable appendable) {
            return format(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(), appendable);
        }

        @Override
        default <T> int formatLocalDate(final LocalDate localDate, final T target, final AsciiWriter<? super T> writer) {
            return formatLocalDate(localDate, target, writer, 0);
        }

        @Override
        default <T> int formatLocalDate(final LocalDate localDate,
                                        final T target, final AsciiWriter<? super T> writer, final int offset) {
            return format(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(), target, writer, offset);
        }
    }

    /**
     * Helper class that manages instances of time packers.
     */
    final class Instances {
        private static final DateFormatter[][] BY_FORMAT_AND_VALIDATION_METHOD_NONE = instancesByFormatAndValidationMethod(NO_SEPARATOR);
        private static final DateFormatter[][] BY_FORMAT_AND_VALIDATION_METHOD_DASH = instancesByFormatAndValidationMethod('-');
        private static final DateFormatter[][] BY_FORMAT_AND_VALIDATION_METHOD_SLASH = instancesByFormatAndValidationMethod('/');
        private static final DateFormatter[][] BY_FORMAT_AND_VALIDATION_METHOD_DOT = instancesByFormatAndValidationMethod('.');

        private static DateFormatter valueOf(final DateFormat format, final char separatorChar,
                                             final ValidationMethod validationMethod) {
            if (!format.hasSeparators() | separatorChar == NO_SEPARATOR) {
                return BY_FORMAT_AND_VALIDATION_METHOD_NONE[format.ordinal()][validationMethod.ordinal()];
            }
            if (separatorChar == '-') {
                return BY_FORMAT_AND_VALIDATION_METHOD_DASH[format.ordinal()][validationMethod.ordinal()];
            }
            if (separatorChar == '/') {
                return BY_FORMAT_AND_VALIDATION_METHOD_SLASH[format.ordinal()][validationMethod.ordinal()];
            }
            if (separatorChar == '.') {
                return BY_FORMAT_AND_VALIDATION_METHOD_DOT[format.ordinal()][validationMethod.ordinal()];
            }
            return create(format, separatorChar, validationMethod);
        }

        private static DateFormatter[][] instancesByFormatAndValidationMethod(final char separatorChar) {
            final DateFormatter[][] instances = new DateFormatter[DateFormat.count()][ValidationMethod.count()];
            for (int fOrd = 0; fOrd < DateFormat.count(); fOrd++) {
                final DateFormat format = DateFormat.valueByOrdinal(fOrd);
                if (format.hasSeparators() | separatorChar == NO_SEPARATOR) {
                    for (int vOrd = 0; vOrd < ValidationMethod.count(); vOrd++) {
                        final ValidationMethod validationMethod = ValidationMethod.valueByOrdinal(vOrd);
                        instances[fOrd][vOrd] = create(format, separatorChar, validationMethod);
                    }
                }
            }
            return instances;
        }

        private static DateFormatter create(final DateFormat format, final char separatorChar,
                                            final ValidationMethod validationMethod) {
            return new SimpleDateFormatter(format, separatorChar, validationMethod);
        }
    }
}
