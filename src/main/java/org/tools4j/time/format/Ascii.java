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

final class Ascii {

    static final char NO_SEPARATOR = (char)Byte.MIN_VALUE;

    static byte validateSeparatorChar(final char separator) {
        if (separator <= Byte.MAX_VALUE | separator == NO_SEPARATOR) {
            return (byte)separator;
        }
        throw new IllegalArgumentException("Illegal separator char: " + separator);
    }

    static int digit(final byte ch) {
        return ch - '0';
    }

    static byte digit(final int digit) {
        return (byte)(0x7f & ('0' + digit));
    }

    static boolean isDigit(final byte ch) {
        return '0' <= ch & ch <= '9';
    }

    private Ascii() {
        throw new RuntimeException("No Ascii for you!");
    }
}
