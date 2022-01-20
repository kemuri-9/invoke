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
import java.lang.reflect.Field;
import java.lang.reflect.Member;

final class MemberNameAccessVersion extends MemberNameAccess {

    protected final MethodHandle resolve;

    MemberNameAccessVersion() throws Throwable {
        Lookup fullLookup = InvokeUtils.getFullAccessLookup();
        Class<?> factoryType = VersionSupport.getType(fullLookup, Utils.TYPE_MEMBER_NAME_FACTORY);
        Class<?> memberNameType = VersionSupport.getType(fullLookup, Utils.TYPE_MEMBER_NAME);
        Lookup lookup = fullLookup.in(factoryType);
        resolve = lookup.findVirtual(factoryType, "resolve",
                MethodType.methodType(memberNameType, byte.class, memberNameType, Class.class));
    }

    @Override
    protected Member resolve(Member member, MethodHandles.Lookup lookup) {
        ReferenceKind kind = ReferenceKind.ofByte(getReferenceKind(member));
        if (isResolved(member)) {
            // most resolved members will not have name or type data.
            if (member.getName() == null || getType(member) == null) {
                // expanding them may fill in some data, but it does not for quite a bit
                expand(member);
            }
            if (member.getName() != null && getType(member) != null) {
                // expansion worked
                return member;
            }
            if (kind.isField() && kind.isStatic()) {
                /* static fields have a tendency to not resolve the type in Java <10, and searching by name
                 * also does not resolve, so have to lookup the field itself.
                 * the security manager may get in the way when performing this task,
                 * so perform the lookup with the security manager disabled */
                Field field = SecurityManagerDisabler.getInstance().withSecurityDisabled(()->
                    member.getDeclaringClass().getDeclaredField(member.getName())
                );
                return newMember(field, kind.isSetter());
            }
        }
        return InvokeUtils.invoke(resolve, factory, kind.kind, member, Utils.getLookupType(lookup));
    }
}
