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

import java.lang.reflect.Field;
import java.util.Objects;

record FieldAccess<T>(Field field, Class<T> fieldType) {
    FieldAccess(final Field field, final Class<T> fieldType) {
        this.field = Objects.requireNonNull(field);
        this.fieldType = Objects.requireNonNull(fieldType);
        if (!field.getType().equals(fieldType)) {
            throw new IllegalArgumentException("Invalid fieldType for field " + field.getName() + ", expected " +
                    field.getType().getName() + " but found " + fieldType.getName());
        }
    }

    @SuppressWarnings("SameParameterValue")
    static <T> FieldAccess<T> forField(Class<?> owner, final String fieldName, final Class<T> fieldType) {
        try {
            final Field field = owner.getDeclaredField(fieldName);
            field.setAccessible(true);
            return new FieldAccess<>(field, fieldType);
        } catch (final NoSuchFieldException e) {
            throw new RuntimeException("Could not access field " + owner.getName() + "." + fieldName + ", e=" + e, e);
        }
    }

    T get(final Object instance) {
        try {
            return fieldType.cast(field.get(instance));
        } catch (final Exception e) {
            throw new RuntimeException("Could not fetch value for field " + field + ", e=" + e, e);
        }
    }
}
