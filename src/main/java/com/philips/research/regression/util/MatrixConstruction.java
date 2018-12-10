package com.philips.research.regression.util;

import dk.alexandra.fresco.lib.collections.Matrix;

import java.util.ArrayList;

import static java.util.Arrays.asList;

public class MatrixConstruction {
    public static <T> Matrix<T> matrix(T[][] matrix) {
        int width = 0;
        ArrayList<ArrayList<T>> result = new ArrayList<>();
        for (T[] row: matrix) {
            width = row.length;
            result.add(new ArrayList<>(asList(row)));
        }
        return new Matrix<>(matrix.length, width, result);
    }
}
