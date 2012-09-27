package egap.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

}
