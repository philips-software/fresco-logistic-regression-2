package com.philips.research.regression;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.RealLinearAlgebra;
import dk.alexandra.fresco.lib.real.SReal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Vector;

import static com.philips.research.regression.MatrixConstruction.matrix;
import static java.math.BigDecimal.valueOf;
import static java.util.Arrays.asList;

class LogLikelihoodPrimeTest {
    private VectorRunner2 runner = new VectorRunner2();

    @Test
    @DisplayName("first derivative of log likelihood")
    void logLikelihoodPrime() {
        Matrix<BigDecimal> x = matrix(new BigDecimal[][]{
            {valueOf(1.0), valueOf(2.0), valueOf(3.0), valueOf(4.0)},
            {valueOf(1.1), valueOf(2.2), valueOf(3.3), valueOf(4.4)}
        });
        Vector<BigDecimal> y = new Vector<>(asList(valueOf(0.0), valueOf(1.0)));
        Vector<BigDecimal> beta = new Vector<>(asList(valueOf(0.1), valueOf(0.2), valueOf(0.3), valueOf(0.4)));
        Vector<BigDecimal> expected = new Vector<>(asList(valueOf(-0.9134458), valueOf(-1.826892), valueOf(-2.740337), valueOf(-3.653783)));
        Vector<BigDecimal> result = runner.run(x, y, beta, LogLikelihoodPrime::new);
        VectorAssert.assertEquals(expected, result, 3);
    }
}

class VectorRunner2 extends Runner<Vector<BigDecimal>> {
    Vector<BigDecimal> run(Matrix<BigDecimal> m, Vector<BigDecimal> v1, Vector<BigDecimal> v2, TransformationMVV transformation) {
        return run(builder-> buildTransformation(m, v1, v2, transformation, builder));
    }

    private DRes<Vector<BigDecimal>> buildTransformation(Matrix<BigDecimal> m, Vector<BigDecimal> v1, Vector<BigDecimal> v2, TransformationMVV transformation, ProtocolBuilderNumeric builder) {
        DRes<Matrix<DRes<SReal>>> closedM;
        DRes<Vector<DRes<SReal>>> closedV1;
        DRes<Vector<DRes<SReal>>> closedV2;
        DRes<Vector<DRes<SReal>>> closedResult;

        RealLinearAlgebra real = builder.realLinAlg();
        closedM = real.input(m, 1);
        closedV1 = real.input(v1, 1);
        closedV2 = real.input(v2, 1);
        closedResult = builder.seq(transformation.transform(closedM, closedV1, closedV2));
        DRes<Vector<DRes<BigDecimal>>> opened = real.openVector(closedResult);

        return () -> new VectorUtils().unwrapVector(opened);
    }

    interface TransformationMVV {
        Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> transform(DRes<Matrix<DRes<SReal>>> m, DRes<Vector<DRes<SReal>>> v1, DRes<Vector<DRes<SReal>>> v2);
    }
}
