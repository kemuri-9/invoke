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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Accessor for MemberName and related functionalities
 *
 * Use of {@link Member} is utilized in the APIs here but in reality these are {@code java.lang.invoke.MemberName}s.
 */
abstract class MemberNameAccess {

    /** Taken from MethodHandles.Constants */
    static enum ReferenceKind {

        REF_NONE(0),  // null value
        REF_getField(1),
        REF_getStatic(2),
        REF_putField(3),
        REF_putStatic(4),
        REF_invokeVirtual(5),
        REF_invokeStatic(6),
        REF_invokeSpecial(7),
        REF_newInvokeSpecial(8),
        REF_invokeInterface(9),
        REF_LIMIT(10);

        static ReferenceKind ofByte(byte kind) {
            for (ReferenceKind refKind : values()) {
                if (refKind.kind == kind) {
                    return refKind;
                }
            }
            return REF_NONE;
        }

        public final byte kind;

        ReferenceKind(int kind) {
            this.kind = (byte) kind;
        }

        boolean isField() {
            return (kind <= REF_putStatic.kind);
        }

        boolean isGetter() {
            return (kind <= REF_getStatic.kind);
        }

        boolean isSetter() {
            return isField() && !isGetter();
        }

        boolean isMethod() {
            return !isField() && (kind != REF_newInvokeSpecial.kind);
        }

        boolean isConstructor() {
            return (kind == REF_newInvokeSpecial.kind);
        }

        boolean hasReceiver() {
            return (kind & 1) != 0;
        }

        boolean isStatic() {
            return !hasReceiver() && (kind != REF_newInvokeSpecial.kind);
        }

        boolean doesDispatch() {
            return (kind == REF_invokeVirtual.kind ||
                    kind == REF_invokeInterface.kind);
        }
    }

    private static MemberNameAccessVersion INSTANCE = null;

    static MemberNameAccessVersion getInstance() {
        if (INSTANCE == null) {
            synchronized(MemberNameAccess.class) {
                if (INSTANCE == null) {
                    try {
                        INSTANCE = new MemberNameAccessVersion();
                    } catch (Throwable ex) {
                        throw new UnsupportedOperationException("failed to setup MemberNameAccess", ex);
                    }
                }
            }
        }
        return INSTANCE;
    }

    protected final Object factory;
    protected final MethodHandle getConstructors;
    protected final MethodHandle getFields;
    protected final MethodHandle getMethods;

    protected final MethodHandle getReferenceKind;
    protected final MethodHandle getType;
    protected final MethodHandle isResolved;
    protected final MethodHandle newMemberField;
    protected final MethodHandle newMemberFieldParts;

    protected final MethodHandle expand;

    /* the native implementation for nested types is not implemented in any jdk.
     * the reason being "NYI, and Core Reflection works quite well for this query".
     * so use standard reflection instead for nested types */

    protected MemberNameAccess() throws Throwable {
        Lookup fullLookup = InvokeUtils.getFullAccessLookup();
        Class<?> factoryType = VersionSupport.getType(fullLookup, Utils.TYPE_MEMBER_NAME_FACTORY);
        Class<?> memberNameType = VersionSupport.getType(fullLookup, Utils.TYPE_MEMBER_NAME);
        factory = fullLookup.in(MethodHandles.class).findStaticGetter(MethodHandles.class, "IMPL_NAMES", factoryType)
                .invoke();
        Lookup lookup = fullLookup.in(factoryType);
        getConstructors = lookup.findVirtual(factoryType, "getConstructors",
                MethodType.methodType(List.class, Class.class, Class.class));
        getFields = lookup.findVirtual(factoryType, "getFields", MethodType.methodType(List.class, Class.class,
                boolean.class, String.class, Class.class, Class.class));
        getMethods = lookup.findVirtual(factoryType, "getMethods", MethodType.methodType(List.class, Class.class,
                boolean.class, String.class, MethodType.class, Class.class));

        lookup = fullLookup.in(memberNameType);
        getReferenceKind = lookup.findVirtual(memberNameType, "getReferenceKind", MethodType.methodType(byte.class));
        getType = lookup.findVirtual(memberNameType, "getType", MethodType.methodType(Object.class));
        isResolved = lookup.findVirtual(memberNameType, "isResolved", MethodType.methodType(boolean.class));
        newMemberField = lookup.findConstructor(memberNameType,
                MethodType.methodType(void.class, Field.class, boolean.class));
        newMemberFieldParts = lookup.findConstructor(memberNameType,
                MethodType.methodType(void.class, Class.class, String.class, Class.class, byte.class));

        Class<?> handleNatives = VersionSupport.getType(fullLookup, "java.lang.invoke.MethodHandleNatives");
        expand = fullLookup.in(handleNatives).findStatic(handleNatives, "expand",
                MethodType.methodType(void.class, memberNameType));
    }

    /**
     * Expand the member data from the JVM, where possible
     * @param member {@link Member} to expand its data
     */
    void expand(Member member) {
        InvokeUtils.invoke(expand, member);
    }

    List<Member> getConstructors(MethodHandles.Lookup lookup, Class<?> type) {
        List<Member> members = InvokeUtils.invoke(getConstructors, factory, type, Utils.getLookupType(lookup));
        return resolve(lookup, members);
    }

    List<Member> getFields(MethodHandles.Lookup lookup, Class<?> type, boolean withSuper, String name, Class<?> fieldType) {
        /* withSuper does not actually work in getFields: no super type fields are ever returned.
         * Looks to be bug in the native code. so process type tree manually */
        List<Member> members = new ArrayList<>(32);
        Class<?> lookupType = Utils.getLookupType(lookup);
        Utils.processClassHierarchy(type, withSuper, (checkType)-> {
            List<Member> typeMems = InvokeUtils.invoke(getFields, factory, checkType, false, name, fieldType, lookupType);
            members.addAll(typeMems);
        });
        return resolve(lookup, members);
    }

    List<Member> getMethods(MethodHandles.Lookup lookup, Class<?> type, boolean withSuper, String name, MethodType methodType) {
        /* withSuper does not actually work in getMethods: no super type methods are ever returned.
         * Looks to be bug in the native code. so process type tree manually */
        List<Member> members = new ArrayList<>(32);
        Class<?> lookupType = Utils.getLookupType(lookup);
        Utils.processClassHierarchy(type, withSuper, (checkType)-> {
            List<Member> typeMems = InvokeUtils.invoke(getMethods, factory, checkType, false, name, methodType, lookupType);
            members.addAll(typeMems);
        });
        return resolve(lookup, members);
    }

    /**
     * Retrieve a {@code MemberName}'s reference kind
     * @param member {@code MemberName} to retrieve its reference kind
     * @return reference kind of the MemberName
     */
    byte getReferenceKind(Member member) {
        return InvokeUtils.<Byte>invoke(getReferenceKind, member);
    }

    Object getType(Member member) {
        return InvokeUtils.invoke(getType, member);
    }

    boolean isResolved(Member member) {
        return InvokeUtils.<Boolean>invoke(isResolved, member);
    }

    Member newMember(Class<?> defClass, String name, Class<?> fieldType, byte refKind) {
        Member member = InvokeUtils.invoke(newMemberFieldParts, defClass, name, fieldType, refKind);
        return resolve(member, InvokeUtils.getFullAccessLookup());
    }

    Member newMember(Field field, boolean isSetter) {
        return InvokeUtils.invoke(newMemberField, field, isSetter);
    }

    protected abstract Member resolve(Member member, MethodHandles.Lookup lookup);

    List<Member> resolve(MethodHandles.Lookup lookup, List<Member> members) {
        for (ListIterator<Member> memberIter = members.listIterator(); memberIter.hasNext();) {
            Member member = memberIter.next();
            try {
                Member resolved = resolve(member, lookup);
                memberIter.set(resolved);
            } catch (Throwable t) {
                memberIter.remove();
            }
        }
        return members;
    }
}
