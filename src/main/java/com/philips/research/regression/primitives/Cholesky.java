package com.philips.research.regression.primitives;

import com.philips.research.regression.util.GenericArrayCreation;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;

import java.util.ArrayList;
import java.util.Arrays;

import static com.philips.research.regression.util.GenericArrayCreation.newArray;

class Cholesky implements Computation<Matrix<DRes<SReal>>, ProtocolBuilderNumeric> {
    private DRes<Matrix<DRes<SReal>>> input;

    Cholesky(DRes<Matrix<DRes<SReal>>> input) {
        this.input = input;
    }

    @Override
    public DRes<Matrix<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
        return builder.seq(
            seq -> () -> input.out()
        ).seq(
            this::computeCholesky
        );
    }

    private DRes<Matrix<DRes<SReal>>> computeCholesky(ProtocolBuilderNumeric seq, Matrix<DRes<SReal>> matrix) {
        int d = matrix.getHeight();
        DRes<SReal>[][] a = getElements(matrix);
        for (int j = 0; j < d; ++j) {
            for (int k = 0; k < j; ++k) {
                for (int i = j; i < d; ++i) {
                    a[i][j] = seq.realNumeric().sub(
                        a[i][j],
                        seq.realNumeric().mult(a[i][k], a[j][k])
                    );
                }
            }
            a[j][j] = seq.realAdvanced().sqrt(a[j][j]);
            for (int k = j+1; k < d; ++k) {
                a[k][j] = seq.realNumeric().div(a[k][j], a[j][j]);
            }
        }
        convertToLowerTriangularMatrix(seq, a);
        return () -> createMatrix(a);
    }

    private static void convertToLowerTriangularMatrix(ProtocolBuilderNumeric seq, DRes<SReal>[][] a) {
        int size = a.length;
        for (int r = 0; r < size; ++r) {
            for (int c = r + 1; c < size; c++) {
                a[r][c] = seq.realNumeric().sub(a[r][c], a[r][c]);
            }
        }
    }

    private DRes<SReal>[][] getElements(Matrix<DRes<SReal>> matrix) {
        return matrix.getRows().stream().map(
            row -> row.toArray(GenericArrayCreation.<DRes<SReal>>newArray(0))
        ).toArray(size -> newArray(size));
    }

    private static <T> Matrix<T> createMatrix(T[][] rows) {
        int h = rows.length;
        int w = rows[0].length;
        ArrayList<ArrayList<T>> mat = new ArrayList<>();
        for (T[] row: rows) {
            mat.add(new ArrayList<>(Arrays.asList(row)));
        }
        return new Matrix<>(h, w, mat);
    }
}
