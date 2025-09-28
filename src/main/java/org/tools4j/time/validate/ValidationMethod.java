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
package org.tools4j.time.validate;

import java.util.Objects;

/**
 * Defines different methods to validate values.
 */
public enum ValidationMethod {
    /**
     * No validation is performed.
     */
    UNVALIDATED(DateValidator.UNVALIDATED, TimeValidator.UNVALIDATED),
    /**
     * Validation is performed and failure is signalled by a special result value.
     */
    INVALIDATE_RESULT(DateValidator.INVALIDATE_RESULT, TimeValidator.INVALIDATE_RESULT),
    /**
     * Validation is performed and failure is signalled through an exception.
     */
    THROW_EXCEPTION(DateValidator.THROW_EXCEPTION, TimeValidator.THROW_EXCEPTION);

    private static final ValidationMethod[] VALUES = values();

    private final DateValidator dateValidator;
    private final TimeValidator timeValidator;

    ValidationMethod(final DateValidator dateValidator, final TimeValidator timeValidator) {
        this.dateValidator = Objects.requireNonNull(dateValidator);
        this.timeValidator = Objects.requireNonNull(timeValidator);
    }

    public final DateValidator dateValidator() {
        return dateValidator;
    }

    public final TimeValidator timeValidator() {
        return timeValidator;
    }

    public static int count() {
        return VALUES.length;
    }

    public static ValidationMethod valueByOrdinal(final int ordinal) {
        return VALUES[ordinal];
    }

}
