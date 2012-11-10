package de.jaculon.egap.junit;

import java.util.List;

public interface IPackageResolver {

	/**
	 * Returns all possible packages that can be guessed for the given package.
	 */
	List<MyPackage> getPossiblePackagesFor(MyPackage myPackage);

}