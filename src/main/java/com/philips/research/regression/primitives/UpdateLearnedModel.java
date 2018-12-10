package com.philips.research.regression.primitives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;

import java.util.Vector;

public class UpdateLearnedModel implements Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> {
    private final DRes<Matrix<DRes<SReal>>> L;
    private final DRes<Vector<DRes<SReal>>> beta;
    private final DRes<Vector<DRes<SReal>>> l;

    public UpdateLearnedModel(DRes<Matrix<DRes<SReal>>> L, DRes<Vector<DRes<SReal>>> beta, DRes<Vector<DRes<SReal>>> l) {
        this.L = L;
        this.beta = beta;
        this.l = l;
    }

    @Override
    public DRes<Vector<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
        return builder.seq(seq -> {
            DRes<Vector<DRes<SReal>>> y = seq.seq(new ForwardSubstitution(L, l));
            DRes<Matrix<DRes<SReal>>> LTransposed = builder.realLinAlg().transpose(L);
            DRes<Vector<DRes<SReal>>> r = seq.seq(new BackSubstitution(LTransposed, y));
            return seq.seq(new AddVectors(beta, r));
        });
    }
}

class AddVectors implements Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> {

    private final DRes<Vector<DRes<SReal>>> left;
    private final DRes<Vector<DRes<SReal>>> right;

    AddVectors(DRes<Vector<DRes<SReal>>> left, DRes<Vector<DRes<SReal>>> right) {
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