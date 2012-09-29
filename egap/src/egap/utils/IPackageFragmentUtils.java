package egap.utils;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

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

	/**
	 * Creates a new package in the given project and returns a reference to it.
	 * If the package already exists, this has no effect.
	 * 
	 * @param javaProject the java project where we create the package
	 * @param srcFolderAsString the name of the src folder where to put the
	 *            package (e.g for tests this is maybe "src-test")
	 * @param packageFullyQualified the package we are going to create as
	 *            dot-separated package name
	 * @param progressMonitor a progress monitor. Can be null.
	 * @return the package fragment or null if the
	 * 
	 * @throws JavaModelException
	 */
	public static IPackageFragment createPackageFragment(
			IJavaProject javaProject, String srcFolderAsString,
			String packageFullyQualified, IProgressMonitor progressMonitor)
			throws JavaModelException {
		IPackageFragmentRoot[] srcFoldersInProject = javaProject.getPackageFragmentRoots();
		for (IPackageFragmentRoot srcFolder : srcFoldersInProject) {
			String srcFolderName = srcFolder.getElementName();
			if (srcFolderName.equals(srcFolderAsString)) {
				IPackageFragment packageFragment = srcFolder.createPackageFragment(
						packageFullyQualified,
						false,
						progressMonitor);
				return packageFragment;

			}
		}
		return null;
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
