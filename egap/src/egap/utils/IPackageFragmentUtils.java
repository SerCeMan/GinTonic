package egap.utils;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class IPackageFragmentUtils {

	private static final Splitter SPLIT_ON_DOT = Splitter.on('.');
	private static final Joiner JOIN_ON_DOT = Joiner.on('.');
	
	public static List<IPackageFragment> getParentPackages(IPackageFragment packageFragment, int ascendLevel){
		List<IPackageFragment> parentPackages = Lists.newArrayList();
		getParentPackages(packageFragment, ascendLevel, parentPackages);
		return parentPackages;
	}

	private static void getParentPackages(IPackageFragment packageFragment, int ascendLevel, List<IPackageFragment> parentPackages){
		if (ascendLevel > 0) {
			IPackageFragment parentPackage = getParentPackage(packageFragment);
			if (parentPackage != null) {
				parentPackages.add(parentPackage);
				getParentPackages(parentPackage, ascendLevel - 1, parentPackages);
			}
		}
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
		
		/* This is way to complicated. If you see this code and know the answer please mail me! */
		LinkedList<String> pathOfChild = Lists.newLinkedList(SPLIT_ON_DOT.split(elementName));
		List<String> pathOfParent = pathOfChild.subList(
				0,
				pathOfChild.size() - 1);
		String pathToParentAsDottedName = JOIN_ON_DOT.join(pathOfParent);

		IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) packageFragment.getParent();
		IPackageFragment packageFragmentParent = packageFragmentRoot.getPackageFragment(pathToParentAsDottedName);

		return packageFragmentParent;
	}

	private static boolean isSpecialPackage(String elementName) {
		return elementName.length() == 0 /* the empty package */
				|| elementName.equals("java.lang");
	}

}
