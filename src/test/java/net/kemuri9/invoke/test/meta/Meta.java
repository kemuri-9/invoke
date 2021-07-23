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
package net.kemuri9.invoke.test.meta;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import net.kemuri9.invoke.test.TestUtils;

import test.IFace1;
import test.IFace2;
import test.Type1;
import test.Type2;

public class Meta {

    public static final TypeMeta OBJECT;

    public static final TypeMeta IFACE1 = new TypeMeta(IFace1.class,
            null,
            Arrays.asList(
                    new FieldMeta("IFIELD1", Modifier.PUBLIC | Modifier.FINAL | Modifier.STATIC, Integer.class)
                ),
            Arrays.asList(
                    new ExecutableMeta("isdo1", Modifier.PUBLIC | Modifier.STATIC, boolean.class),
                    new ExecutableMeta("iido1", Modifier.PUBLIC | Modifier.ABSTRACT, boolean.class),
                    new ExecutableMeta("iido2", Modifier.PUBLIC, boolean.class)
                ),
            Arrays.asList(
                    new MemberMeta("Sub1", Modifier.PUBLIC | Modifier.STATIC | Modifier.INTERFACE | Modifier.ABSTRACT)
                )
            );

    public static final TypeMeta IFACE2 = new TypeMeta(IFace2.class,
            null,
            Arrays.asList(
                    new FieldMeta("IFIELD2", Modifier.PUBLIC | Modifier.FINAL | Modifier.STATIC, Integer.class)
                ),
            Arrays.asList(
                    new ExecutableMeta("isdo2", Modifier.PUBLIC | Modifier.STATIC, int.class),
                    new ExecutableMeta("iido3", Modifier.PUBLIC | Modifier.ABSTRACT, int.class),
                    new ExecutableMeta("iido4", Modifier.PUBLIC, int.class)
                ),
            Arrays.asList(
                    new MemberMeta("Sub2", Modifier.PUBLIC | Modifier.STATIC | Modifier.INTERFACE | Modifier.ABSTRACT)
                )
            );

    public static final TypeMeta TYPE1 = new TypeMeta(Type1.class,
            Arrays.asList(
                    new ExecutableMeta("<init>", Modifier.PUBLIC),
                    new ExecutableMeta("<init>", 0, String.class),
                    new ExecutableMeta("<init>", Modifier.PROTECTED, Integer.class),
                    new ExecutableMeta("<init>", Modifier.PRIVATE, boolean.class)
                ),
            Arrays.asList(
                    new FieldMeta("S1", Modifier.FINAL | Modifier.PUBLIC | Modifier.STATIC, String.class),
                    new FieldMeta("S2", Modifier.PUBLIC | Modifier.STATIC, String.class),
                    new FieldMeta("S3", Modifier.FINAL | Modifier.PRIVATE | Modifier.STATIC, Integer.class),
                    new FieldMeta("S4", Modifier.PRIVATE | Modifier.STATIC, Short.class),
                    new FieldMeta("S5", Modifier.FINAL | Modifier.STATIC, Boolean.class),
                    new FieldMeta("S6", Modifier.STATIC, Boolean.class),
                    new FieldMeta("S7", Modifier.FINAL | Modifier.PROTECTED | Modifier.STATIC, Byte.class),
                    new FieldMeta("S8", Modifier.PROTECTED | Modifier.STATIC, Byte.class),
                    new FieldMeta("I1", Modifier.FINAL | Modifier.PUBLIC, String.class),
                    new FieldMeta("I2", Modifier.PUBLIC, int.class),
                    new FieldMeta("I3", Modifier.FINAL | Modifier.PRIVATE, long.class),
                    new FieldMeta("I4", Modifier.PRIVATE, long.class),
                    new FieldMeta("I5", Modifier.FINAL, byte.class),
                    new FieldMeta("I6", 0, byte.class),
                    new FieldMeta("I7", Modifier.FINAL | Modifier.PROTECTED, short.class),
                    new FieldMeta("I8", Modifier.PROTECTED, short.class)
                ),
            Arrays.asList(
                    new ExecutableMeta("lookup", Modifier.PUBLIC | Modifier.STATIC),
                    new ExecutableMeta("sdo1", Modifier.PUBLIC | Modifier.STATIC, String.class),
                    new ExecutableMeta("sdo2", Modifier.PRIVATE | Modifier.STATIC, String.class),
                    new ExecutableMeta("sdo3", Modifier.STATIC, Long.class),
                    new ExecutableMeta("sdo4", Modifier.PROTECTED | Modifier.STATIC, Integer.class),
                    new ExecutableMeta("ido1", Modifier.PUBLIC, String.class),
                    new ExecutableMeta("ido2", Modifier.PRIVATE, byte.class),
                    new ExecutableMeta("ido3", 0, int.class),
                    new ExecutableMeta("ido4", Modifier.PROTECTED, Integer.class)
                ),
            Arrays.asList(
                    new MemberMeta("NS1", Modifier.PUBLIC | Modifier.STATIC),
                    new MemberMeta("NS2", Modifier.STATIC),
                    new MemberMeta("NS3", Modifier.PROTECTED | Modifier.STATIC),
                    new MemberMeta("NS4", Modifier.PRIVATE | Modifier.STATIC),
                    new MemberMeta("NI1", Modifier.PUBLIC),
                    new MemberMeta("NI2", 0),
                    new MemberMeta("NI3", Modifier.PROTECTED),
                    new MemberMeta("NI4", Modifier.PRIVATE)
                )
            );

    public static final TypeMeta TYPE2 = new TypeMeta(Type2.class,
            Arrays.asList(
                    new ExecutableMeta("<init>", Modifier.PUBLIC),
                    new ExecutableMeta("<init>", 0, String.class),
                    new ExecutableMeta("<init>", Modifier.PROTECTED, Integer.class),
                    new ExecutableMeta("<init>", Modifier.PRIVATE, boolean.class)
                ),
            Arrays.asList(
                    new FieldMeta("S9", Modifier.FINAL | Modifier.PUBLIC | Modifier.STATIC, String.class),
                    new FieldMeta("S10", Modifier.PUBLIC | Modifier.STATIC, String.class),
                    new FieldMeta("S11", Modifier.FINAL | Modifier.PRIVATE | Modifier.STATIC, Integer.class),
                    new FieldMeta("S12", Modifier.PRIVATE | Modifier.STATIC, Short.class),
                    new FieldMeta("S13", Modifier.FINAL | Modifier.STATIC, Boolean.class),
                    new FieldMeta("S14", Modifier.STATIC, Boolean.class),
                    new FieldMeta("S15", Modifier.FINAL | Modifier.PROTECTED | Modifier.STATIC, Byte.class),
                    new FieldMeta("S16", Modifier.PROTECTED | Modifier.STATIC, Byte.class),
                    new FieldMeta("I9", Modifier.FINAL | Modifier.PUBLIC, String.class),
                    new FieldMeta("I10", Modifier.PUBLIC, int.class),
                    new FieldMeta("I11", Modifier.FINAL | Modifier.PRIVATE, long.class),
                    new FieldMeta("I12", Modifier.PRIVATE, long.class),
                    new FieldMeta("I13", Modifier.FINAL, byte.class),
                    new FieldMeta("I14", 0, byte.class),
                    new FieldMeta("I15", Modifier.FINAL | Modifier.PROTECTED, short.class),
                    new FieldMeta("I16", Modifier.PROTECTED, short.class)
                ),
            Arrays.asList(
                    new ExecutableMeta("lookup", Modifier.PUBLIC | Modifier.STATIC),
                    new ExecutableMeta("sdo5", Modifier.PUBLIC | Modifier.STATIC, String.class),
                    new ExecutableMeta("sdo6", Modifier.PRIVATE | Modifier.STATIC, String.class),
                    new ExecutableMeta("sdo7", Modifier.STATIC, Long.class),
                    new ExecutableMeta("sdo8", Modifier.PROTECTED | Modifier.STATIC, Integer.class),
                    new ExecutableMeta("ido5", Modifier.PUBLIC, String.class),
                    new ExecutableMeta("ido6", Modifier.PRIVATE, byte.class),
                    new ExecutableMeta("ido7", 0, int.class),
                    new ExecutableMeta("ido8", Modifier.PROTECTED, Integer.class),
                    new ExecutableMeta("iido1", Modifier.PUBLIC, boolean.class),
                    new ExecutableMeta("iido3", Modifier.PUBLIC, int.class)
                ),
            Arrays.asList(
                    new MemberMeta("NS5", Modifier.PUBLIC | Modifier.STATIC),
                    new MemberMeta("NS6", Modifier.STATIC),
                    new MemberMeta("NS7", Modifier.PROTECTED | Modifier.STATIC),
                    new MemberMeta("NS8", Modifier.PRIVATE | Modifier.STATIC),
                    new MemberMeta("NI5", Modifier.PUBLIC),
                    new MemberMeta("NI6", 0),
                    new MemberMeta("NI7", Modifier.PROTECTED),
                    new MemberMeta("NI8", Modifier.PRIVATE)
                )
            );

    private static final Map<Class<?>, TypeMeta> METAS;

    static {
        List<ExecutableMeta> objMethods = new ArrayList<>(Arrays.asList(
                new ExecutableMeta("registerNatives", Modifier.PRIVATE | Modifier.STATIC | Modifier.NATIVE),
                new ExecutableMeta("clone", Modifier.PROTECTED | Modifier.NATIVE),
                new ExecutableMeta("equals", Modifier.PUBLIC, Object.class),
                new ExecutableMeta("finalize", Modifier.PROTECTED),
                new ExecutableMeta("getClass", Modifier.PUBLIC | Modifier.FINAL | Modifier.NATIVE),
                new ExecutableMeta("hashCode", Modifier.PUBLIC | Modifier.NATIVE),
                new ExecutableMeta("notify", Modifier.PUBLIC | Modifier.FINAL | Modifier.NATIVE),
                new ExecutableMeta("notifyAll", Modifier.PUBLIC | Modifier.FINAL | Modifier.NATIVE),
                new ExecutableMeta("toString", Modifier.PUBLIC),
                new ExecutableMeta("wait", Modifier.PUBLIC | Modifier.FINAL),
                new ExecutableMeta("wait", Modifier.PUBLIC | Modifier.FINAL | Modifier.NATIVE, long.class),
                new ExecutableMeta("wait", Modifier.PUBLIC | Modifier.FINAL, long.class, int.class)
        ));
        if (TestUtils.isAtLeastJava(14)) {
            // registerNatives was removed in java 14
            objMethods.remove(0);
        }

        OBJECT = new TypeMeta(Object.class,
                Arrays.asList(new ExecutableMeta("<init>", Modifier.PUBLIC)), null, objMethods, null);

        Map<Class<?>, TypeMeta> metas = new HashMap<>();
        metas.put(IFACE1.type, IFACE1);
        metas.put(IFACE2.type, IFACE2);
        metas.put(OBJECT.type, OBJECT);
        metas.put(TYPE1.type, TYPE1);
        metas.put(TYPE2.type, TYPE2);
        METAS = Collections.unmodifiableMap(metas);
    }

    public static TypeMeta getMeta(Class<?> type) {
        return getMeta(type, false, null);
    }

    public static TypeMeta getMeta(Class<?> type, boolean includeParents, Predicate<? super MemberMeta> filter) {
        TypeMeta meta = METAS.get(type);
        if (meta == null || !includeParents) {
            return meta;
        }

        Set<Class<?>> processed = new HashSet<>();
        LinkedList<Class<?>> toProcess = new LinkedList<>();
        toProcess.add(type);
        while (!toProcess.isEmpty()) {
            Class<?> check = toProcess.poll();
            if (!processed.add(check)) {
                continue;
            }
            Collections.addAll(toProcess, check.getInterfaces());
            toProcess.add(check.getSuperclass());
            toProcess.removeIf(Objects::isNull);
            if (check == type) {
                continue;
            }
            TypeMeta sub = METAS.get(check);
            if (sub != null) {
                meta = meta.merge(sub, filter);
            }
        }
        return meta;
    }
}
