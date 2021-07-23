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
package net.kemuri9.invoke.test;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

public class TestUtils {

    public static boolean isJava8() {
        String javaVersion = System.getProperty("java.version");
        return (javaVersion.matches("1\\.8.*"));
    }

    public static boolean isAtLeastJava(int version) {
        String javaVersion = System.getProperty("java.version");
        if (version == 8 && isJava8()) {
            return true;
        }
        int javaVersionInt = Integer.parseInt(javaVersion.split("\\.", 2)[0]);
        return javaVersionInt >= version;
    }

    public static <T> T getValue(T value) {
        return value;
    }

    public static void throwIOException() throws IOException {
        throw new IOException("failed");
    }

    public static void throwUnsupportedOperationException() {
        throw new UnsupportedOperationException("not supported");
    }

    public static final MethodHandle HANDLE_THROW_IO_EX;
    public static final MethodHandle HANDLE_THROW_UNSUPPORTED_EX;
    public static final Method METHOD_THROW_IO_EX;
    public static final Method METHOD_THROW_UNSUPPORTED_EX;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            HANDLE_THROW_IO_EX = lookup.findStatic(TestUtils.class, "throwIOException", MethodType.methodType(void.class));
            HANDLE_THROW_UNSUPPORTED_EX = lookup
                    .findStatic(TestUtils.class, "throwUnsupportedOperationException", MethodType.methodType(void.class));
            METHOD_THROW_IO_EX = lookup.revealDirect(HANDLE_THROW_IO_EX).reflectAs(Method.class, lookup);
            METHOD_THROW_UNSUPPORTED_EX = lookup.revealDirect(HANDLE_THROW_UNSUPPORTED_EX).reflectAs(Method.class, lookup);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
