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

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import net.kemuri9.invoke.InvokeExecutable;
import net.kemuri9.invoke.InvokeField;
import net.kemuri9.invoke.InvokeUtils;
import net.kemuri9.invoke.UnreflectToMethodHandle;
import net.kemuri9.invoke.test.meta.ExecutableMeta;
import net.kemuri9.invoke.test.meta.FieldMeta;
import net.kemuri9.invoke.test.meta.MemberMeta;
import net.kemuri9.invoke.test.meta.Meta;
import net.kemuri9.invoke.test.meta.TypeMeta;

import test.IFace1;
import test.IFace2;
import test.LookupFactory;
import test.Type1;
import test.Type2;

public class InvokeUtilsTest {

    private static class InvokeUtilsDerived extends InvokeUtils {}

    private static final MethodHandles.Lookup MY_LOOKUP = MethodHandles.lookup();

    private static <T extends Member> Map<String, List<T>> asMap(List<T> members, boolean includeClass) {
        Function<T, String> mapper = includeClass ? (f -> f.getDeclaringClass().getName() + "." + f.getName()) : Member::getName;
        return members.stream().collect(Collectors.groupingBy(mapper));
    }

    private static Predicate<? super MemberMeta> getParentFilter(MethodHandles.Lookup lookup) {
        return (lookup == InvokeUtils.getFullAccessLookup()) ? null : (m -> !m.isPrivate());
    }

    public static Stream<Arguments> getFullAccessLookups() {
        return Stream.of(
                Arguments.of(InvokeUtils.getFullAccessLookup(), Type1.class),
                Arguments.of(Type1.lookup(), Type1.class),
                Arguments.of(InvokeUtils.getFullAccessLookup(), Type2.class),
                Arguments.of(Type2.lookup(), Type2.class),
                Arguments.of(InvokeUtils.getFullAccessLookup(), IFace1.class),
                Arguments.of(InvokeUtils.getFullAccessLookup(), IFace2.class)
           );
    }

    public static Stream<Arguments> getLookups() {
        return Stream.of(
                Arguments.of(new Object[] { null }),
                Arguments.of(MethodHandles.publicLookup())
            );
    }

    public static Stream<Arguments> getPublicAccessLookups() {
        return Stream.of(
                // having a lookup that is in a different package is pretty much the same as a standard public lookup
                Arguments.of(MY_LOOKUP, Type1.class),
                Arguments.of(MethodHandles.publicLookup(), Type1.class),
                // null is the same as using the default, which is the public lookup
                Arguments.of(null, Type1.class),
                Arguments.of(MY_LOOKUP, Type2.class),
                Arguments.of(MethodHandles.publicLookup(), Type2.class),
                Arguments.of(null, Type2.class),
                Arguments.of(null, IFace1.class),
                Arguments.of(null, IFace2.class)
            );
    }

    public static Stream<Arguments> getTypes() {
        return Stream.of(
                Arguments.of(Type1.class),
                Arguments.of(Type2.class),
                Arguments.of(IFace1.class),
                Arguments.of(IFace2.class)
            );
    }



    private static final Set<String> INTRINSIFIED_METHODS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "java.lang.Object.clone", "java.lang.Object.finalize",
            "java.lang.Object.getClass", "java.lang.Object.hashCode",
            "java.lang.Object.notify", "java.lang.Object.notifyAll",
            "java.lang.Object.wait")));


    @BeforeEach
    public void beforeEach() {
        InvokeUtils.setDefaultLookup(null);
    }

    private <T> void checkCtors(List<InvokeExecutable<T>> ctors, List<ExecutableMeta> meta) {
        Assertions.assertEquals(ctors.size(), meta.size());
        for (ExecutableMeta cMeta : meta) {
            boolean found = false;
            for (InvokeExecutable<T> ctor : ctors) {
                found |= Arrays.equals(cMeta.argTypes, ctor.getType().parameterArray());
            }
            Assertions.assertTrue(found, "unable to find ctor for meta " + cMeta);
        }
    }

    private void checkFields(List<InvokeField> fields, List<FieldMeta> meta, boolean includeClass) {

        Assertions.assertEquals(meta.size(), fields.size());
        Assertions.assertEquals(meta.stream().filter(FieldMeta::isStatic).count(),
                fields.stream().filter(f -> Modifier.isStatic(f.getModifiers())).count());
        Assertions.assertEquals(meta.stream().filter(FieldMeta::isInstance).count(),
                fields.stream().filter(f -> !Modifier.isStatic(f.getModifiers())).count());

        Map<String, List<InvokeField>> fieldMap = asMap(fields, includeClass);
        for (FieldMeta fMeta : meta) {
            String name = fMeta.getName(includeClass);
            Assertions.assertTrue(fieldMap.containsKey(name), ()-> fieldMap.keySet() + " does not contain field " + name);
        }

        for (InvokeField field : fields) {
            // getter should always be available
            Assertions.assertNotNull(field.getGetterHandle());
            // but final fields will not have setters in java 8
            if (!TestUtils.isJava8() || !Modifier.isFinal(field.getModifiers())) {
                Assertions.assertNotNull(field.getSetterHandle());
            }
        }
    }

    private void checkMethods(Class<?> lookupClass, List<InvokeExecutable<?>> methods,
            List<ExecutableMeta> meta, boolean includeClass) {
        Assertions.assertEquals(meta.size(), methods.size(), ()-> "expected " + meta + " but found " + methods);
        Map<String, List<InvokeExecutable<?>>> map = asMap(methods, includeClass);

        Assertions.assertEquals(meta.stream().filter(MemberMeta::isStatic).count(),
                methods.stream().filter(f -> Modifier.isStatic(f.getModifiers())).count());
        Assertions.assertEquals(meta.stream().filter(MemberMeta::isInstance).count(),
                methods.stream().filter(f -> !Modifier.isStatic(f.getModifiers())).count());

        for (ExecutableMeta mMeta : meta) {
            String name = mMeta.getName(includeClass);
            Assertions.assertTrue(map.containsKey(name), ()-> map.keySet() + " does not contain method " + name);
            List<InvokeExecutable<?>> methodList = map.get(name);
            Class<?>[] searchArgs = mMeta.getArgTypes();
            List<List<Class<?>>> types = methodList.stream().map(m -> m.getType().parameterList()).collect(Collectors.toList());
            InvokeExecutable<?> method = methodList.stream()
                    .filter(e -> Arrays.equals(searchArgs, e.getType().parameterArray())).findFirst().orElse(null);
            // in most JVMs, a number of methods on Object has been intrinsified to where it is specific to each class.
            if (method == null && INTRINSIFIED_METHODS.contains(name)) {
                searchArgs[0] = lookupClass;
                method = methodList.stream()
                        .filter(e -> Arrays.equals(searchArgs, e.getType().parameterArray())).findFirst().orElse(null);
            }
            Assertions.assertNotNull(method, ()-> "unable to find method " + name + " with arguments " +
                    Arrays.asList(searchArgs) + " among " + types);
            Assertions.assertEquals(mMeta.modifiers, method.getModifiers(), "found " + method + " but expected " + mMeta);
            Assertions.assertNotNull(method.getHandle());
        }
    }

    private void checkNestedTypes(List<Member> nestedTypes, List<MemberMeta> meta, boolean includeClass) {

        Assertions.assertEquals(meta.size(), nestedTypes.size(), ()-> "expected " + meta + " but found " + nestedTypes);
        Assertions.assertEquals(meta.stream().filter(MemberMeta::isStatic).count(),
                nestedTypes.stream().filter(f -> Modifier.isStatic(f.getModifiers())).count());
        Assertions.assertEquals(meta.stream().filter(MemberMeta::isInstance).count(),
                nestedTypes.stream().filter(f -> !Modifier.isStatic(f.getModifiers())).count());

        Map<String, List<Member>> map = asMap(nestedTypes, includeClass);
        for (MemberMeta mMeta : meta) {
            String name = mMeta.getName(includeClass);
            Assertions.assertTrue(map.containsKey(name), ()-> map.keySet() + " does not contain nested type " + name);
            List<Member> nTypes = map.get(name);
            Member nType = nTypes.stream().filter(t -> t.getModifiers() == mMeta.getModifiers()).findAny().orElse(null);
            Assertions.assertNotNull(nType, ()-> "could not find " + name + " with modifiers " + mMeta.getModifiers()
                + " among " + nTypes);
        }
    }

    @Test
    public void testCanDerive() {
        Assertions.assertTrue(new InvokeUtilsDerived() instanceof InvokeUtils);
    }

    @ParameterizedTest(name = "testGetConstructorFullAccess - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getFullAccessLookups")
    public <T> void testGetConstructorFullAccess(MethodHandles.Lookup lookup, Class<T> type)
            throws NoSuchMethodException, SecurityException, IllegalAccessException {
        List<InvokeExecutable<T>> ctors = new ArrayList<>();
        TypeMeta typeMeta = Meta.getMeta(type);
        for (ExecutableMeta meta : typeMeta.constructors) {
            @SuppressWarnings("unchecked")
            Constructor<T> ctor = (Constructor<T>) typeMeta.type.getDeclaredConstructor(meta.argTypes);
            ctors.add(InvokeUtils.getConstructor(lookup, ctor));
        }
        checkCtors(ctors, typeMeta.constructors);
    }

    @ParameterizedTest(name = "testGetConstructorNull - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getLookups")
    public void testGetConstructorNull(MethodHandles.Lookup lookup) {
        Assertions.assertThrows(IllegalArgumentException.class, ()-> InvokeUtils.getConstructor(lookup, null));
    }

    @ParameterizedTest(name = "testGetConstructorPublicAccess - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getPublicAccessLookups")
    public <T> void testGetConstructorPublicAccess(MethodHandles.Lookup lookup, Class<T> type)
            throws NoSuchMethodException, SecurityException {
        List<InvokeExecutable<T>> ctors = new ArrayList<>();
        TypeMeta typeMeta = Meta.getMeta(type);
        for (ExecutableMeta meta : typeMeta.constructors) {
            @SuppressWarnings("unchecked")
            Constructor<T> ctor = (Constructor<T>) typeMeta.type.getDeclaredConstructor(meta.argTypes);
            try {
                ctors.add(InvokeUtils.getConstructor(lookup, ctor));
                Assertions.assertTrue(meta.isPublic(), "can access public constructor when should not be able to");
            } catch (IllegalAccessException ex) {
                Assertions.assertFalse(meta.isPublic(), "cannot access public constructor when should be able to");
            }
        }

        List<ExecutableMeta> expectedCtors = typeMeta.getPublicConstructors();
        checkCtors(ctors, expectedCtors);
    }

    @ParameterizedTest(name = "testGetConstructorsPublicAccess - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getPublicAccessLookups")
    public <T> void testGetConstructorsPublicAccess(MethodHandles.Lookup lookup, Class<T> type) {
        List<InvokeExecutable<T>> ctors = InvokeUtils.getConstructors(lookup, type);
        Assertions.assertNotNull(ctors);
        ctors.removeIf(Member::isSynthetic);

        List<ExecutableMeta> expectedCtors = Meta.getMeta(type).getPublicConstructors();
        checkCtors(ctors, expectedCtors);
    }

    @ParameterizedTest(name = "testGetConstructorsPackageAccess - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getTypes")
    public <T> void testGetConstructorsPackageAccess(Class<T> type) {
        List<InvokeExecutable<T>> ctors = InvokeUtils.getConstructors(LookupFactory.lookup(), type);
        Assertions.assertNotNull(ctors);
        ctors.removeIf(Member::isSynthetic);

        List<ExecutableMeta> expectedCtors = Meta.getMeta(type).constructors.stream()
                .filter(f -> !f.isPrivate()).collect(Collectors.toList());
        checkCtors(ctors, expectedCtors);
    }

    @ParameterizedTest(name = "testGetConstructorsFullAccess - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getFullAccessLookups")
    public <T> void testGetConstructorsFullAccess(MethodHandles.Lookup lookup, Class<T> type) {
        List<InvokeExecutable<T>> ctors = InvokeUtils.getConstructors(lookup, type);
        Assertions.assertNotNull(ctors);
        ctors.removeIf(Member::isSynthetic);
        checkCtors(ctors, Meta.getMeta(type).constructors);
    }

    @ParameterizedTest(name = "testGetConstructorsNull - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getLookups")
    public void testGetConstructorsNull(MethodHandles.Lookup lookup) {
        Assertions.assertThrows(IllegalArgumentException.class, ()-> InvokeUtils.getConstructors(lookup, null));
    }

    @ParameterizedTest(name = "testGetFieldFullAccess - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getFullAccessLookups")
    public void testGetFieldFullAccess(MethodHandles.Lookup lookup, Class<?> type)
            throws NoSuchFieldException, SecurityException, IllegalAccessException {
        List<InvokeField> fields = new ArrayList<>();
        TypeMeta typeMeta = Meta.getMeta(type);
        for (FieldMeta field : typeMeta.fields) {
            fields.add(InvokeUtils.getField(lookup, typeMeta.type.getDeclaredField(field.name)));
        }
        checkFields(fields, typeMeta.fields, false);
    }

    @ParameterizedTest(name = "testGetFieldNull - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getLookups")
    public void testGetFieldNull(MethodHandles.Lookup lookup) {
        Assertions.assertThrows(IllegalArgumentException.class, ()-> InvokeUtils.getField(lookup, null));
    }

    @ParameterizedTest(name = "testGetFieldPackageAccess - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getTypes")
    public void testGetFieldPackageAccess(Class<?> type) throws NoSuchFieldException, SecurityException {
        List<InvokeField> fields = new ArrayList<>();
        TypeMeta typeMeta = Meta.getMeta(type);
        MethodHandles.Lookup lookup = LookupFactory.lookup();
        for (FieldMeta field : typeMeta.fields) {
            try {
                fields.add(InvokeUtils.getField(lookup, typeMeta.type.getDeclaredField(field.name)));
                Assertions.assertFalse(field.isPrivate());
            } catch (IllegalAccessException ex) {
                Assertions.assertTrue(field.isPrivate());
            }
        }
        List<FieldMeta> expectedFields = typeMeta.fields.stream().filter(f -> !f.isPrivate()).collect(Collectors.toList());
        checkFields(fields, expectedFields, false);
    }

    @ParameterizedTest(name = "testGetFieldPublicAccess - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getPublicAccessLookups")
    public void testGetFieldPublicAccess(MethodHandles.Lookup lookup, Class<?> type)
            throws NoSuchFieldException, SecurityException {
        List<InvokeField> fields = new ArrayList<>();
        TypeMeta typeMeta = Meta.getMeta(type);
        for (FieldMeta field : typeMeta.fields) {
            try {
                fields.add(InvokeUtils.getField(lookup, typeMeta.type.getDeclaredField(field.name)));
                Assertions.assertTrue(field.isPublic());
            } catch (IllegalAccessException ex) {
                Assertions.assertFalse(field.isPublic());
            }
        }
        List<FieldMeta> expectedFields = typeMeta.getPublicFields();
        checkFields(fields, expectedFields, false);
    }

    @ParameterizedTest(name = "testGetFieldsFullAccess - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getFullAccessLookups")
    public void testGetFieldsFullAccess(MethodHandles.Lookup lookup, Class<?> type) {
        List<InvokeField> fields = InvokeUtils.getFields(lookup, type, false);
        Assertions.assertNotNull(fields);
        TypeMeta typeMeta = Meta.getMeta(type);
        fields.removeIf(Member::isSynthetic);
        checkFields(fields, typeMeta.fields, false);
    }

    @ParameterizedTest(name = "testGetFieldsNull - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getLookups")
    public void testGetFieldsNull(MethodHandles.Lookup lookup) {
        Assertions.assertThrows(IllegalArgumentException.class, ()-> InvokeUtils.getFields(lookup, null, false));
    }

    @ParameterizedTest(name = "testGetFieldsPackageAccess - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getTypes")
    public void testGetFieldsPackageAccess(Class<?> type) {
        List<InvokeField> fields = InvokeUtils.getFields(LookupFactory.lookup(), type, false);
        Assertions.assertNotNull(fields);
        fields.removeIf(Member::isSynthetic);

        List<FieldMeta> expectedFields = Meta.getMeta(type).fields.stream()
                .filter(f -> !f.isPrivate()).collect(Collectors.toList());
        checkFields(fields, expectedFields, false);
    }

    @ParameterizedTest(name = "testGetFieldsPackageAccessInheritance - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getTypes")
    public void testGetFieldsPackageAccessInheritance(Class<?> type) {
        List<InvokeField> fields = InvokeUtils.getFields(LookupFactory.lookup(), type, true);
        Assertions.assertNotNull(fields);
        fields.removeIf(Member::isSynthetic);

        List<FieldMeta> expectedFields = Meta.getMeta(type, true, null).fields.stream()
                .filter(f -> !f.isPrivate()).collect(Collectors.toList());
        checkFields(fields, expectedFields, true);
    }

    @ParameterizedTest(name = "testGetFieldsPublicAccess - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getPublicAccessLookups")
    public void testGetFieldsPublicAccess(MethodHandles.Lookup lookup, Class<?> type) {
        List<InvokeField> fields = InvokeUtils.getFields(lookup, type, false);
        Assertions.assertNotNull(fields);
        fields.removeIf(Member::isSynthetic);

        List<FieldMeta> expectedFields = Meta.getMeta(type).getPublicFields();
        checkFields(fields, expectedFields, false);
    }

    @ParameterizedTest(name = "testGetFieldsPublicAccessInheritance - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getPublicAccessLookups")
    public void testGetFieldsPublicAccessInheritance(MethodHandles.Lookup lookup, Class<?> type) {
        List<InvokeField> fields = InvokeUtils.getFields(lookup, type, true);
        Assertions.assertNotNull(fields);
        fields.removeIf(Member::isSynthetic);

        List<FieldMeta> expectedFields = Meta.getMeta(type, true, MemberMeta::isPublic).getPublicFields();
        checkFields(fields, expectedFields, true);
    }

    @ParameterizedTest(name = "testGetMethodFullAccess - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getFullAccessLookups")
    public void testGetMethodFullAccess(MethodHandles.Lookup lookup, Class<?> type)
            throws NoSuchMethodException, SecurityException, IllegalAccessException {
        List<InvokeExecutable<?>> methods = new ArrayList<>();
        TypeMeta typeMeta = Meta.getMeta(type);
        for (ExecutableMeta method : typeMeta.methods) {
            methods.add(InvokeUtils.getMethod(lookup, typeMeta.type.getDeclaredMethod(method.name, method.argTypes)));
        }
        List<ExecutableMeta> expectedMethods = typeMeta.methods;
        checkMethods(type, methods, expectedMethods, false);
    }

    @ParameterizedTest(name = "testGetMethodNull - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getLookups")
    public void testGetMethodNull(MethodHandles.Lookup lookup) {
        Assertions.assertThrows(IllegalArgumentException.class, ()-> InvokeUtils.getMethod(lookup, null));
    }

    @ParameterizedTest(name = "testGetMethodPublicAccess - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getPublicAccessLookups")
    public void testGetMethodPublicAccess(MethodHandles.Lookup lookup, Class<?> type)
            throws NoSuchMethodException, SecurityException {
        List<InvokeExecutable<?>> methods = new ArrayList<>();
        TypeMeta typeMeta = Meta.getMeta(type);
        for (ExecutableMeta method : typeMeta.methods) {
            try {
                methods.add(InvokeUtils.getMethod(lookup, typeMeta.type.getDeclaredMethod(method.name, method.argTypes)));
                Assertions.assertTrue(method.isPublic());
            } catch (IllegalAccessException ex) {
                Assertions.assertFalse(method.isPublic());
            }
        }

        List<ExecutableMeta> expectedMethods = typeMeta.getPublicMethods();
        checkMethods(type, methods, expectedMethods, false);
    }

    @ParameterizedTest(name = "testGetMethodsFullAccess - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getFullAccessLookups")
    public void testGetMethodsFullAccess(MethodHandles.Lookup lookup, Class<?> type) {
        List<InvokeExecutable<?>> methods = InvokeUtils.getMethods(lookup, type, false);
        Assertions.assertNotNull(methods);
        methods.removeIf(Member::isSynthetic);
        checkMethods(type, methods, Meta.getMeta(type).methods, false);
    }

    @ParameterizedTest(name = "testGetMethodsFullAccessInheritance - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getFullAccessLookups")
    public void testGetMethodsFullAccessInheritance(MethodHandles.Lookup lookup, Class<?> type) {
        List<InvokeExecutable<?>> methods = InvokeUtils.getMethods(lookup, type, true);
        Assertions.assertNotNull(methods);
        methods.removeIf(Member::isSynthetic);
        Predicate<? super MemberMeta> filter = getParentFilter(lookup);
        checkMethods(type, methods, Meta.getMeta(type, true, filter).methods, true);
    }

    @ParameterizedTest(name = "testGetMethodsNull - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getLookups")
    public void testGetMethodsNull(MethodHandles.Lookup lookup) {
        Assertions.assertThrows(IllegalArgumentException.class, ()-> InvokeUtils.getMethods(lookup, null, false));
    }

    @ParameterizedTest(name = "testGetMethodsPackageAccess - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getTypes")
    public void testGetMethodsPackageAccess(Class<?> type) {
        List<InvokeExecutable<?>> methods = InvokeUtils.getMethods(LookupFactory.lookup(), type, false);
        Assertions.assertNotNull(methods);
        methods.removeIf(Member::isSynthetic);

        List<ExecutableMeta> expectedMethods = Meta.getMeta(type).methods.stream()
                .filter(f -> !f.isPrivate()).collect(Collectors.toList());
        checkMethods(type, methods, expectedMethods, false);
    }

    @ParameterizedTest(name = "testGetMethodsPackageAccessInheritance - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getTypes")
    public void testGetMethodsPackageAccessInheritance(Class<?> type) {
        MethodHandles.Lookup lookup = LookupFactory.lookup();
        List<InvokeExecutable<?>> methods = InvokeUtils.getMethods(lookup, type, true);
        Assertions.assertNotNull(methods);
        methods.removeIf(Member::isSynthetic);

        List<ExecutableMeta> expectedMethods = Meta.getMeta(type, true, null).methods.stream()
                .filter(f -> !f.isPrivate()).collect(Collectors.toList());
        checkMethods(lookup.lookupClass(), methods, expectedMethods, true);
    }

    @ParameterizedTest(name = "testGetMethodsPublicAccess - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getPublicAccessLookups")
    public void testGetMethodsPublicAccess(MethodHandles.Lookup lookup, Class<?> type) {
        List<InvokeExecutable<?>> methods = InvokeUtils.getMethods(lookup, type, false);
        Assertions.assertNotNull(methods);
        methods.removeIf(Member::isSynthetic);

        List<ExecutableMeta> expectedMethods = Meta.getMeta(type).getPublicMethods();
        checkMethods(type, methods, expectedMethods, false);
    }

    @ParameterizedTest(name = "testGetMethodsPublicAccessInheritance - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getPublicAccessLookups")
    public void testGetMethodsPublicAccessInheritance(MethodHandles.Lookup lookup, Class<?> type) {
        List<InvokeExecutable<?>> methods = InvokeUtils.getMethods(lookup, type, true);
        Assertions.assertNotNull(methods);
        methods.removeIf(Member::isSynthetic);

        MethodHandles.Lookup lookup2 = lookup;
        if (lookup2 == null) {
            lookup2 = InvokeUtils.getDefaultLookup();
        }
        List<ExecutableMeta> expectedMethods = Meta.getMeta(type, true, MemberMeta::isPublic).getPublicMethods();
        if (lookup2 != MethodHandles.publicLookup()) {
            // as a derivative of Object, the protected methods on Object are also accessible
            Meta.getMeta(Object.class).methods.stream().filter(MemberMeta::isProtected).forEach(expectedMethods::add);
        }
        Class<?> lookupType = lookup2.lookupClass();
        checkMethods(lookupType, methods, expectedMethods, true);
    }

    @ParameterizedTest(name = "testGetNestedTypesFullAccess - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getFullAccessLookups")
    public void testGetNestedTypesFullAccess(MethodHandles.Lookup lookup, Class<?> type) {
        List<Member> types = InvokeUtils.getNestedTypes(lookup, type, false);
        Assertions.assertNotNull(types);
        types.removeIf(Member::isSynthetic);
        checkNestedTypes(types, Meta.getMeta(type).nestedTypes, false);
    }

    @ParameterizedTest(name = "testGetNestedTypesFullAccessInheritance - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getFullAccessLookups")
    public void testGetNestedTypesFullAccessInheritance(MethodHandles.Lookup lookup, Class<?> type) {
        List<Member> types = InvokeUtils.getNestedTypes(lookup, type, true);
        Assertions.assertNotNull(types);
        types.removeIf(Member::isSynthetic);
        checkNestedTypes(types, Meta.getMeta(type, true, null).nestedTypes, false);
    }

    @ParameterizedTest(name = "testNestedTypesNull - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getLookups")
    public void testGetNestedTypesNull(MethodHandles.Lookup lookup) {
        Assertions.assertThrows(IllegalArgumentException.class, ()-> InvokeUtils.getNestedTypes(lookup, null, false));
    }

    @ParameterizedTest(name = "testGetNestedTypesPackageAccess - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getTypes")
    public void testGetNestedTypesPackageAccess(Class<?> type) {
        List<Member> nestedTypes = InvokeUtils.getNestedTypes(LookupFactory.lookup(), type, false);
        Assertions.assertNotNull(nestedTypes);
        nestedTypes.removeIf(Member::isSynthetic);

        // types within the package have access to private member types apparently
        List<MemberMeta> expected = Meta.getMeta(type).nestedTypes;
        checkNestedTypes(nestedTypes, expected, false);
    }

    @ParameterizedTest(name = "testGetNestedTypesPackageAccessInheritance - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getTypes")
    public void testGetNestedTypesPackageAccessInheritance(Class<?> type) {
        List<Member> nestedTypes = InvokeUtils.getNestedTypes(LookupFactory.lookup(), type, true);
        Assertions.assertNotNull(nestedTypes);
        nestedTypes.removeIf(Member::isSynthetic);

        // types within the package have access to private member types apparently
        List<MemberMeta> expected = Meta.getMeta(type, true, null).nestedTypes;
        checkNestedTypes(nestedTypes, expected, true);
    }

    @ParameterizedTest(name = "testGetNestedTypesPublicAccess - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getPublicAccessLookups")
    public void testGetNestedTypesPublicAccess(MethodHandles.Lookup lookup, Class<?> type) {
        List<Member> types = InvokeUtils.getNestedTypes(lookup, type, false);
        Assertions.assertNotNull(types);
        types.removeIf(Member::isSynthetic);

        // public and protected member types are publicly accessible
        List<MemberMeta> expectedTypes = Meta.getMeta(type).nestedTypes.stream()
                .filter((nt)-> nt.isPublic() || nt.isProtected()).collect(Collectors.toList());
        checkNestedTypes(types, expectedTypes, false);
    }

    @ParameterizedTest(name = "testGetNestedTypesPublicAccessInheritance - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getPublicAccessLookups")
    public void testGetNestedTypesPublicAccessInheritance(MethodHandles.Lookup lookup, Class<?> type) {
        List<Member> types = InvokeUtils.getNestedTypes(lookup, type, true);
        Assertions.assertNotNull(types);
        types.removeIf(Member::isSynthetic);

        // public and protected member types are publicly accessible
        List<MemberMeta> expectedTypes = Meta.getMeta(type, true, null).nestedTypes.stream()
                .filter((nt)-> nt.isPublic() || nt.isProtected()).collect(Collectors.toList());
        checkNestedTypes(types, expectedTypes, true);
    }

    @Test
    public void testInvokeHandleNull() {
        Assertions.assertThrows(IllegalArgumentException.class, ()-> InvokeUtils.invoke(null));
    }

    @Test
    public void testInvokeIOException() throws NoSuchMethodException, IllegalAccessException {
        RuntimeException ex = Assertions.assertThrows(RuntimeException.class,
                ()-> InvokeUtils.invoke(TestUtils.HANDLE_THROW_IO_EX));
        Assertions.assertTrue(ex.getCause() instanceof IOException);
    }

    @Test
    public void testInvokeUnsupportedOperationException() throws NoSuchMethodException, IllegalAccessException {
        Assertions.assertThrows(UnsupportedOperationException.class,
                ()-> InvokeUtils.invoke(TestUtils.HANDLE_THROW_UNSUPPORTED_EX));
    }

    @Test
    public void testInvokeQuietly() throws NoSuchMethodException, IllegalAccessException {
        Assertions.assertNull(InvokeUtils.invokeQuietly(null));
        Assertions.assertNull(InvokeUtils.invokeQuietly(TestUtils.HANDLE_THROW_IO_EX));
        Assertions.assertNull(InvokeUtils.invokeQuietly(TestUtils.HANDLE_THROW_UNSUPPORTED_EX));
        MethodHandle handle = MethodHandles.publicLookup()
                .findStatic(getClass(), "getTypes", MethodType.methodType(Stream.class));
        Stream<Arguments> typeArgs = InvokeUtils.invokeQuietly(handle);
        Assertions.assertNotNull(typeArgs);
        Assertions.assertTrue(typeArgs instanceof Stream);
        Function<Stream<Arguments>, List<Object>> flatten = (stream)-> stream.map(Arguments::get).map(Arrays::asList)
                .flatMap(List::stream).collect(Collectors.toList());
        Assertions.assertEquals(flatten.apply(getTypes()), flatten.apply(typeArgs));
    }

    @Test
    public void testSetDefaultLookup() {
        Assertions.assertSame(MethodHandles.publicLookup(), InvokeUtils.getDefaultLookup());

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        InvokeUtils.setDefaultLookup(lookup);
        Assertions.assertSame(lookup, InvokeUtils.getDefaultLookup());
    }

    @ParameterizedTest(name = "testUnreflectElementNull - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getLookups")
    public void testUnreflectElementNull(MethodHandles.Lookup lookup) throws IllegalAccessException {
        Assertions.assertNull(InvokeUtils.unreflect(lookup, null, UnreflectToMethodHandle.FIELD_SETTER));
    }

    @ParameterizedTest(name = "testUnreflectUnreflectorNull - " + ParameterizedTest.DEFAULT_DISPLAY_NAME)
    @MethodSource(value = "net.kemuri9.invoke.test.InvokeUtilsTest#getLookups")
    public void testUnreflectUnreflectorNull(MethodHandles.Lookup lookup)
            throws IllegalAccessException, NoSuchFieldException, SecurityException {
        Field field = Type1.class.getField("S1");
        Assertions.assertThrows(IllegalArgumentException.class, ()-> InvokeUtils.unreflect(lookup, field, null));
    }
}
