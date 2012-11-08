package de.jaculon.egap.junit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassUnderTestPackageResolver implements IPackageResolver {

	private final List<MyPackage> testPackagePrefixes;

	public ClassUnderTestPackageResolver(List<MyPackage> testPackagePrefixes) {
		this.testPackagePrefixes = testPackagePrefixes;
	}

	public ClassUnderTestPackageResolver(MyPackage... testPackagePrefixes) {
		this.testPackagePrefixes = Arrays.asList(testPackagePrefixes);
	}

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
				if(i == maxIndex){
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
