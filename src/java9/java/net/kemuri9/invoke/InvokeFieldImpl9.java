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
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

/**
 * Base implementation of {@link InvokeField} for Java 9+
 */
abstract class InvokeFieldImpl9 extends InvokeFieldImpl implements InvokeField {

    protected final VarHandle handle;

    protected InvokeFieldImpl9(Member member, VarHandle handle) {
        super(member);
        this.handle = handle;
    }

    @Override
    public Field getField(MethodHandles.Lookup lookup) throws IllegalAccessException {
        lookup = InvokeUtils.defaultLookup(lookup);
        if (member instanceof Field) {
            return super.getField(lookup);
        }
        // VarHandle fields cannot be uncracked, so a crackable handle needs to be attained first
        try {
            MethodHandle mHandle = Modifier.isStatic(getModifiers())
                    ? lookup.findStaticGetter(getDeclaringClass(), getName(), getType().returnType())
                    : lookup.findGetter(getDeclaringClass(), getName(), getType().returnType());
            return lookup.revealDirect(mHandle).reflectAs(Field.class, lookup);
        } catch (NoSuchFieldException ex) {
            throw new IllegalAccessException(lookup + " has no access to " + this);
        }
    }

    @Override
    public MethodHandle getGetterHandle() {
        return handle.toMethodHandle(AccessMode.GET);
    }

    @Override
    public MethodHandle getSetterHandle() {
        return handle.toMethodHandle(AccessMode.SET);
    }

    @Override
    public MethodType getType() {
        return handle.accessModeType(AccessMode.GET);
    }

    @Override
    public VarHandle getVarHandle() {
        return handle;
    }
}
