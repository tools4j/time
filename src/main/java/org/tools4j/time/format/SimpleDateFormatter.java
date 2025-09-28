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

import org.tools4j.time.validate.DateValidator;
import org.tools4j.time.validate.ValidationMethod;

import java.util.Objects;

final class SimpleDateFormatter implements DateFormatter.Default {

    private final DateFormat format;
    private final byte separator;
    private final ValidationMethod validationMethod;

    SimpleDateFormatter(final DateFormat format, final char separator, final ValidationMethod validationMethod) {
        this.format = Objects.requireNonNull(format);
        this.separator = Ascii.validateSeparatorChar(separator);
        this.validationMethod = Objects.requireNonNull(validationMethod);
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

    @SuppressWarnings("PointlessArithmeticExpression")
    @Override
    public <T> int format(final int year, final int month, final int day, final T target, final AsciiWriter<? super T> writer, final int offset) {
        if (DateValidator.INVALID == validationMethod().dateValidator().validateDay(year, month, day)) {
            return INVALID;
        }
        final DateFormat format = format();
        final int offsetY = offset + format.offsetYear();
        final int offsetM = offset + format.offsetMonth();
        final int offsetD = offset + format.offsetDay();
        writer.writeChar(target, offsetY + 0, Ascii.digit((year / 1000) % 10));
        writer.writeChar(target, offsetY + 1, Ascii.digit((year / 100) % 10));
        writer.writeChar(target, offsetY + 2, Ascii.digit((year / 10) % 10));
        writer.writeChar(target, offsetY + 3, Ascii.digit(year % 10));
        writer.writeChar(target, offsetM + 0, Ascii.digit((month / 10) % 10));
        writer.writeChar(target, offsetM + 1, Ascii.digit(month % 10));
        writer.writeChar(target, offsetD + 0, Ascii.digit((day / 10) % 10));
        writer.writeChar(target, offsetD + 1, Ascii.digit(day % 10));
        final int offSep1 = format.offsetSeparatorOne();
        final int offSep2 = format.offsetSeparatorTwo();
        if (offSep1 >= 0) {
            writer.writeChar(target, offset + offSep1, separator);
        }
        if (offSep2 >= 0) {
            writer.writeChar(target, offset + offSep2, separator);
        }
        return format.length();
    }

    @Override
    public String toString() {
        return "SimpleDateFormatter[format=" + format() + ", separator=" + toSeparatorString(separator()) + "]";
    }

    static String toSeparatorString(final char separator) {
        return separator == NO_SEPARATOR ? "<none>" : "'" + separator + "'";
    }
}