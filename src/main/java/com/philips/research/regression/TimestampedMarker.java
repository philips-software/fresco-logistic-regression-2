package com.philips.research.regression;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TimestampedMarker implements Computation<Void, ProtocolBuilderNumeric> {

    private static final Logger logger = LoggerFactory.getLogger(TimestampedMarker.class);
    private final String message;

    TimestampedMarker(String message) {
        this.message = message;
    }

    @Override
    public DRes<Void> buildComputation(ProtocolBuilderNumeric builder) {
        return builder.seq(seq -> {
            if (seq.getBasicNumericContext().getMyId() == 1) {
                logger.debug(message);
            }
            return null;
        });
    }
}
