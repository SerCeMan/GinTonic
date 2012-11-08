package de.jaculon.egap.junit;

import java.util.List;

public interface IPackageResolver {

	List<MyPackage> getPossiblePackagesFor(MyPackage myPackage);

}