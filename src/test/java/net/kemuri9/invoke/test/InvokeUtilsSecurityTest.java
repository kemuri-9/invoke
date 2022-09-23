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

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Timeout;

/**
 * Validate all the tests under a security manager to verify that it gets disabled when needed
 */
@Timeout(value = 5, unit = TimeUnit.SECONDS)
public class InvokeUtilsSecurityTest extends InvokeUtilsTest {

    private static void setSecurityManager(SecurityManager sm) throws Throwable {
        if (TestUtils.isAtLeastJava(18)) {
            String smProp = System.getProperty("java.security.manager");
            if (!"allow".equals(smProp)) {
                return;
            }
        }
        System.setSecurityManager(sm);
    }

    @BeforeAll
    public static void beforeAll() throws Throwable {
        setSecurityManager(new TestSecurityManager());
    }

    @AfterAll
    public static void afterAll() throws Throwable {
        setSecurityManager(null);
    }
}
