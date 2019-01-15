package com.philips.research.regression.util;

import java.math.BigDecimal;
import java.util.List;

public class VectorUtils {
    public static BigDecimal multiply(List<BigDecimal> v1, List<BigDecimal> v2) {
        BigDecimal result = BigDecimal.ZERO;
        for (int i = 0; i < v1.size(); ++i) {
            result = result.add(v1.get(i).multiply(v2.get(i)));
        }
        return result;
    }
}
