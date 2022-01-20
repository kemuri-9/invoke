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

import java.lang.invoke.VarHandle;
import java.lang.reflect.Member;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class InvokeFieldStaticImpl<F> extends InvokeFieldImpl9 implements InvokeFieldStatic<F> {

    InvokeFieldStaticImpl(Member member, VarHandle handle) {
        super(member, handle);
    }

    @Override
    public void accept(F u) {
        try {
            handle.set(u);
        } catch (Throwable ex) {
            throw Utils.asException(ex, IllegalArgumentException.class, u + " is an invalid values");
        }
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
        try {
            return Utils.cast(handle.get());
        } catch (Throwable ex) {
            throw Utils.asException(ex, UnsupportedOperationException.class, "failed to retrieve value");
        }
    }

    @Override
    public Supplier<F> getGetter() {
        return this;
    }

    @Override
    public Consumer<F> getSetter() {
        return this;
    }
}
