package com.philips.research.regression.util;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;

import java.util.Vector;

public class AddVectors implements Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> {

    private final DRes<Vector<DRes<SReal>>> left;
    private final DRes<Vector<DRes<SReal>>> right;

    public AddVectors(DRes<Vector<DRes<SReal>>> left, DRes<Vector<DRes<SReal>>> right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public DRes<Vector<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
        Vector<DRes<SReal>> result = new Vector<>();
        for (int i=0; i<left.out().size(); i++) {
            DRes<SReal> sum = builder.realNumeric().add(left.out().get(i), right.out().get(i));
            result.add(sum);
        }
        return () -> result;
    }
}
