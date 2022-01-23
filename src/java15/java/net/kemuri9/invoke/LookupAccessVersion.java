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
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

final class LookupAccessVersion extends LookupAccess {

    protected final MethodHandle resolveMethod;
    protected final MethodHandle resolveVarHandle;

    protected LookupAccessVersion() throws Throwable {
        MethodHandles.Lookup lookup = InvokeUtils.getFullAccessLookup().in(MethodHandles.Lookup.class);
        Class<?> memberNameType = VersionSupport.getType(InvokeUtils.getFullAccessLookup(), Utils.TYPE_MEMBER_NAME);
        resolveMethod = lookup.findVirtual(MethodHandles.Lookup.class, "getDirectMethodCommon",
                MethodType.methodType(MethodHandle.class, byte.class, Class.class, memberNameType,
                        boolean.class, boolean.class, MethodHandles.Lookup.class));
        resolveVarHandle = lookup.findVirtual(MethodHandles.Lookup.class, "getFieldVarHandleCommon",
                MethodType.methodType(VarHandle.class, byte.class, byte.class, Class.class,
                        memberNameType, memberNameType, boolean.class));
    }

    @Override
    MethodHandle resolveMethod(MethodHandles.Lookup lookup, Member member) {
        byte refKind = MemberNameAccess.getInstance().getReferenceKind(member);
        if (Modifier.isPrivate(member.getModifiers()) && Utils.isFullLookup(lookup)) {
            /* starting in java 12 private methods are not resolvable from full access directly,
             * requiring to lookup as the target class to be able to resolve */
            lookup = lookup.in(member.getDeclaringClass());
        }
        return InvokeUtils.invoke(resolveMethod, lookup, refKind, member.getDeclaringClass(),
                member, false, true, lookup);
    }
    
    VarHandle resolveVarHandle(MethodHandles.Lookup lookup, Field field) throws IllegalAccessException {
        // do a plain unreflect to verify the accesses first though
        lookup.unreflectGetter(field);
        MemberNameAccessVersion mnAccess = MemberNameAccess.getInstance();
        Member getter = mnAccess.untrust(mnAccess.newMember(field, false));
        Member setter = mnAccess.untrust(mnAccess.newMember(field, true));
        VarHandle handle = resolveVarHandle(lookup, getter, setter);
        return handle;
    }
    
    VarHandle resolveVarHandle(MethodHandles.Lookup lookup, Member getter, Member setter) {
        MemberNameAccess mnAccess = MemberNameAccess.getInstance();
        byte getRefKind = mnAccess.getReferenceKind(getter);
        byte putRefKind = mnAccess.getReferenceKind(setter);
        return InvokeUtils.invoke(resolveVarHandle, lookup, getRefKind, putRefKind,
                getter.getDeclaringClass(), getter, setter, false);
    }

    VarHandle resolveVarHandle(MethodHandles.Lookup lookup, RefGetSet members) {
       return resolveVarHandle(lookup, members.getter, members.setter);
    }
}
