package com.philips.research.regression;

import java.util.Arrays;

class GenericArrayUtils {
    @SafeVarargs
    static <E> E[] newArray(int length, E... array) {
        return Arrays.copyOf(array, length);
    }
}
