/**
 * Copyright 2021,2022 Steven Walters
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kemuri9.invoke.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class InvokePrivate {

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object obj) {
        return (T) obj;
    }

    public static <T> T getFieldValue(Class<?> type, String fieldName) throws NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        return getFieldValue(type, fieldName, null);
    }

    public static <T> T getFieldValue(Class<?> type, String fieldName, Object instance)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        if (type == null) {
            type = instance.getClass();
        }
        Field field = type.getDeclaredField(fieldName);
        return getFieldValue(field, instance);
    }

    public static <T> T getFieldValue(Field field) throws IllegalArgumentException, IllegalAccessException {
        return getFieldValue(field, null);
    }

    public static <T> T getFieldValue(Field field, Object instance)
            throws IllegalArgumentException, IllegalAccessException {
        field.setAccessible(true);
        try {
            return cast(field.get(instance));
        } finally {
            field.setAccessible(false);
        }
    }

    public static void setFieldValue(Class<?> type, String fieldName, Object instance, Object value)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = type.getDeclaredField(fieldName);
        setFieldValue(field, instance, value);
    }

    public static void setFieldValue(Field field, Object instance, Object value)
            throws IllegalArgumentException, IllegalAccessException {
        field.setAccessible(true);
        try {
            field.set(instance, value);
        } finally {
            field.setAccessible(false);
        }
    }

    public static <T> T instantiate(Constructor<?> ctor, Object... args)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        ctor.setAccessible(true);
        try {
            return cast(ctor.newInstance(args));
        } finally {
            ctor.setAccessible(false);
        }
    }
}
