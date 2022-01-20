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
import java.lang.invoke.VarHandle;
import java.lang.reflect.Member;

final class MemberNameAccessVersion extends MemberNameAccess {

    protected final MethodHandle resolve;
    protected final VarHandle memNameFlags;

    MemberNameAccessVersion() throws Throwable {
        MethodHandles.Lookup fullLookup = InvokeUtils.getFullAccessLookup();
        Class<?> factoryType = VersionSupport.getType(fullLookup, Utils.TYPE_MEMBER_NAME_FACTORY);
        Class<?> memberNameType = VersionSupport.getType(fullLookup, Utils.TYPE_MEMBER_NAME);
        MethodHandles.Lookup lookup = fullLookup.in(factoryType);
        resolve = lookup.findVirtual(factoryType, "resolve",
                MethodType.methodType(memberNameType, byte.class, memberNameType, Class.class, boolean.class));
        memNameFlags = fullLookup.in(memberNameType).findVarHandle(memberNameType, "flags", int.class);
    }
    
    Member untrust(Member member) {
        /* starting in java 15, final fields have a trust concept to where they can no longer be set.
         * untrust the fields so they can always be set when using the full access lookup */
        try {
            int flags = (Integer) memNameFlags.get(member);
            int newFlags = flags & ~0x00200000;
            if (flags != newFlags) {
                memNameFlags.set(member, newFlags);
            }
        } catch (Throwable t) {}
        return member;
    }

    @Override
    protected Member resolve(Member member, MethodHandles.Lookup lookup) {
        if (isResolved(member)) {
            if (member.getName() == null || getType(member) == null) {
                expand(member);
            }
            return member;
        }
        return InvokeUtils.invoke(resolve, factory, getReferenceKind(member), member, Utils.getLookupType(lookup), false);
    }
}
