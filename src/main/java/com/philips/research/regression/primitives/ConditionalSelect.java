package com.philips.research.regression.primitives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.RealNumeric;
import dk.alexandra.fresco.lib.real.SReal;

public class ConditionalSelect implements Computation<SReal, ProtocolBuilderNumeric> {

    private final DRes<SReal> left;
    private final DRes<SReal> right;
    private final DRes<SInt> condition;

    public ConditionalSelect(DRes<SInt> selector, DRes<SReal> left, DRes<SReal> right) {
        this.condition = selector;
        this.left = left;
        this.right = right;
    }

    @Override
    public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
        RealNumeric numeric = builder.realNumeric();
        DRes<SReal> sub = numeric.sub(left, right);
        DRes<SReal> mult = numeric.mult(numeric.fromSInt(condition), sub);
        return numeric.add(mult, right);
    }
}

