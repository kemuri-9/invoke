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

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Member;

/**
 * Perform a unreflecting Lookup on {@link MethodHandles.Lookup} to retrieve some result
 * @param <E> Type of {@link Member} to unreflect on
 * @param <R> Type of return of the unreflection.
 */
public interface Unreflector<E extends Member, R> {

    /**
     * Perform the lookup operation
     * @param lookup {@link MethodHandles.Lookup} to lookup with
     * @param element {@link Member} to lookup
     * @return looked up {@code element}
     * @throws IllegalAccessException When access checking fails
     */
    public R unreflect(MethodHandles.Lookup lookup, E element) throws IllegalAccessException;
}
