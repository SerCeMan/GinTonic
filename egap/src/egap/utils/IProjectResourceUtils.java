package egap.utils;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;

import egap.guice.ProjectResource;

public class IProjectResourceUtils {
	
	public static void openEditorWithStatementDeclaration(
			IProjectResource navigationEndpoint, Integer startPosition) {
		IFile srcFile = getIFile(navigationEndpoint);
		IFileUtils.selectAndRevealInEditor(
				srcFile,
				startPosition,
				0);
	}
	
	public static void openEditorWithStatementDeclaration(
			IProjectResource navigationEndpoint) {
		openEditorWithStatementDeclaration(navigationEndpoint, navigationEndpoint.getStartPosition());
	}

	public static IFile getIFile(IProjectResource navigationEndpoint) {
		IFile srcFile = IFileUtils.getIFile(
				navigationEndpoint.getProjectName(),
				navigationEndpoint.getSrcFolderPathComponents(),
				navigationEndpoint.getPackageNameComponents(),
				navigationEndpoint.getTypeName());
		return srcFile;
	}

	public static ICompilationUnit getICompilationUnit(
			IProjectResource navigationEndpoint) {
		IFile srcFile = getIFile(navigationEndpoint);
		ICompilationUnit compilationUnit = JavaCore.createCompilationUnitFrom(srcFile);
		return compilationUnit;
	}

	public static ProjectResource createProjectResource(ASTNode astNode,
			CompilationUnit astRoot, ICompilationUnit icompilationUnit) {
		ProjectResource origin = new ProjectResource();
		int length = astNode.getLength();
		origin.setLength(length);
		int startPosition = astNode.getStartPosition();
		origin.setStartPosition(startPosition);
	
		IResource resource = icompilationUnit.getResource();
		IProject project = resource.getProject();
		origin.setProjectName(project.getName());
	
		List<String> srcFolderPath = ICompilationUnitUtils.getSrcFolderPathComponents(icompilationUnit);
		origin.setSrcFolderPathComponents(srcFolderPath);
		
		PackageDeclaration packageBinding = astRoot.getPackage();
		Name name = packageBinding.getName();
		String packageFullyQualified = name.getFullyQualifiedName();
		List<String> parts = StringUtils.split('.', packageFullyQualified);
		origin.setPackagePathComponents(parts);
	
		String typeName = ICompilationUnitUtils.getNameWithoutJavaExtension(icompilationUnit);
		origin.setTypeName(typeName);
		return origin;
	}

	

}
