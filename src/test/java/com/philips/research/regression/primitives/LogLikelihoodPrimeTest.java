package com.philips.research.regression.primitives;

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

import static com.philips.research.regression.Runner.run;
import static com.philips.research.regression.util.BigDecimalUtils.arrayOf;
import static com.philips.research.regression.util.ListAssert.assertEquals;
import static com.philips.research.regression.util.ListConversions.unwrap;
import static com.philips.research.regression.util.MatrixConstruction.matrix;
import static com.philips.research.regression.util.VectorUtils.vectorOf;

@DisplayName("Log Likelihood Prime")
class LogLikelihoodPrimeTest {

    @Test
    @DisplayName("calculates log(likelihood')")
    void logLikelihoodPrime() {
        Matrix<BigDecimal> x = matrix(
            arrayOf(1.0, 2.0, 3.0, 4.0),
            arrayOf(1.1, 2.2, 3.3, 4.4));
        Vector<BigDecimal> y = vectorOf(0.0, 1.0);
        Vector<BigDecimal> beta = vectorOf(0.1, 0.2, 0.3, 0.4);
        Vector<BigDecimal> expected = vectorOf(-0.9134458, -1.826892, -2.740337, -3.653783);
        List<BigDecimal> result = run(new LogLikelihoodPrimeApplication(x, y, beta));
        assertEquals(expected, result, 0.0001);
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
