package com.philips.research.regression;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;

import java.util.List;
import java.util.Vector;

public class FitLogisticModel implements Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> {
    private final List<DRes<Matrix<DRes<SReal>>>> Xs;
    private final List<DRes<Vector<DRes<SReal>>>> Ys;
    private final double lambda;
    private final int numberOfIterations;

    public FitLogisticModel(List<DRes<Matrix<DRes<SReal>>>> Xs, List<DRes<Vector<DRes<SReal>>>> Ys, double lambda, int numberOfIterations) {
        this.Xs = Xs;
        this.Ys = Ys;
        this.lambda = lambda;
        this.numberOfIterations = numberOfIterations;
    }

    @Override
    public DRes<Vector<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
        return builder.seq(seq -> Vector::new);
    }
}
