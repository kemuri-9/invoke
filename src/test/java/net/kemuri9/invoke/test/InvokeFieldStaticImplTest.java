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
package net.kemuri9.invoke.test;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import net.kemuri9.invoke.InvokeFieldStatic;
import net.kemuri9.invoke.InvokeUtils;

import test.Type1;

public class InvokeFieldStaticImplTest {

    private static InvokeFieldStatic<String> TYPE1_S1;
    private static InvokeFieldStatic<String> TYPE1_S1_2;
    private static InvokeFieldStatic<String> TYPE1_S2;

    @BeforeAll
    public static void beforeAll() throws IllegalAccessException, NoSuchFieldException, SecurityException {
        TYPE1_S1 = InvokeUtils.getField(MethodHandles.publicLookup(), Type1.class.getField("S1")).asStatic();
        TYPE1_S1_2 = InvokeUtils.getField(InvokeUtils.getFullAccessLookup(), Type1.class.getField("S1")).asStatic();
        TYPE1_S2 = InvokeUtils.getField(MethodHandles.publicLookup(), Type1.class.getField("S2")).asStatic();
    }

    public static Stream<Arguments> getFields() {
        return Stream.of(
                Arguments.of(TYPE1_S1, "S1"),
                Arguments.of(TYPE1_S1_2, "S1"),
                Arguments.of(TYPE1_S2, "S2")
        );
    }

    @AfterEach
    public void afterEach() {
        // revert values to the original values
        Type1.S2 = "S2";
        // see testSetterFinalAccess
        TYPE1_S1_2.accept("S1");
    }

    @Test
    public void testAsInstance() {
        Assertions.assertThrows(IllegalStateException.class, ()-> TYPE1_S1.asInstance());
    }

    @Test
    public void testAsStatic() {
        Assertions.assertSame(TYPE1_S1_2, TYPE1_S1_2.asStatic());
    }

    @ParameterizedTest(name = "testAttributes - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeFieldStaticImplTest#getFields")
    public void testAttributes(InvokeFieldStatic<?> invokeField) throws Exception {
        Field field = invokeField.getDeclaringClass().getDeclaredField(invokeField.getName());

        Assertions.assertEquals(field.getDeclaringClass(), invokeField.getDeclaringClass());
        Assertions.assertEquals(field.getModifiers(), invokeField.getModifiers());
        Assertions.assertEquals(field.getName(), invokeField.getName());
        Assertions.assertEquals(field.isSynthetic(), invokeField.isSynthetic());
    }

    @ParameterizedTest(name = "testGetField - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeFieldStaticImplTest#getFields")
    public void testGetField(InvokeFieldStatic<?> invokeField) throws Exception {
        Field expected = invokeField.getDeclaringClass().getDeclaredField(invokeField.getName());
        Field actual = invokeField.getField(null);
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest(name = "testGetter - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeFieldStaticImplTest#getFields")
    public void testGetter(InvokeFieldStatic<?> field, Object fieldValue) throws Throwable {
        Assertions.assertSame(fieldValue, field.get());
        Assertions.assertSame(fieldValue, field.getGetterHandle().invoke());
        Assertions.assertSame(fieldValue, field.getGetter().get());
    }

    @Test
    public void testGetType() {
        Assertions.assertEquals(MethodType.methodType(String.class), TYPE1_S1.getType());
        Assertions.assertEquals(MethodType.methodType(String.class), TYPE1_S2.getType());
    }

    @Test
    public void testSetter() throws Throwable {
        Consumer<String> checkAssignment = (check) -> {
            Assertions.assertSame(check, TYPE1_S2.get());
            Assertions.assertSame(check, Type1.S2);
            Assertions.assertSame(check, TYPE1_S2.getGetter().get());
            Assertions.assertSame(check, InvokeUtils.invoke(TYPE1_S2.getGetterHandle()));
        };

        String val = RandomStringUtils.random(5);
        TYPE1_S2.accept(val);
        checkAssignment.accept(val);

        val = RandomStringUtils.random(5);
        TYPE1_S2.getSetter().accept(val);
        checkAssignment.accept(val);

        val = RandomStringUtils.random(5);
        TYPE1_S2.getSetterHandle().invoke(val);
        checkAssignment.accept(val);
    }

    @Test
    public void testSetterFinal() throws Throwable {
        if (TestUtils.isJava8()) {
            // java 8 has different behavior than later versions due to not having VarHandle
            Assumptions.assumeTrue(TestUtils.isJava8());
            Assertions.assertNull(TYPE1_S1.getSetterHandle());
            Assertions.assertThrows(IllegalStateException.class, ()-> TYPE1_S1.getSetter());
            Assertions.assertThrows(IllegalStateException.class, ()-> TYPE1_S1.accept("some"));
        } else {
            Assertions.assertNotNull(TYPE1_S1.getSetterHandle());
            Assertions.assertThrows(UnsupportedOperationException.class, ()-> TYPE1_S1.accept("some"));
            Assertions.assertThrows(UnsupportedOperationException.class, ()-> TYPE1_S1.getSetter().accept("some"));
            Assertions.assertThrows(UnsupportedOperationException.class, ()-> TYPE1_S1.getSetterHandle().invoke("some"));
        }
    }

    @Test
    public void testSetterFinalAccess() throws Throwable {
        // very that final fields CAN be set when they are initialized through the full access lookup
        Consumer<String> checkAssignment = (check) -> {
            Assertions.assertSame(check, TYPE1_S1.get());
            Assertions.assertSame(check, Type1.S1);
            Assertions.assertSame(check, TYPE1_S1.getGetter().get());
            Assertions.assertSame(check, InvokeUtils.invoke(TYPE1_S1.getGetterHandle()));
        };

        String val = RandomStringUtils.random(5);
        TYPE1_S1_2.accept(val);
        checkAssignment.accept(val);

        val = RandomStringUtils.random(5);
        TYPE1_S1_2.getSetter().accept(val);
        checkAssignment.accept(val);

        val = RandomStringUtils.random(5);
        TYPE1_S1_2.getSetterHandle().invoke(val);
        checkAssignment.accept(val);
    }

    @Test
    public void testSetterInvalid() {
        InvokeFieldStatic<Long> s2 = TYPE1_S2.asStatic();
        Assertions.assertThrows(ClassCastException.class, ()-> s2.accept(5l));
    }
}
