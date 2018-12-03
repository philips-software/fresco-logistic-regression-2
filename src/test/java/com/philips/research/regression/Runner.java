package com.philips.research.regression;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixUtils;
import dk.alexandra.fresco.lib.real.RealLinearAlgebra;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePool;

import java.math.BigDecimal;

import static dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy.SEQUENTIAL;

class Runner<OutputT> extends AbstractDummyArithmeticTest {
    OutputT run(Application<OutputT, ProtocolBuilderNumeric> application) {
        OutputReference output = new OutputReference();
        runTest(new TestThreadRunner.TestThreadFactory<DummyArithmeticResourcePool, ProtocolBuilderNumeric>() {
            @Override
            public TestThreadRunner.TestThread<DummyArithmeticResourcePool, ProtocolBuilderNumeric> next() {
                return new TestThreadRunner.TestThread<DummyArithmeticResourcePool, ProtocolBuilderNumeric>() {
                    @Override
                    public void test() {
                        output.value = runApplication(application);
                    }
                };
            }
        }, SEQUENTIAL, 1);
        return output.value;
    }

    class OutputReference {
        OutputT value;
    }
}

class MatrixRunner extends Runner<Matrix<BigDecimal>> {
    Matrix<BigDecimal> run(Matrix<BigDecimal> input, Transformation transformation) {
        Application<Matrix<BigDecimal>, ProtocolBuilderNumeric> application
            = builder -> buildTransformation(input, transformation, builder);
        return run(application);
    }

    private DRes<Matrix<BigDecimal>> buildTransformation(Matrix<BigDecimal> input, Transformation transformation, ProtocolBuilderNumeric builder) {
        DRes<Matrix<DRes<SReal>>> closed, calculated;
        DRes<Matrix<DRes<BigDecimal>>> opened;

        RealLinearAlgebra real = builder.realLinAlg();

        closed = real.input(input, 1);
        calculated = builder.seq(transformation.transform(closed));
        opened = real.openMatrix(calculated);

        return () -> new MatrixUtils().unwrapMatrix(opened);
    }

    interface Transformation {
        Computation<Matrix<DRes<SReal>>, ProtocolBuilderNumeric> transform(DRes<Matrix<DRes<SReal>>> input);
    }
}
