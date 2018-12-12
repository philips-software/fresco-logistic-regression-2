package com.philips.research.regression;

import java.math.BigDecimal;
import java.util.Vector;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toCollection;

/**
 * An example data set, used for testing.
 *
 * The data was extracted from the 1974 Motor Trend US magazine, and comprises
 * fuel consumption and 10 aspects of automobile design and performance for 32
 * automobiles (1973–74 models).
 *
 * https://rdrr.io/r/datasets/mtcars.html
 */
class CarDataSet {

    static BigDecimal[] hp1 = stream(new Double[]{
        110.0, 110.0, 93.0, 110.0, 175.0, 105.0, 245.0, 62.0,
        95.0, 123.0, 123.0, 180.0, 180.0, 180.0, 205.0, 215.0
    }).map(BigDecimal::valueOf).toArray(BigDecimal[]::new);
    static BigDecimal[] hp2 = stream(new Double[]{
        230.0, 66.0, 52.0, 65.0, 97.0, 150.0, 150.0, 245.0,
        175.0, 66.0, 91.0, 113.0, 264.0, 175.0, 335.0, 109.0
    }).map(BigDecimal::valueOf).toArray(BigDecimal[]::new);

    static BigDecimal[] wt1 = stream(new Double[]{
        2.62, 2.875, 2.32, 3.215, 3.44, 3.46, 3.57, 3.19,
        3.15, 3.44, 3.44, 4.07, 3.73, 3.78, 5.25, 5.424
    }).map(BigDecimal::valueOf).toArray(BigDecimal[]::new);
    static BigDecimal[] wt2 = stream(new Double[]{
        5.345, 2.2, 1.615, 1.835, 2.465, 3.52, 3.435, 3.84,
        3.845, 1.935, 2.14, 1.513, 3.17, 2.77, 3.57, 2.78
    }).map(BigDecimal::valueOf).toArray(BigDecimal[]::new);

    static Vector<BigDecimal> am1 = stream(new Double[]{
        1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0,
        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0
    }).map(BigDecimal::valueOf).collect(toCollection(Vector::new));

    static Vector<BigDecimal> am2 = stream(new Double[]{
        0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0,
        0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0
    }).map(BigDecimal::valueOf).collect(toCollection(Vector::new));
}
