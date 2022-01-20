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
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

final class VersionSupport {

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

    static List<GetFullAccess> getLookups() {
        ServiceLoader<GetFullAccess> loader = ServiceLoader.load(GetFullAccess.class);
        List<GetFullAccess> lookups = new ArrayList<>();
        // only if invoke is open to us can we use the full access direct
        if (java.lang.invoke.MethodHandles.class.getModule().isOpen("java.lang.invoke", VersionSupport.class.getModule())) {
            lookups.add(new GetFullAccessDirect());
        }
        loader.forEach(lookups::add);
        lookups.sort(Comparator.comparing(GetFullAccess::getPriority));
        return lookups;
    }

    static Class<?> getType(Lookup lookup, String name) throws ClassNotFoundException {
        return Class.forName(name);
    }

    static boolean isAccessible(Lookup lookup, Class<?> nestedType) {
        if (Utils.isFullLookup(lookup)) {
            return true;
        }
        try {
            lookup.accessClass(nestedType);
            return true;
        } catch (IllegalAccessException ex) {
            return false;
        }
    }

    static InvokeField newField(Member member, VarHandle handle) {
        return (Modifier.isStatic(member.getModifiers()))
                ? new InvokeFieldStaticImpl<>(member, handle)
                : new InvokeFieldInstanceImpl<>(member, handle);
    }

    private VersionSupport() {}
}
