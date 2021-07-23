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
import java.util.function.Consumer;
import java.util.function.Supplier;

final class InvokeFieldStaticImpl<F> extends InvokeFieldImpl implements InvokeFieldStatic<F> {

    private static final class Getter<F> implements Supplier<F> {

        private final MethodHandle handle;

        Getter(MethodHandle handle) {
            this.handle = handle;
        }

        @Override
        public F get() {
            try {
                return Utils.cast(handle.invoke());
            } catch (Throwable ex) {
                throw Utils.asException(ex, UnsupportedOperationException.class, "failed to retrieve value");
            }
        }
    }

    private static final class Setter<F> implements Consumer<F> {

        private final MethodHandle handle;

        Setter(MethodHandle handle) {
            this.handle = handle;
        }

        @Override
        public void accept(F u) {
            try {
                handle.invoke(u);
            } catch (Throwable ex) {
                throw Utils.asException(ex, IllegalArgumentException.class, u + " is not a valid value");
            }
        }
    }

    private final Getter<F> getter;
    private final Setter<F> setter;

    InvokeFieldStaticImpl(Member member, MethodHandle getter, MethodHandle setter) {
        super(member);
        this.getter = new Getter<>(getter);
        this.setter = (setter == null) ? null : new Setter<>(setter);
    }

    @Override
    public void accept(F u) {
        getSetter().accept(u);
    }

    @Override
    public <A, B> InvokeFieldInstance<A, B> asInstance() {
        throw new IllegalStateException(this + " is a static field");
    }

    @Override
    public <A> InvokeFieldStatic<A> asStatic() {
        return Utils.cast(this);
    }

    @Override
    public F get() {
        return getGetter().get();
    }

    @Override
    public Getter<F> getGetter() {
        return getter;
    }

    @Override
    public MethodHandle getGetterHandle() {
        return getter.handle;
    }

    @Override
    public Setter<F> getSetter() {
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
