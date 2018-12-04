package com.philips.research.regression;

import org.junit.Assert;

import java.math.BigDecimal;
import java.util.Vector;

import static java.math.RoundingMode.HALF_UP;

class VectorAssert {
    static <T> void assertEquals(Vector<T> expected, Vector<T> actual) {
        Assert.assertEquals(expected, actual);
    }

    static void assertEquals(Vector<BigDecimal> expected, Vector<BigDecimal> actual, int scale) {
        assertEquals(
            VectorConversions.map(expected, value -> value.setScale(scale, HALF_UP)),
            VectorConversions.map(actual, value -> value.setScale(scale, HALF_UP))
        );
    }
}
