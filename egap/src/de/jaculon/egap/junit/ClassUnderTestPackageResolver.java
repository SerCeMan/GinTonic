package de.jaculon.egap.junit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassUnderTestPackageResolver implements MyPackageResolver {

	private final List<MyPackage> testPackagePrefixes;

	public ClassUnderTestPackageResolver(List<MyPackage> testPackagePrefixes) {
		this.testPackagePrefixes = testPackagePrefixes;
	}

	/**
	 * <h1>Example:</h1>
	 *
	 * <pre>
	 * classUnderTestPackageResolver = new ClassUnderTestPackageResolver(
	 * 				new MyPackage("test"),
	 * 				new MyPackage("test", "de"));
	 *
	 * Means: All of my tests are located in a package that either starts with
	 * "test" or "test.de".
	 * </pre>
	 *
	 */
	public ClassUnderTestPackageResolver(MyPackage... testPackagePrefixes) {
		this.testPackagePrefixes = Arrays.asList(testPackagePrefixes);
	}

	/**
	 * Returns all possible packages for the given test package to look for the
	 * classUnderTest.
	 *
	 * This implementation returns all permutations that can be created by
	 * subtracting the testPackagePrefixes from the given package.
	 *
	 * <h1>Example:</h1>
	 *
	 * <pre>
	 * resolver = new ClassUnderTestPackageResolver("test") // all my tests are in a package starting with test
	 * $ resolver.getPossiblePackagesFor("test", "de", "gqhnet")
	 * >> "de", "gqhnet"
	 * </pre>
	 */
	@Override
	public List<MyPackage> getPossiblePackagesFor(MyPackage testPackage) {

		List<MyPackage> myPackages = new ArrayList<MyPackage>(
				testPackagePrefixes.size());

		List<String> packageParts = testPackage.getPackageParts();

		for (MyPackage testPackagePrefix : testPackagePrefixes) {
			List<String> packagePartsOfPrefix = testPackagePrefix.getPackageParts();
			int maxIndex = packagePartsOfPrefix.size();

			int nrOfMatches = 0;
			for (int i = 0; i < packageParts.size(); i++) {
				if (i == maxIndex) {
					break;
				}
				String packagePart = packageParts.get(i);
				String anObject = packagePartsOfPrefix.get(i);
				if (packagePart.equals(anObject)) {
					nrOfMatches++;
				}
			}

			if (nrOfMatches > 0) {
				List<String> restOfPackage = packageParts.subList(
						nrOfMatches,
						packageParts.size());
				myPackages.add(new MyPackage(restOfPackage));
			}
		}

		return myPackages;
	}

}
