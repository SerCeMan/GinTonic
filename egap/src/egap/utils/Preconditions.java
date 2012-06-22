package egap.utils;



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

	public static void checkState(boolean state) {
		if(!state){
			throw new IllegalStateException();
		}
	}

	public static void checkState(boolean state, String message) {
		if(!state){
			throw new IllegalStateException(message);
		}
	}

}
