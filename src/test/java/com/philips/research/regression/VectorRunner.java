package com.philips.research.regression;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.RealLinearAlgebra;
import dk.alexandra.fresco.lib.real.SReal;

import java.math.BigDecimal;
import java.util.Vector;

class VectorRunner extends Runner<Vector<BigDecimal>> {
    Vector<BigDecimal> run(Matrix<BigDecimal> l, Vector<BigDecimal> b, TransformationMV transformation) {
        return run(builder -> buildTransformation(l, b, transformation, builder));
    }

    private DRes<Vector<BigDecimal>> buildTransformation(Matrix<BigDecimal> l, Vector<BigDecimal> b, TransformationMV transformation, ProtocolBuilderNumeric builder) {
        DRes<Matrix<DRes<SReal>>> closedInputMatrix;
        DRes<Vector<DRes<SReal>>> closedInputVector;
        DRes<Vector<DRes<SReal>>> closedResult;

        RealLinearAlgebra real = builder.realLinAlg();
        closedInputMatrix = real.input(l, 1);
        closedInputVector = real.input(b, 1);
        closedResult = builder.seq(transformation.transform(closedInputMatrix, closedInputVector));
        DRes<Vector<DRes<BigDecimal>>> opened = real.openVector(closedResult);

        return () -> new VectorUtils().unwrapVector(opened);
    }

    interface TransformationMV {
        Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> transform(DRes<Matrix<DRes<SReal>>> m, DRes<Vector<DRes<SReal>>> v);
    }
}
