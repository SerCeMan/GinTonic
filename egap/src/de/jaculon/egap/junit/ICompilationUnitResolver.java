package de.jaculon.egap.junit;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;

public class ICompilationUnitResolver {

	private OpenJavaProjectsResolver openJavaProjectsResolver;
	private IPackageResolver packageResolver;
	private ITypeNameResolver iTypeNameResolver;
	private ICompilationUnitHelper iCompilationUnitHelper;
	private MyPackageFragmentHelper myPackageHelper;

	private List<MySourceFolder> srcFoldersToLookForMatchingCompilationUnit;

	public ICompilationUnitResolver() {
		super();
	}

	public void setiPackageFragmentHelper(
			MyPackageFragmentHelper iPackageFragmentHelper) {
		this.myPackageHelper = iPackageFragmentHelper;
	}

	public void setiTypeNameResolver(ITypeNameResolver iTypeNameResolver) {
		this.iTypeNameResolver = iTypeNameResolver;
	}

	public void setFoldersToLookForMatchingCompilationUnit(
			List<MySourceFolder> foldersToLookForMatchingCompilationUnit) {
		this.srcFoldersToLookForMatchingCompilationUnit = foldersToLookForMatchingCompilationUnit;
	}

	public void setPackageResolver(IPackageResolver packageResolver) {
		this.packageResolver = packageResolver;
	}

	public void setiCompilationUnitHelper(
			ICompilationUnitHelper iCompilationUnitHelper) {
		this.iCompilationUnitHelper = iCompilationUnitHelper;
	}

	public void setOpenJavaProjectsResolver(
			OpenJavaProjectsResolver openJavaProjectsResolver) {
		this.openJavaProjectsResolver = openJavaProjectsResolver;
	}

	/**
	 * Returns the classUnderTest for the given ICompilationUnit or null if it
	 * could not be found.
	 *
	 * The classUnderTest is resolved using the following algorithm:
	 *
	 * <ol>
	 *
	 * <li>First we check the project of the given compilation unit if we can
	 * find the classUnderTest</li>
	 * <li>If we could not find it then we check all open java projects</li>
	 *
	 * </ol>
	 */
	public ICompilationUnit resolve(ICompilationUnit junitTest) {

		String projectNameOfCompilationUnit = iCompilationUnitHelper.getJavaProjectName(junitTest);

		MyPackage myPackage = myPackageHelper.getPackageFor(junitTest);

		List<MyPackage> packageParts = packageResolver.getPossiblePackagesFor(myPackage);
		String primaryTypeName = iCompilationUnitHelper.getPrimaryTypeName(junitTest);
		List<String> possibleClassUnderTestTypeNames = iTypeNameResolver.getPossibleTypeNamesFor(primaryTypeName);

		ICompilationUnit compilationUnit = doResolve(
				projectNameOfCompilationUnit,
				packageParts,
				possibleClassUnderTestTypeNames);

		if (compilationUnit != null) {
			return compilationUnit;
		}

		List<String> openJavaProjects = openJavaProjectsResolver.resolve(projectNameOfCompilationUnit);
		for (String openJavaProject : openJavaProjects) {
			compilationUnit = doResolve(
					openJavaProject,
					packageParts,
					possibleClassUnderTestTypeNames);

			if (compilationUnit != null) {
				return compilationUnit;
			}
		}

		return null;
	}

	private ICompilationUnit doResolve(String projectName,
			List<MyPackage> myPackages,
			List<String> possibleTypeNames) {
		for (MySourceFolder srcFolder : srcFoldersToLookForMatchingCompilationUnit) {
			for (MyPackage myPackage : myPackages) {
				for (String classUnderTestTypeName : possibleTypeNames) {
					ICompilationUnit classUnderTest = iCompilationUnitHelper.resolve(
							projectName,
							srcFolder,
							myPackage,
							classUnderTestTypeName);
					if (classUnderTest != null) {
						return classUnderTest;
					}
				}
			}
		}
		return null;
	}

}
