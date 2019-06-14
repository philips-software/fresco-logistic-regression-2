package com.philips.research.regression.primitives;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;

import java.math.BigDecimal;
import java.util.Vector;

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
    private int length;

    public DPNoiseFactory(BigDecimal epsilon, BigDecimal lambda, int length) {
        this.epsilon = epsilon;
        this.lambda = lambda;
        this.length = length;
    }

    @Override
    public Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> createNoiseGenerator(DRes<Vector<DRes<SReal>>> input) {
        return new DPNoiseGenerator(input, epsilon, lambda, length);
    }
}

class DPNoiseGenerator implements Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> {
    private final DRes<Vector<DRes<SReal>>> input;
    private final BigDecimal epsilon;
    private final BigDecimal lambda;
    private int length;

    public DPNoiseGenerator(DRes<Vector<DRes<SReal>>> input, BigDecimal epsilon, BigDecimal lambda, int length) {
        this.input = input;
        this.epsilon = epsilon;
        this.lambda = lambda;
        this.length = length;
    }

    @Override
    public DRes<Vector<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
        int nv = input.out().size();
        double shape = nv;
        double scale = 2.0 / (length * epsilon.doubleValue() * lambda.doubleValue());
        DRes<SReal> noiseLen = builder.seq(GammaDistribution.random(shape, scale, builder.getRealNumericContext().getPrecision()));

        Vector<DRes<SReal>> noise = new Vector<>();
        DRes<SReal> sumOfSquares = builder.realNumeric().known(valueOf(0));
        for (int i = 0; i < nv; ++i) {
            DRes<SReal> rand = builder.seq(NormalDistribution.random(builder.getRealNumericContext().getPrecision()));
            noise.add(rand);
            DRes<SReal> square = builder.realNumeric().mult(rand, rand);
            sumOfSquares = builder.realNumeric().add(sumOfSquares, square);
        }
        DRes<SReal> norm = builder.seq(new RealNumericSqrt(sumOfSquares));
        DRes<SReal> normInverse = builder.realNumeric().div(builder.realNumeric().known(valueOf(1)), norm);
        DRes<SReal> normInverseTimesNoiseLen = builder.realNumeric().mult(normInverse, noiseLen);
        for (int i = 0; i < nv; ++i) {
            DRes<SReal> scaled = builder.realNumeric().mult(noise.get(i), normInverseTimesNoiseLen);
            noise.set(i, scaled);
        }

        return () -> noise;
    }
}
