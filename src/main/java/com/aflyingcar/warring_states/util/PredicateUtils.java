package com.aflyingcar.warring_states.util;

import java.util.function.Predicate;

public class PredicateUtils {
    public static <T> Predicate<T> not(Predicate<T> predicate) {
        return predicate.negate();
    }

    public static <T> Predicate<T> or(Predicate<T> a, Predicate<T> b) {
        return a.or(b);
    }
}
