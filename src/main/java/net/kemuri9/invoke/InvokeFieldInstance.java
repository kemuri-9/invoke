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

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Represents an instance Field and its access through the Invocation engine
 *
 * @param <C> Class type the field exists on
 * @param <T> Type of field on the class
 */
public interface InvokeFieldInstance<C, T> extends Function<C, T>, BiConsumer<C, T>, InvokeField {

    /**
     * Retrieve the {@link Function} that can retrieve values of the underlying field given instances of the class
     * @return {@link Function} that can perform get operations for the underlying field
     */
    public Function<C, T> getGetter();

    /**
     * Retrieve the {@link BiConsumer} that can set values of the underlying field given
     *  instances of the class and field value
     * @return {@link BiConsumer} that can set values of the underlying field.
     * @throws IllegalStateException If the associated field does not have set access permissions
     */
    public BiConsumer<C, T> getSetter();
}
