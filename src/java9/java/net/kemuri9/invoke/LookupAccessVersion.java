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
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Member;

final class LookupAccessVersion extends LookupAccess {

    protected final MethodHandle resolveMethod;
    protected final MethodHandle resolveVarHandle;

    protected LookupAccessVersion() throws Throwable {
        MethodHandles.Lookup lookup = InvokeUtils.getFullAccessLookup().in(MethodHandles.Lookup.class);
        Class<?> memberNameType = VersionSupport.getType(InvokeUtils.getFullAccessLookup(), Utils.TYPE_MEMBER_NAME);
        resolveMethod = lookup.findVirtual(MethodHandles.Lookup.class, "getDirectMethodCommon",
                MethodType.methodType(MethodHandle.class, byte.class, Class.class, memberNameType,
                        boolean.class, boolean.class, Class.class));
        resolveVarHandle = lookup.findVirtual(MethodHandles.Lookup.class, "getFieldVarHandleCommon",
                MethodType.methodType(VarHandle.class, byte.class, byte.class, Class.class,
                        memberNameType, memberNameType, boolean.class));
    }

    @Override
    MethodHandle resolveMethod(MethodHandles.Lookup lookup, Member member) {
        byte refKind = MemberNameAccess.getInstance().getReferenceKind(member);
        return InvokeUtils.invoke(resolveMethod, lookup, refKind, member.getDeclaringClass(),
                member, false, true, Utils.getLookupType(lookup));
    }

    VarHandle resolveVarHandle(MethodHandles.Lookup lookup, Field field) throws IllegalAccessException {
        return InvokeUtils.unreflect(lookup, field, UnreflectToVarHandle.FIELD);
    }

    VarHandle resolveVarHandle(MethodHandles.Lookup lookup, RefGetSet members) {
        MemberNameAccess mnAccess = MemberNameAccess.getInstance();
        byte getRefKind = mnAccess.getReferenceKind(members.getter);
        byte putRefKind = mnAccess.getReferenceKind(members.setter);
        return InvokeUtils.invoke(resolveVarHandle, lookup, getRefKind, putRefKind,
                members.getter.getDeclaringClass(), members.getter, members.setter, false);
    }
}
