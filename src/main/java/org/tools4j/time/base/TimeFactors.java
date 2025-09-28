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
package org.tools4j.time.base;

public final class TimeFactors {
    public static final int HOURS_PER_DAY = 24;
    public static final int MINUTES_PER_HOUR = 60;
    public static final int SECONDS_PER_MINUTE = 60;
    public static final int MILLIS_PER_SECOND = 1000;
    public static final int MICROS_PER_MILLI = 1000;
    public static final int NANOS_PER_MICRO = 1000;

    public static final int NANOS_PER_MILLI = NANOS_PER_MICRO * MICROS_PER_MILLI;

    public static final int MICROS_PER_SECOND = MICROS_PER_MILLI * MILLIS_PER_SECOND;
    public static final int NANOS_PER_SECOND = NANOS_PER_MICRO * MICROS_PER_SECOND;

    public static final int MILLIS_PER_MINUTE = MILLIS_PER_SECOND * SECONDS_PER_MINUTE;
    public static final long NANOS_PER_MINUTE = NANOS_PER_SECOND * (long)SECONDS_PER_MINUTE;

    public static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
    public static final int MILLIS_PER_HOUR = MILLIS_PER_SECOND * SECONDS_PER_HOUR;
    public static final long NANOS_PER_HOUR = NANOS_PER_SECOND * (long)SECONDS_PER_HOUR;

    public static final int MINUTES_PER_DAY = MINUTES_PER_HOUR * HOURS_PER_DAY;
    public static final int SECONDS_PER_DAY = SECONDS_PER_MINUTE * MINUTES_PER_DAY;
    public static final int MILLIS_PER_DAY = MILLIS_PER_SECOND * SECONDS_PER_DAY;
    public static final long NANOS_PER_DAY = NANOS_PER_SECOND * (long)SECONDS_PER_DAY;

    private TimeFactors() {
        throw new RuntimeException("No TimeFactors for you!");
    }
}
