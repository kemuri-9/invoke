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
package net.kemuri9.invoke.test.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class TypeMeta {

    private static <T extends MemberMeta> List<T> add(List<T> current, List<T> parent, Predicate<? super MemberMeta> filter) {
        List<T> ret = new ArrayList<>(current.size() + parent.size());
        ret.addAll(current);
        parent.stream().filter(filter).forEach(ret::add);
        return ret;
    }

    public final Class<?> type;
    public final List<ExecutableMeta> constructors;
    public final List<FieldMeta> fields;
    public final List<ExecutableMeta> methods;
    public final List<MemberMeta> nestedTypes;

    public TypeMeta(Class<?> type, List<ExecutableMeta> constructors, List<FieldMeta> fields,
            List<ExecutableMeta> methods, List<MemberMeta> nestedTypes) {
        this.type = type;
        this.constructors = unmodifiable(constructors);
        this.fields = unmodifiable(fields);
        this.methods = unmodifiable(methods);
        this.nestedTypes = unmodifiable(nestedTypes);
    }

    public List<ExecutableMeta> getPublicConstructors() {
        return constructors.stream().filter(MemberMeta::isPublic).collect(Collectors.toList());
    }

    public List<FieldMeta> getPublicFields() {
        return fields.stream().filter(MemberMeta::isPublic).collect(Collectors.toList());
    }

    public List<ExecutableMeta> getPublicMethods() {
        return methods.stream().filter(MemberMeta::isPublic).collect(Collectors.toList());
    }

    public List<MemberMeta> getPublicNestedTypes() {
        return nestedTypes.stream().filter(MemberMeta::isPublic).collect(Collectors.toList());
    }

    public TypeMeta merge(TypeMeta parent, Predicate<? super MemberMeta> filter) {
        if (filter == null) {
            filter = (f)-> true;
        }
        List<FieldMeta> fields = add(this.fields, parent.fields, filter);
        List<ExecutableMeta> methods = add(this.methods, parent.methods, filter);
        List<MemberMeta> nestedTypes = add(this.nestedTypes, parent.nestedTypes, filter);
        return new TypeMeta(type, constructors, fields, methods, nestedTypes);
    }

    private <T extends MemberMeta> List<T> unmodifiable(List<T> list) {
        List<T> ret = (list == null) ? Collections.emptyList() : Collections.unmodifiableList(list);
        // initialize declaring class when not yet initialized
        ret.stream().filter(m -> m.declaringClass == null).forEach(m -> m.declaringClass = type);
        return ret;
    }
}
