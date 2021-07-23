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
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import net.kemuri9.invoke.InvokeExecutable;
import net.kemuri9.invoke.InvokeUtils;

import test.Type1;

public class InvokeExecutableImplTest {

    public static final Long foo() {
        return null;
    }

    private static InvokeExecutable<?> getInvokeExecutable(Executable executable) throws IllegalAccessException {
        return (executable instanceof Method)
                ? InvokeUtils.getMethod(MethodHandles.lookup(), (Method) executable)
                : InvokeUtils.getConstructor(MethodHandles.lookup(), (Constructor<?>) executable);
    }

    public static Stream<Arguments> getExecutables() {
        return Stream.of(
            Arguments.of(CTOR),
            Arguments.of(METHOD_FOO),
            Arguments.of(METHOD_RETURN_INT_VALUE)
        );
    }

    public static Integer returnIntValue(Integer val) {
        return val;
    }

    private static final Constructor<InvokeExecutableImplTest> CTOR;
    private static final Method METHOD_FOO;
    private static final Method METHOD_RETURN_INT_VALUE;

    static {
        try {
            Class<InvokeExecutableImplTest> type = InvokeExecutableImplTest.class;
            CTOR = type.getConstructor();
            METHOD_FOO = type.getDeclaredMethod("foo");
            METHOD_RETURN_INT_VALUE = type.getDeclaredMethod("returnIntValue", Integer.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testApply() throws Exception {
        InvokeExecutable<Integer> exec = InvokeUtils.getMethod(MethodHandles.lookup(), METHOD_RETURN_INT_VALUE);
        SecureRandom rand = new SecureRandom();
        IntStream.range(0, 5).forEach((unused)-> {
            Integer value = rand.nextInt();
            Integer ret = exec.apply(new Object[] { value });
            Assertions.assertSame(value, ret);
        });
        Assertions.assertNull(exec.apply(new Object[] { null }));
    }

    @Test
    public void testApply2() throws Exception {
        InvokeExecutable<InvokeExecutableImplTest> exec = InvokeUtils.getConstructor(MethodHandles.lookup(), CTOR);
        Assertions.assertTrue(exec.apply(new Object[0]) instanceof InvokeExecutableImplTest);
    }

    @Test
    public void testApplyInvalid() throws Exception {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        InvokeExecutable<Integer> exec = InvokeUtils.getMethod(lookup, METHOD_RETURN_INT_VALUE);
        Assertions.assertThrows(WrongMethodTypeException.class, ()-> exec.apply(null));
        Assertions.assertThrows(WrongMethodTypeException.class, ()-> exec.apply(new Object[0]));
        Assertions.assertThrows(WrongMethodTypeException.class, ()-> exec.apply(new Object[] { 2, null }));
        Assertions.assertThrows(ClassCastException.class, ()-> exec.apply(new Object[] { 5l }));

        InvokeExecutable<?> exec2 = InvokeUtils.getMethod(lookup, TestUtils.METHOD_THROW_IO_EX);
        Assertions.assertThrows(RuntimeException.class, ()-> exec2.apply(new Object[0]));

        InvokeExecutable<?> exec3 = InvokeUtils.getMethod(lookup, TestUtils.METHOD_THROW_UNSUPPORTED_EX);
        Assertions.assertThrows(UnsupportedOperationException.class, ()-> exec3.apply(new Object[0]));
    }

    @ParameterizedTest(name = "testAttributes - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeExecutableImplTest#getExecutables")
    public void testAttributes(Executable executable) throws Exception {
        InvokeExecutable<?> exec = getInvokeExecutable(executable);
        Assertions.assertEquals(executable.getDeclaringClass(), exec.getDeclaringClass());
        Assertions.assertEquals(executable.getModifiers(), exec.getModifiers());
        Assertions.assertEquals(executable.getName(), exec.getName());
        Assertions.assertEquals(executable.isSynthetic(), exec.isSynthetic());
        Assertions.assertEquals(executable.toString(), exec.toString());
    }

    @Test
    public void testInvoke() throws Exception {
        InvokeExecutable<Integer> exec = InvokeUtils.getMethod(MethodHandles.lookup(), METHOD_RETURN_INT_VALUE);
        SecureRandom rand = new SecureRandom();
        IntStream.range(0, 5).forEach((unused)-> {
            Integer value = rand.nextInt();
            Integer ret = exec.invoke(value);
            Assertions.assertSame(value, ret);
        });
        Assertions.assertNull(exec.invoke(new Object[] { null }));
    }

    @Test
    public void testInvoke2() throws Exception {
        InvokeExecutable<InvokeExecutableImplTest> exec = InvokeUtils.getConstructor(MethodHandles.lookup(), CTOR);
        Assertions.assertTrue(exec.invoke() instanceof InvokeExecutableImplTest);
    }

    @Test
    public void testInvokeInvalid() throws Exception {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        InvokeExecutable<Integer> exec = InvokeUtils.getMethod(lookup, METHOD_RETURN_INT_VALUE);
        Assertions.assertThrows(WrongMethodTypeException.class, ()-> exec.invoke());
        Assertions.assertThrows(WrongMethodTypeException.class, ()-> exec.invoke(2, null));
        Assertions.assertThrows(ClassCastException.class, ()-> exec.invoke(5l));

        InvokeExecutable<?> exec2 = InvokeUtils.getMethod(lookup, TestUtils.METHOD_THROW_IO_EX);
        Assertions.assertThrows(RuntimeException.class, ()-> exec2.invoke());

        InvokeExecutable<?> exec3 = InvokeUtils.getMethod(lookup, TestUtils.METHOD_THROW_UNSUPPORTED_EX);
        Assertions.assertThrows(UnsupportedOperationException.class, ()-> exec3.invoke());
    }

    @ParameterizedTest(name = "testGetExecutable - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeExecutableImplTest#getExecutables")
    public void testGetExecutable(Executable executable) throws Exception {
        InvokeExecutable<?> exec = getInvokeExecutable(executable);
        Executable exec2 = exec.getExecutable(InvokeUtils.getFullAccessLookup());
        Assertions.assertEquals(executable, exec2);
    }

    @Test
    public void testGetExecutable2() throws Exception {
        List<InvokeExecutable<?>> execs = InvokeUtils.getMethods(InvokeUtils.getFullAccessLookup(), Type1.class, false);
        for (InvokeExecutable<?> exec : execs) {
            MethodHandles.Lookup lookup = null;
            if (!Modifier.isPublic(exec.getModifiers())) {
                Assertions.assertThrows(IllegalAccessException.class, ()-> exec.getExecutable(null));
                lookup = InvokeUtils.getFullAccessLookup();
            }

            Class<?>[] paramTypes = exec.getType().parameterArray();
            if (!Modifier.isStatic(exec.getModifiers())) {
                Class<?>[] paramTypes2 = new Class<?>[paramTypes.length-1];
                System.arraycopy(paramTypes, 1, paramTypes2, 0, paramTypes2.length);
                paramTypes = paramTypes2;
            }
            Method expected = exec.getDeclaringClass().getDeclaredMethod(exec.getName(), paramTypes);
            Executable actual = exec.getExecutable(lookup);
            Assertions.assertEquals(expected, actual);
        }
    }
}
