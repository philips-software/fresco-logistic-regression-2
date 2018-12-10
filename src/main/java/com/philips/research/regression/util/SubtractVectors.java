package com.philips.research.regression.util;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;

import java.util.Vector;

public class SubtractVectors extends BinaryVectorOperation {

    public SubtractVectors(DRes<Vector<DRes<SReal>>> left, DRes<Vector<DRes<SReal>>> right) {
        super(left, right);
    }

    protected DRes<SReal> combine(ProtocolBuilderNumeric builder, DRes<SReal> left, DRes<SReal> right) {
        return builder.realNumeric().sub(left, right);
    }
}
