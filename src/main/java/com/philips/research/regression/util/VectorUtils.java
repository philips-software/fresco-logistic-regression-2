package com.philips.research.regression.util;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class VectorUtils {
    public static BigDecimal multiply(List<BigDecimal> v1, List<BigDecimal> v2) {
        BigDecimal result = BigDecimal.ZERO;
        for (int i = 0; i < v1.size(); ++i) {
            result = result.add(v1.get(i).multiply(v2.get(i)));
        }
        return result;
    }

    public static Vector<BigDecimal> vectorWithZeros(int size) {
        BigDecimal[] zeros = BigDecimalUtils.zeros(size);
        return new Vector<>(Arrays.asList(zeros));
    }

    public static Vector<BigDecimal> vectorOf(double... doubles) {
        return new Vector<>(BigDecimalUtils.listOf(doubles));
    }
}
