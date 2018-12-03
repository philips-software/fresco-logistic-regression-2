package com.philips.research.regression;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixUtils;
import dk.alexandra.fresco.lib.real.RealLinearAlgebra;
import dk.alexandra.fresco.lib.real.SReal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.philips.research.regression.MatrixAssert.assertEquals;
import static com.philips.research.regression.MatrixConstruction.matrix;
import static java.lang.Math.sqrt;
import static java.math.BigDecimal.valueOf;

@DisplayName("Cholesky decomposition")
class CholeskyTest {

    private Runner<Matrix<BigDecimal>> runner = new Runner<>();

    @Test
    @DisplayName("calculates the Cholesky decomposition")
    void calculatesCholesky() {
        Matrix<BigDecimal> input = matrix(new BigDecimal[][]{
            {valueOf(2.0), valueOf(1.0)},
            {valueOf(1.0), valueOf(2.0)}
        });

        Matrix<BigDecimal> expected = matrix(new BigDecimal[][]{
            {valueOf(sqrt(2.0)), valueOf(0.0)},
            {valueOf(1.0 / sqrt(2.0)), valueOf(sqrt(3.0 / 2.0))}
        });

        Application<Matrix<BigDecimal>, ProtocolBuilderNumeric> application = builder -> {
            DRes<Matrix<DRes<SReal>>> closed, cholesky;
            DRes<Matrix<DRes<BigDecimal>>> opened;

            RealLinearAlgebra real = builder.realLinAlg();

            closed = real.input(input, 1);
            cholesky = builder.seq(new Cholesky(closed));
            opened = real.openMatrix(cholesky);

            return () -> new MatrixUtils().unwrapMatrix(opened);
        };

        assertEquals(expected, runner.run(application), 3);
    }
}

