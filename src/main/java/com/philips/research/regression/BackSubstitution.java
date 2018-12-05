package com.philips.research.regression;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;

import java.util.Arrays;
import java.util.Vector;

import static com.philips.research.regression.GenericArrayCreation.newArray;

public class BackSubstitution implements Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> {
    private final DRes<Matrix<DRes<SReal>>> matrix;
    private final DRes<Vector<DRes<SReal>>> vector;

    BackSubstitution(DRes<Matrix<DRes<SReal>>> matrix, DRes<Vector<DRes<SReal>>> vector) {
        this.matrix = matrix;
        this.vector = vector;
    }

    @Override
    public DRes<Vector<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
        return builder.seq(
            seq -> {
                Matrix<DRes<SReal>> U = this.matrix.out();
                Vector<DRes<SReal>> b = this.vector.out();
                DRes<SReal>[] x = computeBackSubstitution(seq, U, b);
                return () -> new Vector<>(Arrays.asList(x));
            }
        );
    }

    private DRes<SReal>[] computeBackSubstitution(ProtocolBuilderNumeric seq, Matrix<DRes<SReal>> u, Vector<DRes<SReal>> b) {
        int n =  b.size();
        DRes<SReal>[] x = newArray(n);

        for (int i = n-1; i >= 0; --i) {
            x[i] = b.get(i);
            for (int j = i+1; j < n; ++j) {
                x[i] = seq.realNumeric().sub(x[i], seq.realNumeric().mult(u.getRow(i).get(j), x[j]));
            }
            x[i] = seq.realNumeric().div(x[i], u.getRow(i).get(i));
        }
        return x;
    }
}
