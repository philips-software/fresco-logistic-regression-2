package com.philips.research.regression;

import java.util.Arrays;

class GenericArrayCreation {
    @SafeVarargs
    static <E> E[] newArray(int length, E... array) {
        return Arrays.copyOf(array, length);
    }
}
