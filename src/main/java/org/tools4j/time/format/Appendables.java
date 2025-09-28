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

import java.io.IOException;

final class Appendables {
    private static final int CAPACITY = DateFormat.YYYY_MM_DD.length() + 1 + TimeFormat.HH_MM_SS_NNNNNNNNN.length();
    private static final ThreadLocal<StringBuilder> STRING_BUILDER_THREAD_LOCAL = ThreadLocal.withInitial(
            () -> new StringBuilder(CAPACITY)
    );

    static StringBuilder acquireStringBuilder(final Appendable appendable) {
        if (appendable instanceof StringBuilder) {
            return (StringBuilder)appendable;
        }
        final StringBuilder temp = STRING_BUILDER_THREAD_LOCAL.get();
        temp.setLength(0);
        return temp;
    }

    static void appendAndReleaseStringBuilder(final Appendable appendable, final StringBuilder temp) {
        if (appendable != temp) {
            try {
                appendable.append(temp);
            } catch (final IOException e) {
                throw new RuntimeException("Error while trying to append to Appendable", e);
            } finally {
                temp.setLength(0);
            }
        }
    }

    private Appendables() {
        throw new RuntimeException("No Appendables for you!");
    }
}
