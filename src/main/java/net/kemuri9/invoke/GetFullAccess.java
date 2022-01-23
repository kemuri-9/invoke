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
import java.lang.invoke.MethodHandles.Lookup;
import java.security.PrivilegedExceptionAction;

/**
 * Service interface for declaring how to retrieve the JVM's Full access {@link MethodHandles.Lookup}
 * @since 1.1
 */
public interface GetFullAccess extends PrivilegedExceptionAction<MethodHandles.Lookup> {

    /**
     * Retrieve the priority of the access lookup in comparison to other access lookups.
     * @return priority of the access lookup in comparison to others.
     */
    public default int getPriority() {
        return 0;
    }

    /**
     * Attempt to access the full access {@link Lookup}.
     * @return Full access {@link Lookup}. {@code null} indicates the operation failed.
     * @throws Exception When the operation fails
     */
    @Override
    public MethodHandles.Lookup run() throws Exception;
}
