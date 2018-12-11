package com.philips.research.regression.util;

import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;
import java.util.List;

public class ListAssert {
    public static void assertEquals(List<BigDecimal> expected, List<BigDecimal> actual, double delta) {
        double[] expectedArray = expected.stream().mapToDouble(BigDecimal::doubleValue).toArray();
        double[] actualArray = actual.stream().mapToDouble(BigDecimal::doubleValue).toArray();
        Assertions.assertArrayEquals(expectedArray, actualArray, delta);
    }
}
