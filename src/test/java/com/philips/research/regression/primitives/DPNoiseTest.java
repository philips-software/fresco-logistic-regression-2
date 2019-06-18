package com.philips.research.regression.primitives;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Vector;

import static com.philips.research.regression.Runner.run;
import static com.philips.research.regression.util.ListConversions.unwrap;
import static java.math.BigDecimal.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Meilof DPNoise")
class DPNoiseTest {
    private DoubleSummaryStatistics stats;

    @BeforeEach
    void setUp() {
        List<BigDecimal> noiseVector = run(new DPNoiseTestApp());
        stats = noiseVector
            .stream()
            .mapToDouble(BigDecimal::doubleValue)
            .summaryStatistics();
    }

    @Test
    @DisplayName("mean is close to zero")
    void meanIsCloseToZero() {
        assertEquals(0.0, stats.getAverage(), 0.1);
    }

    @Test
    @DisplayName("it has negative values")
    void hasNegativeValues() {
        assertTrue(stats.getMin() < 0);
    }

    @Test
    @DisplayName("it has positive values")
    void hasPositiveValues() {
        assertTrue(stats.getMax() > 0);
    }
}

class DPNoiseTestApp implements Application<List<BigDecimal>, ProtocolBuilderNumeric> {
    @Override
    public DRes<List<BigDecimal>> buildComputation(ProtocolBuilderNumeric builder) {
        int numInputs = 200;
        DRes<Vector<DRes<SReal>>> result = builder.seq(new DPNoiseGenerator(valueOf(1.0 / 5.0), valueOf(1), 10, numInputs));
        DRes<Vector<DRes<BigDecimal>>> opened = builder.realLinAlg().openVector(result);
        return () -> unwrap(opened);
    }
}
