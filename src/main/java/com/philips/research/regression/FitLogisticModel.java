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
            log(seq, "Started computation");
            int width = Xs.get(0).out().getWidth();

            log(seq, "Compute hessian");
            DRes<Matrix<DRes<SReal>>> H = null;
            for (DRes<Matrix<DRes<SReal>>> X : Xs) {
                DRes<Matrix<DRes<SReal>>> hessian = seq.seq(new Hessian(X));
                H = H == null ? hessian : seq.realLinAlg().add(H, hessian);
            }

            log(seq, "Cholesky");
            Matrix<BigDecimal> I = identity(width);
            H = seq.realLinAlg().sub(H, scale(lambda, I));
            DRes<Matrix<DRes<SReal>>> L = seq.seq(new Cholesky(seq.realLinAlg().scale(valueOf(-1), H)));

            DRes<Vector<DRes<SReal>>> beta = seq.realLinAlg().input(new Vector<>(nCopies(width, valueOf(0))), 1);
            for (int i=0; i<numberOfIterations; i++) {
                log(seq, "Iteration " + i);
                DRes<Vector<DRes<SReal>>> lprime = null;
                for (int party=1; party<=Xs.size(); party++) {
                    log(seq, "    logLikelihoodPrime " + party);
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
                log(seq, "Update learned model");
                beta = seq.seq(new UpdateLearnedModel(L, beta, lprime));
            }

            log(seq, "Finished computation");

            return beta;
        });
    }

    private void log(ProtocolBuilderNumeric builder, String msg) {
        builder.seq(new TimestampedMarker(msg));
    }

    private static Matrix<BigDecimal> scale(double factor, Matrix<BigDecimal> matrix) {
        return map(matrix, valueOf(factor)::multiply);
    }
}

