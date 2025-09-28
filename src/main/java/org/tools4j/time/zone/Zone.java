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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface Zone {

    int INVALID = Integer.MIN_VALUE;

    ZoneId zoneId();

    int standardOffsetSeconds(long secondsSinceEpoch);

    int offsetSeconds(long secondsSinceEpoch);

    int offsetSeconds(int zoneYear, int zoneMonth, int zoneDay,
                      int zoneHour, int zoneMinute, int zoneSecond, int zoneNano);

    static Zone systemDefault() {
        return Instances.SYSTEM_DEFAULT;
    }

    static Zone utc() {
        return Instances.UTC;
    }

    static Zone newYork() {
        return Instances.NEW_YORK;
    }

    static Zone forZoneId(final String zoneId) {
        return Instances.forZoneId(zoneId);
    }

    final class Instances {
        private static final Zone UTC = new OffsetZone(ZoneOffset.UTC);
        private static final Zone SYSTEM_DEFAULT = create(ZoneId.systemDefault());
        private static final Zone NEW_YORK = Instances.create("America/New_York");

        private static final Map<String, Zone> CACHE = new ConcurrentHashMap<>();

        private static Zone forZoneId(final String zoneId) {
            return CACHE.computeIfAbsent(zoneId, Instances::create);
        }

        private static Zone create(final String zoneId) {
            return create(ZoneId.of(zoneId));
        }

        private static Zone create(final ZoneId zoneId) {
            if (zoneId instanceof ZoneOffset) {
                return new OffsetZone((ZoneOffset)zoneId);
            }
            return new RulesZone(zoneId);
        }

        static {
            CACHE.put(UTC.zoneId().getId(), UTC);
            CACHE.put(SYSTEM_DEFAULT.zoneId().getId(), SYSTEM_DEFAULT);
            CACHE.put(NEW_YORK.zoneId().getId(), NEW_YORK);
        }
    }
}
