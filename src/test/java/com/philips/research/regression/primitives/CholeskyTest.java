package com.philips.research.regression.primitives;

import com.philips.research.regression.Runner;
import dk.alexandra.fresco.lib.collections.Matrix;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.philips.research.regression.util.MatrixAssert.assertEquals;
import static com.philips.research.regression.util.MatrixConstruction.matrix;
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

        assertEquals(expected, runner.run(new MatrixTransformation(input, Cholesky::new)), 3);
    }
}

