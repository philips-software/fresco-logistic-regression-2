package com.philips.research.regression;

import dk.alexandra.fresco.lib.collections.Matrix;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Vector;

import static com.philips.research.regression.MatrixConstruction.matrix;
import static java.math.BigDecimal.valueOf;
import static java.util.Arrays.asList;

class LogLikelihoodPrimeTest {
    private VectorRunner2 runner = new VectorRunner2();

    @Test
    @DisplayName("first derivative of log likelihood")
    void logLikelihoodPrime() {
        Matrix<BigDecimal> x = matrix(new BigDecimal[][]{
            {valueOf(1.0), valueOf(2.0), valueOf(3.0), valueOf(4.0)},
            {valueOf(1.1), valueOf(2.2), valueOf(3.3), valueOf(4.4)}
        });
        Vector<BigDecimal> y = new Vector<>(asList(valueOf(0.0), valueOf(1.0)));
        Vector<BigDecimal> beta = new Vector<>(asList(valueOf(0.1), valueOf(0.2), valueOf(0.3), valueOf(0.4)));
        Vector<BigDecimal> expected = new Vector<>(asList(valueOf(-0.9134458), valueOf(-1.826892), valueOf(-2.740337), valueOf(-3.653783)));
        Vector<BigDecimal> result = runner.run(x, y, beta, LogLikelihoodPrime::new);
        VectorAssert.assertEquals(expected, result, 3);
    }
}
