package ru.naumen.gintonic.utils;

import java.util.List;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;

public class IPackageFragmentUtils {

	public static List<IPackageFragment> getParentPackages(
			IPackageFragment packageFragment, int ascendLevel) {
		List<IPackageFragment> parentPackages = ListUtils.newArrayList();
		getParentPackages(packageFragment, ascendLevel, parentPackages);
		return parentPackages;
	}

	public static IPackageFragment getParentPackage(
			IPackageFragment packageFragment) {
		if (packageFragment instanceof IPackageFragmentRoot) {
			return null;
		}

		String elementName = packageFragment.getElementName();

		if (isSpecialPackage(elementName)) {
			return null;
		}

		/*
		 * This is way to complicated. If you see this code and know the answer
		 * please mail me!
		 */
		List<String> pathOfChild = StringUtils.split('.', elementName);
		List<String> pathOfParent = pathOfChild.subList(
				0,
				pathOfChild.size() - 1);
		String pathToParentAsDottedName = StringUtils.join('.', pathOfParent);

		IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) packageFragment.getParent();
		IPackageFragment packageFragmentParent = packageFragmentRoot.getPackageFragment(pathToParentAsDottedName);

		return packageFragmentParent;
	}

	private static void getParentPackages(IPackageFragment packageFragment,
			int ascendLevel, List<IPackageFragment> parentPackages) {
		if (ascendLevel > 0) {
			IPackageFragment parentPackage = getParentPackage(packageFragment);
			if (parentPackage != null) {
				parentPackages.add(parentPackage);
				getParentPackages(
						parentPackage,
						ascendLevel - 1,
						parentPackages);
			}
		}
	}

	private static boolean isSpecialPackage(String elementName) {
		return elementName.length() == 0 /* the empty package */
				|| elementName.equals("java.lang");
	}

}
