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
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import net.kemuri9.invoke.InvokeFieldInstance;
import net.kemuri9.invoke.InvokeUtils;

import test.Type1;

public class InvokeFieldInstanceImplTest {

    private static InvokeFieldInstance<Type1, String> TYPE1_I1;
    private static InvokeFieldInstance<Type1, String> TYPE1_I1_2;
    private static InvokeFieldInstance<Type1, Integer> TYPE1_I2;

    public static Stream<Arguments> getFields() {
        return Stream.of(
                Arguments.of(TYPE1_I1, new Type1(), "I1"),
                Arguments.of(TYPE1_I1_2, new Type1(), "I1"),
                Arguments.of(TYPE1_I2, new Type1(), 2)
        );
    }

    @BeforeAll
    public static void beforeAll() throws IllegalAccessException, NoSuchFieldException, SecurityException {
        TYPE1_I1 = InvokeUtils.getField(MethodHandles.publicLookup(), Type1.class.getField("I1")).asInstance();
        TYPE1_I1_2 = InvokeUtils.getField(InvokeUtils.getFullAccessLookup(), Type1.class.getField("I1")).asInstance();
        TYPE1_I2 = InvokeUtils.getField(MethodHandles.publicLookup(), Type1.class.getField("I2")).asInstance();
    }

    @Test
    public void testAsInstance() {
        Assertions.assertSame(TYPE1_I1_2, TYPE1_I1_2.asInstance());
    }

    @Test
    public void testAsStatic() {
        Assertions.assertThrows(IllegalStateException.class, ()-> TYPE1_I1.asStatic());
    }

    @ParameterizedTest(name = "testAttributes - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeFieldInstanceImplTest#getFields")
    public <C> void testAttributes(InvokeFieldInstance<C, ?> invokeField) throws Exception {
        Field field = invokeField.getDeclaringClass().getDeclaredField(invokeField.getName());

        Assertions.assertEquals(field.getDeclaringClass(), invokeField.getDeclaringClass());
        Assertions.assertEquals(field.getModifiers(), invokeField.getModifiers());
        Assertions.assertEquals(field.getName(), invokeField.getName());
        Assertions.assertEquals(field.isSynthetic(), invokeField.isSynthetic());
    }

    @ParameterizedTest(name = "testGetField - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeFieldInstanceImplTest#getFields")
    public <C> void testGetField(InvokeFieldInstance<C, ?> invokeField) throws Exception {
        Field expected = invokeField.getDeclaringClass().getDeclaredField(invokeField.getName());
        Field actual = invokeField.getField(null);
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest(name = "testGetter - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeFieldInstanceImplTest#getFields")
    public <C> void testGetter(InvokeFieldInstance<C, ?> field, C instance, Object fieldValue) throws Throwable {
        Assertions.assertSame(fieldValue, field.apply(instance));
        Assertions.assertSame(fieldValue, field.getGetterHandle().invoke(instance));
        Assertions.assertSame(fieldValue, field.getGetter().apply(instance));
    }

    @Test
    public void testGetterInvalid() {
        Assertions.assertThrows(NullPointerException.class, ()-> TYPE1_I1.apply(null));
        Assertions.assertThrows(NullPointerException.class, ()-> TYPE1_I1.getGetter().apply(null));
        InvokeFieldInstance<String, String> invalid = TYPE1_I1.asInstance();
        Assertions.assertThrows(ClassCastException.class, ()-> invalid.apply("5"));
        Assertions.assertThrows(ClassCastException.class, ()-> invalid.getGetter().apply("5"));
    }

    @Test
    public void testGetType() {
        Assertions.assertEquals(MethodType.methodType(String.class, Type1.class), TYPE1_I1.getType());
        Assertions.assertEquals(MethodType.methodType(int.class, Type1.class), TYPE1_I2.getType());
    }

    @Test
    public void testSetter() throws Throwable {
        BiConsumer<Type1, Integer> checkAssignment = (instance, check) -> {
            Assertions.assertEquals(check, TYPE1_I2.apply(instance));
            Assertions.assertEquals(check, instance.I2);
            Assertions.assertEquals(check, TYPE1_I2.getGetter().apply(instance));
            Assertions.assertEquals(check, InvokeUtils.invoke(TYPE1_I2.getGetterHandle(), instance));
        };

        SecureRandom random = new SecureRandom();
        Type1 instance = new Type1();
        Integer val = random.nextInt();
        TYPE1_I2.accept(instance, val);
        checkAssignment.accept(instance, val);

        val = random.nextInt();
        TYPE1_I2.getSetter().accept(instance, val);
        checkAssignment.accept(instance, val);

        val = random.nextInt();
        TYPE1_I2.getSetterHandle().invoke(instance, val);
        checkAssignment.accept(instance, val);
    }

    @Test
    public void testSetterFinal() throws Throwable {
        // this can only be tested on java 8, as the other versions have different behavior
        if (TestUtils.isJava8()) {
            Assertions.assertNull(TYPE1_I1.getSetterHandle());
            Assertions.assertThrows(IllegalStateException.class, ()-> TYPE1_I1.getSetter());
            Assertions.assertThrows(IllegalStateException.class, ()-> TYPE1_I1.accept(new Type1(), "some"));
        } else {
            Assertions.assertNotNull(TYPE1_I1.getSetterHandle());
            Type1 instance = new Type1();
            Assertions.assertThrows(UnsupportedOperationException.class, ()-> TYPE1_I1.accept(instance, "some"));
            Assertions.assertThrows(UnsupportedOperationException.class, ()-> TYPE1_I1.getSetter().accept(instance, "some"));
            Assertions.assertThrows(UnsupportedOperationException.class, ()-> TYPE1_I1.getSetterHandle().invoke(instance, "some"));
        }
    }

    @Test
    public void testSetterFinalAccess() throws Throwable {
        // final fields CAN be set when they are accessed through the full access lookup
        BiConsumer<Type1, String> checkAssignment = (instance, check) -> {
            Assertions.assertSame(check, TYPE1_I1.apply(instance));
            Assertions.assertSame(check, instance.I1);
            Assertions.assertSame(check, TYPE1_I1.getGetter().apply(instance));
            Assertions.assertSame(check, InvokeUtils.invoke(TYPE1_I1.getGetterHandle(), instance));
        };

        Type1 instance = new Type1();
        String val = RandomStringUtils.random(5);
        TYPE1_I1_2.accept(instance, val);
        checkAssignment.accept(instance, val);

        val = RandomStringUtils.random(5);
        TYPE1_I1_2.getSetter().accept(instance, val);
        checkAssignment.accept(instance, val);

        val = RandomStringUtils.random(5);
        TYPE1_I1_2.getSetterHandle().invoke(instance, val);
        checkAssignment.accept(instance, val);
    }

    @Test
    public void testSetterInvalid() {
        InvokeFieldInstance<Type1, Long> invalid1 = TYPE1_I2.asInstance();
        Type1 instance = new Type1();
        Assertions.assertThrows(NullPointerException.class, ()-> TYPE1_I2.accept(null, null));
        Assertions.assertThrows(NullPointerException.class, ()-> TYPE1_I2.accept(instance, null));
        Assertions.assertThrows(ClassCastException.class, ()-> invalid1.accept(instance, 5l));
    }
}
