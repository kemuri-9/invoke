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
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.util.function.Function;

/**
 * Represents an Executable and its access through the Invocation engine
 *
 * @param <R> Type of return on the executable
 */
public interface InvokeExecutable<R> extends Member, Function<Object[], R> {

    /**
     * Perform a var-args execution on the executable.
     * If the Executable is an instance method, the instance should be the first argument
     * @param args arguments to execute with
     * @return return value of the invocation
     * @throws WrongMethodTypeException When the number of arguments does not match the {@link MethodType}
     * @throws ClassCastException When the type of arguments does not match the {@link MethodType}
     * @throws RuntimeException When the underlying executable throws an exception
     */
    public R invoke(Object... args);

    /**
     * Retrieve the {@link Executable} that this InvokeExecutable represents.
     * @param lookup {@link MethodHandles.Lookup} to access the field with. {@code null} indicates to use the default lookup
     * @return {@link Executable} that this {@link InvokeExecutable} represents.
     * @throws IllegalAccessException When {@code lookup} does not have access to the executable
     */
    public Executable getExecutable(MethodHandles.Lookup lookup) throws IllegalAccessException;

    /**
     * Retrieve the {@link MethodHandle} that performs the execution
     * @return {@link MethodHandle} that performs the execution
     */
    public MethodHandle getHandle();

    /**
     * Retrieve the {@link MethodType} for the Executable
     * @return {@link MethodType} for the Executable
     */
    public MethodType getType();
}
