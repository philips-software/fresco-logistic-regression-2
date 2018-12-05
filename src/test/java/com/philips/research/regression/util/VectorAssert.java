package com.philips.research.regression.util;

import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;
import java.util.Vector;

import static java.math.RoundingMode.HALF_UP;

public class VectorAssert {
    public static void assertEquals(Vector<BigDecimal> expected, Vector<BigDecimal> actual, int scale) {
        Assertions.assertEquals(
            VectorConversions.map(expected, value -> value.setScale(scale, HALF_UP)),
            VectorConversions.map(actual, value -> value.setScale(scale, HALF_UP))
        );
    }
}
