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

import java.io.FileDescriptor;
import java.security.AccessControlException;
import java.security.Permission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TestSecurityManager extends SecurityManager {

    private static final Set<String> ACCEPTABLE_CLASSES =
            new HashSet<>(Arrays.asList(
                    // utilizing lambdas causes a hit, so allow them
                    "java.lang.invoke.InnerClassLambdaMetafactory",
                    // utilizing enum map causes a hit on the enumeration key universe, so allow it
                    "java.util.EnumMap",
                    // allow full access lookup to function
                    "net.kemuri9.invoke.GetFullAccessDirect"
                    ));

    @Override
    public void checkPermission(Permission perm) {
        if ("accessClassInPackage.sun.invoke.util".equals(perm.getName())) {
            return;
        }
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        for (StackTraceElement elem : trace) {
            if (ACCEPTABLE_CLASSES.contains(elem.getClassName())) {
                return;
            }
            if (elem.getClassName().equals(this.getClass().getName())) {
                // this method itself does not allow access
                continue;
            }
            if (elem.getClassName().startsWith("net.kemuri9.invoke.test")) {
                // allow tests
                return;
            }
            if (elem.getClassName().startsWith("net.kemuri9.invoke.")) {
//                System.err.println("rejecting " + perm);
//                new Throwable().printStackTrace(System.err);
                throw new AccessControlException("access denied " + perm, perm);
            }
        }
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
        checkPermission(perm);
    }

    @Override
    public void checkPropertyAccess(String key) {}

    @Override
    public void checkPropertiesAccess() {}

    @Override
    public void checkRead(FileDescriptor fd) {}

    @Override
    public void checkRead(String file) {}

    @Override
    public void checkRead(String file, Object context) {}
}
