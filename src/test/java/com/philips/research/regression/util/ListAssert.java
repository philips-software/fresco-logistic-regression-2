package com.philips.research.regression.util;

import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;
import java.util.List;

import static com.philips.research.regression.util.ListConversions.map;
import static java.math.RoundingMode.HALF_UP;

public class ListAssert {
    public static void assertEquals(List<BigDecimal> expected, List<BigDecimal> actual, int scale) {
        Assertions.assertEquals(
            map(expected, value -> value.setScale(scale, HALF_UP)),
            map(actual, value -> value.setScale(scale, HALF_UP))
        );
    }
}
