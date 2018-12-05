package com.philips.research.regression;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;

import java.util.Arrays;
import java.util.Vector;

import static java.math.BigDecimal.ZERO;

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
    public DRes<Vector<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
        DRes<SReal>[] result = GenericArrayUtils.newArray(beta.out().size());
        Arrays.fill(result, builder.realNumeric().known(ZERO));
        for (int k=0; k<result.length; k++) {
            for (int i=0; i<x.out().getRows().size(); i++) {
                int finalI = i;
                result[k] =
                    builder.realNumeric().add(
                        result[k],
                        builder.realNumeric().mult(
                            builder.realNumeric().sub(
                                y.out().get(i),
                                builder.seq(new Likelihood(() -> new Vector<>(x.out().getRow(finalI)), beta))
                            ),
                            x.out().getRow(i).get(k)
                        )
                    );
            }
        }
        return () -> new Vector<>(Arrays.asList(result));
    }
}
