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
import java.lang.invoke.MethodType;
import java.lang.reflect.Member;
import java.util.function.BiConsumer;
import java.util.function.Function;

final class InvokeFieldInstanceImpl<C, F> extends InvokeFieldImpl implements InvokeFieldInstance<C, F> {

    private static final class Getter<C, F> implements Function<C, F> {

        private final MethodHandle handle;

        Getter(MethodHandle handle) {
            this.handle = handle;
        }

        @Override
        public F apply(C t) {
            try {
                return Utils.cast(handle.invoke(t));
            } catch (Throwable ex) {
                throw Utils.asException(ex, IllegalArgumentException.class, t + " is not a valid value");
            }
        }
    }

    private static final class Setter<C, F> implements BiConsumer<C, F> {

        private final MethodHandle handle;

        Setter(MethodHandle handle) {
            this.handle = handle;
        }

        @Override
        public void accept(C t, F u) {
            try {
                handle.invoke(t, u);
            } catch (Throwable ex) {
                throw Utils.asException(ex, IllegalArgumentException.class, t + " and " + u + " are not valid values");
            }
        }
    }

    private final Getter<C, F> getter;
    private final Setter<C, F> setter;

    InvokeFieldInstanceImpl(Member member, MethodHandle getter, MethodHandle setter) {
        super(member);
        this.getter = new Getter<>(getter);
        this.setter = (setter == null) ? null : new Setter<>(setter);
    }

    @Override
    public void accept(C t, F u) {
        getSetter().accept(t, u);
    }

    @Override
    public F apply(C t) {
        return getGetter().apply(t);
    }

    @Override
    public <A, B> InvokeFieldInstance<A, B> asInstance() {
        return Utils.cast(this);
    }

    @Override
    public <A> InvokeFieldStatic<A> asStatic() {
        throw new IllegalStateException(this + " is an instance field");
    }

    @Override
    public Getter<C, F> getGetter() {
        return getter;
    }

    @Override
    public MethodHandle getGetterHandle() {
        return getter.handle;
    }

    @Override
    public Setter<C, F> getSetter() {
        if (setter == null) {
            throw new IllegalStateException("No setter available for " + this);
        }
        return setter;
    }

    @Override
    public MethodHandle getSetterHandle() {
        return (setter == null) ? null : setter.handle;
    }

    @Override
    public MethodType getType() {
        return getter.handle.type();
    }
}
