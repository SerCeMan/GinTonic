package de.jaculon.egap.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SetUtils {
    
    public static <E> Set<E> immutableSetOf(E... elements) {
        return Collections.unmodifiableSet(newHashSet(elements));
    }

	public static <E> HashSet<E> newHashSet(E... elements) {
		int capacity = elements.length;
		HashSet<E> set = new HashSet<E>(capacity);
		for (E e : elements) {
			set.add(e);
		}
		return set;
	}

}
