package com.philips.research.regression.app;

import com.philips.research.regression.FitLogisticModel;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.RealLinearAlgebra;
import dk.alexandra.fresco.lib.real.SReal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static com.philips.research.regression.util.ListConversions.unwrap;

public class LogisticRegression implements Application<List<BigDecimal>, ProtocolBuilderNumeric> {
    private final int party;
    private final Matrix<BigDecimal> matrix;
    private final Vector<BigDecimal> vector;
    private final double lambda;
    private final int iterations;

    public LogisticRegression(int party, Matrix<BigDecimal> matrix, Vector<BigDecimal> vector, double lambda, int iterations) {
        this.party = party;
        this.matrix = matrix;
        this.vector =  vector;
        this.lambda = lambda;
        this.iterations = iterations;
    }

    @Override
    public DRes<List<BigDecimal>> buildComputation(ProtocolBuilderNumeric builder) {
        return builder.seq(seq -> {
            RealLinearAlgebra linAlg = seq.realLinAlg();
            DRes<Matrix<DRes<SReal>>> x1 = (party == 1)
                ? linAlg.input(matrix, 1)
                : matrixWithNulls(linAlg, matrix.getHeight(), matrix.getWidth(), 1);
            DRes<Vector<DRes<SReal>>> y1 = (party == 1)
                ? linAlg.input(vector, 1)
                : vectorWithNulls(linAlg, vector.size(), 1);
            DRes<Matrix<DRes<SReal>>> x2 = (party == 2)
                ? linAlg.input(matrix, 2)
                : matrixWithNulls(linAlg, matrix.getHeight(), matrix.getWidth(), 2);
            DRes<Vector<DRes<SReal>>> y2 = (party == 2)
                ? linAlg.input(vector, 2)
                : vectorWithNulls(linAlg, vector.size(), 2);

            List<DRes<Matrix<DRes<SReal>>>> closedXs = new ArrayList<>();
            closedXs.add(x1);
            closedXs.add(x2);

            List<DRes<Vector<DRes<SReal>>>> closedYs = new ArrayList<>();
            closedYs.add(y1);
            closedYs.add(y2);

            DRes<Vector<DRes<SReal>>> result = seq.seq(new FitLogisticModel(closedXs, closedYs, lambda, iterations));
            DRes<Vector<DRes<BigDecimal>>> opened = seq.realLinAlg().openVector(result);
            return () -> unwrap(opened);
        });
    }

    private static DRes<Vector<DRes<SReal>>> vectorWithNulls(RealLinearAlgebra linAlg, int size, int party) {
        Vector<BigDecimal> v = new Vector<>();
        for (int i = 0; i < size; ++i) {
            v.add(null);
        }
        return linAlg.input(v, party);
    }

    private static DRes<Matrix<DRes<SReal>>> matrixWithNulls(RealLinearAlgebra linAlg, int height, int width, int party) {
        ArrayList<BigDecimal> nullRow = new ArrayList<>(width);
        for (int c = 0; c < width; ++c) {
            nullRow.add(null);
        }
        Matrix<BigDecimal> m = new Matrix<>(height, width, r -> new ArrayList<>(nullRow));
        return linAlg.input(m, party);
    }
}
