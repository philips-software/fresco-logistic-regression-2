package com.philips.research.regression.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Vector;

import static com.philips.research.regression.util.VectorUtils.vectorOf;
import static org.junit.jupiter.api.Assertions.*;

class VectorUtilsTest {

    @Test
    void times() {
        Vector<BigDecimal> v1 = vectorOf(1.0, 2.0, 3.0);
        Vector<BigDecimal> v2 = vectorOf(1.1, 2.2, 3.3);
        BigDecimal product = VectorUtils.multiply(v1, v2);
        assertEquals(15.4 , product.doubleValue());
    }
}
