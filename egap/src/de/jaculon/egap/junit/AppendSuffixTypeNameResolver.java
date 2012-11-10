package de.jaculon.egap.junit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppendSuffixTypeNameResolver implements ITypeNameResolver {

	private final List<String> suffixes;

	public AppendSuffixTypeNameResolver(List<String> suffixes) {
		this.suffixes = suffixes;
	}

	public AppendSuffixTypeNameResolver(String... suffixes) {
		this.suffixes = Arrays.asList(suffixes);
	}

	/**
	 * Returns a list of type names for the given type name. The type names are
	 * calculated by appending the suffixes to the typeName.
	 *
	 * <h1>Example:</h1>
	 *
	 * <pre>
	 * $ resolver = new AppendSuffixTypeNameResolver("Test", "Tester");
	 * $ resolver.getPossibleTypeNamesFor("Foo")
	 * >> ["FooTest", "FooTester"]
	 * </pre>
	 */
	@Override
	public List<String> getTypeNamesFor(String typeName) {
		List<String> typeNames = new ArrayList<String>(suffixes.size());
		for (String testSuffix : suffixes) {
			String newTypeName = typeName + testSuffix;
			typeNames.add(newTypeName);
		}
		return typeNames;
	}

}
