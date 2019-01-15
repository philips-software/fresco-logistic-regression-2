package com.philips.research.regression;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static com.philips.research.regression.CarDataSet.*;
import static com.philips.research.regression.Runner.run;
import static com.philips.research.regression.util.ListAssert.assertEquals;
import static com.philips.research.regression.util.ListConversions.unwrap;
import static com.philips.research.regression.util.MatrixConstruction.matrix;
import static com.philips.research.regression.util.MatrixConversions.transpose;
import static java.math.BigDecimal.valueOf;
import static java.util.Arrays.asList;
import static java.util.Arrays.fill;

@DisplayName("Logistic Regression")
class FitLogisticModelTest {

    @Test
    @DisplayName("performs logistic regression")
    void fitsLogisticModel() {
        BigDecimal intercept = valueOf(1.65707);
        BigDecimal beta_hp = valueOf(0.00968555);
        BigDecimal beta_wt = valueOf(-1.17481);

        List<Matrix<BigDecimal>> Xs = asList(X1, X2);
        List<Vector<BigDecimal>> Ys = asList(am1, am2);

        List<BigDecimal> beta = run(new FitLogisticModelApplication(Xs, Ys, 1.0, 5), 2);

        assertEquals(asList(beta_hp, beta_wt, intercept), beta, 0.001);
    }

    private static BigDecimal[] ones;
    static {
        ones = new BigDecimal[hp1.length];
        fill(ones, BigDecimal.ONE);
    }

    private static Matrix<BigDecimal> X1 = transpose(matrix(hp1, wt1, ones));
    private static Matrix<BigDecimal> X2 = transpose(matrix(hp2, wt2, ones));

}

class FitLogisticModelApplication implements Application<List<BigDecimal>, ProtocolBuilderNumeric> {

    private List<Matrix<BigDecimal>> Xs;
    private List<Vector<BigDecimal>> Ys;
    private double lambda;
    private int numberOfIterations;

    FitLogisticModelApplication(List<Matrix<BigDecimal>> Xs, List<Vector<BigDecimal>> Ys, double lambda, int numberOfIterations) {
        this.Xs = Xs;
        this.Ys = Ys;
        this.lambda = lambda;
        this.numberOfIterations = numberOfIterations;
    }

    @Override
    public DRes<List<BigDecimal>> buildComputation(ProtocolBuilderNumeric builder) {
        return builder.seq(seq -> {
            List<DRes<Matrix<DRes<SReal>>>> closedXs = new ArrayList<>();
            List<DRes<Vector<DRes<SReal>>>> closedYs = new ArrayList<>();

            for (int party = 1; party <= Xs.size(); party++) {
                Matrix<BigDecimal> X = Xs.get(party - 1);
                DRes<Matrix<DRes<SReal>>> closedX = seq.realLinAlg().input(X, party);
                closedXs.add(closedX);
            }

            for (int party = 1; party <= Ys.size(); party++) {
                Vector<BigDecimal> Y = Ys.get(party - 1);
                DRes<Vector<DRes<SReal>>> closedY = seq.realLinAlg().input(Y, party);
                closedYs.add(closedY);
            }

            DRes<Vector<DRes<SReal>>> result = seq.seq(new FitLogisticModel(closedXs, closedYs, lambda, numberOfIterations));
            DRes<Vector<DRes<BigDecimal>>> opened = seq.realLinAlg().openVector(result);

            return () -> unwrap(opened);
        });
    }
}
