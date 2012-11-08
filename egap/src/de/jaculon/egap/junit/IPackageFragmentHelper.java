package de.jaculon.egap.junit;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;

public class IPackageFragmentHelper {

	public MyPackage getPackageFor(ICompilationUnit iCompilationUnit){
		IType primaryType = iCompilationUnit.findPrimaryType();
		IPackageFragment packageFragment = primaryType.getPackageFragment();
		String elementName = packageFragment.getElementName();
		if(elementName.isEmpty()){
			return new MyPackage();
		}
		String[] packageParts = elementName.split("\\.");
		return new MyPackage(packageParts);
	}

}
