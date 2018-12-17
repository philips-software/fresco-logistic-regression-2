package com.philips.research.regression.app;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.RealNumeric;
import dk.alexandra.fresco.lib.real.SReal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CloseList implements ComputationParallel<List<DRes<SReal>>, ProtocolBuilderNumeric> {

    private final List<BigDecimal> openInputs;
    private final int numberOfInputs;
    private final int inputParty;
    private final boolean isInputProvider;

    /**
     * See {@link dk.alexandra.fresco.framework.builder.numeric.Collections#closeList(List, int)
     * closeList}.
     */
    private CloseList(List<BigDecimal> openInputs, int inputParty) {
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
    private CloseList(int numberOfInputs, int inputParty) {
        super();
        this.openInputs = new ArrayList<>();
        this.numberOfInputs = numberOfInputs;
        this.inputParty = inputParty;
        this.isInputProvider = false;
    }

    private List<DRes<SReal>> buildAsProvider(RealNumeric numeric) {
        return openInputs.stream().map(openInput -> numeric.input(openInput, inputParty))
            .collect(Collectors.toList());
    }

    private List<DRes<SReal>> buildAsReceiver(RealNumeric numeric) {
        List<DRes<SReal>> closed = new ArrayList<>();
        for (int i = 0; i < numberOfInputs; i++) {
            closed.add(numeric.input(null, inputParty));
        }
        return closed;
    }

    @Override
    public DRes<List<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
        RealNumeric numeric = builder.realNumeric();
        List<DRes<SReal>> closedInputs = isInputProvider
            ? buildAsProvider(numeric)
            : buildAsReceiver(numeric);
        return () -> closedInputs;
    }

    static DRes<List<DRes<SReal>>> close(ProtocolBuilderNumeric builder, List<BigDecimal> openList, int inputParty) {
        return builder.par(new CloseList(openList, inputParty));
    }

    static DRes<List<DRes<SReal>>> close(ProtocolBuilderNumeric builder, int numberOfInputs, int inputParty) {
        return builder.par(new CloseList(numberOfInputs, inputParty));
    }
}


