package com.philips.research.regression;

import java.util.ArrayList;
import java.util.Vector;
import java.util.stream.Collectors;

class VectorConversions {
    static <T, U> Vector<U> map(Vector<T> vector, ElementConversion<T,U> conversion) {
        ArrayList<U> mapped = vector.stream().map(conversion::convert).collect(Collectors.toCollection(ArrayList::new));
        return new Vector<>(mapped);
    }

    interface ElementConversion<T,U> {
        U convert(T value);
    }
}
