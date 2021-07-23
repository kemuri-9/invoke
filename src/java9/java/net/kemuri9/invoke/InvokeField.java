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
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Member;

/**
 * Represents a Field and its access through the Invocation engine
 */
public interface InvokeField extends Member {

    /**
     * Retrieve the Field as a {@link InvokeFieldInstance}
     * @param <C> Type of Class
     * @param <F> Type of Field data
     * @return {@link InvokeFieldInstance} representing this {@link InvokeField}
     * @throws IllegalStateException When not an instance field
     */
    public <C, F> InvokeFieldInstance<C, F> asInstance();

    /**
     * Retrieve the Field as a {@link InvokeFieldStatic}
     * @param <F> Type of Field data
     * @return {@link InvokeFieldStatic} representing this {@link InvokeField}
     * @throws IllegalStateException When not a static field
     */
    public <F> InvokeFieldStatic<F> asStatic();

    /**
     * Retrieve the {@link Field} represented by this {@link InvokeField}
     * @param lookup {@link MethodHandles.Lookup} to access the field with. {@code null} indicates to use the default lookup
     * @return {@link Field} represented by this {@link InvokeField}
     * @throws IllegalAccessException When {@code lookup} does not have access to the indicated field
     */
    public Field getField(MethodHandles.Lookup lookup) throws IllegalAccessException;

    /**
     * Retrieve the {@link MethodHandle} that performs the get operation
     * @return {@link MethodHandle} to perform the get operation
     */
    public MethodHandle getGetterHandle();

    /**
     * Retrieve the {@link MethodHandle} that performs the set operation
     * @return {@link MethodHandle} to perform the set operation
     */
    public MethodHandle getSetterHandle();

    /**
     * Retrieve the type of Field
     * @return type of field
     */
    public MethodType getType();

    /**
     * Retrieve the {@link VarHandle} that represents the field
     * @return {@link VarHandle} that represents the field
     * @since Java 9
     */
    public VarHandle getVarHandle();
}
