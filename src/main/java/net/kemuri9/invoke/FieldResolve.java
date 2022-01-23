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
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

final class FieldResolve {

    static InvokeField getField(Field field, Lookup lookup) throws IllegalAccessException {
        MethodHandle getter = InvokeUtils.unreflect(lookup, field, UnreflectToMethodHandle.FIELD_GETTER);
        MethodHandle setter = null;
        try {
            setter = InvokeUtils.unreflect(lookup, field, UnreflectToMethodHandle.FIELD_SETTER);
        } catch (IllegalAccessException ex) {
            // read only field
        }
        return newField(field, getter, setter);
    }

    static List<InvokeField> getFields(Lookup lookup, List<RefGetSet> fieldMembers) {
        List<InvokeField> ret = new ArrayList<>(fieldMembers.size());
        LookupAccessVersion access = LookupAccess.getInstance();
        for (RefGetSet ref : fieldMembers) {
            MethodHandle getter, setter = null;
            try {
                getter = access.resolveField(lookup, ref.getter);
            } catch (RuntimeException ex) {
                // do not have access permissions, so skip it
                continue;
            }

            try {
                setter = access.resolveField(lookup, ref.setter);
            } catch (RuntimeException ex) {
                // read only field
            }
            ret.add(newField(ref.getter, getter, setter));
        }
        return ret;
    }

    private static InvokeField newField(Member member, MethodHandle getter, MethodHandle setter) {
        return (Modifier.isStatic(member.getModifiers()))
                ? new InvokeFieldStaticImpl<>(member, getter, setter)
                : new InvokeFieldInstanceImpl<>(member, getter, setter);
    }

    private FieldResolve() {}
}
