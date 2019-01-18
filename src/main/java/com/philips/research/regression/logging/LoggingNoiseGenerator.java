package com.philips.research.regression.logging;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;

import java.math.BigDecimal;
import java.util.Vector;

import static com.philips.research.regression.util.ListConversions.unwrapVector;

public class LoggingNoiseGenerator implements Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> {

    private final Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> noiseGenerator;

    public LoggingNoiseGenerator(Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> noiseGenerator) {
        this.noiseGenerator = noiseGenerator;
    }

    @Override
    public DRes<Vector<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
        DRes<Vector<DRes<SReal>>> noise = noiseGenerator.buildComputation(builder);
        return builder.seq(seq -> {
            DRes<Vector<DRes<BigDecimal>>> openedNoise = seq.realLinAlg().openVector(noise);
            return () -> openedNoise;
        }).seq((seq, openedNoise) -> {
            Vector<BigDecimal> unwrapped = unwrapVector(openedNoise);
            TimestampedMarker.log(seq, "        used noise: " + unwrapped);
            return noise;
        });
    }
}
