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
import java.lang.invoke.MethodType;
import java.lang.reflect.Member;

abstract class LookupAccess {

    private static LookupAccessVersion INSTANCE;

    static LookupAccessVersion getInstance() {
        if (INSTANCE == null) {
            synchronized(LookupAccess.class) {
                if (INSTANCE == null) {
                    try {
                        INSTANCE = new LookupAccessVersion();
                    } catch (Throwable t) {
                        throw new UnsupportedOperationException("failed to initialize Lookup access", t);
                    }
                }
            }
        }
        return INSTANCE;
    }

    protected final MethodHandle resolveConstructor;
    protected final MethodHandle resolveField;

    protected LookupAccess() throws Throwable {
        MethodHandles.Lookup lookup = InvokeUtils.getFullAccessLookup().in(MethodHandles.Lookup.class);
            Class<?> memberNameType = VersionSupport.getType(InvokeUtils.getFullAccessLookup(), Utils.TYPE_MEMBER_NAME);
        resolveConstructor = lookup.findVirtual(MethodHandles.Lookup.class, "getDirectConstructorCommon",
                MethodType.methodType(MethodHandle.class, Class.class, memberNameType, boolean.class));
        resolveField = lookup.findVirtual(MethodHandles.Lookup.class, "getDirectFieldCommon",
                MethodType.methodType(MethodHandle.class, byte.class, Class.class, memberNameType, boolean.class));
    }

    MethodHandle resolveConstructor(MethodHandles.Lookup lookup, Member member) {
        return InvokeUtils.invoke(resolveConstructor, lookup, member.getDeclaringClass(), member, false);
    }

    MethodHandle resolveField(MethodHandles.Lookup lookup, Member member) {
        byte refKind = MemberNameAccess.getInstance().getReferenceKind(member);
        return InvokeUtils.invoke(resolveField, lookup, refKind, member.getDeclaringClass(), member, false);
    }

    abstract MethodHandle resolveMethod(MethodHandles.Lookup lookup, Member member);
}
