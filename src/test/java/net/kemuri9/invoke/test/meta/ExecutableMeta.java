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

import java.util.Arrays;

public class ExecutableMeta extends MemberMeta {

    public final Class<?>[] argTypes;

    public ExecutableMeta(String name, int modifiers, Class<?>... argTypes) {
        super(name, modifiers);
        this.argTypes = argTypes;
    }

    public Class<?>[] getArgTypes() {
        if (isStatic()) {
            return argTypes;
        }
        Class<?>[] ret = new Class<?>[argTypes.length+1];
        System.arraycopy(argTypes, 0, ret, 1, argTypes.length);
        ret[0] = this.declaringClass;
        return ret;
    }

    @Override
    public String toString() {
        String str = super.toString();
        return str.substring(0, str.length()-1) + ",args=" + Arrays.asList(getArgTypes()) + "]";
    }
}
