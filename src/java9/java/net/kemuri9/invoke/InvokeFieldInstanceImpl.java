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
import java.util.function.BiConsumer;
import java.util.function.Function;

final class InvokeFieldInstanceImpl<C, F> extends InvokeFieldImpl9 implements InvokeFieldInstance<C, F> {

    InvokeFieldInstanceImpl(Member member, VarHandle handle) {
        super(member, handle);
    }

    @Override
    public void accept(C t, F u) {
        try {
            handle.set(t, u);
        } catch (Throwable ex) {
            throw Utils.asException(ex, IllegalArgumentException.class, t + " and " + u + " are invalid values");
        }
    }

    @Override
    public F apply(C t) {
        try {
            return Utils.cast(handle.get(t));
        } catch (Throwable ex) {
            throw Utils.asException(ex, IllegalArgumentException.class, t + " is an invalid value");
        }
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
    public Function<C, F> getGetter() {
        return this;
    }

    @Override
    public BiConsumer<C, F> getSetter() {
        return this;
    }
}
