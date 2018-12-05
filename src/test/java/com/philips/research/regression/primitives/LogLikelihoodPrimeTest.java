package com.philips.research.regression.primitives;

import com.philips.research.regression.Runner;
import com.philips.research.regression.util.ListAssert;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.RealLinearAlgebra;
import dk.alexandra.fresco.lib.real.SReal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Vector;

import static com.philips.research.regression.util.MatrixConstruction.matrix;
import static com.philips.research.regression.util.ListConversions.unwrap;
import static java.math.BigDecimal.valueOf;
import static java.util.Arrays.asList;

@DisplayName("Log Likelihood Prime")
class LogLikelihoodPrimeTest {
    private Runner<List<BigDecimal>> runner = new Runner<>();

    @Test
    @DisplayName("calculates log(likelihood')")
    void logLikelihoodPrime() {
        Matrix<BigDecimal> x = matrix(new BigDecimal[][]{
            {valueOf(1.0), valueOf(2.0), valueOf(3.0), valueOf(4.0)},
            {valueOf(1.1), valueOf(2.2), valueOf(3.3), valueOf(4.4)}
        });
        Vector<BigDecimal> y = new Vector<>(asList(valueOf(0.0), valueOf(1.0)));
        Vector<BigDecimal> beta = new Vector<>(asList(valueOf(0.1), valueOf(0.2), valueOf(0.3), valueOf(0.4)));
        Vector<BigDecimal> expected = new Vector<>(asList(valueOf(-0.9134458), valueOf(-1.826892), valueOf(-2.740337), valueOf(-3.653783)));
        List<BigDecimal> result = runner.run(new LogLikelihoodPrimeApplication(x, y, beta));
        ListAssert.assertEquals(expected, result, 3);
    }
}

class LogLikelihoodPrimeApplication implements Application<List<BigDecimal>, ProtocolBuilderNumeric> {

    private final Matrix<BigDecimal> x;
    private final Vector<BigDecimal> y;
    private final Vector<BigDecimal> beta;

    LogLikelihoodPrimeApplication(Matrix<BigDecimal> x, Vector<BigDecimal> y, Vector<BigDecimal>beta) {
        this.x = x;
        this.y = y;
        this.beta = beta;
    }

    @Override
    public DRes<List<BigDecimal>> buildComputation(ProtocolBuilderNumeric builder) {
        DRes<Matrix<DRes<SReal>>> closedX;
        DRes<Vector<DRes<SReal>>> closedY, closedBeta, closedResult;
        RealLinearAlgebra real = builder.realLinAlg();
        closedX = real.input(x, 1);
        closedY = real.input(y, 1);
        closedBeta = real.input(beta, 1);
        closedResult = builder.seq(new LogLikelihoodPrime(closedX, closedY, closedBeta));
        DRes<Vector<DRes<BigDecimal>>> opened = real.openVector(closedResult);
        return () -> unwrap(opened);
    }
}
