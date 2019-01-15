package com.philips.research.regression.util;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;

import java.util.Vector;

abstract class BinaryVectorOperation implements Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> {

    private final DRes<Vector<DRes<SReal>>> left;
    private final DRes<Vector<DRes<SReal>>> right;

    BinaryVectorOperation(DRes<Vector<DRes<SReal>>> left, DRes<Vector<DRes<SReal>>> right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public DRes<Vector<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
        Vector<DRes<SReal>> result = new Vector<>();
        for (int i=0; i<left.out().size(); i++) {
            DRes<SReal> element = combine(builder, left.out().get(i), right.out().get(i));
            result.add(element);
        }
        return () -> result;
    }

    abstract DRes<SReal> combine(ProtocolBuilderNumeric builder, DRes<SReal> left, DRes<SReal> right);
}
