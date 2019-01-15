package com.philips.research.regression.util;

import dk.alexandra.fresco.lib.collections.Matrix;
import org.junit.Assert;

import java.math.BigDecimal;
import java.util.Objects;

import static com.philips.research.regression.util.MatrixConversions.map;
import static java.math.RoundingMode.HALF_UP;

public class MatrixAssert {
    private static <T> void assertEquals(Matrix<T> expected, Matrix<T> actual) {
        Assert.assertEquals(
            new MatrixWithEquality<>(expected),
            new MatrixWithEquality<>(actual)
        );
    }

    public static void assertEquals(Matrix<BigDecimal> expected, Matrix<BigDecimal> actual, int scale) {
        assertEquals(
          map(expected, value -> value.setScale(scale, HALF_UP)),
          map(actual, value -> value.setScale(scale, HALF_UP))
        );
    }
}

class MatrixWithEquality<T> extends Matrix<T> {
    MatrixWithEquality(Matrix<T> other) {
        super(other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Matrix<?> matrix1 = (Matrix<?>) o;
        return getWidth() == matrix1.getWidth() &&
            getHeight() == matrix1.getHeight() &&
            getRows().equals(matrix1.getRows());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getWidth(), getHeight(), getRows());
    }
}

