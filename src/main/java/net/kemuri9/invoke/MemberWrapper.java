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
 * Handle the basics of implementing a {@link Member} by wrapping it {@link Member}
 */
class MemberWrapper implements Member {

    protected final Member member;

    protected MemberWrapper(Member member) {
        this.member = member;
    }

    @Override
    public Class<?> getDeclaringClass() {
        return member.getDeclaringClass();
    }

    @Override
    public int getModifiers() {
        return member.getModifiers();
    }

    @Override
    public String getName() {
        return member.getName();
    }

    @Override
    public boolean isSynthetic() {
        return member.isSynthetic();
    }

    @Override
    public String toString() {
        return member.toString();
    }
}
