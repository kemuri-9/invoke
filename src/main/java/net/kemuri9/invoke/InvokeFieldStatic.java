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

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a static Field and its access through the Invocation engine
 *
 * @param <T> Type of field on the class
 */
public interface InvokeFieldStatic<T> extends InvokeField, Consumer<T>, Supplier<T> {

    /**
     * Retrieve the {@link Function} that can retrieve the value of the underlying field
     * @return {@link Function} that can perform get operations for the underlying field
     */
    public Supplier<T> getGetter();

    /**
     * Retrieve the {@link Consumer} that can set the value of the underlying field given
     * instances of the class and field value
     * @return {@link BiConsumer} that can set values of the underlying field
     */
    public Consumer<T> getSetter();
}
