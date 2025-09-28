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

import org.tools4j.time.base.Epoch;
import org.tools4j.time.pack.DatePacker;
import org.tools4j.time.validate.DateValidator;
import org.tools4j.time.validate.TimeValidator;
import org.tools4j.time.validate.ValidationMethod;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneOffsetTransitionRule;
import java.time.zone.ZoneRules;
import java.util.Arrays;

final class RulesZone implements Zone {

    private final ZoneId zoneId;
    private final ZoneRules zoneRules;
    private final long[] standardTransitions;
    private final ZoneOffset[] standardOffsets;
    private final long[] savingsInstantTransitions;
    private final LocalDateTime[] savingsLocalTransitions;
    private final ZoneOffset[] wallOffsets;
    private final ZoneOffsetTransitionRule[] lastRules;
    private final ZoneOffsetTransition[][] transitionsByYear;

    RulesZone(final ZoneId zoneId) {
        this.zoneId = zoneId;
        this.zoneRules = zoneId.getRules();
        this.standardTransitions = ZoneRulesAccess.STANDARD_TRANSITIONS.get(zoneRules);
        this.standardOffsets = ZoneRulesAccess.STANDARD_OFFSETS.get(zoneRules);
        this.savingsInstantTransitions = ZoneRulesAccess.SAVINGS_INSTANT_TRANSITIONS.get(zoneRules);
        this.savingsLocalTransitions = ZoneRulesAccess.SAVINGS_LOCAL_TRANSITIONS.get(zoneRules);
        this.wallOffsets = ZoneRulesAccess.WALL_OFFSETS.get(zoneRules);
        this.lastRules = ZoneRulesAccess.LAST_RULES.get(zoneRules);
        this.transitionsByYear = new ZoneOffsetTransition[DateValidator.YEAR_MAX + 1][];
    }

    @Override
    public ZoneId zoneId() {
        return zoneId;
    }

    @Override
    public int standardOffsetSeconds(final long secondsSinceEpoch) {
        if (zoneRules.isFixedOffset()) {
            return zoneRules.getStandardOffset(null).getTotalSeconds();
        }
        return offsetSeconds(secondsSinceEpoch, standardTransitions, standardOffsets);
    }

    @Override
    public int offsetSeconds(final long secondsSinceEpoch) {
        if (zoneRules.isFixedOffset()) {
            return zoneRules.getStandardOffset(null).getTotalSeconds();
        }
        // check if using last rules
        if (lastRules.length > 0 &&
                secondsSinceEpoch > savingsInstantTransitions[savingsInstantTransitions.length - 1]) {
            final int year = findYear(secondsSinceEpoch, wallOffsets[wallOffsets.length - 1]);
            if (year == DatePacker.INVALID) {
                return INVALID;
            }
            final ZoneOffsetTransition[] transitions = transitionsForYear(year);
            ZoneOffsetTransition trans = null;
            for (final ZoneOffsetTransition transition : transitions) {
                trans = transition;
                if (secondsSinceEpoch < trans.toEpochSecond()) {
                    return trans.getOffsetBefore().getTotalSeconds();
                }
            }
            return trans.getOffsetAfter().getTotalSeconds();
        }
        return offsetSeconds(secondsSinceEpoch, savingsInstantTransitions, wallOffsets);
    }

    @Override
    public int offsetSeconds(final int zoneYear, final int zoneMonth, final int zoneDay,
                             final int zoneHour, final int zoneMinute, final int zoneSecond, final int zoneNano) {
        if (zoneRules.isFixedOffset()) {
            return zoneRules.getStandardOffset(null).getTotalSeconds();
        }
        if (!DateValidator.isValidDate(zoneYear, zoneMonth, zoneDay) ||
                !TimeValidator.isValidTimeWithNanos(zoneHour, zoneMinute, zoneSecond, zoneNano)) {
            return INVALID;
        }
        return getOffsetAtOrBefore(zoneYear, zoneMonth, zoneDay, zoneHour, zoneMinute, zoneSecond, zoneNano)
                .getTotalSeconds();
    }

    /**
     * Finds the appropriate transition array for the given year.
     *
     * @param year the year
     * @return the transition array
     */
    private ZoneOffsetTransition[] transitionsForYear(final int year) {
        ZoneOffsetTransition[] transArray = transitionsByYear[year];
        if (transArray != null) {
            return transArray;
        }
        final ZoneOffsetTransitionRule[] ruleArray = lastRules;
        transArray = new ZoneOffsetTransition[ruleArray.length];
        for (int i = 0; i < ruleArray.length; i++) {
            transArray[i] = ruleArray[i].createTransition(year);
        }
        transitionsByYear[year] = transArray;
        return transArray;
    }

    private static int findYear(final long secondsSinceEpoch, final ZoneOffset offset) {
        final long localSecond = secondsSinceEpoch + offset.getTotalSeconds();
        final int packedDate = Epoch.valueOf(ValidationMethod.INVALIDATE_RESULT).fromEpochSecond(localSecond, DatePacker.BINARY);
        return packedDate == DatePacker.INVALID ? DatePacker.INVALID : DatePacker.BINARY.unpackYear(packedDate);
    }

    /**
     * @see ZoneRules#getStandardOffset(Instant) and last bit of {@link ZoneRules#getOffset(Instant)}.
     */
    private static int offsetSeconds(final long secondsSinceEpoch,
                                     final long[] secondsSinceEpochArray,
                                     final ZoneOffset[] offsetArray) {
        int index = Arrays.binarySearch(secondsSinceEpochArray, secondsSinceEpoch);
        if (index < 0) {
            // switch negative insert position to start of matched range
            index = -index - 2;
        }
        return offsetArray[index + 1].getTotalSeconds();
    }

    /**
     * @see ZoneRules#getOffsetInfo(LocalDateTime)
     */
    @SuppressWarnings("JavadocReference")
    private ZoneOffset getOffsetAtOrBefore(final int zoneYear, final int zoneMonth, final int zoneDay,
                                           final int zoneHour, final int zoneMinute, final int zoneSecond, final int zoneNano) {
        if (savingsInstantTransitions.length == 0) {
            return standardOffsets[0];
        }
        // check if using last rules
        if (lastRules.length > 0 &&
//                dt.isAfter(savingsLocalTransitions[savingsLocalTransitions.length - 1])) {
                compare(savingsLocalTransitions[savingsLocalTransitions.length - 1],
                        zoneYear, zoneMonth, zoneDay, zoneHour, zoneMinute, zoneSecond, zoneNano) < 0) {
            final ZoneOffsetTransition[] transArray = transitionsForYear(zoneYear);
            Object info = null;
            for (ZoneOffsetTransition trans : transArray) {
                info = findOffsetInfo(zoneYear, zoneMonth, zoneDay, zoneHour, zoneMinute, zoneSecond, zoneNano, trans);
                if (info instanceof ZoneOffsetTransition) {
                    return ((ZoneOffsetTransition) info).getOffsetBefore();
                }
                if (info.equals(trans.getOffsetBefore())) {
                    return trans.getOffsetBefore();
                }
            }
            return info instanceof ZoneOffset ? ((ZoneOffset)info) : ((ZoneOffsetTransition)info).getOffsetBefore();
        }

        // using historic rules
        int index  = binarySearch(savingsLocalTransitions, zoneYear, zoneMonth, zoneDay, zoneHour, zoneMinute, zoneSecond, zoneNano);
        if (index == -1) {
            // before first transition
            return wallOffsets[0];
        }
        if (index < 0) {
            // switch negative insert position to start of matched range
            index = -index - 2;
        } else if (index < savingsLocalTransitions.length - 1 &&
                savingsLocalTransitions[index].equals(savingsLocalTransitions[index + 1])) {
            // handle overlap immediately following gap
            index++;
        }
        if ((index & 1) == 0) {
            // gap or overlap
            return wallOffsets[index / 2];
//            final LocalDateTime dtBefore = savingsLocalTransitions[index];
//            final LocalDateTime dtAfter = savingsLocalTransitions[index + 1];
//            final ZoneOffset offsetBefore = wallOffsets[index / 2];
//            final ZoneOffset offsetAfter = wallOffsets[index / 2 + 1];
//            if (offsetAfter.getTotalSeconds() > offsetBefore.getTotalSeconds()) {
//                // gap
//                return new ZoneOffsetTransition(dtBefore, offsetBefore, offsetAfter);//NOTE garbage
//            } else {
//                // overlap
//                return new ZoneOffsetTransition(dtAfter, offsetBefore, offsetAfter);//NOTE garbage
//            }
        } else {
            // normal (neither gap nor overlap)
            return wallOffsets[index / 2 + 1];
        }
    }

    /**
     * @see ZoneRules#findOffsetInfo(LocalDateTime, ZoneOffsetTransition)
     */
    @SuppressWarnings("JavadocReference")
    private Object findOffsetInfo(final int zoneYear, final int zoneMonth, final int zoneDay,
                                  final int zoneHour, final int zoneMinute, final int zoneSecond, final int zoneNano,
                                  final ZoneOffsetTransition trans) {
        final LocalDateTime localTransition = trans.getDateTimeBefore();
        if (trans.isGap()) {
//            if (dt.isBefore(localTransition)) {
            if (compare(localTransition, zoneYear, zoneMonth, zoneDay, zoneHour, zoneMinute, zoneSecond, zoneNano) > 0) {
                return trans.getOffsetBefore();
            }
//            if (dt.isBefore(trans.getDateTimeAfter())) {//NOTE: garbage
            if (compare(localTransition, trans.getOffsetAfter().getTotalSeconds() - trans.getOffsetBefore().getTotalSeconds(),
                    zoneYear, zoneMonth, zoneDay, zoneHour, zoneMinute, zoneSecond, zoneNano) > 0) {
                return trans;
            } else {
                return trans.getOffsetAfter();
            }
        } else {
//            if (dt.isBefore(localTransition) == false) {
            if (compare(localTransition, zoneYear, zoneMonth, zoneDay, zoneHour, zoneMinute, zoneSecond, zoneNano) <= 0) {
                return trans.getOffsetAfter();
            }
//            if (dt.isBefore(trans.getDateTimeAfter())) {//NOTE: garbage
            if (compare(localTransition, trans.getOffsetAfter().getTotalSeconds() - trans.getOffsetBefore().getTotalSeconds(),
                    zoneYear, zoneMonth, zoneDay, zoneHour, zoneMinute, zoneSecond, zoneNano) > 0) {
                return trans.getOffsetBefore();
            } else {
                return trans;
            }
        }
    }

    private static int compare(final LocalDateTime first, final int offsetSeconds,
                               final int year, final int month, final int day,
                               final int hour, final int minute, final int second, final int nano) {
        final int cmp = Long.compare(
                first.toEpochSecond(ZoneOffset.UTC),
                Epoch.valueOf(ValidationMethod.UNVALIDATED).toEpochSecond(year, month, day, hour, minute, second)
        );
        if (cmp != 0) return cmp;
        return first.getNano() - nano;
    }

    private static int compare(final LocalDateTime first,
                               final int year, final int month, final int day,
                               final int hour, final int minute, final int second, final int nano) {
        int cmp = first.getYear() - year;
        if (cmp != 0) return cmp;
        cmp = first.getMonthValue() - month;
        if (cmp != 0) return cmp;
        cmp = first.getDayOfMonth() - day;
        if (cmp != 0) return cmp;
        cmp = first.getHour() - hour;
        if (cmp != 0) return cmp;
        cmp = first.getMinute() - minute;
        if (cmp != 0) return cmp;
        cmp = first.getSecond() - second;
        if (cmp != 0) return cmp;
        return first.getNano() - nano;
    }

    private static int binarySearch(final LocalDateTime[] a,
                                    final int year, final int month, final int day,
                                    final int hour, final int minute, final int second, final int nano) {
        int low = 0;
        int high = a.length - 1;

        while (low <= high) {
            final int mid = (low + high) >>> 1;
            final LocalDateTime midVal = a[mid];
            final int cmp = compare(midVal, year, month, day, hour, minute, second, nano);
            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }

    @Override
    public int hashCode() {
        return zoneId.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final RulesZone rulesZone = (RulesZone) o;
        return zoneId.equals(rulesZone.zoneId);
    }

    @Override
    public String toString() {
        return zoneId.toString();
    }
}
