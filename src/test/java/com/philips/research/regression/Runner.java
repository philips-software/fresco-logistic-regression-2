package com.philips.research.regression;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner;
import dk.alexandra.fresco.framework.builder.BuildStep;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.collections.MatrixUtils;
import dk.alexandra.fresco.lib.real.RealLinearAlgebra;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticResourcePool;

import java.math.BigDecimal;
import java.util.Vector;

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
        return run(builder -> buildTransformation(input, transformation, builder));
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

class VectorRunner extends Runner<Vector<BigDecimal>> {
    Vector<BigDecimal> run(Matrix<BigDecimal> l, Vector<BigDecimal> b, Transformation transformation) {
        return run(builder -> buildTransformation(l, b, transformation, builder));
    }

    private DRes<Vector<BigDecimal>> buildTransformation(Matrix<BigDecimal> l, Vector<BigDecimal> b, Transformation transformation, ProtocolBuilderNumeric builder) {
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

    interface Transformation {
        Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> transform(DRes<Matrix<DRes<SReal>>> m, DRes<Vector<DRes<SReal>>> v);
    }
}

class BigDecimalRunner extends Runner<BigDecimal> {
    BigDecimal run(Vector<BigDecimal> v1, Vector<BigDecimal> v2, Transformation transformation) {
        return run(builder -> buildTransformation(v1, v2, transformation, builder));
    }

    private DRes<BigDecimal> buildTransformation(Vector<BigDecimal> v1, Vector<BigDecimal> v2, Transformation transformation, ProtocolBuilderNumeric builder) {
        DRes<Vector<DRes<SReal>>> closedV1, closedV2;

        RealLinearAlgebra real = builder.realLinAlg();
        closedV1 = real.input(v1, 1);
        closedV2 = real.input(v2, 1);
        DRes<SReal> closedResult = builder.seq(transformation.transform(closedV1, closedV2));
        DRes<BigDecimal> opened = builder.realNumeric().open(closedResult);
        return opened::out;
    }

    interface Transformation {
        Computation<SReal, ProtocolBuilderNumeric> transform(DRes<Vector<DRes<SReal>>> m, DRes<Vector<DRes<SReal>>> v);
    }
}
