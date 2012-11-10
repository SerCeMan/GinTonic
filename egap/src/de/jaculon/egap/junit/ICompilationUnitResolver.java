package de.jaculon.egap.junit;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;

import de.jaculon.egap.helper.ICompilationUnitHelper;
import de.jaculon.egap.helper.ProjectHelper;

/**
 * An {@link ICompilationUnitResolver} tries to find a matching ICompilationUnit
 * for a given ICompilationUnit.
 *
 * A typical usage is to find a junit test class for a classUnderTest.
 *
 *
 * @author tmajunke
 */
public class ICompilationUnitResolver {

	private ProjectHelper openJavaProjectsResolver;
	private MyPackageResolver packageResolver;
	private ITypeNameResolver typeNameResolver;
	private ICompilationUnitHelper iCompilationUnitHelper;
	private MyPackageHelper myPackageHelper;

	private List<MySourceFolder> srcFoldersToLookForMatchingCompilationUnit;

	protected ICompilationUnitResolver() {
		super();
	}

	public void setiPackageFragmentHelper(
			MyPackageHelper iPackageFragmentHelper) {
		this.myPackageHelper = iPackageFragmentHelper;
	}

	public void setiTypeNameResolver(ITypeNameResolver iTypeNameResolver) {
		this.typeNameResolver = iTypeNameResolver;
	}

	public void setFoldersToLookForMatchingCompilationUnit(
			List<MySourceFolder> foldersToLookForMatchingCompilationUnit) {
		this.srcFoldersToLookForMatchingCompilationUnit = foldersToLookForMatchingCompilationUnit;
	}

	public void setPackageResolver(MyPackageResolver packageResolver) {
		this.packageResolver = packageResolver;
	}

	public void setiCompilationUnitHelper(
			ICompilationUnitHelper iCompilationUnitHelper) {
		this.iCompilationUnitHelper = iCompilationUnitHelper;
	}

	public void setOpenJavaProjectsResolver(
			ProjectHelper openJavaProjectsResolver) {
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
	public ICompilationUnit resolve(ICompilationUnit iCompilationUnit) {

		String projectNameOfCompilationUnit = iCompilationUnitHelper.getJavaProjectName(iCompilationUnit);

		MyPackage myPackage = myPackageHelper.getPackageFor(iCompilationUnit);

		List<MyPackage> packageParts = packageResolver.getPossiblePackagesFor(myPackage);
		String primaryTypeName = iCompilationUnitHelper.getPrimaryTypeName(iCompilationUnit);
		List<String> possibleClassUnderTestTypeNames = typeNameResolver.getPossibleTypeNamesFor(primaryTypeName);

		ICompilationUnit resolvedCompilationUnit = doResolve(
				projectNameOfCompilationUnit,
				packageParts,
				possibleClassUnderTestTypeNames);

		if (resolvedCompilationUnit != null) {
			return resolvedCompilationUnit;
		}

		List<String> openJavaProjects = openJavaProjectsResolver.findOpenJavaProjects(projectNameOfCompilationUnit);
		for (String openJavaProject : openJavaProjects) {
			resolvedCompilationUnit = doResolve(
					openJavaProject,
					packageParts,
					possibleClassUnderTestTypeNames);

			if (resolvedCompilationUnit != null) {
				return resolvedCompilationUnit;
			}
		}

		return null;
	}

	private ICompilationUnit doResolve(String projectName,
			List<MyPackage> myPackages, List<String> possibleTypeNames) {
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
