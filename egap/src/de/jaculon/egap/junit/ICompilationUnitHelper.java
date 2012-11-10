package de.jaculon.egap.junit;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;

import de.jaculon.egap.utils.IFileUtils;

public class ICompilationUnitHelper {

	/**
	 * Returns the ICompilationUnit as addressed by projectName, srcFolder,
	 * package and type name. The ICompilationUnit unit is only returned if it
	 * exists (see {@link ICompilationUnit#exists()}). If the ICompilationUnit
	 * could not be resolved or does not exist then null is returned.
	 */
	public ICompilationUnit resolve(String projectName,
			MySourceFolder srcFolder, MyPackage myPackage, String typeName) {
		IFile javaFile = IFileUtils.getJavaFile(
				projectName,
				srcFolder.getSourceFolderParts(),
				myPackage.getPackageParts(),
				typeName);
		ICompilationUnit compilationUnit = JavaCore.createCompilationUnitFrom(javaFile);
		return compilationUnit;
	}

	/**
	 * Returns the project name for the given compilation unit.
	 */
	public String getJavaProjectName(ICompilationUnit icompilationUnit) {
		return icompilationUnit.getJavaProject().getElementName();
	}

	/**
	 * Returns the simple name for the given compilation unit.
	 */
	public String getPrimaryTypeName(ICompilationUnit icompilationUnit) {
		return icompilationUnit.findPrimaryType().getElementName();
	}

}
