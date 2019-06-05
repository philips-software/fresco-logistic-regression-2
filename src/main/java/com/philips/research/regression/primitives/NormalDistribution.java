package com.philips.research.regression.primitives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.RealNumeric;
import dk.alexandra.fresco.lib.real.SReal;

import java.math.BigDecimal;
import java.math.BigInteger;

import static java.math.BigDecimal.valueOf;

class NormalDistribution {
    static Computation<SReal, ProtocolBuilderNumeric> random(int precision) {
        return builder -> builder
            .seq(seq -> {
                IterationState state = new IterationState();
                state.ok = () -> FALSE;
                return () -> state;
            })
            .whileLoop(
                state -> isFalse(state.ok.out()),
                (seq, previousState) -> {
                    RealNumeric r = seq.realNumeric();
                    IterationState state = new IterationState();
                    state.w1 = seq.seq(UniformDistribution.random(valueOf(-1), valueOf(1), precision));
                    state.w2 = seq.seq(UniformDistribution.random(valueOf(-1), valueOf(1), precision));
                    state.v = r.add(r.mult(state.w1, state.w1), r.mult(state.w2, state.w2));
                    state.ok = seq.numeric().open(r.leq(state.v, r.known(BigDecimal.ONE)));
                    return () -> state;
                }
            ).seq((seq, state) -> {
                RealNumeric r = seq.realNumeric();
                DRes<SReal> s = seq.seq(new RealNumericSqrt(state.v));
                DRes<SReal> logv = seq.realAdvanced().log(state.v);
                DRes<SReal> lsquared = r.mult(r.known(valueOf(-2)), logv);
                DRes<SReal> l = seq.seq(new RealNumericSqrt(lsquared));
                return r.mult(l, r.div(state.w1, s));
            });
    }

    private static class IterationState {
        DRes<BigInteger> ok;
        DRes<SReal> w1;
        DRes<SReal> w2;
        DRes<SReal> v;
    }

    private static BigInteger TRUE = BigInteger.ONE;
    private static BigInteger FALSE = BigInteger.ZERO;

    private static boolean isTrue(BigInteger i) {
        return i.compareTo(TRUE) == 0;
    }

    private static boolean isFalse(BigInteger i) {
        return !isTrue(i);
    }
}
