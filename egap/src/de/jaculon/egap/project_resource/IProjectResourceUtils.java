package de.jaculon.egap.project_resource;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;

import de.jaculon.egap.select_and_reveal.SelectAndReveal;
import de.jaculon.egap.source_reference.SourceCodeReference;
import de.jaculon.egap.utils.IFileUtils;

public class IProjectResourceUtils {

	public static void openEditorWithStatementDeclaration(
			SourceCodeReference navigationEndpoint, Integer startPosition) {
		IFile srcFile = getJavaFile(navigationEndpoint);
		SelectAndReveal.selectAndReveal(srcFile, startPosition, 0);
	}

	public static void openEditorWithStatementDeclaration(
			SourceCodeReference projectResource) {
		openEditorWithStatementDeclaration(
				projectResource,
				projectResource.getOffset());
	}

	/**
	 * Returns the project resource as {@link IFile}.
	 *
	 * @param projectResource the projectResource
	 * @return the project resource as {@link IFile}.
	 */
	public static IFile getJavaFile(SourceCodeReference projectResource) {
		IFile srcFile = IFileUtils.getJavaFile(
				projectResource.getProjectName(),
				projectResource.getSrcFolderPathComponents(),
				projectResource.getPackageNameComponents(),
				projectResource.getPrimaryTypeName());
		return srcFile;
	}

	public static ICompilationUnit getICompilationUnit(
			SourceCodeReference projectResource) {
		IFile srcFile = getJavaFile(projectResource);
		ICompilationUnit compilationUnit = JavaCore.createCompilationUnitFrom(srcFile);
		return compilationUnit;
	}

}
