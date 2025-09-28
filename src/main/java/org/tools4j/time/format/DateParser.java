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

public interface DateParser {
    int INVALID = DateValidator.INVALID;
    long INVALID_EPOCH = DateValidator.INVALID_EPOCH;
    byte INVALID_SEPARATOR = -1;
    char DEFAULT_SEPARATOR = '-';
    char NO_SEPARATOR = Ascii.NO_SEPARATOR;

    DateFormat format();
    char separator();
    ValidationMethod validationMethod();

    int parseYear(CharSequence charSequence);
    int parseYear(CharSequence charSequence, int offset);
    <S> int parseYear(S source, AsciiReader<? super S> reader);
    <S> int parseYear(S source, AsciiReader<? super S> reader, int offset);
    int parseMonth(CharSequence charSequence);
    int parseMonth(CharSequence charSequence, int offset);
    <S> int parseMonth(S source, AsciiReader<? super S> reader);
    <S> int parseMonth(S source, AsciiReader<? super S> reader, int offset);
    int parseDay(CharSequence charSequence);
    int parseDay(CharSequence charSequence, int offset);
    <S> int parseDay(S source, AsciiReader<? super S> reader);
    <S> int parseDay(S source, AsciiReader<? super S> reader, int offset);
    int parseAsPackedDate(CharSequence charSequence, Packing packing);
    int parseAsPackedDate(CharSequence charSequence, int offset, Packing packing);
    <S> int parseAsPackedDate(S source, AsciiReader<? super S> reader, Packing packing);
    <S> int parseAsPackedDate(S source, AsciiReader<? super S> reader, int offset, Packing packing);
    long parseAsEpochDay(CharSequence charSequence);
    long parseAsEpochDay(CharSequence charSequence, int offset);
    <S> long parseAsEpochDay(S source, AsciiReader<? super S> reader);
    <S> long parseAsEpochDay(S source, AsciiReader<? super S> reader, int offset);
    long parseAsEpochSecond(CharSequence charSequence);
    long parseAsEpochSecond(CharSequence charSequence, int offset);
    <S> long parseAsEpochSecond(S source, AsciiReader<? super S> reader);
    <S> long parseAsEpochSecond(S source, AsciiReader<? super S> reader, int offset);
    long parseAsEpochSecond(CharSequence charSequence, Zone zone);
    long parseAsEpochSecond(CharSequence charSequence, int offset, Zone zone);
    <S> long parseAsEpochSecond(S source, AsciiReader<? super S> reader, Zone zone);
    <S> long parseAsEpochSecond(S source, AsciiReader<? super S> reader, int offset, Zone zone);
    long parseAsEpochMilli(CharSequence charSequence);
    long parseAsEpochMilli(CharSequence charSequence, int offset);
    <S> long parseAsEpochMilli(S source, AsciiReader<? super S> reader);
    <S> long parseAsEpochMilli(S source, AsciiReader<? super S> reader, int offset);
    long parseAsEpochMilli(CharSequence charSequence, Zone zone);
    long parseAsEpochMilli(CharSequence charSequence, int offset, Zone zone);
    <S> long parseAsEpochMilli(S source, AsciiReader<? super S> reader, Zone zone);
    <S> long parseAsEpochMilli(S source, AsciiReader<? super S> reader, int offset, Zone zone);
    @Allocation(RESULT)
    LocalDate parseAsLocalDate(CharSequence charSequence);
    @Allocation(RESULT)
    LocalDate parseAsLocalDate(CharSequence charSequence, int offset);
    @Allocation(RESULT)
    <S> LocalDate parseAsLocalDate(S source, AsciiReader<? super S> reader);
    @Allocation(RESULT)
    <S> LocalDate parseAsLocalDate(S source, AsciiReader<? super S> reader, int offset);
    char parseSeparator(CharSequence charSequence, int separatorIndex);
    char parseSeparator(CharSequence charSequence, int offset, int separatorIndex);
    <S> char parseSeparator(S source, AsciiReader<? super S> reader, int separatorIndex);
    <S> char parseSeparator(S source, AsciiReader<? super S> reader, int offset, int separatorIndex);

    boolean isValid(CharSequence charSequence);
    boolean isValid(CharSequence charSequence, int offset);
    <S> boolean isValid(S source, AsciiReader<? super S> reader);
    <S> boolean isValid(S source, AsciiReader<? super S> reader, int offset);

    /**
     * Returns a date parser for the specified format which performs no validation.  If the date format contains a
     * separator character, '-' is used.
     * @param format the date format
     * @return a cached parser instance
     */
    static DateParser valueOf(final DateFormat format) {
        return valueOf(format, ValidationMethod.UNVALIDATED);
    }

    /**
     * Returns a date parser for the specified format and separator character which performs no validation.
     * @param format the date format
     * @param separatorChar the character separating date parts, e.g. '-' in '20170-07-31';
     *                      ignored if format has no separator characters
     * @return a cached parser instance for {@link #NO_SEPARATOR} or standard separators '-', '/' and '.'; and otherwise
     *         a newly created parser instance
     */
    @Allocation(value = RESULT, rare = true, text="new instance only for format with non-standard separator char")
    static DateParser valueOf(final DateFormat format, final char separatorChar) {
        return valueOf(format, separatorChar, ValidationMethod.UNVALIDATED);
    }

    /**
     * Returns a date parser for the specified format and validation method.  If the date format contains a separator
     * character, '-' is used.
     * @param format the date format
     * @param validationMethod the type of date validation to perform
     * @return a cached parser instance
     */
    static DateParser valueOf(final DateFormat format, final ValidationMethod validationMethod) {
        return valueOf(format, DEFAULT_SEPARATOR, validationMethod);
    }

    /**
     * Returns a date parser for the specified format, separator character and validation method
     * @param format the date format
     * @param separatorChar the character separating date parts, e.g. '-' in '20170-07-31';
     *                      ignored if format has no separator characters
     * @param validationMethod the type of date validation to perform
     * @return a cached parser instance for {@link #NO_SEPARATOR} or standard separators '-', '/' and '.'; and otherwise
     *         a newly created parser instance
     */
    @Allocation(value = RESULT, rare = true, text="new instance only for format with non-standard separator char")
    static DateParser valueOf(final DateFormat format, final char separatorChar, final ValidationMethod validationMethod) {
        return Instances.valueOf(format, separatorChar, validationMethod);
    }

    interface Default extends DateParser {
        @Override
        default int parseYear(final CharSequence charSequence) {
            return parseYear(charSequence, AsciiReader.CHAR_SEQUENCE);
        }
        @Override
        default int parseYear(final CharSequence charSequence, final int offset) {
            return parseYear(charSequence, AsciiReader.CHAR_SEQUENCE, offset);
        }
        @Override
        default <S> int parseYear(final S source, final AsciiReader<? super S> reader) {
            return parseYear(source, reader, 0);
        }
        @Override
        default int parseMonth(final CharSequence charSequence) {
            return parseMonth(charSequence, AsciiReader.CHAR_SEQUENCE);
        }
        @Override
        default int parseMonth(final CharSequence charSequence, final int offset) {
            return parseMonth(charSequence, AsciiReader.CHAR_SEQUENCE, offset);
        }
        @Override
        default <S> int parseMonth(final S source, final AsciiReader<? super S> reader) {
            return parseMonth(source, reader, 0);
        }
        @Override
        default int parseDay(final CharSequence charSequence) {
            return parseDay(charSequence, AsciiReader.CHAR_SEQUENCE);
        }
        @Override
        default int parseDay(final CharSequence charSequence, final int offset) {
            return parseDay(charSequence, AsciiReader.CHAR_SEQUENCE, offset);
        }
        @Override
        default <S> int parseDay(final S source, final AsciiReader<? super S> reader) {
            return parseDay(source, reader, 0);
        }
        @Override
        default int parseAsPackedDate(final CharSequence charSequence, final Packing packing) {
            return parseAsPackedDate(charSequence, AsciiReader.CHAR_SEQUENCE, packing);
        }
        @Override
        default int parseAsPackedDate(final CharSequence charSequence, final int offset, final Packing packing) {
            return parseAsPackedDate(charSequence, AsciiReader.CHAR_SEQUENCE, offset, packing);
        }
        @Override
        default <S> int parseAsPackedDate(final S source, final AsciiReader<? super S> reader, final Packing packing) {
            return parseAsPackedDate(source, reader, 0, packing);
        }
        @Override
        default long parseAsEpochDay(final CharSequence charSequence) {
            return parseAsEpochDay(charSequence, AsciiReader.CHAR_SEQUENCE);
        }
        @Override
        default long parseAsEpochDay(final CharSequence charSequence, final int offset) {
            return parseAsEpochDay(charSequence, AsciiReader.CHAR_SEQUENCE, offset);
        }
        @Override
        default <S> long parseAsEpochDay(final S source, final AsciiReader<? super S> reader) {
            return parseAsEpochDay(source, reader, 0);
        }

        @Override
        default long parseAsEpochSecond(final CharSequence charSequence) {
            return parseAsEpochSecond(charSequence, AsciiReader.CHAR_SEQUENCE);
        }
        @Override
        default long parseAsEpochSecond(final CharSequence charSequence, final int offset) {
            return parseAsEpochSecond(charSequence, AsciiReader.CHAR_SEQUENCE, offset);
        }
        @Override
        default <S> long parseAsEpochSecond(final S source, final AsciiReader<? super S> reader) {
            return parseAsEpochSecond(source, reader, 0);
        }
        @Override
        default <S> long parseAsEpochSecond(final S source, final AsciiReader<? super S> reader, final int offset) {
            final long epochDay = parseAsEpochDay(source, reader, offset);
            return epochDay != INVALID_EPOCH ? epochDay * SECONDS_PER_DAY : INVALID_EPOCH;
        }

        @Override
        default long parseAsEpochSecond(final CharSequence charSequence, final Zone zone) {
            return parseAsEpochSecond(charSequence, AsciiReader.CHAR_SEQUENCE, zone);
        }
        @Override
        default long parseAsEpochSecond(final CharSequence charSequence, final int offset, final Zone zone) {
            return parseAsEpochSecond(charSequence, AsciiReader.CHAR_SEQUENCE, offset, zone);
        }
        @Override
        default <S> long parseAsEpochSecond(final S source, final AsciiReader<? super S> reader, final Zone zone) {
            return parseAsEpochSecond(source, reader, 0, zone);
        }
        @Override
        default <S> long parseAsEpochSecond(final S source, final AsciiReader<? super S> reader, final int offset, final Zone zone) {
            final int packed = parseAsPackedDate(source, reader, offset, Packing.BINARY);
            if (packed != INVALID) {
                final int year = DatePacker.BINARY.unpackYear(packed);
                final int month = DatePacker.BINARY.unpackMonth(packed);
                final int day = DatePacker.BINARY.unpackDay(packed);
                final int offsetSeconds = zone.offsetSeconds(year, month, day, 0, 0, 0, 0);
                return Epoch.valueOf(ValidationMethod.UNVALIDATED).toEpochSecond(year, month, day) - offsetSeconds;
            }
            return INVALID_EPOCH;
        }

        @Override
        default long parseAsEpochMilli(final CharSequence charSequence) {
            return parseAsEpochMilli(charSequence, AsciiReader.CHAR_SEQUENCE);
        }
        @Override
        default long parseAsEpochMilli(final CharSequence charSequence, final int offset) {
            return parseAsEpochMilli(charSequence, AsciiReader.CHAR_SEQUENCE, offset);
        }
        @Override
        default <S> long parseAsEpochMilli(final S source, final AsciiReader<? super S> reader) {
            return parseAsEpochMilli(source, reader, 0);
        }
        @Override
        default <S> long parseAsEpochMilli(final S source, final AsciiReader<? super S> reader, final int offset) {
            final long epochDay = parseAsEpochDay(source, reader, offset);
            return epochDay != INVALID_EPOCH ? epochDay * MILLIS_PER_DAY : INVALID_EPOCH;
        }
        @Override
        default long parseAsEpochMilli(final CharSequence charSequence, final Zone zone) {
            return parseAsEpochMilli(charSequence, AsciiReader.CHAR_SEQUENCE, zone);
        }
        @Override
        default long parseAsEpochMilli(final CharSequence charSequence, final int offset, final Zone zone) {
            return parseAsEpochMilli(charSequence, AsciiReader.CHAR_SEQUENCE, offset, zone);
        }
        @Override
        default <S> long parseAsEpochMilli(final S source, final AsciiReader<? super S> reader, final Zone zone) {
            return parseAsEpochMilli(source, reader, 0, zone);
        }
        @Override
        default <S> long parseAsEpochMilli(final S source, final AsciiReader<? super S> reader, final int offset, final Zone zone) {
            final long epochSecond = parseAsEpochSecond(source, reader, offset, zone);
            return epochSecond != INVALID_EPOCH ? epochSecond * MILLIS_PER_SECOND : INVALID_EPOCH;
        }
        @Override
        default LocalDate parseAsLocalDate(final CharSequence charSequence) {
            return parseAsLocalDate(charSequence, AsciiReader.CHAR_SEQUENCE);
        }
        @Allocation(RESULT)
        @Override
        default LocalDate parseAsLocalDate(final CharSequence charSequence, final int offset) {
            return parseAsLocalDate(charSequence, AsciiReader.CHAR_SEQUENCE, offset);
        }
        @Allocation(RESULT)
        @Override
        default <S> LocalDate parseAsLocalDate(final S source, final AsciiReader<? super S> reader) {
            return parseAsLocalDate(source, reader, 0);
        }
        @Override
        default char parseSeparator(final CharSequence charSequence, final int separatorIndex) {
            return parseSeparator(charSequence, AsciiReader.CHAR_SEQUENCE, 0, separatorIndex);
        }
        @Override
        default char parseSeparator(final CharSequence charSequence, final int offset, final int separatorIndex) {
            return parseSeparator(charSequence, AsciiReader.CHAR_SEQUENCE, offset, separatorIndex);
        }
        @Override
        default <S> char parseSeparator(final S source, final AsciiReader<? super S> reader, final int separatorIndex) {
            return parseSeparator(source, reader, 0, separatorIndex);
        }
        @Override
        default boolean isValid(final CharSequence charSequence) {
            return isValid(charSequence, AsciiReader.CHAR_SEQUENCE, 0);
        }
        @Override
        default boolean isValid(final CharSequence charSequence, final int offset) {
            return isValid(charSequence, AsciiReader.CHAR_SEQUENCE, offset);
        }
        @Override
        default <S> boolean isValid(final S source, final AsciiReader<? super S> reader) {
            return isValid(source, reader, 0);
        }
    }

    /**
     * Helper class that manages instances of time packers.
     */
    final class Instances {
        private static final DateParser[][] BY_FORMAT_AND_VALIDATION_METHOD_NONE = instancesByFormatAndValidationMethod(NO_SEPARATOR);
        private static final DateParser[][] BY_FORMAT_AND_VALIDATION_METHOD_DASH = instancesByFormatAndValidationMethod('-');
        private static final DateParser[][] BY_FORMAT_AND_VALIDATION_METHOD_SLASH = instancesByFormatAndValidationMethod('/');
        private static final DateParser[][] BY_FORMAT_AND_VALIDATION_METHOD_DOT = instancesByFormatAndValidationMethod('.');

        private static DateParser valueOf(final DateFormat format, final char separatorChar,
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

        private static DateParser[][] instancesByFormatAndValidationMethod(final char separatorChar) {
            final DateParser[][] instances = new DateParser[DateFormat.count()][ValidationMethod.count()];
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

        private static DateParser create(final DateFormat format, final char separatorChar,
                                         final ValidationMethod validationMethod) {
            return validationMethod == ValidationMethod.UNVALIDATED ? new SimpleDateParser(format, separatorChar) :
                    new ValidatingDateParser(format, separatorChar, validationMethod);
        }
    }
}
