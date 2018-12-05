package com.philips.research.regression;

import dk.alexandra.fresco.lib.collections.Matrix;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Vector;

import static com.philips.research.regression.MatrixConstruction.matrix;
import static com.philips.research.regression.VectorAssert.assertEquals;
import static java.math.BigDecimal.valueOf;
import static java.util.Arrays.asList;

@DisplayName("Forward and Backward Substitution")
class SubstitutionTest {

    private Runner<Vector<BigDecimal>> runner = new Runner<>();

    @Test
    @DisplayName("performs forward substitution")
    void forwardSubstitution() {
        Matrix<BigDecimal> L = matrix(new BigDecimal[][]{
            {valueOf(1.0), valueOf(0.0), valueOf(0.0)},
            {valueOf(-2.0), valueOf(1.0), valueOf(0.0)},
            {valueOf(1.0), valueOf(6.0), valueOf(1.0)}
        });
        Vector<BigDecimal> b  = new Vector<>(asList(valueOf(2.0), valueOf(-1.0), valueOf(4.0)));
        Vector<BigDecimal> expected = new Vector<>(asList(valueOf(2.0), valueOf(3.0), valueOf(-16.0)));
        assertEquals(expected, runner.run(new MatrixVectorApplication(L, b, ForwardSubstitution::new)), 3);
    }

    @Test
    @DisplayName("performs back substitution")
    void backSubstitution() {
        Matrix<BigDecimal> L = matrix(new BigDecimal[][]{
            {valueOf(1.0), valueOf(-2.0), valueOf(1.0)},
            {valueOf(0.0), valueOf(1.0), valueOf(6.0)},
            {valueOf(0.0), valueOf(0.0), valueOf(1.0)}
        });
        Vector<BigDecimal> b  = new Vector<>(asList(valueOf(4.0), valueOf(-1.0), valueOf(2.0)));
        Vector<BigDecimal> expected = new Vector<>(asList(valueOf(-24.0), valueOf(-13.0), valueOf(2.0)));
        assertEquals(expected, runner.run(new MatrixVectorApplication(L, b, BackSubstitution::new)), 3);
    }
}
