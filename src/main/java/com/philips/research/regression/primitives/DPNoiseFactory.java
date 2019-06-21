package com.philips.research.regression.primitives;

import com.philips.research.regression.util.AddVectors;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.RealNumeric;
import dk.alexandra.fresco.lib.real.SReal;

import java.math.BigDecimal;
import java.util.Vector;

import static com.philips.research.regression.logging.TimestampedMarker.log;
import static java.math.BigDecimal.valueOf;

/* Implements DPNoise from "logreg.pdf":

   DpNoise[epsilon_, lambda_, len_] := Block[{nv, noiselen, vec},
       (* calculate random noise: Chaudhuri, Alg 1 *)
       nv = Length[X[[1]]];
       (* select length *)
       noiselen = RandomVariate[GammaDistribution[nv, 2 / (len epsilon lambda)]]; (* create random nv-dimensional sphere *)
       vec = Table[RandomVariate[NormalDistribution[0, 1]], {i, 1, nv}];
       Return[vec / Norm[vec] * noiselen]
   ];
   DpNoise[epsilon_] := DpNoise[epsilon, 1, Length[X]];

 */

public class DPNoiseFactory implements NoiseFactory {
    private final BigDecimal epsilon;
    private final BigDecimal lambda;
    private final int numVars;
    private final int numberOfInputs;
    private final int numParties;

    DPNoiseFactory(BigDecimal epsilon, BigDecimal lambda, int numVars, int numberOfInputs, int numParties) {
        this.epsilon = epsilon;
        this.lambda = lambda;
        this.numVars = numVars;
        this.numberOfInputs = numberOfInputs;
        this.numParties = numParties;
    }

    @Override
    public Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> createNoiseGenerator(DRes<Vector<DRes<SReal>>> input) {
        return new TotalNTimes(
            numParties,
            () -> new DPNoiseGenerator(epsilon, lambda, numVars, numberOfInputs));
    }
}

interface ComputationCreator {
    Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> createComputation();
}

class TotalNTimes implements Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> {
    private final int n;
    private final ComputationCreator computationCreator;

    TotalNTimes(int n, ComputationCreator computationCreator) {
        this.n = n;
        this.computationCreator = computationCreator;
    }

    @Override
    public DRes<Vector<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
        return builder.seq(seq -> {
            DRes<Vector<DRes<SReal>>> sum = seq.seq(computationCreator.createComputation());
            for (int i = 1; i < n; ++i) {
                DRes<Vector<DRes<SReal>>> next = seq.seq(computationCreator.createComputation());
                sum = seq.seq(new AddVectors(sum, next));
            }
            return sum;
        });
    }
}

class DPNoiseGenerator implements Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> {
    private final BigDecimal epsilon;
    private final BigDecimal lambda;
    private final int numVars;
    private final int numberOfInputs;

    DPNoiseGenerator(BigDecimal epsilon, BigDecimal lambda, int numVars, int numberOfInputs) {
        this.epsilon = epsilon;
        this.lambda = lambda;
        this.numVars = numVars;
        this.numberOfInputs = numberOfInputs;
    }

    @Override
    public DRes<Vector<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
        RealNumeric r = builder.realNumeric();
        int precision = builder.getRealNumericContext().getPrecision();
        double scale = 2.0 / (numberOfInputs * epsilon.doubleValue() * lambda.doubleValue());

        log(builder, "Generating noise with epsilon " + epsilon
            + ", lambda " + lambda
            + ", shape " + numVars
            + ", scale " + scale);

        DRes<SReal> noiseLen = builder.seq(GammaDistribution.random(numVars, scale, precision));

        Vector<DRes<SReal>> noise = new Vector<>();
        DRes<SReal> sumOfSquares = r.known(valueOf(0));
        for (int i = 0; i < numVars; ++i) {
            DRes<SReal> rand = builder.seq(NormalDistribution.random(precision));
            noise.add(rand);
            DRes<SReal> square = r.mult(rand, rand);
            sumOfSquares = r.add(sumOfSquares, square);
        }

        DRes<SReal> norm = builder.seq(new RealNumericSqrt(sumOfSquares));
        DRes<SReal> noiseLenDividedByNorm = r.div(noiseLen, norm);
        for (int i = 0; i < numVars; ++i) {
            DRes<SReal> scaled = r.mult(noise.get(i), noiseLenDividedByNorm);
            noise.set(i, scaled);
        }

        return () -> noise;
    }
}
