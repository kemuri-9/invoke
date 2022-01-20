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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.kemuri9.invoke.InvokeField;
import net.kemuri9.invoke.InvokeUtils;

import test.Type1;

public class InvokeFieldImplTest {

    @Test
    public void testGetField() throws Exception {
        List<InvokeField> fields = InvokeUtils.getFields(InvokeUtils.getFullAccessLookup(), Type1.class, false);
        for (InvokeField field : fields) {
            MethodHandles.Lookup lookup = null;
            if (!Modifier.isPublic(field.getModifiers())) {
                Assertions.assertThrows(IllegalAccessException.class, ()-> field.getField(null));
                lookup = InvokeUtils.getFullAccessLookup();
            }

            Field expected = field.getDeclaringClass().getDeclaredField(field.getName());
            Field actual = field.getField(lookup);
            Assertions.assertEquals(expected, actual);
        }
    }
}
