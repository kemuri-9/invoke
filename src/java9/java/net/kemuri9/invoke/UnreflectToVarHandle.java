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

import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Member;

/**
 * Specialization of {@link Unreflector} for returning {@link VarHandle}s
 * @param <E> Type of {@link Member} to unreflect on
 * @since Java 9
 */
public interface UnreflectToVarHandle<E extends Member> extends Unreflector<E, VarHandle> {

    /** {@link UnreflectToVarHandle} for a {@link Field} */
    public static UnreflectToVarHandle<Field> FIELD = (l, f)-> l.unreflectVarHandle(f);
}
