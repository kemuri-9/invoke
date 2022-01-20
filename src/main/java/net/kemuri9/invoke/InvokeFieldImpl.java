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

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Member;

/**
 * Base implementation of {@link InvokeField}
 */
abstract class InvokeFieldImpl extends MemberWrapper implements InvokeField {

    protected InvokeFieldImpl(Member member) {
        super(member);
    }

    @Override
    public Field getField(MethodHandles.Lookup lookup) throws IllegalAccessException {
        lookup = InvokeUtils.defaultLookup(lookup);
        if (member instanceof Field) {
            // perform an unreflect to have the access verified
            lookup.unreflectGetter((Field) member);
            return (Field) member;
        }
        // reveal direct to get the field
        try {
            return lookup.revealDirect(getGetterHandle()).reflectAs(Field.class, lookup);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace(System.err);
            throw (IllegalAccessException) ex.getCause();
        }
    }

    @Override
    public String toString() {
        return new StringBuilder(64).append(member.getDeclaringClass().getName()).append(".")
                .append(member.getName()).append("[type=")
                .append(getType().returnType()).append(", modifiers=").append(member.getModifiers())
                .append("]").toString();
    }
}
