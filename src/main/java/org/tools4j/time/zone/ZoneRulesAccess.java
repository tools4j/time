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

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.zone.ZoneOffsetTransitionRule;
import java.time.zone.ZoneRules;

final class ZoneRulesAccess {
    static final FieldAccess<long[]> STANDARD_TRANSITIONS = FieldAccess.forField(ZoneRules.class, "standardTransitions", long[].class);
    static final FieldAccess<ZoneOffset[]> STANDARD_OFFSETS = FieldAccess.forField(ZoneRules.class, "standardOffsets", ZoneOffset[].class);
    static final FieldAccess<long[]> SAVINGS_INSTANT_TRANSITIONS = FieldAccess.forField(ZoneRules.class, "savingsInstantTransitions", long[].class);
    static final FieldAccess<LocalDateTime[]> SAVINGS_LOCAL_TRANSITIONS = FieldAccess.forField(ZoneRules.class, "savingsLocalTransitions", LocalDateTime[].class);
    static final FieldAccess<ZoneOffset[]> WALL_OFFSETS = FieldAccess.forField(ZoneRules.class, "wallOffsets", ZoneOffset[].class);
    static final FieldAccess<ZoneOffsetTransitionRule[]> LAST_RULES = FieldAccess.forField(ZoneRules.class, "lastRules", ZoneOffsetTransitionRule[].class);

    private ZoneRulesAccess() {
        throw new RuntimeException("No ZoneRulesAccess for you!");
    }
}
