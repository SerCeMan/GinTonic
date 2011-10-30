package egap.utils;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.ui.CodeGeneration;


public class ICompilationUnitUtils {

	public static final String JAVA_EXTENSION = ".java";

	public static ICompilationUnit createJavaCompilationUnit(
			IPackageFragment packageFragment, String className, String javaCode) {
	
		ICompilationUnit newCompilationUnit = null;
		try {
			newCompilationUnit = packageFragment.createCompilationUnit(
					className + JAVA_EXTENSION,
					"",
					true,
					null);
			newCompilationUnit.becomeWorkingCopy(null);
	
			String content = CodeGeneration.getCompilationUnitContent(
					newCompilationUnit,
					null,
					javaCode,
					StringUtils.LINE_SEPARATOR);
	
			IBuffer buffer = newCompilationUnit.getBuffer();
			buffer.setContents(content);
			newCompilationUnit.commitWorkingCopy(false, null);
	
		} catch (CoreException e) {
			throw new RuntimeException(e);
		} finally {
			if (newCompilationUnit != null) {
				try {
					newCompilationUnit.discardWorkingCopy();
				} catch (JavaModelException e) {
					throw new RuntimeException(e);
				}
			}
		}
	
		return newCompilationUnit;
	}

	/**
	 * Returns the name of the {@link ICompilationUnit} without the .java
	 * extension.
	 * 
	 * @param compilationUnit the {@link ICompilationUnit}
	 * @return the name without the .java extension.
	 */
	public static String getNameWithoutJavaExtension(
			ICompilationUnit compilationUnit) {
		String elementName = compilationUnit.getElementName();
		return elementName.replace(JAVA_EXTENSION, "");
	}

	public static String getSrcFolderName(ICompilationUnit compilationUnit){
		IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) compilationUnit.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
		IResource underlyingResource;
		try {
			underlyingResource = packageFragmentRoot.getUnderlyingResource();
		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		}
		IFolder folder = (IFolder) underlyingResource;
		String srcFolderName = folder.getName();
		return srcFolderName;
	}

	public static Integer getStartPositionOfTopLevelType(ICompilationUnit compilationUnit) {
		ASTParser astParser = ASTParser.newParser(AST.JLS3);
		astParser.setSource(compilationUnit);
		astParser.setKind(ASTParser.K_COMPILATION_UNIT);
		CompilationUnit astRoot = (CompilationUnit) astParser.createAST(null);
	
		@SuppressWarnings("unchecked")
		List<TypeDeclaration> types = astRoot.types();
	
		if (types.size() > 0) {
			TypeDeclaration typeDeclaration = types.get(0);
			SimpleName name = typeDeclaration.getName();
			int startPosition = name.getStartPosition();
			return startPosition;
		}
		return null;
	}
	
	public static void viewInEditor(ICompilationUnit iCompilationUnit){
		IResource resource = iCompilationUnit.getResource();

		if (resource instanceof IFile) {
			IFile file = (IFile) resource;
			Integer startPositionOfTopLevelType = ICompilationUnitUtils.getStartPositionOfTopLevelType(iCompilationUnit);
			IFileUtils.selectAndRevealInEditor(
					file,
					startPositionOfTopLevelType,
					0);
		}
	}
	
	
}
