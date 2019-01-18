package com.philips.research.regression.primitives;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Vector;

import static com.philips.research.regression.Runner.run;
import static com.philips.research.regression.util.BigDecimalUtils.arrayOf;
import static com.philips.research.regression.util.ListAssert.assertEquals;
import static com.philips.research.regression.util.ListConversions.unwrap;
import static com.philips.research.regression.util.MatrixConstruction.matrix;
import static java.math.BigDecimal.valueOf;
import static java.util.Arrays.asList;

@DisplayName("Learned model updates")
class UpdateLearnedModelTest {

    @Test
    @DisplayName("updates learned model using previous value and first derivative")
    void updatesLearnedModel() {
        Matrix<BigDecimal> X = matrix(
            arrayOf(1.0, 2.0),
            arrayOf(3.0, 4.0));
        Matrix<BigDecimal> H = hessian(X);
        Matrix<BigDecimal> L = choleskyDecomposition(negate(H));
        Vector<BigDecimal> l = new Vector<>(asList(valueOf(7.0), valueOf(8.0)));
        Vector<BigDecimal> beta = new Vector<>(asList(valueOf(5.0), valueOf(6.0)));

        beta = run(new UpdateLearnedModelApplication(L, beta, l, null));

        assertEquals(asList(valueOf(33.0), valueOf(-12.0)), beta, 0.005);
    }

    @Test
    @DisplayName("updates learned model using differential privacy")
    void updatesLearnedModelUsingDifferentialPrivacy() {
        Matrix<BigDecimal> X = matrix(
            arrayOf(1.0, 2.0),
            arrayOf(3.0, 4.0));
        Matrix<BigDecimal> H = hessian(X);
        Matrix<BigDecimal> L = choleskyDecomposition(negate(H));
        Vector<BigDecimal> l = new Vector<>(asList(valueOf(7.0), valueOf(8.0)));
        Vector<BigDecimal> beta = new Vector<>(asList(valueOf(5.0), valueOf(6.0)));

        beta = run(new UpdateLearnedModelApplication(L, beta, l, new HalfOffDummyNoiseFactory()));

        Assertions.assertEquals(33.0 + 0.5, beta.get(0).doubleValue(), 0.005);
        Assertions.assertEquals(-12.0 + 0.5, beta.get(1).doubleValue(), 0.005);
    }

    private Matrix<BigDecimal> hessian(Matrix<BigDecimal> matrix) {
        return run(new MatrixTransformation(matrix, Hessian::new));
    }

    private Matrix<BigDecimal> choleskyDecomposition(Matrix<BigDecimal> matrix) {
        return run(new MatrixTransformation(matrix, Cholesky::new));
    }

    private Matrix<BigDecimal> negate(Matrix<BigDecimal> matrix) {
        return run(new MatrixTransformation(matrix, input -> builder ->
            builder.realLinAlg().scale(valueOf(-1.0), input))
        );
    }
}

class UpdateLearnedModelApplication implements Application<Vector<BigDecimal>, ProtocolBuilderNumeric> {

    private final Matrix<BigDecimal> L;
    private final Vector<BigDecimal> beta;
    private final Vector<BigDecimal> l;
    private final NoiseFactory noiseFactory;

    UpdateLearnedModelApplication(Matrix<BigDecimal> L, Vector<BigDecimal> beta, Vector<BigDecimal> l, NoiseFactory noiseFactory) {
        this.L = L;
        this.beta = beta;
        this.l = l;
        this.noiseFactory = noiseFactory;
    }

    @Override
    public DRes<Vector<BigDecimal>> buildComputation(ProtocolBuilderNumeric builder) {
        DRes<Matrix<DRes<SReal>>> LClosed = builder.realLinAlg().input(L, 1);
        DRes<Vector<DRes<SReal>>> betaClosed = builder.realLinAlg().input(beta, 1);
        DRes<Vector<DRes<SReal>>> lClosed = builder.realLinAlg().input(l, 1);
        DRes<Vector<DRes<SReal>>> result;
        if (noiseFactory != null) {
            result = builder.seq(new UpdateLearnedModel(LClosed, betaClosed, lClosed, noiseFactory));
        } else {
            result = builder.seq(new UpdateLearnedModel(LClosed, betaClosed, lClosed));
        }
        DRes<Vector<DRes<BigDecimal>>> opened = builder.realLinAlg().openVector(result);
        return () -> new Vector<>(unwrap(opened));
    }
}

class HalfOffDummyNoiseFactory implements NoiseFactory {

    @Override
    public Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> createNoiseGenerator(DRes<Vector<DRes<SReal>>> input) {
        return new HalfAdder(input);
    }

}

class HalfAdder implements Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> {

    private final DRes<Vector<DRes<SReal>>> input;

    public HalfAdder(DRes<Vector<DRes<SReal>>> input) {
        this.input = input;
    }

    @Override
    public DRes<Vector<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
        return builder.seq(seq -> {
            Vector<DRes<SReal>> v = new Vector<>();
            DRes<SReal> half = seq.realNumeric().known(valueOf(0.5));
            for (int i = 0; i < input.out().size(); ++i) {
                v.add(half);
            }
            return () -> v;
        });
    }
}
