package com.philips.research.regression.primitives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.propability.SampleLaplaceDistribution;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Vector;

import static java.math.BigDecimal.valueOf;

class LaplaceNoiseGenerator implements Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> {

    private final DRes<Vector<DRes<SReal>>> input;
    private final BigDecimal epsilon;
    private final BigDecimal sensitivity;

    LaplaceNoiseGenerator(DRes<Vector<DRes<SReal>>> input, BigDecimal epsilon, BigDecimal sensitivity) {
        this.input = input;
        this.epsilon = epsilon;
        this.sensitivity = sensitivity;
    }

    @Override
    public DRes<Vector<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
        return builder.seq(seq -> {
            Vector<DRes<SReal>> noiseVector = new Vector<>();
            BigDecimal b = sensitivity
                .divide(epsilon, 15, RoundingMode.HALF_UP)
                .divide(valueOf(input.out().size()), 15, RoundingMode.HALF_UP);
            for (int i = 0; i < input.out().size(); ++i) {
                DRes<SReal> noise = seq.seq(new SampleLaplaceDistribution(b));
                noiseVector.add(noise);
            }
            return () -> noiseVector;
        });
    }
}
