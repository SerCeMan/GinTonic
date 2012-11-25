package de.jaculon.egap.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;




public class StringUtils {

	public static final char THE_DOT = '.';
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

	public static final String MAP_TYPE = "java.util.Map";
	public static final String SET_TYPE = "java.util.Set";

	private static final Map<String, String> PRIMITIVES = MapUtils.newHashMap();
	public static final String CLASS_TYPE = "java.lang.Class";
	static {
		PRIMITIVES.put("int", "java.lang.Integer");
		PRIMITIVES.put("double", "java.lang.Double");
		PRIMITIVES.put("float", "java.lang.Float");
		PRIMITIVES.put("long", "java.lang.Long");
		PRIMITIVES.put("boolean", "java.lang.Boolean");
		PRIMITIVES.put("byte", "java.lang.Byte");
		PRIMITIVES.put("char", "java.lang.Character");
		PRIMITIVES.put("short", "java.lang.Short");
	}

	public static String pathToJavaClasspath(String filename) {
		String pathDotted = filename.replace('/', THE_DOT);
		String pathValidClasspath = pathDotted.replace(
				ICompilationUnitUtils.JAVA_EXTENSION,
				"");
		return pathValidClasspath;
	}

	public static String qualifiedNameToSimpleName(
			String targetTypeNameFullyQualified) {
		String[] parts = targetTypeNameFullyQualified.split("\\.");
		String simpleName = parts[parts.length - 1];
		return simpleName;
	}

	/**
	 * Returns the name with a capitalized first character.
	 *
	 * @param name the name to be capitalized
	 * @return the name with a capitalized first character.
	 */
	public static String capitalize(String name) {
		StringBuilder result = new StringBuilder(name.length());
		char upperCase = Character.toUpperCase(name.charAt(0));
		result.append(upperCase);
		result.append(name.substring(1));
		return result.toString();
	}

	/**
	 * Translates primitives like int, double, etc to the fully qualified
	 * wrapper class name(eg. for the primitive "int" you get
	 * "java.lang.Integer"). If the given String is not a primitive type
	 * then itself is returned.
	 */
	public static String translatePrimitiveToWrapper(String primitive) {
		String wrapper = PRIMITIVES.get(primitive);
		if (wrapper != null) {
			return wrapper;
		}
		return primitive;
	}

	public static String getSimpleName(String typeFullyQualified) {
		if(typeFullyQualified == null){
			return null;
		}
		String[] parts = typeFullyQualified.split("\\.");

		return parts[parts.length - 1];
	}

	public static String join(char c, List<String> elements) {
		return join(c, elements, 10);
	}

	public static String join(char c, List<String> elements, int expectedSizeOfElement) {
		int capacity = elements.size() * expectedSizeOfElement + elements.size();
		StringBuilder stringBuilder = new StringBuilder(capacity);
		int i = 0;
		for (String element : elements) {
			if(i++ > 0){
				stringBuilder.append(c);
			}
			stringBuilder.append(element);
		}

		return stringBuilder.toString();
	}

	public static List<String> split(char c, String stringToSplit) {
		String[] splitString = stringToSplit.split("\\" + c);
		return Arrays.asList(splitString);
	}





}
