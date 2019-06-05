package com.philips.research.regression.primitives;

import java.math.BigInteger;

class BigIntegerBooleans {

    private static BigInteger TRUE = BigInteger.ONE;
    static BigInteger FALSE = BigInteger.ZERO;

    private static boolean isTrue(BigInteger i) {
        return i.compareTo(TRUE) == 0;
    }

    static boolean isFalse(BigInteger i) {
        return !isTrue(i);
    }
}
