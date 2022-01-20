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

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.security.PrivilegedExceptionAction;

/**
 * Directly access the field through reflection. In Java 9+, this may fail or cause a warning due
 * to trying to access a field that is not public and not open for reflection.
 */
final class GetFullAccessDirect implements PrivilegedExceptionAction<Lookup> {

	@Override
	public Lookup run() throws Exception {
		Field field = Lookup.class.getDeclaredField("IMPL_LOOKUP");
		field.setAccessible(true);
		try {
			Object lookup = field.get(null);
			return (Lookup) lookup;
		} finally {
			field.setAccessible(false);
		}
	}
}
