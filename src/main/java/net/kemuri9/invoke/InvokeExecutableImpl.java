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
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Implementation of {@link InvokeExecutable}
 *
 * @param <R> Type of return
 */
final class InvokeExecutableImpl<R> extends MemberWrapper implements InvokeExecutable<R> {

    private final MethodHandle handle;

    InvokeExecutableImpl(Member member, MethodHandle handle) {
        super(member);
        this.handle = handle;
    }

    @Override
    public R apply(Object[] arguments) {
        try {
            Object ret = handle.invokeWithArguments(arguments);
            return Utils.cast(ret);
        } catch (Throwable t) {
            throw Utils.asException(t, RuntimeException.class,
                    "failed to execute " + this + " with arguments " + Arrays.toString(arguments));
        }
    }

    @Override
    public Executable getExecutable(Lookup lookup) throws IllegalAccessException {
        lookup = InvokeUtils.defaultLookup(lookup);
        if (member instanceof Executable) {
            // perform an unreflect to check the access
            UnreflectToMethodHandle.EXECUTABLE.unreflect(lookup, (Executable) member);
        }
        /* internally the names for constructors are <init>,
         * but in reflection this becomes the declaring class name */
        String name = getName();
        Class<? extends Executable> memberType =
                getDeclaringClass().getName().equals(name) ? Constructor.class : Method.class;
        try {
            return lookup.revealDirect(handle).reflectAs(memberType, lookup);
        } catch (IllegalArgumentException ex) {
            throw (IllegalAccessException) ex.getCause();
        }
    }

    @Override
    public MethodHandle getHandle() {
        return handle;
    }

    @Override
    public MethodType getType() {
        return handle.type();
    }

    @Override
    public R invoke(Object... args) {
        return apply(args);
    }
}
