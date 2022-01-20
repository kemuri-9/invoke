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

import java.lang.reflect.Member;

/**
 * Wraps a class as a Member. This is exclusively used by nested classes
 */
final class ClassMember implements Member {

    final Class<?> parent;
    final Class<?> clazz;

    ClassMember(Class<?> clazz) {
        // prefetch the declaring class to avoid the security manager getting in the way later
        parent = clazz.getDeclaringClass();
        this.clazz = clazz;
    }

    @Override
    public Class<?> getDeclaringClass() {
        return parent;
    }

    @Override
    public String getName() {
        // use simple name to only have the nested class name
        return clazz.getSimpleName();
    }

    @Override
    public int getModifiers() {
        return clazz.getModifiers();
    }

    @Override
    public boolean isSynthetic() {
        return clazz.isSynthetic();
    }

    @Override
    public String toString() {
        return clazz.getName();
    }
}
