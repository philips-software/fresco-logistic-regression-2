package com.philips.research.regression;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixUtils;
import dk.alexandra.fresco.lib.real.RealLinearAlgebra;
import dk.alexandra.fresco.lib.real.SReal;

import java.math.BigDecimal;

class MatrixRunner extends Runner<Matrix<BigDecimal>> {
    Matrix<BigDecimal> run(Matrix<BigDecimal> input, Transformation transformation) {
        return run(builder -> buildTransformation(input, transformation, builder));
    }

    private DRes<Matrix<BigDecimal>> buildTransformation(Matrix<BigDecimal> input, Transformation transformation, ProtocolBuilderNumeric builder) {
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
