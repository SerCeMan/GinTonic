package de.jaculon.egap.junit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassUnderTestTypeNameResolver implements ITypeNameResolver {

	private final List<String> testSuffixes;

	public ClassUnderTestTypeNameResolver(List<String> testSuffixes) {
		this.testSuffixes = testSuffixes;
	}

	public ClassUnderTestTypeNameResolver(String... testSuffixes) {
		this.testSuffixes = Arrays.asList(testSuffixes);
	}

	/**
	 * Returns the type name for the classUnderTest for the given unitTestName.
	 * The type name is calculated by removing the test suffix from the
	 * unitTestName if the unitTestName ends with test suffix.
	 *
	 * * <h1>Example:</h1>
	 *
	 * <pre>
	 * $ resolver = new ClassUnderTestTypeNameResolver("Test", "Tester");
	 * $ resolver.getPossibleTypeNamesFor("FooTest")
	 * >> ["Foo"]
	 * </pre>
	 */
	@Override
	public List<String> getPossibleTypeNamesFor(String unitTestName) {
		List<String> typeNames = new ArrayList<String>();
		for (String testSuffix : testSuffixes) {
			if (unitTestName.endsWith(testSuffix)) {
				String classUnderTestName = unitTestName.replace(testSuffix, "");
				typeNames.add(classUnderTestName);
			}
		}
		return typeNames;
	}

}
