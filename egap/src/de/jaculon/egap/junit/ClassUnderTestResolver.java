package de.jaculon.egap.junit;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;

public class ClassUnderTestResolver {

	private OpenJavaProjectsResolver openJavaProjectsResolver;
	private PackageResolver packageResolver;
	private ICompilationUnitHelper iCompilationUnitHelper;

	private List<MySourceFolder> srcFolderToLookForClassUnderTest;

	public void setSrcFolderToLookForClassUnderTest(
			List<MySourceFolder> srcFolderToLookForClassUnderTest) {
		this.srcFolderToLookForClassUnderTest = srcFolderToLookForClassUnderTest;
	}

	public void setPackageResolver(PackageResolver packageResolver) {
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

		List<MyPackage> packageParts = packageResolver.getPossiblePackagesFor(junitTest);
		List<String> possibleClassUnderTestTypeNames = iCompilationUnitHelper.getPossibleClassUnderTestNamesFor(junitTest);

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
		for (MySourceFolder srcFolder : srcFolderToLookForClassUnderTest) {
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
