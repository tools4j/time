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

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Objects;

import static org.tools4j.time.base.Allocation.Type.RESULT;

final class ValidatingDateParser implements DateParser.Default {

    private final DateFormat format;
    private final ValidationMethod validationMethod;
    private final byte separator;

    ValidatingDateParser(final DateFormat format, final char separator,
                         final ValidationMethod validationMethod) {
        this.format = Objects.requireNonNull(format);
        this.validationMethod = Objects.requireNonNull(validationMethod);
        this.separator = Ascii.validateSeparatorChar(separator);
    }

    @Override
    public DateFormat format() {
        return format;
    }

    @Override
    public char separator() {
        return (char)separator;
    }

    @Override
    public ValidationMethod validationMethod() {
        return validationMethod;
    }

    @Override
    public <S> int parseYear(final S source, final AsciiReader<? super S> reader, final int offset) {
        return toYear(validationMethod(), format(), source, reader, offset);
    }
    private static <S> int toYear(final ValidationMethod validationMethod, final DateFormat format,
                                  final S source, final AsciiReader<? super S> reader, final int offset) {
        final int off = offset + format.offsetYear();
        final byte ch1 = reader.readChar(source, off);
        final byte ch2 = reader.readChar(source, off + 1);
        final byte ch3 = reader.readChar(source, off + 2);
        final byte ch4 = reader.readChar(source, off + 3);
        if (Ascii.isDigit(ch1) & Ascii.isDigit(ch2) & Ascii.isDigit(ch3) & Ascii.isDigit(ch4)) {
            final int year = Ascii.digit(ch1) * 1000 + Ascii.digit(ch2) * 100 + Ascii.digit(ch3) * 10 + Ascii.digit(ch4);
            if (year > 0) {
                return year;
            }
            return invalid(validationMethod, format, year, "Invalid year in date string: ", source, reader, offset);
        }
        return invalid(validationMethod, format, INVALID, "Illegal year digit in date string: ", source, reader, offset);
    }

    @Override
    public <S> int parseMonth(final S source, final AsciiReader<? super S> reader, final int offset) {
        return toMonth(validationMethod(), format(), source, reader, offset);
    }
    private static <S> int toMonth(final ValidationMethod validationMethod, final DateFormat format,
                                   final S source, final AsciiReader<? super S> reader, final int offset) {
        final int off = offset + format.offsetMonth();
        final byte ch1 = reader.readChar(source, off);
        final byte ch2 = reader.readChar(source, off + 1);
        if ((ch1 == '0' & '1' <= ch2 & ch2 <= '9') | (ch1 == '1' & '0' <= ch2 & ch2 <= '2')) {
            return Ascii.digit(ch1) * 10 + Ascii.digit(ch2);
        }
        if (Ascii.isDigit(ch1) & Ascii.isDigit(ch2)) {
            return invalid(validationMethod, format, INVALID, "Invalid month in date string: ", source, reader, offset);
        }
        return invalid(validationMethod, format, INVALID, "Illegal month digit in date string: ", source, reader, offset);
    }

    @Override
    public <S> int parseDay(final S source, final AsciiReader<? super S> reader, final int offset) {
        return toDay(validationMethod(), format(), source, reader, offset);
    }
    private static <S> int toDay(final ValidationMethod validationMethod, final DateFormat format,
                                 final S source, final AsciiReader<? super S> reader, final int offset) {
        final int off = offset + format.offsetDay();
        final byte ch1 = reader.readChar(source, off);
        final byte ch2 = reader.readChar(source, off + 1);
        if (Ascii.isDigit(ch1) & Ascii.isDigit(ch2)) {
            final int year = toYear(validationMethod, format, source, reader, offset);
            final int month = toMonth(validationMethod, format, source, reader, offset);
            final int day = Ascii.digit(ch1) * 10 + Ascii.digit(ch2);
            if (DateValidator.isValidDate(year, month, day)) {
                return day;
            }
            return invalid(validationMethod, format, INVALID, "Invalid day in date string: ", source, reader, offset);
        }
        return invalid(validationMethod, format, INVALID, "Illegal day digit in date string: ", source, reader, offset);
    }

    @Override
    public <S> int parseAsPackedDate(final S source, final AsciiReader<? super S> reader, final int offset, final Packing packing) {
        final int year = parseYear(source, reader, offset);
        final int month = parseMonth(source, reader, offset);
        final int day = parseDay(source, reader, offset);
        if (year != INVALID & month != INVALID & day != INVALID & hasValidSeparators(source, reader, offset)) {
            return DatePacker.valueOf(packing).pack(year, month, day);
        }
        return invalid(INVALID, "Invalid separator char in date string: ", source, reader, offset);
    }

    @Override
    public <S> long parseAsEpochDay(final S source, final AsciiReader<? super S> reader, final int offset) {
        final int year = parseYear(source, reader, offset);
        final int month = parseMonth(source, reader, offset);
        final int day = parseDay(source, reader, offset);
        if (hasValidSeparators(source, reader, offset)) {
            return Epoch.valueOf(ValidationMethod.UNVALIDATED).toEpochDay(year, month, day);
        }
        return invalidEpoch(INVALID_EPOCH, "Invalid separator char in date string: ", source, reader, offset);
    }

    @Allocation(RESULT)
    @Override
    public <S> LocalDate parseAsLocalDate(final S source, final AsciiReader<? super S> reader, final int offset) {
        final int year = parseYear(source, reader, offset);
        final int month = parseMonth(source, reader, offset);
        final int day = parseDay(source, reader, offset);
        DateValidator.THROW_EXCEPTION.validateDay(year, month, day);
        if (hasValidSeparators(source, reader, offset)) {
            return LocalDate.of(year, month, day);
        }
        throw new DateTimeException(toString("Invalid separator char in date string: ", source, reader, offset, format().length()));
    }

    @Override
    public <S> char parseSeparator(final S source, final AsciiReader<? super S> reader, final int offset, final int separatorIndex) {
        final int separatorOffset = separatorIndex == 0 ? format().offsetSeparatorOne() :
                separatorIndex == 1 ? format().offsetSeparatorTwo() : -1;
        if (separatorOffset >= 0) {
            final byte sep = reader.readChar(source, offset + separatorOffset);
            if (separator == sep | separator == (byte)NO_SEPARATOR) {
                return (char)sep;
            }
            return invalidSeparator((char)sep, "Invalid separator char in date string: ", source, reader, offset);
        }
        return NO_SEPARATOR;
    }

    @Override
    public <S> boolean isValid(final S source, final AsciiReader<? super S> reader, final int offset) {
        return isValid(format(), separator, source, reader, offset);
    }
    static <S> boolean isValid(final DateFormat format, final byte separatorChar,
                               final S source, final AsciiReader<? super S> reader, final int offset) {
        final int day = toDay(ValidationMethod.INVALIDATE_RESULT, format, source, reader, offset);
        return day != INVALID && hasValidSeparators(format, separatorChar, source, reader, offset);
    }

    private <S> boolean hasValidSeparators(final S source, final AsciiReader<? super S> reader, final int offset) {
        return hasValidSeparators(format(), separator, source, reader, offset);
    }
    private static <S> boolean hasValidSeparators(final DateFormat format, final byte separatorChar,
                                                  final S source, final AsciiReader<? super S> reader, final int offset) {
        if (separatorChar == (byte)NO_SEPARATOR) {
            return true;
        }
        final int offsetOne = format.offsetSeparatorOne();
        if (offsetOne >= 0) {
            if (separatorChar != reader.readChar(source, offset + offsetOne)) {
                return false;
            }
        }
        final int offsetTwo = format.offsetSeparatorTwo();
        if (offsetTwo >= 0) {
            return separatorChar == reader.readChar(source, offset + offsetTwo);
        }
        return true;
    }

    @SuppressWarnings("SameParameterValue")
    private <S> int invalid(final int value, final String message,
                            final S source, final AsciiReader<? super S> reader, final int offset) {
        return invalid(validationMethod(), format(), value, message, source, reader, offset);
    }

    private static <S> int invalid(final ValidationMethod validationMethod, final DateFormat dateFormat,
                                   final int value, final String message,
                                   final S source, final AsciiReader<? super S> reader, final int offset) {
        return (int)invalidValue(validationMethod, dateFormat, value, INVALID, message, source, reader, offset);
    }

    @SuppressWarnings("SameParameterValue")
    private <S> char invalidSeparator(final char value, final String message,
                                      final S source, final AsciiReader<? super S> reader, final int offset) {
        return (char)invalidValue(validationMethod(), format(), value, INVALID_SEPARATOR, message, source, reader, offset);
    }

    @SuppressWarnings("SameParameterValue")
    private <S> long invalidEpoch(final long value, final String message,
                                  final S source, final AsciiReader<? super S> reader, final int offset) {
        return invalidValue(validationMethod(), format(), value, INVALID_EPOCH, message, source, reader, offset);
    }
    private static <S> long invalidValue(final ValidationMethod validationMethod, final DateFormat dateFormat,
                                         final long value, final long invalidValue, final String message,
                                         final S source, final AsciiReader<? super S> reader, final int offset) {
        return switch (validationMethod) {
            case UNVALIDATED -> value;
            case INVALIDATE_RESULT -> invalidValue;
            case THROW_EXCEPTION ->
                    throw new DateTimeException(toString(message, source, reader, offset, dateFormat.length()));
            //noinspection UnnecessaryDefault
            default -> throw new IllegalStateException("Unsupported validation method: " + validationMethod);
        };
    }

    @Allocation(RESULT)
    private static <S> String toString(final String prefix,
                                       final S source, final AsciiReader<? super S> reader, final int offset,
                                       final int length) {
        final StringBuilder sb = new StringBuilder(prefix.length() + length);
        sb.append(prefix);
        for (int i = 0; i < length; i++) {
            sb.append((char)reader.readChar(source, offset + i));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "ValidatingDateParser[format=" + format() + ", separator=" + SimpleDateParser.toSeparatorString(separator())
                + ", validationMethod=" + validationMethod() + "]";
    }
}