package com.philips.research.regression.primitives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.RealNumeric;
import dk.alexandra.fresco.lib.real.SReal;

import java.math.BigInteger;

import static com.philips.research.regression.primitives.BigIntegerBooleans.FALSE;
import static com.philips.research.regression.primitives.BigIntegerBooleans.isFalse;
import static java.lang.Math.sqrt;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.valueOf;

class GammaDistribution {
    static Computation<SReal, ProtocolBuilderNumeric> random(double shape, double scale, int precision) {
        // Implements https://www.hongliangjie.com/2012/12/19/how-to-generate-gamma-random-variables/

        assert(shape >= 1);
        double d = shape - 1./3.;
        double c = 1. / sqrt(9. * d);
        return builder -> builder
            .seq(seq -> {
                IterationState state = new IterationState();
                state.ok = () -> FALSE;
                return () -> state;
            }).whileLoop(
                state -> isFalse(state.ok.out()),
                (seq, previousState) -> {
                    IterationState state = new IterationState();
                    RealNumeric r = seq.realNumeric();

                    DRes<SReal> Z = seq.seq(NormalDistribution.random(precision));
                    DRes<SReal> minZ = r.known(valueOf(-1. / c));
                    Z = seq.seq(new ConditionalSelect(r.leq(minZ, Z), Z, r.known(valueOf(0)))); // Z = condselect(Z>-1/c, Z, 0)

                    DRes<SReal> U = seq.seq(UniformDistribution.random(precision));
                    DRes<SReal> quberootV = r.add(ONE, r.mult(valueOf(c), Z));
                    state.V = r.mult(quberootV, r.mult(quberootV, quberootV));

                    DRes<SReal> logU = seq.realAdvanced().log(U);
                    DRes<SReal> logV = seq.realAdvanced().log(state.V);
                    DRes<SReal> Zsquared = r.mult(Z, Z);
                    DRes<SReal> minLogU =
                        r.add(
                            r.sub(
                                r.add(
                                    valueOf(d),
                                    r.mult(valueOf(0.5), Zsquared)
                                ),
                                r.mult(valueOf(d), state.V)
                            ),
                            r.mult(
                                valueOf(d),
                                logV
                            )
                        );

                    state.ok = seq.numeric().open(
                        seq.numeric().mult( // AND
                            r.leq(minZ, Z), // TODO: replace by b-trick (condselect)
                            r.leq(minLogU, logU)
                        )
                    );
                    return () -> state;
                }
            ).seq((seq, state) -> {
                RealNumeric r = seq.realNumeric();
                return r.div(r.mult(valueOf(d), state.V), valueOf(scale));
            });
    }

    private static class IterationState {
        DRes<BigInteger> ok;
        DRes<SReal> V;
    }
}
