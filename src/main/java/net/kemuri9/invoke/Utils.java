/**
 * Copyright 2021 Steven Walters
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
package net.kemuri9.invoke;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Consumer;

class Utils {

    static final String TYPE_MEMBER_NAME = "java.lang.invoke.MemberName";
    static final String TYPE_MEMBER_NAME_FACTORY = "java.lang.invoke.MemberName$Factory";

    static RuntimeException asException(Throwable t, Class<? extends RuntimeException> asType, String message) {
       if (t instanceof RuntimeException) {
           return cast(t);
       }
       try {
           return asType.getDeclaredConstructor(String.class, Throwable.class).newInstance(message, t);
       } catch (Exception ex) {
           return new RuntimeException(message, t);
       }
    }

    @SuppressWarnings("unchecked")
    static <T> T cast(Object o) {
        return (T) o;
    }

    static <T> T defaultValue(T value, T def) {
        return (value == null) ? def : value;
    }

    static Class<?> getLookupType(MethodHandles.Lookup lookup) {
        // if the full access lookup, then the access is not restricted.
        return isFullLookup(lookup) ? null : lookup.lookupClass();
    }

    static int getLookupModes(MethodHandles.Lookup lookup) {
        // if the full access lookup, then the access is not restricted
        return isFullLookup(lookup) ? -1 : lookup.lookupModes();
    }

    static boolean isFullLookup(MethodHandles.Lookup lookup) {
        /* to avoid requiring the acquisition of the full access lookup,
         * check toString for the special "/trusted" name instead */
        return "/trusted".equals(lookup.toString());
    }

    static <T> T notNull(T value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " is null");
        }
        return value;
    }

    static void processClassHierarchy(Class<?> type, boolean includeParents, Consumer<Class<?>> process) {
        if (!includeParents) {
            process.accept(type);
            return;
        }

        // including inherited types requires traversing both the interface and super inheritance trees
        Set<Class<?>> processed = new HashSet<>();
        LinkedList<Class<?>> toProcess = new LinkedList<>();
        toProcess.add(type);
        while (!toProcess.isEmpty()) {
            Class<?> checkType = toProcess.poll();
            if (!processed.add(checkType)) {
                continue;
            }
            Collections.addAll(toProcess, checkType.getInterfaces());
            if (checkType.getSuperclass() != null) {
                toProcess.add(checkType.getSuperclass());
            }
            process.accept(checkType);
        }
    }

    private Utils() {}
}
