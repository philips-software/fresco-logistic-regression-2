package com.philips.research.regression;

import com.philips.research.regression.primitives.Cholesky;
import com.philips.research.regression.primitives.Hessian;
import com.philips.research.regression.primitives.LogLikelihoodPrime;
import com.philips.research.regression.primitives.UpdateLearnedModel;
import com.philips.research.regression.util.AddVectors;
import com.philips.research.regression.util.ScaleVector;
import com.philips.research.regression.util.SubtractVectors;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;

import java.math.BigDecimal;
import java.util.List;
import java.util.Vector;

import static com.philips.research.regression.util.MatrixConstruction.identity;
import static com.philips.research.regression.util.MatrixConversions.map;
import static java.math.BigDecimal.valueOf;
import static java.util.Collections.nCopies;

public class FitLogisticModel implements Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> {
    private final List<DRes<Matrix<DRes<SReal>>>> Xs;
    private final List<DRes<Vector<DRes<SReal>>>> Ys;
    private final double lambda;
    private final int numberOfIterations;

    public FitLogisticModel(List<DRes<Matrix<DRes<SReal>>>> Xs, List<DRes<Vector<DRes<SReal>>>> Ys, double lambda, int numberOfIterations) {
        this.Xs = Xs;
        this.Ys = Ys;
        this.lambda = lambda;
        this.numberOfIterations = numberOfIterations;
    }

    @Override
    public DRes<Vector<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
        return builder.seq(seq -> {
            int width = Xs.get(0).out().getWidth();

            DRes<Matrix<DRes<SReal>>> L = seq.seq(new CholeskyDecompositionOfHessian());
            DRes<Vector<DRes<SReal>>> beta = seq.realLinAlg().input(new Vector<>(nCopies(width, valueOf(0))), 1);
            for (int i=0; i<numberOfIterations; i++) {
                beta = seq.seq(new SingleIteration(beta, L));
            }

            return beta;
        });
    }

    private static Matrix<BigDecimal> scale(double factor, Matrix<BigDecimal> matrix) {
        return map(matrix, valueOf(factor)::multiply);
    }

    private class CholeskyDecompositionOfHessian implements Computation<Matrix<DRes<SReal>>, ProtocolBuilderNumeric> {

        @Override
        public DRes<Matrix<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
            return builder.seq(seq -> {
                int width = Xs.get(0).out().getWidth();

                DRes<Matrix<DRes<SReal>>> H = null;
                for (DRes<Matrix<DRes<SReal>>> X: Xs) {
                    DRes<Matrix<DRes<SReal>>> hessian = seq.seq(new Hessian(X));
                    H = H == null ? hessian : seq.realLinAlg().add(H, hessian);
                }

                Matrix<BigDecimal> I = identity(width);
                H = seq.realLinAlg().sub(H, scale(lambda, I));
                return seq.seq(new Cholesky(seq.realLinAlg().scale(valueOf(-1), H)));
            });
        }
    }

    private class SingleIteration implements Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> {
        private DRes<Vector<DRes<SReal>>> beta;
        private DRes<Matrix<DRes<SReal>>> L;

        private SingleIteration(DRes<Vector<DRes<SReal>>> initialBeta, DRes<Matrix<DRes<SReal>>> L) {
            this.beta = initialBeta;
            this.L = L;
        }

        @Override
        public DRes<Vector<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
            return builder.seq(seq -> {
                DRes<Vector<DRes<SReal>>> lprime = null;
                for (int party=1; party<=Xs.size(); party++) {
                    DRes<Matrix<DRes<SReal>>> X = Xs.get(party - 1);
                    DRes<Vector<DRes<SReal>>> Y = Ys.get(party - 1);
                    DRes<Vector<DRes<SReal>>> logLikelihoodPrime = seq.seq(new LogLikelihoodPrime(X, Y, beta));
                    if (lprime == null) {
                        lprime = logLikelihoodPrime;
                    } else {
                        lprime = seq.seq(new AddVectors(lprime, logLikelihoodPrime));
                    }
                }

                lprime = seq.seq(new SubtractVectors(lprime, seq.seq(new ScaleVector(valueOf(lambda), beta))));
                return seq.seq(new UpdateLearnedModel(L, beta, lprime));
            });
        }
    }
}

