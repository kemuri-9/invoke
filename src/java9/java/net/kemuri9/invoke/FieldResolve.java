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

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

final class FieldResolve {

    static InvokeField getField(Field field, Lookup lookup) throws IllegalAccessException {
        VarHandle handle = null;
        try {
            handle = LookupAccess.getInstance().resolveVarHandle(lookup, field);
        } catch (UnsupportedOperationException ex) {
            handle = InvokeUtils.unreflect(lookup, field, UnreflectToVarHandle.FIELD);
        }
        return newField(field, handle);
    }

    static List<InvokeField> getFields(Lookup lookup, List<RefGetSet> fieldMembers) {
        List<InvokeField> ret = new ArrayList<>(fieldMembers.size());
        LookupAccessVersion access = LookupAccess.getInstance();
        for (RefGetSet ref : fieldMembers) {
            try {
                VarHandle handle = access.resolveVarHandle(lookup, ref);
                ret.add(newField(ref.getter, handle));
            } catch (RuntimeException ex) {
                // do not have access permissions, so skip it
            }
        }
        return ret;
    }

    private static InvokeField newField(Member member, VarHandle handle) {
        return (Modifier.isStatic(member.getModifiers()))
                ? new InvokeFieldStaticImpl<>(member, handle)
                : new InvokeFieldInstanceImpl<>(member, handle);
    }

    private FieldResolve() {}
}
