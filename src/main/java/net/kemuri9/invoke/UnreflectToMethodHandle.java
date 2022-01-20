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
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 * Specialization of {@link Unreflector} for returning {@link MethodHandle}s
 *
 * @param <E> Type of {@link Member} to lookup
 */
public interface UnreflectToMethodHandle<E extends Member> extends Unreflector<E, MethodHandle> {

    /** {@link UnreflectToMethodHandle} to invoke a {@link Constructor} */
    public static final UnreflectToMethodHandle<Constructor<?>> CONSTRUCTOR = (l, c)-> l.unreflectConstructor(c);

    /** {@link UnreflectToMethodHandle} to invoke a {@link Executable} */
    public static final UnreflectToMethodHandle<Executable> EXECUTABLE =
            (l, e)-> (e instanceof Constructor) ? l.unreflectConstructor((Constructor<?>) e) : l.unreflect((Method) e);

    /** {@link UnreflectToMethodHandle} to get the value of a {@link Field} */
    public static final UnreflectToMethodHandle<Field> FIELD_GETTER = (l, f)-> l.unreflectGetter(f);

    /** {@link UnreflectToMethodHandle} to set the value of a {@link Field} */
    public static final UnreflectToMethodHandle<Field> FIELD_SETTER = (l, f)-> l.unreflectSetter(f);

    /** {@link UnreflectToMethodHandle} to invoke a {@link Method} */
    public static final UnreflectToMethodHandle<Method> METHOD = (l, m)-> l.unreflect(m);
}
