package com.philips.research.regression;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixUtils;
import dk.alexandra.fresco.lib.real.RealLinearAlgebra;
import dk.alexandra.fresco.lib.real.SReal;

import java.math.BigDecimal;

class MatrixTransformation implements Application<Matrix<BigDecimal>, ProtocolBuilderNumeric> {

    private final Transformation transformation;
    private final Matrix<BigDecimal> input;

    MatrixTransformation(Matrix<BigDecimal> input, Transformation transformation) {
        this.transformation = transformation;
        this.input = input;
    }

    @Override
    public DRes<Matrix<BigDecimal>> buildComputation(ProtocolBuilderNumeric builder) {
        DRes<Matrix<DRes<SReal>>> closed, calculated;
        DRes<Matrix<DRes<BigDecimal>>> opened;

        RealLinearAlgebra real = builder.realLinAlg();

        closed = real.input(input, 1);
        calculated = builder.seq(transformation.transform(closed));
        opened = real.openMatrix(calculated);

        return () -> new MatrixUtils().unwrapMatrix(opened);
    }

    interface Transformation {
        Computation<Matrix<DRes<SReal>>, ProtocolBuilderNumeric> transform(DRes<Matrix<DRes<SReal>>> input);
    }
}
