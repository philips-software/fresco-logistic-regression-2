package com.philips.research.regression;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.RealLinearAlgebra;
import dk.alexandra.fresco.lib.real.SReal;

import java.math.BigDecimal;

public class Hessian implements Computation<Matrix<DRes<SReal>>, ProtocolBuilderNumeric> {

    private DRes<Matrix<DRes<SReal>>> input;

    Hessian(DRes<Matrix<DRes<SReal>>> input) {
        this.input = input;
    }

    @Override
    public DRes<Matrix<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
        RealLinearAlgebra algebra = builder.realLinAlg();
        return algebra.scale(
            BigDecimal.valueOf(-0.25),
            algebra.mult(
                algebra.transpose(input),
                input
            )
        );
    }
}
