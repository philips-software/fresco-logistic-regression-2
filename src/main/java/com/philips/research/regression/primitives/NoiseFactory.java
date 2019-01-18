package com.philips.research.regression.primitives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;

import java.util.Vector;

public interface NoiseFactory {
    Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> createNoiseGenerator(DRes<Vector<DRes<SReal>>> input);
}
