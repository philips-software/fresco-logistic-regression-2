package com.philips.research.regression.util;

import dk.alexandra.fresco.framework.DRes;

import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class ListConversions {
    public static <T, U> List<U> map(List<T> list, ElementConversion<T,U> conversion) {
        return list.stream().map(conversion::convert).collect(Collectors.toList());
    }

    public static <T, L extends List<DRes<T>>> List<T> unwrap(DRes<L> list) {
        return list.out().stream().map(DRes::out).collect(Collectors.toList());
    }

    public static <T, L extends Vector<DRes<T>>> Vector<T> unwrapVector(DRes<L> list) {
        return list.out().stream().map(DRes::out).collect(Collectors.toCollection(Vector::new));
    }

    public interface ElementConversion<T,U> {
        U convert(T value);
    }
}
