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
package net.kemuri9.invoke;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Utilities for Invocation
 */
public class InvokeUtils {

    private static MethodHandles.Lookup LOOKUP_DEFAULT = null;
    private static MethodHandles.Lookup LOOKUP_FULL_ACCESS = null;

    static MethodHandles.Lookup defaultLookup(MethodHandles.Lookup lookup) {
        return (lookup == null) ? getDefaultLookup() : lookup;
    }

    /**
     * Retrieve a {@link InvokeExecutable} for the specified {@link Constructor}
     * @param <T> Type to construct
     * @param lookup {@link MethodHandles.Lookup} to unreflect with.
     *  {@code null} indicates to use the default lookup
     * @param ctor {@link Constructor} to retrieve an {@link InvokeExecutable} for
     * @return {@link InvokeExecutable} that can invoke {@link Constructor}
     * @throws IllegalAccessException When the unreflect operation fails
     * @throws IllegalArgumentException When {@code ctor} is {@code null}
     */
    public static <T> InvokeExecutable<T> getConstructor(MethodHandles.Lookup lookup, Constructor<T> ctor)
            throws IllegalAccessException {
        Utils.notNull(ctor, "ctor");
        MethodHandle handle = unreflect(lookup, ctor, UnreflectToMethodHandle.CONSTRUCTOR);
        return new InvokeExecutableImpl<>(ctor, handle);
    }

    /**
     * Retrieve all {@link InvokeExecutable}s representing constructors for the specified {@link Class} accessible by the
     * specified lookup
     * @param <T> Type to construct
     * @param lookup {@link MethodHandles.Lookup} to perform the lookup with
     * @param type {@link Class} to retrieve its constructors
     * @return {@link List} of {@link InvokeExecutable}s representing the accessible constructors
     * @throws IllegalArgumentException When {@code type} is {@code null}
     * @throws UnsupportedOperationException When the operation is not supported
     */
    public static <T> List<InvokeExecutable<T>> getConstructors(MethodHandles.Lookup lookup, Class<T> type) {
        Utils.notNull(type, "type");
        lookup = defaultLookup(lookup);
        // try by reflection first, since a security manager is most often not in place to prevent the access
        try {
            return getConstructorsReflection(lookup, type);
        } catch (SecurityException ex) {
            // SM blocked access, so try by the lookup variation
            return getConstructorsLookup(lookup, type);
        }
    }

    private static <T> List<InvokeExecutable<T>> getConstructorsLookup(MethodHandles.Lookup lookup, Class<T> type) {
        List<Member> members = MemberNameAccess.getInstance().getConstructors(lookup, type);
        List<InvokeExecutable<T>> executables = new ArrayList<>(members.size());
        LookupAccess access = LookupAccess.getInstance();
        for (Member member : members) {
            try {
                MethodHandle handle = access.resolveConstructor(lookup, member);
                executables.add(new InvokeExecutableImpl<>(member, handle));
            } catch (RuntimeException ex) {
                // do not have access permissions, so skip it
            }
        }
        return executables;
    }

    private static <T> List<InvokeExecutable<T>> getConstructorsReflection(MethodHandles.Lookup lookup, Class<T> type) {
        Constructor<T>[] constructors = Utils.cast(type.getDeclaredConstructors());
        List<InvokeExecutable<T>> executables = new ArrayList<>(constructors.length);
        for (Constructor<T> cons : constructors) {
            try {
                executables.add(getConstructor(lookup, cons));
            } catch (IllegalAccessException ex) {
                // no access, so skip it
            }
        }
        return executables;
    }

    /**
     * Retrieve the default {@link MethodHandles.Lookup} to utilize when one is not provided.<br>
     * If no default is specified, then {@link MethodHandles#publicLookup()} is utilized
     * @return {@link MethodHandles.Lookup} to utilize as a default.
     * @see #setDefaultLookup(MethodHandles.Lookup)
     */
    public static MethodHandles.Lookup getDefaultLookup() {
        MethodHandles.Lookup lookup = null;
        synchronized(InvokeUtils.class) {
            lookup = LOOKUP_DEFAULT;
        }
        return Utils.defaultValue(lookup, MethodHandles.publicLookup());
    }

    /**
     * Retrieve all {@link InvokeField}s for the specified {@link Class} accessible by the specified lookup
     * @param lookup {@link MethodHandles.Lookup} to perform the lookup with
     * @param type {@link Class} to retrieve its fields
     * @param includeInherited state of including fields from inherited (parent) types
     * @return {@link List} of {@link InvokeField}s representing the accessible fields
     * @throws IllegalArgumentException When {@code type} is {@code null}
     * @throws UnsupportedOperationException When the operation is not supported
     */
    public static List<InvokeField> getFields(MethodHandles.Lookup lookup, Class<?> type, boolean includeInherited) {
        Utils.notNull(type, "type");
        lookup = defaultLookup(lookup);
        // try by reflection first, since a security manager is most often not in place to prevent the access
        try {
            return getFieldsReflection(lookup, type, includeInherited);
        } catch (SecurityException ex) {
            // SM blocked access, so try by the lookup variation
            return getFieldsLookup(lookup, type, includeInherited);
        }
    }

    private static List<InvokeField> getFieldsLookup(MethodHandles.Lookup lookup, Class<?> type, boolean includeInherited) {
        /* returned members are only the getters, no setters are ever included.
         * so there is no need to process the list looking for setters. */
        List<Member> members = MemberNameAccess.getInstance().getFields(lookup, type, includeInherited, null, null);
        List<RefGetSet> refs = new ArrayList<>(members.size());
        for (Member member : members) {
            refs.add(new RefGetSet(member));
        }
        return VersionSupport.getFields(lookup, refs);
    }

    private static List<InvokeField> getFieldsReflection(MethodHandles.Lookup lookup, Class<?> type, boolean includeInherited) {
        List<InvokeField> fields = new ArrayList<>(64);
        Utils.processClassHierarchy(type, includeInherited, (t)-> {
           for (Field field : t.getDeclaredFields()) {
               try {
                   fields.add(VersionSupport.getField(field, lookup));
               } catch (IllegalAccessException e) {
                   // no access, so skip it
               }
           }
        });
        return fields;
    }

    /**
     * Retrieve a {@link InvokeField} for the specified {@link Field}
     * @param lookup {@link MethodHandles.Lookup} to unreflect with.
     *  {@code null} indicates to use the default lookup
     * @param field {@link Field} to retrieve an {@link InvokeField} for
     * @return {@link InvokeField} that can get and set {@link Field}
     * @throws IllegalAccessException When the unreflect operation fails
     * @throws IllegalArgumentException When {@code field} is {@code null}
     */
    public static InvokeField getField(MethodHandles.Lookup lookup, Field field)
            throws IllegalAccessException {
        Utils.notNull(field, "field");
        lookup = defaultLookup(lookup);
        return VersionSupport.getField(field, lookup);
    }

    /**
     * Retrieve a {@link MethodHandles.Lookup} that has unrestricted (full) access to the JVM
     * @return {@link MethodHandles.Lookup} with full unrestricted accesses
     * @throws UnsupportedOperationException When the unrestricted access Lookup cannot be retrieved
     */
    public static MethodHandles.Lookup getFullAccessLookup() throws UnsupportedOperationException {
        if (LOOKUP_FULL_ACCESS != null) {
            return LOOKUP_FULL_ACCESS;
        }
        MethodHandles.Lookup lookup = null;
        for (Iterator<PrivilegedExceptionAction<MethodHandles.Lookup>> iter = VersionSupport.getLookups().iterator();
                iter.hasNext() && lookup == null;) {
            try {
                lookup = AccessController.doPrivileged(iter.next());
            } catch (Throwable t) {
                continue;
            }
        }
        if (lookup == null || !Utils.isFullLookup(lookup)) {
            throw new UnsupportedOperationException("Unable to attain full access MethodHandles.Lookup");
        }
        LOOKUP_FULL_ACCESS = lookup;
        return lookup;
    }

    /**
     * Retrieve a {@link InvokeExecutable} for the specified {@link Method}
     * @param <R> Type of return value on the method
     * @param lookup {@link MethodHandles.Lookup} to unreflect with.
     *  {@code null} indicates to use the default lookup
     * @param method {@link Method} to retrieve an {@link InvokeExecutable} for
     * @return {@link InvokeExecutable} that can invoke {@link Method}
     * @throws IllegalAccessException When the unreflect operation fails
     * @throws IllegalArgumentException When {@code method} is {@code null}
     */
    public static <R> InvokeExecutable<R> getMethod(MethodHandles.Lookup lookup, Method method)
            throws IllegalAccessException {
        Utils.notNull(method, "method");
        MethodHandle handle = unreflect(lookup, method, UnreflectToMethodHandle.METHOD);
        return new InvokeExecutableImpl<>(method, handle);
    }

    /**
     * Retrieve all {@link InvokeExecutable}s representing methods for the specified {@link Class} accessible by the
     * specified lookup
     * @param lookup {@link MethodHandles.Lookup} to perform the lookup with
     * @param type {@link Class} to retrieve its methods
     * @param includeInherited state of including methods from inherited (parent) types
     * @return {@link List} of {@link InvokeExecutable}s representing the accessible methods
     * @throws IllegalArgumentException When {@code type} is {@code null}
     * @throws UnsupportedOperationException When the operation is not supported
     */
    public static List<InvokeExecutable<?>> getMethods(MethodHandles.Lookup lookup, Class<?> type, boolean includeInherited) {
        Utils.notNull(type, "type");
        lookup = defaultLookup(lookup);
        // try by reflection first, since a security manager is most often not in place to prevent the access
        try {
            return getMethodsReflection(lookup, type, includeInherited);
        } catch (SecurityException ex) {
            // SM blocked access, so try by the lookup variation
            return getMethodsLookup(lookup, type, includeInherited);
        }
    }

    private static List<InvokeExecutable<?>> getMethodsLookup(MethodHandles.Lookup lookup, Class<?> type, boolean includeInherited) {
        List<Member> members = MemberNameAccess.getInstance().getMethods(lookup, type, includeInherited, null, null);
        List<InvokeExecutable<?>> executables = new ArrayList<>(members.size());
        LookupAccess access = LookupAccess.getInstance();
        for (Member member : members) {
            try {
                MethodHandle handle = access.resolveMethod(lookup, member);
                executables.add(new InvokeExecutableImpl<>(member, handle));
            } catch (RuntimeException ex) {
                // do not have access permissions, so skip it
            }
        }
        return executables;
    }

    private static List<InvokeExecutable<?>> getMethodsReflection(MethodHandles.Lookup lookup, Class<?> type, boolean includeInherited) {
        List<InvokeExecutable<?>> executables = new ArrayList<>(32);
        Utils.processClassHierarchy(type, includeInherited, (t)-> {
            for (Method method : t.getDeclaredMethods()) {
                try {
                    MethodHandle handle = UnreflectToMethodHandle.METHOD.unreflect(lookup, method);
                    executables.add(new InvokeExecutableImpl<>(method, handle));
                } catch (IllegalAccessException ex) {
                    // do not have access permissions, so skip it
                }
            }
        });
        return executables;
    }

    /**
     * Retrieve all {@link Member}s representing nested types within the specified {@link Class} accessible by the
     * specified lookup
     * @param lookup {@link MethodHandles.Lookup} to perform the lookup with
     * @param type {@link Class} to retrieve its nested types
     * @param includeInherited state of including nested types from inherited (parent) types
     * @return {@link List} of {@link Member}s indicating accessible nested types
     * @throws IllegalArgumentException When {@code type} is {@code null}
     * @throws UnsupportedOperationException When the operation is not supported
     */
    public static List<Member> getNestedTypes(MethodHandles.Lookup lookup, Class<?> type, boolean includeInherited) {
        Utils.notNull(type, "type");

        // disable security mgr first as it can get in the way
        return SecurityManagerDisabler.getInstance().withSecurityDisabled(()->
            getNestedTypesInt(defaultLookup(lookup), type, includeInherited)
        );
    }

    private static List<Member> getNestedTypesInt(MethodHandles.Lookup lookup, Class<?> type, boolean includeInherited) {
        List<Class<?>> types = new ArrayList<>();

        Utils.processClassHierarchy(type, includeInherited,
                (checkType)-> Collections.addAll(types, checkType.getDeclaredClasses()));
        // remove any inaccessible types
        types.removeIf((t)-> !VersionSupport.isAccessible(lookup, t));
        List<Member> members = new ArrayList<>(types.size());
        for (Class<?> mType : types) {
            members.add(new ClassMember(mType));
        }
        return members;
    }

    /**
     * Perform an invocation on a {@link MethodHandle}
     * @param <R> Type of return
     * @param handle {@link MethodHandle} to invoke.
     * @param args arguments to the method invocation.
     *  non-static handles should have the object instance as the first argument.
     * @return result of the invocation.
     * @throws IllegalArgumentException When {@code handle} is {@code null}
     * @throws RuntimeException When invoking {@code handle} fails
     */
    public static <R> R invoke(MethodHandle handle, Object... args) {
        Utils.notNull(handle, "handle");
        try {
            Object ret = handle.invokeWithArguments(args);
            return Utils.cast(ret);
        } catch (Throwable t) {
            throw Utils.asException(t, RuntimeException.class, "invocation failed");
        }
    }

    /**
     * Perform an invocation on a {@link MethodHandle} quietly.
     * Any Throwable from the invocation is squashed and {@code null} is returned.
     * @param <R> Type of return
     * @param handle {@link MethodHandle} to invoke
     * @param args arguments to the method invocation.
     *  non-static handles should have the object instance as the first argument.
     * @return result of the invocation. {@code null} if an error occurs in the invocation
     */
    public static <R> R invokeQuietly(MethodHandle handle, Object... args) {
        if (handle == null) {
            return null;
        }
        try {
            Object ret = handle.invokeWithArguments(args);
            return Utils.cast(ret);
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Set the default {@link MethodHandles.Lookup} to utilize when one is not specified
     * @param lookup {@link MethodHandles.Lookup} to utilize as a default.
     * @see #getDefaultLookup()
     */
    public static void setDefaultLookup(MethodHandles.Lookup lookup) {
        synchronized(InvokeUtils.class) {
            LOOKUP_DEFAULT = lookup;
        }
    }

    /**
     * Perform an Unreflection operation
     * @param <E> Type of {@link Member} to unreflect
     * @param <R> Type of return from unreflection
     * @param lookup {@link MethodHandles.Lookup} to lookup with. {@code null} indicates to utilize {@link #getDefaultLookup()}
     * @param element {@link Member} to unreflect
     * @param unreflector {@link Unreflector} operation to perform
     * @return Result of the unreflection operation. {@code null} if {@code element} is {@code null}
     * @throws IllegalAccessException When the unreflect operation fails
     * @throws IllegalArgumentException When {@code unreflector} is {@code null}
     * @see Unreflector
     */
    public static <E extends Member, R> R unreflect(MethodHandles.Lookup lookup, E element,
            Unreflector<? super E, ? extends R> unreflector) throws IllegalAccessException {
        if (element == null) {
            return null;
        }
        Utils.notNull(unreflector, "unreflector");
        lookup = defaultLookup(lookup);
        return unreflector.unreflect(lookup, element);
    }

    /**
     * Derivable, but not directly instantiable
     */
    protected InvokeUtils() {}
}
