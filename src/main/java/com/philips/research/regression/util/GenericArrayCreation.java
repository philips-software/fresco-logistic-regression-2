package com.philips.research.regression.util;

import java.util.Arrays;

public class GenericArrayCreation {
    @SafeVarargs
    public static <E> E[] newArray(int length, E... array) {
        return Arrays.copyOf(array, length);
    }
}
