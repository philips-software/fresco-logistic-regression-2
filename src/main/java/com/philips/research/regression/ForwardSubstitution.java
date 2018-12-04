package com.philips.research.regression;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;

import java.util.Arrays;
import java.util.Vector;

public class ForwardSubstitution implements Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> {
    private final DRes<Matrix<DRes<SReal>>> matrix;
    private final DRes<Vector<DRes<SReal>>> vector;

    public ForwardSubstitution(DRes<Matrix<DRes<SReal>>> matrix, DRes<Vector<DRes<SReal>>> vector) {
        this.matrix = matrix;
        this.vector = vector;
    }

    @Override
    public DRes<Vector<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
        return builder.seq(
            seq -> {
                Matrix<DRes<SReal>> L = this.matrix.out();
                Vector<DRes<SReal>> b = this.vector.out();
                DRes<SReal>[] y = computeForwardSubstitution(seq, L, b);
                return () -> new Vector<>(Arrays.asList(y));
            }
        );
    }

    private static DRes<SReal>[] computeForwardSubstitution(ProtocolBuilderNumeric seq, Matrix<DRes<SReal>> l, Vector<DRes<SReal>> b) {
        int n = b.size();
        DRes<SReal>[] y = newArray(n);

        for (int i = 0; i < n; ++i) {
            y[i] = b.get(i);
            for (int j = 0; j < i; ++j) {
                y[i] = seq.realNumeric().sub(y[i], seq.realNumeric().mult(l.getRow(i).get(j), y[j]));
            }
            y[i] = seq.realNumeric().div(y[i], l.getRow(i).get(i));
        }
        return y;
    }

    @SafeVarargs
    private static <E> E[] newArray(int length, E... array) {
        return Arrays.copyOf(array, length);
    }
}
