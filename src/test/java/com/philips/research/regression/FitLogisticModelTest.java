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
import java.util.stream.Collectors;

import static com.philips.research.regression.Runner.run;
import static com.philips.research.regression.util.GenericArrayCreation.newArray;
import static com.philips.research.regression.util.ListAssert.assertEquals;
import static com.philips.research.regression.util.ListConversions.unwrap;
import static com.philips.research.regression.util.MatrixConstruction.matrix;
import static java.math.BigDecimal.valueOf;
import static java.util.Arrays.*;

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

    private static BigDecimal[] hp1 = stream(new Double[]{
        110.0, 110.0, 93.0, 110.0, 175.0, 105.0, 245.0, 62.0,
        95.0, 123.0, 123.0, 180.0, 180.0, 180.0, 205.0, 215.0
    }).map(BigDecimal::valueOf).toArray(BigDecimal[]::new);

    private static BigDecimal[] hp2 = stream(new Double[]{
        230.0, 66.0, 52.0, 65.0, 97.0, 150.0, 150.0, 245.0,
        175.0, 66.0, 91.0, 113.0, 264.0, 175.0, 335.0, 109.0
    }).map(BigDecimal::valueOf).toArray(BigDecimal[]::new);

    private static BigDecimal[] wt1 = stream(new Double[]{
        2.62, 2.875, 2.32, 3.215, 3.44, 3.46, 3.57, 3.19,
        3.15, 3.44, 3.44, 4.07, 3.73, 3.78, 5.25, 5.424
    }).map(BigDecimal::valueOf).toArray(BigDecimal[]::new);

    private static BigDecimal[] wt2 = stream(new Double[]{
        5.345, 2.2, 1.615, 1.835, 2.465, 3.52, 3.435, 3.84,
        3.845, 1.935, 2.14, 1.513, 3.17, 2.77, 3.57, 2.78
    }).map(BigDecimal::valueOf).toArray(BigDecimal[]::new);

    private static BigDecimal[] ones;
    static {
        ones = new BigDecimal[hp1.length];
        fill(ones, BigDecimal.ONE);
    }

    private static Matrix<BigDecimal> X1 = transpose(matrix(new BigDecimal[][]{
        hp1,
        wt1,
        ones
    }));

    private static Matrix<BigDecimal> X2 = transpose(matrix(new BigDecimal[][]{
        hp2,
        wt2,
        ones
    }));

    private static Vector<BigDecimal> am1 = stream(new Double[]{
        1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0,
        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0
    }).map(BigDecimal::valueOf).collect(Collectors.toCollection(Vector::new));

    private static Vector<BigDecimal> am2 = stream(new Double[]{
        0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0,
        0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0
    }).map(BigDecimal::valueOf).collect(Collectors.toCollection(Vector::new));

    static <T> Matrix<T> transpose(Matrix<T> matrix) {
        T[][] transposed = newArray(matrix.getWidth());
        for (int i=0; i<transposed.length; i++) {
            transposed[i] = matrix.getColumn(i).toArray(newArray(matrix.getHeight()));
        }
        return matrix(transposed);
    }
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
