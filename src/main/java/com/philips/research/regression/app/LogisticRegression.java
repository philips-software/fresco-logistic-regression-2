package com.philips.research.regression.app;

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
import static com.philips.research.regression.util.MatrixConstruction.matrixWithZeros;
import static com.philips.research.regression.util.VectorUtils.vectorWithZeros;
import static java.math.BigDecimal.valueOf;

class LogisticRegression implements Application<List<BigDecimal>, ProtocolBuilderNumeric> {
    private final int myId;
    private final Matrix<BigDecimal> matrix;
    private final Vector<BigDecimal> vector;
    private final double lambda;
    private final int iterations;
    private final double privacyBudget;
    private final double sensitivity;

    LogisticRegression(int myId, Matrix<BigDecimal> matrix, Vector<BigDecimal> vector, double lambda, int iterations, double privacyBudget, double sensitivity) {
        this.myId = myId;
        this.matrix = matrix;
        this.vector =  vector;
        this.lambda = lambda;
        this.iterations = iterations;
        this.privacyBudget = privacyBudget;
        this.sensitivity = sensitivity;
    }

    @Override
    public DRes<List<BigDecimal>> buildComputation(ProtocolBuilderNumeric builder) {
        final double sensitivity = this.sensitivity > 0
            ? this.sensitivity
            : 1.0 / (matrix.getHeight() * 2.0);
        return builder.par(par -> {
            DRes<Matrix<DRes<SReal>>> x1, x2;
            DRes<Vector<DRes<SReal>>> y1, y2;
            RealLinearAlgebra linAlg = par.realLinAlg();
            if (myId == 1) {
                x1 = linAlg.input(matrix, 1);
                y1 = linAlg.input(vector, 1);
                x2 = linAlg.input(matrixWithZeros(matrix.getHeight(), matrix.getWidth()), 2);
                y2 = linAlg.input(vectorWithZeros(vector.size()), 2);
            } else {
                x1 = linAlg.input(matrixWithZeros(matrix.getHeight(), matrix.getWidth()), 1);
                y1 = linAlg.input(vectorWithZeros(vector.size()), 1);
                x2 = linAlg.input(matrix, 2);
                y2 = linAlg.input(vector, 2);
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

            DRes<Vector<DRes<SReal>>> result = privacyBudget > 0
                ? seq.seq(new FitLogisticModel(closedXs, closedYs, lambda, iterations, matrix, vector, valueOf(privacyBudget), valueOf(sensitivity)))
                : seq.seq(new FitLogisticModel(closedXs, closedYs, lambda, iterations, matrix, vector));
            DRes<Vector<DRes<BigDecimal>>> opened = seq.realLinAlg().openVector(result);
            return () -> unwrap(opened);
        });
    }

}

