package com.philips.research.regression.util;

import dk.alexandra.fresco.lib.collections.Matrix;

import java.math.BigDecimal;
import java.util.ArrayList;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.util.Arrays.asList;

public class MatrixConstruction {
    @SafeVarargs
    public static <T> Matrix<T> matrix(T[]... matrix) {
        int width = 0;
        ArrayList<ArrayList<T>> result = new ArrayList<>();
        for (T[] row: matrix) {
            width = row.length;
            result.add(new ArrayList<>(asList(row)));
        }
        return new Matrix<>(matrix.length, width, result);
    }


    public static Matrix<BigDecimal> identity(int size) {
        ArrayList<ArrayList<BigDecimal>> identity = new ArrayList<>();
        for (int row = 0; row< size; row++) {
            ArrayList<BigDecimal> elements = new ArrayList<>();
            for (int column = 0; column< size; column++) {
                elements.add(row == column ? ONE : ZERO);
            }
            identity.add(elements);
        }
        return new Matrix<>(size, size, identity);
    }

    public static Matrix<BigDecimal> matrixWithZeros(int height, int width) {
        ArrayList<BigDecimal> zeroRow = new ArrayList<>(asList(BigDecimalUtils.zeros(width)));
        return new Matrix<>(height, width, r -> new ArrayList<>(zeroRow));
    }
}
