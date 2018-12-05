package com.philips.research.regression.util;

import dk.alexandra.fresco.framework.DRes;

import java.util.Vector;
import java.util.stream.Collectors;

public class VectorConversions {
    public static <T, U> Vector<U> map(Vector<T> vector, ElementConversion<T,U> conversion) {
        return vector.stream().map(conversion::convert).collect(Collectors.toCollection(Vector::new));
    }

    public static <T> Vector<T> unwrapVector(DRes<Vector<DRes<T>>> vector) {
        return vector.out().stream().map(DRes::out).collect(Collectors.toCollection(Vector::new));
    }

    public interface ElementConversion<T,U> {
        U convert(T value);
    }
}
