package com.philips.research.regression.primitives;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Vector;

import static com.philips.research.regression.Runner.run;
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
        Matrix<BigDecimal> X = matrix(new BigDecimal[][]{
            {valueOf(1.0), valueOf(2.0)},
            {valueOf(3.0), valueOf(4.0)}
        });
        Matrix<BigDecimal> H = hessian(X);
        Matrix<BigDecimal> L = choleskyDecomposition(negate(H));
        Vector<BigDecimal> l = new Vector<>(asList(valueOf(7.0), valueOf(8.0)));
        Vector<BigDecimal> beta = new Vector<>(asList(valueOf(5.0), valueOf(6.0)));

        beta = run(new UpdateLearnedModelApplication(L, beta, l));

        assertEquals(asList(valueOf(33.0), valueOf(-12.0)), beta, 0.01);
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

    UpdateLearnedModelApplication(Matrix<BigDecimal> L, Vector<BigDecimal> beta, Vector<BigDecimal> l) {
        this.L = L;
        this.beta = beta;
        this.l = l;
    }

    @Override
    public DRes<Vector<BigDecimal>> buildComputation(ProtocolBuilderNumeric builder) {
        DRes<Matrix<DRes<SReal>>> LClosed = builder.realLinAlg().input(L, 1);
        DRes<Vector<DRes<SReal>>> betaClosed = builder.realLinAlg().input(beta, 1);
        DRes<Vector<DRes<SReal>>> lClosed = builder.realLinAlg().input(l, 1);
        DRes<Vector<DRes<SReal>>> result = builder.seq(new UpdateLearnedModel(LClosed, betaClosed, lClosed));
        DRes<Vector<DRes<BigDecimal>>> opened = builder.realLinAlg().openVector(result);
        return () -> new Vector<>(unwrap(opened));
    }
}
