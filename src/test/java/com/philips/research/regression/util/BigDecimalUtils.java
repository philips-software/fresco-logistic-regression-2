package com.philips.research.regression.util;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class BigDecimalUtils {
    public static BigDecimal[] arrayOf(double... doubles) {
        return Arrays.stream(doubles).mapToObj(BigDecimal::valueOf).toArray(BigDecimal[]::new);
    }

    static List<BigDecimal> listOf(double... doubles) {
        return Arrays.stream(doubles).mapToObj(BigDecimal::valueOf).collect(Collectors.toList());
    }

    public static Vector<BigDecimal> vectorOf(double... doubles) {
        return new Vector<>(listOf(doubles));
    }
}
