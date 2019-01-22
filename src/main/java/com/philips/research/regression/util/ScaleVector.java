package com.philips.research.regression.util;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;

import java.math.BigDecimal;
import java.util.Vector;

// TODO: remove once this pull request is accepted: https://github.com/aicis/fresco/pull/328
public class ScaleVector implements Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> {

    private final DRes<Vector<DRes<SReal>>> operand;
    private final BigDecimal factor;

    public ScaleVector(BigDecimal factor, DRes<Vector<DRes<SReal>>> operand) {
        this.operand = operand;
        this.factor = factor;
    }

    @Override
    public DRes<Vector<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
        Vector<DRes<SReal>> result = new Vector<>();
        for (DRes<SReal> element: operand.out()) {
            result.add(builder.realNumeric().mult(factor, element));
        }
        return () -> result;
    }
}
