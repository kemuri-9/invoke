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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.security.PrivilegedExceptionAction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class GetFullAccessDirectTest {

    @Test
    void testRun() throws Exception {
        // this is intended primarily for java 8 only, so only run in java 8
        Assumptions.assumeTrue(TestUtils.isJava8());
        PrivilegedExceptionAction<Lookup> action = InvokePrivate.instantiate(
                Class.forName("net.kemuri9.invoke.GetFullAccessDirect").getDeclaredConstructor());
        MethodHandles.Lookup lookup = action.run();
        MethodHandles.Lookup expected = InvokePrivate.getFieldValue(MethodHandles.Lookup.class, "IMPL_LOOKUP");
        Assertions.assertSame(expected, lookup);
    }
}
