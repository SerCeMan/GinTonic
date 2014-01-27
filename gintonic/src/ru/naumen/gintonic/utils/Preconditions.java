package ru.naumen.gintonic.utils;



public class Preconditions {

	public static void checkNotNull(Object o) {
		if(o == null){
			throw new NullPointerException();
		}
	}

	public static void checkNotNull(Object o, String message) {
		if(o == null){
			throw new NullPointerException(message);
		}
	}


}
