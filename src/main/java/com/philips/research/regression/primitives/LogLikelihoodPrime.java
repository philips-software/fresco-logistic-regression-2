package com.philips.research.regression.primitives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;

import java.util.Arrays;
import java.util.Vector;

import static com.philips.research.regression.util.GenericArrayCreation.newArray;
import static java.math.BigDecimal.ZERO;
import static java.util.Arrays.asList;

public class LogLikelihoodPrime implements Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> {
    private final DRes<Matrix<DRes<SReal>>> x;
    private final DRes<Vector<DRes<SReal>>> y, beta;

    public LogLikelihoodPrime(DRes<Matrix<DRes<SReal>>> x, DRes<Vector<DRes<SReal>>> y, DRes<Vector<DRes<SReal>>> beta) {
        this.x = x;
        this.y = y;
        this.beta = beta;
    }

    @Override
    public DRes<Vector<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
        int betaSize = beta.out().size();
        return builder.seq(seq -> {
            DRes<SReal>[] result = newArray(betaSize);
            Arrays.fill(result, seq.realNumeric().known(ZERO));
            return () -> new Pair<>(result, 0);
        }).whileLoop(input -> input.getSecond() < betaSize, (seq, input) -> {
            DRes<SReal>[] result = input.getFirst();
            int k = input.getSecond();
            DRes<SReal> resultK = seq.par(new SingleIteration(k));
            result[k] = resultK;
            return () -> new Pair<>(result, k + 1);
        }).seq((seq, input) -> () -> new Vector<>(asList(input.getFirst())));
    }

    private class SingleIteration implements ComputationParallel<SReal, ProtocolBuilderNumeric> {
        private int k;
        private DRes<SReal> resultK;

        SingleIteration(int k) {
            this.k = k;
        }

        @Override
        public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
            return builder.seq(seq -> {
                resultK = seq.realNumeric().known(ZERO);
                for (int i = 0; i < x.out().getRows().size(); i++) {
                    int finalI = i;
                    resultK =
                        seq.realNumeric().add(
                            resultK,
                            seq.realNumeric().mult(
                                seq.realNumeric().sub(
                                    y.out().get(i),
                                    seq.seq(new Likelihood(() -> new Vector<>(x.out().getRow(finalI)), beta))
                                ),
                                x.out().getRow(i).get(k)
                            )
                        );
                }
                return resultK;
            });
        }
    }
}
