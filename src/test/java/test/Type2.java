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
public class Type2 extends Type1 implements IFace1, IFace2 {

    public static class NS5 {}
    static class NS6 {}
    protected static class NS7 {}
    private static class NS8 {}

    public class NI5 {}
    class NI6 {}
    protected class NI7 {}
    private class NI8 {}

    // to avoid constant inlines, need to use a function invocation in some places
    public static final String S9 = TestUtils.getValue("S9");
    public static String S10 = "S10";
    private static final Integer S11 = 11;
    private static Short S12 = (short) 12;
    static final Boolean S13 = Boolean.FALSE;
    static Boolean S14 = Boolean.TRUE;
    protected static final Byte S15 = (byte) 15;
    protected static Byte S16 = (byte) 16;

    public static MethodHandles.Lookup lookup() {
        return MethodHandles.lookup();
    }

    public static String sdo5(String value) {
        return value;
    }

    private static String sdo6(String value) {
        return value;
    }

    static Integer sdo7(Long value) {
        return (value == null) ? null : value.intValue();
    }

    protected static Long sdo8(Integer value) {
        return (value == null) ? null : value.longValue();
    }

    public final String I9 = TestUtils.getValue("I9");
    public int I10 = 10;
    private final long I11 = 11;
    private long I12 = 12;
    final byte I13 = (byte) 13;
    byte I14 = (byte) 14;
    protected final short I15 = (short) 15;
    protected short I16 = (short) 16;

    public Type2() {}

    Type2(String unused) {}

    protected Type2(Integer unused) {}

    private Type2(boolean unused) {}

    public byte ido5(String value) {
        return Byte.valueOf(value);
    }

    private short ido6(byte value) {
        return value;
    }

    long ido7(int value) {
        return value;
    }

    protected Integer ido8(Integer value) {
        return value;
    }

    @Override
    public void iido1(boolean unused) {}

    @Override
    public void iido3(int unused) {}
}
