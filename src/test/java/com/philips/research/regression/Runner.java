package com.philips.research.regression;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestFrameworkException;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.RealLinearAlgebra;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePool;

import java.math.BigDecimal;
import java.util.Vector;

import static dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy.SEQUENTIAL;

class Runner<OutputT> {
    OutputT run(Application<OutputT, ProtocolBuilderNumeric> application) {
        try {
            // run application inside test framework
            return new TestFramework().run(application);
        } catch (TestFrameworkException exception) {
            // strip test framework exceptions to get to the actual exception
            if (exception.getCause().getCause() instanceof RuntimeException) {
                throw (RuntimeException) exception.getCause().getCause();
            } else {
                throw exception;
            }
        }
    }

    private class TestFramework extends AbstractDummyArithmeticTest {
        private OutputT run(Application<OutputT, ProtocolBuilderNumeric> application) {
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
    }

    private class OutputReference {
        OutputT value;
    }
}

class VectorRunner extends Runner<Vector<BigDecimal>> {
    Vector<BigDecimal> run(Matrix<BigDecimal> l, Vector<BigDecimal> b, TransformationMV transformation) {
        return run(builder -> buildTransformation(l, b, transformation, builder));
    }

    private DRes<Vector<BigDecimal>> buildTransformation(Matrix<BigDecimal> l, Vector<BigDecimal> b, TransformationMV transformation, ProtocolBuilderNumeric builder) {
        DRes<Matrix<DRes<SReal>>> closedInputMatrix;
        DRes<Vector<DRes<SReal>>> closedInputVector;
        DRes<Vector<DRes<SReal>>> closedResult;

        RealLinearAlgebra real = builder.realLinAlg();
        closedInputMatrix = real.input(l, 1);
        closedInputVector = real.input(b, 1);
        closedResult = builder.seq(transformation.transform(closedInputMatrix, closedInputVector));
        DRes<Vector<DRes<BigDecimal>>> opened = real.openVector(closedResult);

        return () -> new VectorUtils().unwrapVector(opened);
    }

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

    interface TransformationMV {
        Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> transform(DRes<Matrix<DRes<SReal>>> m, DRes<Vector<DRes<SReal>>> v);
    }

    interface TransformationMVV {
        Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> transform(DRes<Matrix<DRes<SReal>>> m, DRes<Vector<DRes<SReal>>> v1, DRes<Vector<DRes<SReal>>> v2);
    }
}
