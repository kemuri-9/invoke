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
 * Holds a reference to the getter and setter pair for a field
 */
final class RefGetSet {

    final Member getter;
    final Member setter;

    RefGetSet(Member getter) {
        this.getter = getter;
        MemberNameAccess memName = MemberNameAccess.getInstance();
        this.setter = memName.newMember(getter.getDeclaringClass(), getter.getName(),
                // adding 2 to a getter ends up with the corresponding setter
                (Class<?>) memName.getType(getter), (byte) (memName.getReferenceKind(getter) + 2));
    }
}
