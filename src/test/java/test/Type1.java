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
package test;

import java.lang.invoke.MethodHandles;

import net.kemuri9.invoke.test.TestUtils;

@SuppressWarnings("unused")
public class Type1 {

    public static class NS1 {}
    static class NS2 {}
    protected static class NS3 {}
    private static class NS4 {}

    public class NI1 {}
    class NI2 {}
    protected class NI3 {}
    private class NI4 {}

    // to avoid constant inlines, need to use a function invocation in some places
    public static final String S1 = TestUtils.getValue("S1");
    public static String S2 = "S2";
    private static final Integer S3 = 3;
    private static Short S4 = (short) 4;
    static final Boolean S5 = Boolean.FALSE;
    static Boolean S6 = Boolean.TRUE;
    protected static final Byte S7 = (byte) 7;
    protected static Byte S8 = (byte) 8;

    public static MethodHandles.Lookup lookup() {
        return MethodHandles.lookup();
    }

    public static String sdo1(String value) {
        return value;
    }

    private static String sdo2(String value) {
        return value;
    }

    static Integer sdo3(Long value) {
        return (value == null) ? null : value.intValue();
    }

    protected static Long sdo4(Integer value) {
        return (value == null) ? null : value.longValue();
    }

    public final String I1 = TestUtils.getValue("I1");
    public int I2 = 2;
    private final long I3 = 3;
    private long I4 = 4;
    final byte I5 = (byte) 5;
    byte I6 = (byte) 6;
    protected final short I7 = (short) 7;
    protected short I8 = (short) 8;

    public Type1() {}

    Type1(String unused) {}

    protected Type1(Integer unused) {}

    private Type1(boolean unused) {}


    public byte ido1(String value) {
        return Byte.valueOf(value);
    }

    private short ido2(byte value) {
        return value;
    }

    long ido3(int value) {
        return value;
    }

    protected Integer ido4(Integer value) {
        return value;
    }
}
