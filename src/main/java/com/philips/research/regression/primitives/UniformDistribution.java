package com.philips.research.regression.primitives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.RealNumeric;
import dk.alexandra.fresco.lib.real.SReal;

import java.math.BigDecimal;

class UniformDistribution {
    static Computation<SReal, ProtocolBuilderNumeric> random(BigDecimal min, BigDecimal max, int precision) {
        BigDecimal scale = max.subtract(min);
        return builder -> {
            RealNumeric r = builder.realNumeric();
            DRes<SReal> unscaled = builder.seq(random(precision));
            return r.add(min, r.mult(scale, unscaled));
        };
    }

    static Computation<SReal, ProtocolBuilderNumeric> random(int precision) {
        return builder -> builder.realAdvanced().random(precision);
    }
}
