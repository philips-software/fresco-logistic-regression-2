package com.philips.research.regression.app;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixUtils;
import dk.alexandra.fresco.lib.real.SReal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CloseMatrix
    implements ComputationParallel<Matrix<DRes<SReal>>, ProtocolBuilderNumeric> {

    private final Matrix<BigDecimal> openMatrix;
    private final int inputParty;
    private final int height;
    private final int width;
    private final boolean isInputProvider;

    /**
     * See {@link dk.alexandra.fresco.framework.builder.numeric.Collections#closeMatrix(Matrix, int)
     * closeMatrix}.
     */
    private CloseMatrix(Matrix<BigDecimal> openMatrix, int inputParty) {
        super();
        this.openMatrix = openMatrix;
        this.height = openMatrix.getHeight();
        this.width = openMatrix.getWidth();
        this.inputParty = inputParty;
        this.isInputProvider = true;
    }

    /**
     * See {@link dk.alexandra.fresco.framework.builder.numeric.Collections#closeMatrix(int, int, int)
     * closeMatrix}.
     */
    private CloseMatrix(int h, int w, int inputParty) {
        super();
        this.openMatrix = null;
        this.height = h;
        this.width = w;
        this.inputParty = inputParty;
        this.isInputProvider = false;
    }

    private List<DRes<List<DRes<SReal>>>> buildAsProvider(ProtocolBuilderNumeric builder) {
        List<DRes<List<DRes<SReal>>>> closedRows = new ArrayList<>();
        for (List<BigDecimal> row : openMatrix.getRows()) {
            DRes<List<DRes<SReal>>> closedRow = CloseList.close(builder, row, inputParty);
            closedRows.add(closedRow);
        }
        return closedRows;
    }

    private List<DRes<List<DRes<SReal>>>> buildAsReceiver(ProtocolBuilderNumeric builder) {
        List<DRes<List<DRes<SReal>>>> closedRows = new ArrayList<>();
        for (int r = 0; r < height; r++) {
            DRes<List<DRes<SReal>>> closedRow = CloseList.close(builder, width, inputParty);
            closedRows.add(closedRow);
        }
        return closedRows;
    }

    @Override
    public DRes<Matrix<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
        List<DRes<List<DRes<SReal>>>> closed =
            isInputProvider ? buildAsProvider(builder) : buildAsReceiver(builder);
        return () -> new MatrixUtils().unwrapRows(closed);
    }

    static DRes<Matrix<DRes<SReal>>> close(ProtocolBuilderNumeric builder, Matrix<BigDecimal> openMatrix, int inputParty) {
        return builder.par(new CloseMatrix(openMatrix, inputParty));
    }

    static DRes<Matrix<DRes<SReal>>> close(ProtocolBuilderNumeric builder, int h, int w, int inputParty) {
        return builder.par(new CloseMatrix(h, w, inputParty));
    }

}

