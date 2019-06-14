package com.philips.research.regression.primitives;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.DoubleSummaryStatistics;
import java.util.Vector;

import static com.philips.research.regression.Runner.run;
import static com.philips.research.regression.primitives.RandomGammaApplication.SCALE;
import static com.philips.research.regression.primitives.RandomGammaApplication.SHAPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Gamma Distribution")
class GammaDistributionTests {

    private static final double MEAN = SHAPE * SCALE;
    private static DoubleSummaryStatistics stats;

    @BeforeAll
    static void setUp() {
        stats = run(new RandomGammaApplication())
            .stream()
            .map(DRes::out)
            .mapToDouble(BigDecimal::doubleValue)
            .summaryStatistics();
    }

    @Test
    @DisplayName("does not generate negative numbers")
    void noNegativeNumbers() {
        assertTrue(stats.getMin() >= 0);
    }

    @Test
    @DisplayName("generates numbers smaller than the mean")
    void smallerThanMean() {
        assertTrue(stats.getMin() < MEAN);
    }

    @Test
    @DisplayName("generates numbers larger than the mean")
    void largerThanMean() {
        assertTrue(stats.getMax() > MEAN);
    }

    @Test
    @DisplayName("has correct mean")
    void mean() {
        assertEquals(MEAN,  stats.getAverage(), 0.2);
    }
}

class RandomGammaApplication implements Application<Vector<DRes<BigDecimal>>, ProtocolBuilderNumeric> {

    static final double SHAPE = 9;
    static final double SCALE = 0.5;

    @Override
    public DRes<Vector<DRes<BigDecimal>>> buildComputation(ProtocolBuilderNumeric builder) {
        Vector<DRes<SReal>> randomNumbers = new Vector<>();
        for (int i=0; i<100; i++) {
            DRes<SReal> random = builder.seq(GammaDistribution.random(SHAPE, SCALE, builder.getRealNumericContext().getPrecision()));
            randomNumbers.add(random);
        }
        return builder.realLinAlg().openVector(() -> randomNumbers);
    }
}


