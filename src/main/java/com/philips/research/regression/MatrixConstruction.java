package com.philips.research.regression;

import dk.alexandra.fresco.lib.collections.Matrix;

import java.util.ArrayList;
import java.util.List;

class MatrixConstruction {
    static <T> Matrix<T> matrix(List<List<T>> rows) {
        int height = rows.size();
        int width = height > 0 ? rows.get(0).size() : 0;
        return matrix(height, width, rows);
    }

    static <T> Matrix<T> matrix(int height, int width, List<List<T>> rows) {
        ArrayList<ArrayList<T>> converted = new ArrayList<>();
        for (List<T> row: rows) {
            width = row.size();
            converted.add(new ArrayList<>(row));
        }
        return new Matrix<>(height, width, converted);
    }
}
