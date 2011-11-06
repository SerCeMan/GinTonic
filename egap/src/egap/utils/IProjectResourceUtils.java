package egap.utils;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import egap.guice.ProjectResource;

public class IProjectResourceUtils {
	
	private static final Splitter SPLITTER_ON_DOT = Splitter.on('.');

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

	public static CompilationUnit getAstNode(
			IProjectResource navigationEndpoint) {
		ICompilationUnit compilationUnit = getICompilationUnit(navigationEndpoint);
		CompilationUnit cu = ASTParserUtils.parseCompilationUnitAst3(compilationUnit);
		return cu;
	}

	public static ITypeBinding getTypeBinding(
			IProjectResource navigationEndpoint) {
		CompilationUnit cu = getAstNode(navigationEndpoint);
		KindOfChecker astVisitor = new KindOfChecker(StringUtils.GUICE_MODULE);
		cu.accept(astVisitor);
		ITypeBinding guiceModulesTypeBinding = astVisitor.typeBinding;
		return guiceModulesTypeBinding;
	}

	public static ProjectResource createNavigationEndpoint(ASTNode astNode,
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
		origin.setSrcFolderPathComponents(Lists.newArrayList(srcFolderPath));
		
		PackageDeclaration packageBinding = astRoot.getPackage();
		Name name = packageBinding.getName();
		String packageFullyQualified = name.getFullyQualifiedName();
		Iterable<String> parts = SPLITTER_ON_DOT.split(packageFullyQualified);
		origin.setPackagePathComponents(Lists.newArrayList(parts));
	
		String typeName = ICompilationUnitUtils.getNameWithoutJavaExtension(icompilationUnit);
		origin.setTypeName(typeName);
		return origin;
	}

	

}
