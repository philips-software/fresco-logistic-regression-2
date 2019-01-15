package com.philips.research.regression.primitives;

import com.philips.research.regression.util.ListAssert;
import dk.alexandra.fresco.lib.collections.Matrix;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Vector;

import static com.philips.research.regression.util.BigDecimalUtils.arrayOf;
import static com.philips.research.regression.util.VectorUtils.vectorOf;
import static com.philips.research.regression.util.MatrixConstruction.matrix;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LocalLogLikelihoodPrimeTest {
    @Test void compute() {
        Matrix<BigDecimal> x = matrix(
            arrayOf(1.0, 2.0, 3.0, 4.0),
            arrayOf(1.1, 2.2, 3.3, 4.4)
        );
        Vector<BigDecimal> y = vectorOf(0.0, 1.0);
        Vector<BigDecimal> beta = vectorOf(0.1, 0.2, 0.3, 0.4);
        Vector<BigDecimal> result = new LocalLogLikelihoodPrime(x, y, beta).compute();
        Vector<BigDecimal> expected = vectorOf(-0.9134458, -1.826892, -2.740337, -3.653783);

        ListAssert.assertEquals(expected, result, 0.0001);
    }

    @Test void Likelihood() {
        Vector<BigDecimal> xi = vectorOf(1.0, 2.0);
        Vector<BigDecimal> beta = vectorOf(0.1, 0.2);
        BigDecimal probability = LocalLogLikelihoodPrime.likelihood(xi, beta);
        assertEquals(0.6224593, probability.doubleValue(), 0.01);
    }
}
