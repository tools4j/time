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

import java.time.LocalDate;
import java.util.Objects;

import static org.tools4j.time.base.Allocation.Type.RESULT;

final class SimpleDateParser implements DateParser.Default {

    private final DateFormat format;
    private final byte separator;

    SimpleDateParser(final DateFormat format, final char separator) {
        this.format = Objects.requireNonNull(format);
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
        return ValidationMethod.UNVALIDATED;
    }

    @Override
    public <S> int parseYear(final S source, final AsciiReader<? super S> reader, final int offset) {
        final int off = offset + format().offsetYear();
        final byte ch1 = reader.readChar(source, off);
        final byte ch2 = reader.readChar(source, off + 1);
        final byte ch3 = reader.readChar(source, off + 2);
        final byte ch4 = reader.readChar(source, off + 3);
        return Ascii.digit(ch1) * 1000 + Ascii.digit(ch2) * 100 + Ascii.digit(ch3) * 10 + Ascii.digit(ch4);
    }

    @Override
    public <S> int parseMonth(final S source, final AsciiReader<? super S> reader, final int offset) {
        final int off = offset + format().offsetMonth();
        final byte ch1 = reader.readChar(source, off);
        final byte ch2 = reader.readChar(source, off + 1);
        return Ascii.digit(ch1) * 10 + Ascii.digit(ch2);
    }

    @Override
    public <S> int parseDay(final S source, final AsciiReader<? super S> reader, final int offset) {
        final int off = offset + format().offsetDay();
        final byte ch1 = reader.readChar(source, off);
        final byte ch2 = reader.readChar(source, off + 1);
        return Ascii.digit(ch1) * 10 + Ascii.digit(ch2);
    }

    @Override
    public <S> int parseAsPackedDate(final S source, final AsciiReader<? super S> reader, final int offset, final Packing packing) {
        final int year = parseYear(source, reader, offset);
        final int month = parseMonth(source, reader, offset);
        final int day = parseDay(source, reader, offset);
        return DatePacker.valueOf(packing).pack(year, month, day);
    }

    @Override
    public <S> long parseAsEpochDay(final S source, final AsciiReader<? super S> reader, final int offset) {
        final int year = parseYear(source, reader, offset);
        final int month = parseMonth(source, reader, offset);
        final int day = parseDay(source, reader, offset);
        return Epoch.valueOf(ValidationMethod.UNVALIDATED).toEpochDay(year, month, day);
    }

    @Allocation(RESULT)
    @Override
    public <S> LocalDate parseAsLocalDate(final S source, final AsciiReader<? super S> reader, final int offset) {
        final int year = parseYear(source, reader, offset);
        final int month = parseMonth(source, reader, offset);
        final int day = parseDay(source, reader, offset);
        DateValidator.THROW_EXCEPTION.validateDay(year, month, day);
        return LocalDate.of(year, month, day);
    }

    @Override
    public <S> char parseSeparator(final S source, final AsciiReader<? super S> reader, final int offset, final int separatorIndex) {
        final int separatorOffset = separatorIndex == 0 ? format().offsetSeparatorOne() :
                separatorIndex == 1 ? format().offsetSeparatorTwo() : -1;
        if (separatorOffset >= 0) {
            return (char) reader.readChar(source, offset + separatorOffset);
        }
        return NO_SEPARATOR;
    }

    @Override
    public <S> boolean isValid(final S source, final AsciiReader<? super S> reader, final int offset) {
        return ValidatingDateParser.isValid(format(), separator, source, reader, offset);
    }

    @Override
    public String toString() {
        return "SimpleDateParser[format=" + format() + ", separator=" + toSeparatorString(separator()) + "]";
    }

    static String toSeparatorString(final char separator) {
        return separator == NO_SEPARATOR ? "<none>" : "'" + separator + "'";
    }
}