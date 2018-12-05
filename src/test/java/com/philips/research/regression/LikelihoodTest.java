package com.philips.research.regression;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.RealLinearAlgebra;
import dk.alexandra.fresco.lib.real.SReal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Vector;

import static com.philips.research.regression.MatrixConstruction.matrix;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_UP;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Likelihood")
class LikelihoodTest {
    private Runner<BigDecimal> runner = new Runner<>();

    @Test
    @DisplayName("computes likelihood")
    void likelihood() {
        Vector<BigDecimal> xi = new Vector<>(asList(valueOf(1.0), valueOf(2.0)));
        Vector<BigDecimal> beta = new Vector<>(asList(valueOf(0.1), valueOf(0.2)));
        BigDecimal expected = new BigDecimal(0.6224593).setScale(4, HALF_UP);
        BigDecimal probability = runner.run(new LikelihoodApplication(xi, beta));
        assertEquals(expected, probability.setScale(4, HALF_UP));
    }

    @Test
    @DisplayName("expects vectors of equal size")
    void likelihoodVectorSize() {
        assertThrows(IllegalArgumentException.class, () -> {
            Vector<BigDecimal> xi = new Vector<>(asList(valueOf(1.0), valueOf(2.0)));
            Vector<BigDecimal> beta = new Vector<>(Collections.singletonList(valueOf(0.1)));
            runner.run(new LikelihoodApplication(xi, beta));
        });
    }

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
        VectorRunner2 runner = new VectorRunner2();
        Vector<BigDecimal> result = runner.run(x, y, beta, LogLikelihoodPrime::new);
        VectorAssert.assertEquals(expected, result, 3);
    }
}

class LikelihoodApplication implements Application<BigDecimal, ProtocolBuilderNumeric> {
    private Vector<BigDecimal> xi;
    private Vector<BigDecimal> beta;

    LikelihoodApplication(Vector<BigDecimal> xi, Vector<BigDecimal> beta) {
        this.xi = xi;
        this.beta = beta;
    }

    @Override
    public DRes<BigDecimal> buildComputation(ProtocolBuilderNumeric builder) {
        DRes<Vector<DRes<SReal>>> closedXi, closedBeta;
        RealLinearAlgebra real = builder.realLinAlg();
        closedXi = real.input(xi, 1);
        closedBeta = real.input(beta, 1);
        DRes<SReal> closedResult = builder.seq(new Likelihood(closedXi, closedBeta));
        return builder.realNumeric().open(closedResult);
    }
}
