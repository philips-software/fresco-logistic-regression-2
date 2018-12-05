package com.philips.research.regression;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.RealLinearAlgebra;
import dk.alexandra.fresco.lib.real.SReal;

import java.math.BigDecimal;
import java.util.Vector;

class MatrixVectorApplication implements Application<Vector<BigDecimal>, ProtocolBuilderNumeric> {

    private final Matrix<BigDecimal> matrix;
    private final Vector<BigDecimal> vector;
    private final Transformation transformation;

    MatrixVectorApplication(Matrix<BigDecimal> matrix, Vector<BigDecimal> vector, Transformation transformation) {
        this.matrix = matrix;
        this.vector = vector;
        this.transformation = transformation;
    }

    @Override
    public DRes<Vector<BigDecimal>> buildComputation(ProtocolBuilderNumeric builder) {
        DRes<Matrix<DRes<SReal>>> closedMatrix;
        DRes<Vector<DRes<SReal>>> closedVector, closedResult;
        RealLinearAlgebra real = builder.realLinAlg();
        closedMatrix = real.input(matrix, 1);
        closedVector = real.input(vector, 1);
        closedResult = builder.seq(transformation.transform(closedMatrix, closedVector));
        DRes<Vector<DRes<BigDecimal>>> opened = real.openVector(closedResult);
        return () -> new VectorUtils().unwrapVector(opened);
    }

    interface Transformation {
        Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> transform(
            DRes<Matrix<DRes<SReal>>> matrix,
            DRes<Vector<DRes<SReal>>> vector
        );
    }
}
