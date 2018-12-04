package com.philips.research.regression;

import dk.alexandra.fresco.framework.TestFrameworkException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Vector;

import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Likelihood")
public class LikelihoodTest {
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
            Vector<BigDecimal> beta = new Vector<>(asList(valueOf(0.1)));
            runner.run(xi, beta, Likelihood::new);
            fail();
        } catch (TestFrameworkException ex) {
            assertEquals(IllegalArgumentException.class, ex.getCause().getCause().getClass());
            seenException = true;
        }
        assertTrue(seenException);
    }
}
