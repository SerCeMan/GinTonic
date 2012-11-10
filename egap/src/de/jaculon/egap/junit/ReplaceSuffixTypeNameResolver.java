package de.jaculon.egap.junit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReplaceSuffixTypeNameResolver implements ITypeNameResolver {

	private final List<String> suffixes;

	public ReplaceSuffixTypeNameResolver(List<String> suffixes) {
		this.suffixes = suffixes;
	}

	public ReplaceSuffixTypeNameResolver(String... suffixes) {
		this.suffixes = Arrays.asList(suffixes);
	}

	/**
	 * Returns a single entry list for the given type name. The type name is
	 * calculated by removing the suffix from the typeName if the typeName ends
	 * with suffix.
	 *
	 * <h1>Example:</h1>
	 *
	 * <pre>
	 * $ resolver = new ReplaceSuffixTypeNameResolver("Test", "Tester");
	 * $ resolver.getPossibleTypeNamesFor("FooTest")
	 * >> ["Foo"]
	 * </pre>
	 */
	@Override
	public List<String> getTypeNamesFor(String typeName) {
		List<String> typeNames = new ArrayList<String>(1);
		for (String testSuffix : suffixes) {
			if (typeName.endsWith(testSuffix)) {
				String classUnderTestName = typeName.replace(testSuffix, "");
				typeNames.add(classUnderTestName);
			}
		}
		return typeNames;
	}

}
