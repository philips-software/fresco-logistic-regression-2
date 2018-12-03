package com.philips.research.regression;

import dk.alexandra.fresco.lib.collections.Matrix;

import java.util.ArrayList;

class MatrixConversions {
    static <T,U> Matrix<U> map(Matrix<T> matrix, ElementConversion<T,U> conversion) {
        ArrayList<ArrayList<U>> result = new ArrayList<>();
        for (ArrayList<T> row: matrix.getRows()) {
            ArrayList<U> newRow = new ArrayList<>();
            for (T element: row) {
                newRow.add(conversion.convert(element));
            }
            result.add(newRow);
        }
        return new Matrix<>(matrix.getWidth(), matrix.getHeight(), result);
    }

    interface ElementConversion<T,U> {
        U convert(T value);
    }
}
