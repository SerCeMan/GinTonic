package de.jaculon.egap.utils;

import java.util.HashSet;

public class SetUtils {

	public static <E> HashSet<E> newHashSet() {
		return new HashSet<E>();
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
