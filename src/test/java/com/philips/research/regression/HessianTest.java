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
import static java.math.BigDecimal.valueOf;

@DisplayName("Hessian matrix")
class HessianTest {

    private Runner<Matrix<BigDecimal>> runner = new Runner<>();

    @Test
    @DisplayName("calculates an approximation of the Hessian matrix")
    void calculatesHessian() {
        Matrix<BigDecimal> input = matrix(new BigDecimal[][]{
            {valueOf(1.0), valueOf(2.0)},
            {valueOf(3.0), valueOf(4.0)},
            {valueOf(5.0), valueOf(6.0)},
        });

        Matrix<BigDecimal> expected = matrix(new BigDecimal[][]{
            {valueOf(-8.75), valueOf(-11.0)},
            {valueOf(-11.0), valueOf(-14.0)},
        });

        Application<Matrix<BigDecimal>, ProtocolBuilderNumeric> application = builder -> {
            DRes<Matrix<DRes<SReal>>> closed, hessian;
            DRes<Matrix<DRes<BigDecimal>>> opened;

            RealLinearAlgebra real = builder.realLinAlg();

            closed = real.input(input, 1);
            hessian = builder.seq(new Hessian(closed));
            opened = real.openMatrix(hessian);

            return () -> new MatrixUtils().unwrapMatrix(opened);
        };

        assertEquals(expected, runner.run(application), 5);
    }
}

