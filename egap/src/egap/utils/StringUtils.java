package egap.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



public class StringUtils {

	public static final char THE_DOT = '.';
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	public static final String MAP_TYPE = "java.util.Map";
	public static final String SET_TYPE = "java.util.Set";
	
	private static final String GUICE_BASE_PACKAGE = "com.google.inject";
	public static final String GUICE_ANNOTATION_ASSISTED = GUICE_BASE_PACKAGE
	+ ".assistedinject.Assisted";
	public static final String GUICE_ANNOTATION_INJECT = GUICE_BASE_PACKAGE + ".Inject";
	public static final String GUICE_PROVIDER = GUICE_BASE_PACKAGE + ".Provider";
	public static final String GUICE_BINDING_ANNOTATION = GUICE_BASE_PACKAGE
	+ ".BindingAnnotation";
	public static final String GUICE_ASSISTEDINJECT_FACTORY_PROVIDER = GUICE_BASE_PACKAGE
	+ ".assistedinject.FactoryProvider";
	public static final String GUICE_TYPE_LITERAL = GUICE_BASE_PACKAGE + ".TypeLiteral";
	public static final String GUICE_PROVIDES = GUICE_BASE_PACKAGE + ".Provides";
	public static final String GUICE_MAP_BINDER = "com.google.inject.multibindings.MapBinder";
	public static final String GUICE_SET_BINDER = "com.google.inject.multibindings.Multibinder";
	public static final String GUICE_SCOPED_BINDING_BUILDER = "com.google.inject.binder.ScopedBindingBuilder";
	public static final String GUICE_LINKED_BINDING_BUILDER = "com.google.inject.binder.LinkedBindingBuilder";
	public static final String GUICE_ANNOTATED_BINDING_BUILDER = "com.google.inject.binder.AnnotatedBindingBuilder";
	public static final String GUICE_ABSTRACT_MODULE = "com.google.inject.AbstractModule";
	public static final String GUICE_CONSTANT_BINDING_BUILDER = "com.google.inject.binder.ConstantBindingBuilder";
	public static final String GUICE_CONSTANT_ANNOTATED_BINDING_BUILDER = "com.google.inject.binder.AnnotatedConstantBindingBuilder";
	public static final String GUICE_SINGLETON_SCOPE = "com.google.inject.Singleton";
//	public static final String GUICE_SCOPE_NO_SCOPE = "NO_SCOPE";
	public static final String GUICE_NAMED = "com.google.inject.name.Named";
	
	
	
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
	
	public static String uncapitalize(String name) {
		StringBuilder result = new StringBuilder(name.length());
		char upperCase = Character.toLowerCase(name.charAt(0));
		result.append(upperCase);
		result.append(name.substring(1));
		return result.toString();
	}
	
	/**
	 * Returns the setter method name for the given property name.
	 * 
	 * @param name the name of the property
	 * @return the setter method name
	 */
	public static String toSetterMethodname(String name) {
		return "set" + capitalize(name);
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
