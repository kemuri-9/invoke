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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Member;

/**
 * Most of the functionality for this module is around bypassing the SecurityManager which may get in the way
 * of performing resolutions and lookups.
 * However, some java versions have incomplete resolutions and require performing direct reflection
 * lookups to complete the resolution.
 * In these scenarios, the SecurityManager can get in the way, so turn it off temporarily.
 */
final class SecurityManagerDisabler {

    static interface Execute<T> {
        T execute() throws Throwable;
    }

    private static SecurityManagerDisabler INSTANCE = null;
    private static final Object[] NULL_SM = new Object[] { null };

    static SecurityManagerDisabler getInstance() {
        if (INSTANCE == null) {
            synchronized(SecurityManagerDisabler.class) {
                if (INSTANCE == null) {
                    try {
                        INSTANCE = new SecurityManagerDisabler();
                    } catch (Throwable t) {
                        throw new UnsupportedOperationException("Unable to setup SecurityManagerDisabler", t);
                    }
                }
            }
        }
        return INSTANCE;
    }

    private final MethodHandle setSecurity;
    private final MethodHandle getSecurity;

    private SecurityManagerDisabler() throws Throwable {
        MethodHandles.Lookup lookup = InvokeUtils.getFullAccessLookup().in(System.class);
        MemberNameAccess mName = MemberNameAccess.getInstance();
        LookupAccess access = LookupAccess.getInstance();
        Member member = mName.newMember(System.class, "security", SecurityManager.class,
                MemberNameAccess.ReferenceKind.REF_putStatic.kind);
        setSecurity = access.resolveField(lookup, member);
        member = mName.newMember(System.class, "security", SecurityManager.class,
                MemberNameAccess.ReferenceKind.REF_getStatic.kind);
        getSecurity = access.resolveField(lookup, member);
    }

    <T> T withSecurityDisabled(Execute<T> execute) {
        SecurityManager sm = InvokeUtils.invoke(getSecurity);
        if (sm != null) {
            InvokeUtils.invoke(setSecurity, NULL_SM);
        }
        try {
            return execute.execute();
        } catch (Throwable t) {
            throw Utils.asException(t, RuntimeException.class, "execution failed");
        } finally {
            if (sm != null) {
                InvokeUtils.invoke(setSecurity, sm);
            }
        }
    }
}
