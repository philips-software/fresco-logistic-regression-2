package com.philips.research.regression.primitives;

import com.philips.research.regression.util.VectorUtils;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class LocalLogLikelihoodPrime {
    private final Matrix<BigDecimal> x;
    private final Vector<BigDecimal> y;
    private final Vector<BigDecimal> beta;

    public LocalLogLikelihoodPrime(Matrix<BigDecimal> x, Vector<BigDecimal> y, Vector<BigDecimal> beta) {
        this.x = x;
        this.y = y;
        this.beta = beta;
    }

    public Vector<BigDecimal> compute() {
        BigDecimal[] result = new BigDecimal[beta.size()];
        Arrays.fill(result, BigDecimal.ZERO);
        for (int k = 0; k < beta.size(); ++k) {
            for (int i = 0; i < x.getHeight(); ++i) {
                result[k] = result[k]
                    .add(
                        y.get(i)
                            .subtract(likelihood(x.getRow(i), beta))
                            .multiply(x.getRow(i).get(k))
                    );
            }
        }
        return new Vector<>(Arrays.asList(result));
    }

    static BigDecimal likelihood(List<BigDecimal> v1, List<BigDecimal> v2) {
        BigDecimal product = VectorUtils.multiply(v1, v2);
        BigDecimal exponential = new BigDecimal(Math.exp(product.negate().doubleValue()));
        BigDecimal plusOne = exponential.add(BigDecimal.ONE);
        return BigDecimal.ONE.divide(plusOne, 15, RoundingMode.HALF_UP);
    }
}
