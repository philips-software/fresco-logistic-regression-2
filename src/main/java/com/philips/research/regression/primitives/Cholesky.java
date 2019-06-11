package com.philips.research.regression.primitives;

import com.philips.research.regression.util.GenericArrayCreation;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.fixed.SFixed;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import static com.philips.research.regression.util.GenericArrayCreation.newArray;
import static java.math.BigDecimal.ZERO;
import static java.math.BigInteger.valueOf;

public class Cholesky implements Computation<Matrix<DRes<SReal>>, ProtocolBuilderNumeric> {
    private DRes<Matrix<DRes<SReal>>> input;

    public Cholesky(DRes<Matrix<DRes<SReal>>> input) {
        this.input = input;
    }

    @Override
    public DRes<Matrix<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
        return builder
            .seq(seq -> {
                Matrix<DRes<SReal>> matrix = input.out();
                DRes<SReal>[][] a = getElements(matrix);
                return () -> new IterationState(a, 0);
            }).whileLoop(
                state -> state.j < state.a.length,
                (seq, state) -> {
                    DRes<SReal>[][] a = state.a;
                    int j = state.j;
                    DRes<SReal> one = seq.realNumeric().known(new BigDecimal(1));
                    int d = a.length;
                    for (int k = 0; k < j; k++) {
                        // TODO: parallelize
                        for (int i = j; i < d; i++) {
                            a[i][j] = seq.realNumeric().sub(
                                a[i][j],
                                seq.realNumeric().mult(a[i][k], a[j][k])
                            );
                        }
                    }
                    a[j][j] = seq.seq(new RealNumericSqrt(a[j][j]));
                    DRes<SReal> ajj_inverse = seq.realNumeric().div(one, a[j][j]);
                    DRes<DRes<SReal>[][]> newA = seq.par(new AjjInverseThing(a, j, ajj_inverse));
                    return () -> new IterationState(newA.out(), j + 1);
            }).seq((seq, state) -> {
                convertToLowerTriangularMatrix(seq, state.a);
                return () -> createMatrix(state.a);
            });
    }

    private static class IterationState {
        DRes<SReal>[][] a;
        int j;

        IterationState(DRes<SReal>[][] a, int j) {
            this.a = a;
            this.j = j;
        }
    }

    private static class AjjInverseThing implements ComputationParallel<DRes<SReal>[][], ProtocolBuilderNumeric> {
        private final DRes<SReal>[][] a;
        private final int j;
        private final DRes<SReal> ajj_inverse;

        public AjjInverseThing(DRes<SReal>[][] a, int j, DRes<SReal> ajj_inverse) {
            this.a = a;
            this.j = j;
            this.ajj_inverse = ajj_inverse;
        }

        @Override
        public DRes<DRes<SReal>[][]> buildComputation(ProtocolBuilderNumeric builder) {
            for (int k = j + 1; k < a.length; k++) {
                a[k][j] = builder.realNumeric().mult(a[k][j], ajj_inverse);
            }

            return () -> a;
        }
    }

    private static void convertToLowerTriangularMatrix(ProtocolBuilderNumeric seq, DRes<SReal>[][] a) {
        int size = a.length;
        for (int r = 0; r < size; r++) {
            for (int c = r + 1; c < size; c++) {
                a[r][c] = seq.realNumeric().known(ZERO);
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
        for (T[] row : rows) {
            mat.add(new ArrayList<>(Arrays.asList(row)));
        }
        return new Matrix<>(h, w, mat);
    }
}

class RealNumericSqrt implements Computation<SReal, ProtocolBuilderNumeric> {

    private DRes<SReal> input;

    RealNumericSqrt(DRes<SReal> input) {
        this.input = input;
    }

    @Override
    public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
        return builder.seq(seq -> {
            SFixed fixed = (SFixed) input.out();
            DRes<SInt> underlyingInt = fixed.getSInt();
            int precision = fixed.getPrecision();
            DRes<SInt> scaled = seq.numeric().mult(valueOf(2).pow(precision), underlyingInt);
            DRes<SInt> sqrt = seq.advancedNumeric().sqrt(scaled, seq.getBasicNumericContext().getMaxBitLength());
            return new SFixed(sqrt, precision);
        });
    }
}
