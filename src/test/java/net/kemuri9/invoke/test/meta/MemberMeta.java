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
package net.kemuri9.invoke.test.meta;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

public class MemberMeta implements Member {
    public final int modifiers;
    public final String name;
    Class<?> declaringClass;

    public MemberMeta(String name, int modifiers) {
        this.name = name;
        this.modifiers = modifiers;
    }

    @Override
    public Class<?> getDeclaringClass() {
        return declaringClass;
    }

    @Override
    public int getModifiers() {
        return modifiers;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getName(boolean fullName) {
        return (fullName) ? declaringClass.getName() + "." + name : name;
    }

    public final boolean isFinal() {
        return Modifier.isFinal(modifiers);
    }

    public final boolean isInstance() {
        return !isStatic();
    }

    public final boolean isPackage() {
        return !isProtected() && !isPublic() && !isPrivate();
    }

    public final boolean isPrivate() {
        return Modifier.isPrivate(modifiers);
    }

    public final boolean isProtected() {
        return Modifier.isProtected(modifiers);
    }

    public final boolean isPublic() {
        return Modifier.isPublic(modifiers);
    }

    public final boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }

    @Override
    public boolean isSynthetic() {
        return (modifiers & 0x00001000) != 0;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[name=" + name + ",modifiers=" + modifiers + "]";
    }
}
