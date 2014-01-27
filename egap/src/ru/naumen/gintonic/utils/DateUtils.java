package ru.naumen.gintonic.utils;

public class DateUtils {
	
	/**
	 * Formats the given milliseconds as string readable for a human. 
	 * 
	 * <h1>Example:</h1>
	 * <pre>
	 * 
	 * $ formatMilliseconds(1)
	 * > 1 ms
	 * 
	 * $ formatMilliseconds(999)
	 * > 999 ms
	 * 
	 * $ formatMilliseconds(1000)
	 * > 00:01 min
	 * 
	 * $ formatMilliseconds(1000 * 60)
	 * > 01:00 min
	 * 
	 * </pre>
	 * 
	 * @param ms the milliseconds
	 * @return the milliseconds as string readable for a human.
	 */
	public static String formatMilliseconds(long ms) {

		if (ms < 1000) {
			return ms + " ms";
		}

		return String.format("%1$TM:%1$TS min", ms);
	}
	
}
