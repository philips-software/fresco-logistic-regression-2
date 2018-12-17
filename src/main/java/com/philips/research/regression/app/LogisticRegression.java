package com.philips.research.regression.app;

import com.philips.research.regression.FitLogisticModel;
import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
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
    private final int myId;
    private final Matrix<BigDecimal> matrix;
    private final Vector<BigDecimal> vector;
    private final double lambda;
    private final int iterations;

    public LogisticRegression(int myId, Matrix<BigDecimal> matrix, Vector<BigDecimal> vector, double lambda, int iterations) {
        this.myId = myId;
        this.matrix = matrix;
        this.vector =  vector;
        this.lambda = lambda;
        this.iterations = iterations;
    }

    @Override
    public DRes<List<BigDecimal>> buildComputation(ProtocolBuilderNumeric builder) {
        return builder.par(par -> {
            DRes<Matrix<DRes<SReal>>> x1, x2;
            DRes<Vector<DRes<SReal>>> y1, y2;
            if (myId == 1) {
                x1 = CloseMatrix.close(par, matrix, 1);
                y1 = CloseVector.close(par, vector, 1);
                x2 = CloseMatrix.close(par, matrix.getHeight(), matrix.getWidth(), 2);
                y2 = CloseVector.close(par, vector.size(), 2);
            } else {
                x1 = CloseMatrix.close(par, matrix.getHeight(), matrix.getWidth(), 1);
                y1 = CloseVector.close(par, vector.size(), 1);
                x2 = CloseMatrix.close(par, matrix, 2);
                y2 = CloseVector.close(par, vector, 2);
            }

            List<DRes<Matrix<DRes<SReal>>>> closedXs = new ArrayList<>();
            closedXs.add(x1);
            closedXs.add(x2);

            List<DRes<Vector<DRes<SReal>>>> closedYs = new ArrayList<>();
            closedYs.add(y1);
            closedYs.add(y2);

            return () -> new Pair<>(closedXs, closedYs);
        }).seq((seq, inputs) -> {
            List<DRes<Matrix<DRes<SReal>>>> closedXs = inputs.getFirst();
            List<DRes<Vector<DRes<SReal>>>> closedYs = inputs.getSecond();

            DRes<Vector<DRes<SReal>>> result = seq.seq(new FitLogisticModel(closedXs, closedYs, lambda, iterations));
            DRes<Vector<DRes<BigDecimal>>> opened = seq.realLinAlg().openVector(result);
            return () -> unwrap(opened);
        });
    }

    private static Vector<BigDecimal> vectorWithNulls(int size) {
        Vector<BigDecimal> v = new Vector<>();
        for (int i = 0; i < size; ++i) {
            v.add(null);
        }
        return v;
    }

    private static Matrix<BigDecimal> matrixWithNulls(int height, int width) {
        ArrayList<BigDecimal> nullRow = new ArrayList<>(width);
        for (int c = 0; c < width; ++c) {
            nullRow.add(null);
        }
        Matrix<BigDecimal> m = new Matrix<>(height, width, r -> new ArrayList<>(nullRow));
        return m;
    }
}

