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
package net.kemuri9.invoke;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * simple functionality specific to Java 8
 */
final class VersionSupport {

    private static final MethodHandle VERIFY_ACCESS;

    static {
        MethodHandle handle = null;
        try {
            Class<?> verify = Class.forName("sun.invoke.util.VerifyAccess");
            handle = MethodHandles.publicLookup().findStatic(verify, "isClassAccessible",
                    MethodType.methodType(boolean.class, Class.class, Class.class, int.class));
        } catch (Throwable t) {}
        VERIFY_ACCESS = handle;
    }

    static List<GetFullAccess> getLookups() {
        ServiceLoader<GetFullAccess> loader = ServiceLoader.load(GetFullAccess.class);
        List<GetFullAccess> lookups = new ArrayList<>();
        lookups.add(new GetFullAccessDirect());
        loader.forEach(lookups::add);
        lookups.sort(Comparator.comparing(GetFullAccess::getPriority));
        return lookups;
    }

    static Class<?> getType(Lookup lookup, String name) throws ClassNotFoundException {
        return Class.forName(name);
    }

    static boolean isAccessible(Lookup lookup, Class<?> nestedType) {
        /* java 8 does not have accessClass on Lookup, as that was added in java 9.
         * accessClass just wraps VerifyAccess.isClassAccessible, so use that directly
         * as there is no module system to get in the way */
        if (Utils.isFullLookup(lookup)) {
            return true;
        }
        Boolean accessible = InvokeUtils.invokeQuietly(VERIFY_ACCESS, nestedType, lookup.lookupClass(), lookup.lookupModes());
        return Utils.defaultValue(accessible, Boolean.TRUE);
    }

    private VersionSupport() {}
}
