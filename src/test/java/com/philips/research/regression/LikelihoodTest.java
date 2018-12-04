package com.philips.research.regression;

import dk.alexandra.fresco.framework.TestFrameworkException;
import dk.alexandra.fresco.lib.collections.Matrix;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Vector;

import static com.philips.research.regression.MatrixConstruction.matrix;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Likelihood")
class LikelihoodTest {
    private BigDecimalRunner runner = new BigDecimalRunner();

    @Test
    @DisplayName("computes likelihood")
    void likelihood() {
        Vector<BigDecimal> xi = new Vector<>(asList(valueOf(1.0), valueOf(2.0)));
        Vector<BigDecimal> beta = new Vector<>(asList(valueOf(0.1), valueOf(0.2)));
        BigDecimal expected = new BigDecimal(0.6224593).setScale(4, HALF_UP);
        BigDecimal probability = runner.run(xi, beta, Likelihood::new);
        assertEquals(expected, probability.setScale(4, HALF_UP));
    }

    @Test
    @DisplayName("expects vectors of equal size")
    void likelihoodVectorSize() {
        boolean seenException = false;
        try {
            Vector<BigDecimal> xi = new Vector<>(asList(valueOf(1.0), valueOf(2.0)));
            Vector<BigDecimal> beta = new Vector<>(Collections.singletonList(valueOf(0.1)));
            runner.run(xi, beta, Likelihood::new);
            fail();
        } catch (TestFrameworkException ex) {
            assertEquals(IllegalArgumentException.class, ex.getCause().getCause().getClass());
            seenException = true;
        }
        assertTrue(seenException);
    }

    @Test
    @DisplayName("first deriviative of log likelihood")
    void logLikelihoodPrime() {
        Matrix<BigDecimal> x = matrix(new BigDecimal[][]{
            {valueOf(1.0), valueOf(2.0), valueOf(3.0), valueOf(4.0)},
            {valueOf(1.1), valueOf(2.2), valueOf(3.3), valueOf(4.4)}
        });
        Vector<BigDecimal> y = new Vector<>(asList(valueOf(0.0), valueOf(1.0)));
        Vector<BigDecimal> beta = new Vector<>(asList(valueOf(0.1), valueOf(0.2), valueOf(0.3), valueOf(0.4)));
        Vector<BigDecimal> expected = new Vector<>(asList(valueOf(-0.9134458), valueOf(-1.826892), valueOf(-2.740337), valueOf(-3.653783)));
        VectorRunner runner = new VectorRunner();
        Vector<BigDecimal> result = runner.run(x, y, beta, LogLikelihoodPrime::new);
        VectorAssert.assertEquals(expected, result, 3);
    }
}
