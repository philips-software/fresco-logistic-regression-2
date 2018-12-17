package com.philips.research.regression.app;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.RealNumeric;
import dk.alexandra.fresco.lib.real.SReal;

import java.math.BigDecimal;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class CloseVector implements ComputationParallel<Vector<DRes<SReal>>, ProtocolBuilderNumeric> {

    private final Vector<BigDecimal> openInputs;
    private final int numberOfInputs;
    private final int inputParty;
    private final boolean isInputProvider;

    /**
     * See {@link dk.alexandra.fresco.framework.builder.numeric.Collections#closeList(List, int)
     * closeList}.
     */
    private CloseVector(Vector<BigDecimal> openInputs, int inputParty) {
        super();
        this.openInputs = openInputs;
        this.numberOfInputs = openInputs.size();
        this.inputParty = inputParty;
        this.isInputProvider = true;
    }

    /**
     * See {@link dk.alexandra.fresco.framework.builder.numeric.Collections#closeList(int, int)
     * closeList}.
     */
    private CloseVector(int numberOfInputs, int inputParty) {
        super();
        this.openInputs = new Vector<>();
        this.numberOfInputs = numberOfInputs;
        this.inputParty = inputParty;
        this.isInputProvider = false;
    }

    private Vector<DRes<SReal>> buildAsProvider(RealNumeric numeric) {
        return openInputs.stream().map(openInput -> numeric.input(openInput, inputParty))
            .collect(Collectors.toCollection(Vector::new));
    }

    private Vector<DRes<SReal>> buildAsReceiver(RealNumeric numeric) {
        Vector<DRes<SReal>> closed = new Vector<>();
        for (int i = 0; i < numberOfInputs; i++) {
            closed.add(numeric.input(new BigDecimal(0), inputParty));
        }
        return closed;
    }

    @Override
    public DRes<Vector<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
        RealNumeric numeric = builder.realNumeric();
        Vector<DRes<SReal>> closedInputs = isInputProvider
            ? buildAsProvider(numeric)
            : buildAsReceiver(numeric);
        return () -> closedInputs;
    }

    static DRes<Vector<DRes<SReal>>> close(ProtocolBuilderNumeric builder, Vector<BigDecimal> openList, int inputParty) {
        return builder.par(new CloseVector(openList, inputParty));
    }

    static DRes<Vector<DRes<SReal>>> close(ProtocolBuilderNumeric builder, int numberOfInputs, int inputParty) {
        return builder.par(new CloseVector(numberOfInputs, inputParty));
    }
}
