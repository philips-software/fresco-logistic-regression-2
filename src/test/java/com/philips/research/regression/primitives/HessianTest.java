package com.philips.research.regression.primitives;

import dk.alexandra.fresco.lib.collections.Matrix;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.philips.research.regression.Runner.run;
import static com.philips.research.regression.util.BigDecimalUtils.arrayOf;
import static com.philips.research.regression.util.MatrixAssert.assertEquals;
import static com.philips.research.regression.util.MatrixConstruction.matrix;

@DisplayName("Hessian matrix")
class HessianTest {

    @Test
    @DisplayName("calculates an approximation of the Hessian matrix")
    void calculatesHessian() {
        Matrix<BigDecimal> input = matrix(
            arrayOf(1.0, 2.0),
            arrayOf(3.0, 4.0),
            arrayOf(5.0, 6.0));

        Matrix<BigDecimal> expected = matrix(
            arrayOf(-8.75, -11.0),
            arrayOf(-11.0, -14.0));

        assertEquals(expected, run(new MatrixTransformation(input, Hessian::new)), 5);
    }
}

