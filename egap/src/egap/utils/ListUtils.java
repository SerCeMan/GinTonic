package egap.utils;

import java.util.ArrayList;
import java.util.LinkedList;

public class ListUtils {

	public static <E> ArrayList<E> newArrayListWithCapacity(int initialArraySize) {
		return new ArrayList<E>(initialArraySize);
	}

	public static <E> ArrayList<E> newArrayList() {
		return new ArrayList<E>();
	}

	public static <E> LinkedList<E> newLinkedList() {
		return new LinkedList<E>();
	}

	public static <E> ArrayList<E> newArrayList(E... elements) {
		ArrayList<E> list = new ArrayList<E>(elements.length);
		for (E e : elements) {
			list.add(e);
		}
		return list;
	}

}
