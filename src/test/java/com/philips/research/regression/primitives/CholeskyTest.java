package com.philips.research.regression.primitives;

import dk.alexandra.fresco.lib.collections.Matrix;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.philips.research.regression.Runner.run;
import static com.philips.research.regression.util.BigDecimalUtils.arrayOf;
import static com.philips.research.regression.util.MatrixAssert.assertEquals;
import static com.philips.research.regression.util.MatrixConstruction.matrix;
import static java.lang.Math.sqrt;
import static java.math.BigDecimal.valueOf;

@DisplayName("Cholesky decomposition")
class CholeskyTest {

    @Test
    @DisplayName("calculates the Cholesky decomposition")
    void calculatesCholesky() {
        Matrix<BigDecimal> input = matrix(
            arrayOf(2.0, 1.0),
            arrayOf(1.0, 2.0));

        Matrix<BigDecimal> expected = matrix(
            arrayOf(sqrt(2.0), 0.0),
            arrayOf(1.0 / sqrt(2.0), sqrt(3.0 / 2.0)));

        assertEquals(expected, run(new MatrixTransformation(input, Cholesky::new)), 3);
    }
}

