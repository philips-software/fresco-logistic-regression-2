package com.philips.research.regression.util;

import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixTestUtils;

public class MatrixConstruction {
    public static <T> Matrix<T> matrix(T[][] matrix) {
        return new MatrixTestUtils().getInputMatrix(matrix);
    }
}
