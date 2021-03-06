package com.philips.research.regression.util;

import dk.alexandra.fresco.lib.collections.Matrix;

import java.util.ArrayList;

import static com.philips.research.regression.util.GenericArrayCreation.newArray;
import static com.philips.research.regression.util.MatrixConstruction.matrix;

public class MatrixConversions {
    public static <T,U> Matrix<U> map(Matrix<T> matrix, ElementConversion<T,U> conversion) {
        ArrayList<ArrayList<U>> result = new ArrayList<>();
        for (ArrayList<T> row: matrix.getRows()) {
            ArrayList<U> newRow = new ArrayList<>();
            for (T element: row) {
                newRow.add(conversion.convert(element));
            }
            result.add(newRow);
        }
        return new Matrix<>(matrix.getHeight(), matrix.getWidth(), result);
    }

    public static <T> Matrix<T> transpose(Matrix<T> matrix) {
        T[][] transposed = newArray(matrix.getWidth());
        for (int i=0; i<transposed.length; i++) {
            transposed[i] = matrix.getColumn(i).toArray(newArray(matrix.getHeight()));
        }
        return matrix(transposed);
    }

    public interface ElementConversion<T,U> {
        U convert(T value);
    }
}
