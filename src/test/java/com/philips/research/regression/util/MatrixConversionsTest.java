package com.philips.research.regression.util;

import dk.alexandra.fresco.lib.collections.Matrix;
import org.junit.jupiter.api.Test;

import static com.philips.research.regression.util.MatrixConstruction.matrix;
import static com.philips.research.regression.util.MatrixConversions.map;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MatrixConversionsTest {

    @Test
    void mapConversAllElements() {
        Matrix<Integer> input = matrix(
            new Integer[]{1, 2, 3},
            new Integer[]{4, 5, 6});
        Matrix<Integer> output = map(input, (element) -> element + 1);
        Matrix<Integer> expected = matrix(
            new Integer[]{2, 3, 4},
            new Integer[]{5, 6, 7});
        MatrixAssert.assertEquals(expected, output);
    }

    @Test
    void mapReturnsMatrixOfSameDimensions() {
        Matrix<Integer> input = matrix(
            new Integer[]{1, 2, 3},
            new Integer[]{4, 5, 6});
        Matrix<Integer> output = map(input, (element) -> element);
        assertEquals(3, output.getWidth());
        assertEquals(2, output.getHeight());
    }
}
