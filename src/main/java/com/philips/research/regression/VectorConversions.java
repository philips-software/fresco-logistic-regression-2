package com.philips.research.regression;

import java.util.Vector;
import java.util.stream.Collectors;

class VectorConversions {
    static <T, U> Vector<U> map(Vector<T> vector, ElementConversion<T,U> conversion) {
        return vector.stream().map(conversion::convert).collect(Collectors.toCollection(Vector::new));
    }

    interface ElementConversion<T,U> {
        U convert(T value);
    }
}
