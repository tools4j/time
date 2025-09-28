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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

@FunctionalInterface
public interface AsciiWriter<T> {
    void writeChar(T target, int index, byte value);

    AsciiWriter<StringBuilder> STRING_BUILDER = (s, i, v) -> {
        s.setLength(Math.max(i + 1, s.length()));
        s.setCharAt(i, (char)v);
    };

    AsciiWriter<StringBuffer> STRING_BUFFER = (s, i, v) -> {
        s.setLength(Math.max(i + 1, s.length()));
        s.setCharAt(i, (char)v);
    };
    AsciiWriter<char[]> CHAR_ARRAY = (a, i, v) -> a[i] = (char)v;
    AsciiWriter<byte[]> BYTE_ARRAY = (a, i, v) -> a[i] = v;
    AsciiWriter<CharBuffer> CHAR_BUFFER = (b, i, v) -> b.put(i, (char)v);
    AsciiWriter<ByteBuffer> BYTE_BUFFER = ByteBuffer::put;
}
