package com.philips.research.regression;

import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixTestUtils;

class MatrixConstruction {
    static <T> Matrix<T> matrix(T[][] matrix) {
        return new MatrixTestUtils().getInputMatrix(matrix);
    }
}
