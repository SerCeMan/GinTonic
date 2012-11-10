package de.jaculon.egap.junit;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;

public class MyPackageFragmentHelper {

	/**
	 * Returns the package of the given ICompilationUnit.
	 */
	public MyPackage getPackageFor(ICompilationUnit iCompilationUnit) {
		IType primaryType = iCompilationUnit.findPrimaryType();
		IPackageFragment packageFragment = primaryType.getPackageFragment();
		String elementName = packageFragment.getElementName();
		if (elementName.isEmpty()) {
			return new MyPackage();
		}
		String[] packageParts = elementName.split("\\.");
		return new MyPackage(packageParts);
	}

}
