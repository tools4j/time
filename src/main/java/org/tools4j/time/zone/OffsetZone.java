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
package org.tools4j.time.zone;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Objects;

final class OffsetZone implements Zone {

    private final ZoneOffset zoneOffset;

    OffsetZone(final ZoneOffset zoneOffset) {
        this.zoneOffset = Objects.requireNonNull(zoneOffset);
    }

    @Override
    public ZoneId zoneId() {
        return zoneOffset;
    }

    @Override
    public int standardOffsetSeconds(final long secondsSinceEpoch) {
        return zoneOffset.getTotalSeconds();
    }

    @Override
    public int offsetSeconds(final long secondsSinceEpoch) {
        return zoneOffset.getTotalSeconds();
    }

    @Override
    public int offsetSeconds(final int zoneYear, final int zoneMonth, final int zoneDay,
                             final int zoneHour, final int zoneMinute, final int zoneSecond, final int zoneNano) {
        return zoneOffset.getTotalSeconds();
    }

    @Override
    public int hashCode() {
        return zoneOffset.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final OffsetZone that = (OffsetZone) o;
        return zoneOffset.equals(that.zoneOffset);
    }

    @Override
    public String toString() {
        return zoneOffset.toString();
    }
}
