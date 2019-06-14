package com.philips.research.regression.primitives;

import com.philips.research.regression.logging.LoggingNoiseGenerator;
import com.philips.research.regression.util.AddVectors;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;

import java.math.BigDecimal;
import java.util.Vector;

import static com.philips.research.regression.logging.TimestampedMarker.log;

public class UpdateLearnedModel implements Computation<Vector<DRes<SReal>>, ProtocolBuilderNumeric> {
    private final DRes<Matrix<DRes<SReal>>> L;
    private final DRes<Vector<DRes<SReal>>> beta;
    private final DRes<Vector<DRes<SReal>>> l;
    private final NoiseFactory noiseFactory;

    public UpdateLearnedModel(DRes<Matrix<DRes<SReal>>> L,
                              DRes<Vector<DRes<SReal>>> beta,
                              DRes<Vector<DRes<SReal>>> l,
                              BigDecimal epsilon,
                              BigDecimal sensitivity,
                              BigDecimal lambda) {
        this.L = L;
        this.beta = beta;
        this.l = l;
        this.noiseFactory = epsilon != null
//            ? new LaplaceNoiseFactory(epsilon, sensitivity)
            ? new DPNoiseFactory(epsilon, lambda, L.out().getHeight())
            : null;
    }

    UpdateLearnedModel(DRes<Matrix<DRes<SReal>>> L, DRes<Vector<DRes<SReal>>> beta, DRes<Vector<DRes<SReal>>> l,
                       NoiseFactory noiseFactory) {
        this.L = L;
        this.beta = beta;
        this.l = l;
        this.noiseFactory = noiseFactory;
    }

    @Override
    public DRes<Vector<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
        return builder.seq(seq -> {
            log(seq, "Forward Substitution");
            DRes<Vector<DRes<SReal>>> y = seq.seq(new ForwardSubstitution(L, l));
            DRes<Matrix<DRes<SReal>>> LTransposed = builder.realLinAlg().transpose(L);
            log(seq, "Back Substitution");
            DRes<Vector<DRes<SReal>>> r = seq.seq(new BackSubstitution(LTransposed, y));
            DRes<Vector<DRes<SReal>>> updatedBeta = seq.par(new AddVectors(beta, r));
            if (this.noiseFactory != null) {
                log(seq, "Adding noise");
                DRes<Vector<DRes<SReal>>> noise = seq.seq(new LoggingNoiseGenerator(noiseFactory.createNoiseGenerator(updatedBeta)));
                updatedBeta = seq.par(new AddVectors(updatedBeta, noise));
            }
            return updatedBeta;
        });
    }
}

