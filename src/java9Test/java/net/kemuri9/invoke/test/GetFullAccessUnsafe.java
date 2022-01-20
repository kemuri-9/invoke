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
package net.kemuri9.invoke.test;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;

import net.kemuri9.invoke.GetFullAccess;

import sun.misc.Unsafe;

/**
 * Uses unsafe to access the full access MethodHandles.Lookup.
 * This methodology does not incur the warning of violating reflection.
 */
public final class GetFullAccessUnsafe implements net.kemuri9.invoke.GetFullAccess {

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public Lookup run() throws Exception {
        /* Though Unsafe.getUnsafe() is protected, with the package being open it can be directly reflected on and accessed */
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        try {
            Object unsafeRaw = theUnsafe.get(null);
            Unsafe unsafe = (Unsafe) unsafeRaw;
            Field implLookup = Lookup.class.getDeclaredField("IMPL_LOOKUP");
            Object lookup = unsafe.getObject(unsafe.staticFieldBase(implLookup), unsafe.staticFieldOffset(implLookup));
            return (Lookup) lookup;
        } finally {
            theUnsafe.setAccessible(false);
        }
    }
}
