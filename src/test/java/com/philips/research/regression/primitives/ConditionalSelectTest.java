package com.philips.research.regression.primitives;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.philips.research.regression.Runner.run;
import static java.math.BigDecimal.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Conditional Select")
public class ConditionalSelectTest {

    @Test
    @DisplayName("it selects the first element when condition is true")
    public void selectFirstElement() {
        BigDecimal result = run(new SelectApplication(true, 1.0, 2.0));
        assertEquals(1.0, result.doubleValue(), 0.001);
    }

    @Test
    @DisplayName("it selects the second element when condition is false")
    public void selectSecondElement() {
        BigDecimal result = run(new SelectApplication(false, 1.0, 2.0));
        assertEquals(2.0, result.doubleValue(), 0.001);
    }
}

class SelectApplication implements Application<BigDecimal, ProtocolBuilderNumeric> {
    private final boolean condition;
    private final double whenTrue;
    private final double whenFalse;

    public SelectApplication(boolean condition, double whenTrue, double whenFalse) {
        this.condition = condition;
        this.whenTrue = whenTrue;
        this.whenFalse = whenFalse;
    }

    @Override
    public DRes<BigDecimal> buildComputation(ProtocolBuilderNumeric builder) {
        DRes<SInt> closedCondition = builder.numeric().input(condition ? 1 : 0, 1);
        DRes<SReal> closedWhenTrue = builder.realNumeric().input(valueOf(whenTrue), 1);
        DRes<SReal> closedWhenFalse = builder.realNumeric().input(valueOf(whenFalse), 1);
        DRes<SReal> closedResult = builder.seq(new ConditionalSelect(closedCondition, closedWhenTrue, closedWhenFalse));
        return builder.realNumeric().open(closedResult);
    }
}

