package de.jaculon.egap.junit;

import java.util.ArrayList;
import java.util.List;

public class ClassUnderTestTypeNameResolver implements TypeNameResolver {

	private final List<String> testSuffixes;

	public ClassUnderTestTypeNameResolver(List<String> testSuffixes) {
		this.testSuffixes = testSuffixes;
	}

	@Override
	public List<String> getPossibleTypeNamesFor(
			String unitTestName) {
		List<String> typeNames = new ArrayList<String>();
		for (String testSuffix : testSuffixes) {
			if(unitTestName.endsWith(testSuffix)){
				String classUnderTestName = unitTestName.replace(testSuffix,"");
				typeNames.add(classUnderTestName);
			}
		}
		return typeNames;
	}

}
