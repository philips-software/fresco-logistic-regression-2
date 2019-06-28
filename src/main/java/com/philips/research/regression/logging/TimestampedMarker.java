package com.philips.research.regression.logging;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimestampedMarker implements Computation<Void, ProtocolBuilderNumeric> {

    private static final Logger logger = LoggerFactory.getLogger(TimestampedMarker.class);
    private final String message;

    private TimestampedMarker(String message) {
        this.message = message;
    }

    public static void log(ProtocolBuilderNumeric builder, String msg) {
        builder.seq(new TimestampedMarker(msg));
    }

    public static void log(ProtocolBuilderNumeric builder, Computation<String, ProtocolBuilderNumeric> computation) {
        if (logger.isDebugEnabled()) {
            builder
                .seq(computation)
                .seq((seq, result) -> seq.seq(new TimestampedMarker(result)));
        }
    }

    @Override
    public DRes<Void> buildComputation(ProtocolBuilderNumeric builder) {
        return builder.seq(seq -> {
            logger.debug(message);
            return null;
        });
    }
}
