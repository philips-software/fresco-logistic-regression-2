package com.philips.research.regression;

import dk.alexandra.fresco.framework.DRes;

import java.util.Vector;
import java.util.stream.Collectors;

class VectorUtils {
    public <T> Vector<T> unwrapVector(DRes<Vector<DRes<T>>> vector) {
        return vector.out().stream().map(DRes::out).collect(Collectors.toCollection(Vector::new));
    }
}
