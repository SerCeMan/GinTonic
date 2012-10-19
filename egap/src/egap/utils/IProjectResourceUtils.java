package egap.utils;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.ITextSelection;

import egap.guice.ProjectResource;

public class IProjectResourceUtils {
	
	public static void openEditorWithStatementDeclaration(
			IProjectResource navigationEndpoint, Integer startPosition) {
		IFile srcFile = getJavaFile(navigationEndpoint);
		IFileUtils.selectAndRevealInEditor(
				srcFile,
				startPosition,
				0);
	}
	
	public static void openEditorWithStatementDeclaration(
			IProjectResource navigationEndpoint) {
		openEditorWithStatementDeclaration(navigationEndpoint, navigationEndpoint.getStartPosition());
	}

	/**
	 * Returns the project resource as {@link IFile}.
	 * 
	 * @param projectResource the projectResource
	 * @return the project resource as {@link IFile}.
	 */
	public static IFile getJavaFile(IProjectResource projectResource) {
		IFile srcFile = IFileUtils.getJavaFile(
				projectResource.getProjectName(),
				projectResource.getSrcFolderPathComponents(),
				projectResource.getPackage(),
				projectResource.getTypeName());
		return srcFile;
	}

	public static ICompilationUnit getICompilationUnit(
			IProjectResource navigationEndpoint) {
		IFile srcFile = getJavaFile(navigationEndpoint);
		ICompilationUnit compilationUnit = JavaCore.createCompilationUnitFrom(srcFile);
		return compilationUnit;
	}

	public static ProjectResource createProjectResource(ICompilationUnit icompilationUnit, ITextSelection textSelection) {
		ProjectResource projectResource = new ProjectResource();
		
		IResource resource = icompilationUnit.getResource();
		IProject project = resource.getProject();
		projectResource.setProjectName(project.getName());
		
		List<String> srcFolderPath = ICompilationUnitUtils.getSrcFolderPathComponents(icompilationUnit);
		projectResource.setSrcFolderPathComponents(srcFolderPath);
		
		IPackageFragment parent = (IPackageFragment) icompilationUnit.getParent();
		String packageDotSeparated = parent.getElementName();
		List<String> packageAsList = StringUtils.split('.', packageDotSeparated);
		projectResource.setPackage(packageAsList);
		
		String typeName = ICompilationUnitUtils.getNameWithoutJavaExtension(icompilationUnit);
		projectResource.setTypeName(typeName);
		
		if(textSelection != null){
			int length = textSelection.getLength();
			projectResource.setLength(length);
			int startPosition = textSelection.getOffset();
			projectResource.setStartPosition(startPosition);
		}
		
		return projectResource;
	}

	public static ProjectResource createProjectResource(ICompilationUnit icompilationUnit) {
		return createProjectResource(icompilationUnit, null);
	}

	

}
