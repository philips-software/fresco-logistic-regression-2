package com.philips.research.regression;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;

import java.util.Vector;

public class LogLikelihoodPrime implements Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> {
    private final DRes<Matrix<DRes<SReal>>> x;
    private final DRes<Vector<DRes<SReal>>> y;
    private final DRes<Vector<DRes<SReal>>> beta;

    LogLikelihoodPrime(DRes<Matrix<DRes<SReal>>> x, DRes<Vector<DRes<SReal>>> y, DRes<Vector<DRes<SReal>>> beta) {
        this.x = x;
        this.y = y;
        this.beta = beta;
    }

    @Override
    public DRes<Vector<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric protocolBuilderNumeric) {
        return y;
    }
}
