package de.jaculon.egap.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.jdt.ui.SharedASTProvider;




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

	public static List<String> getSrcFolderPathComponents(ICompilationUnit compilationUnit){
		IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) compilationUnit.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
		IResource underlyingResource;
		try {
			underlyingResource = packageFragmentRoot.getUnderlyingResource();
		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		}
		List<String> folderPathSegments = ListUtils.newArrayList();
		IContainer parent = (IFolder) underlyingResource;
		while(true){
			if(parent instanceof IProject){
				break;
			}
			IFolder middleFolder = (IFolder) parent;
			String folderName = middleFolder.getName();
			folderPathSegments.add(folderName);
			parent = parent.getParent();
		}
		
		Collections.reverse(folderPathSegments);
		
		return folderPathSegments;
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

	/**
	 * Returns all {@link InjectionPoint}s for the given compilation unit. The
	 * superclasses of the given compilation unit are also included in the returned list.
	 */
	public static List<InjectionPoint> findInjectionPoints(
			ICompilationUnit compilationUnit) throws JavaModelException {
		final List<InjectionPoint> injectionPoints = new ArrayList<InjectionPoint>(
				30);
		
		List<IType> types = new ArrayList<IType>(5);
		
		IType primaryType = compilationUnit.findPrimaryType();
		types.add(primaryType);
		
		ITypeHierarchy supertypeHierarchy = primaryType.newSupertypeHierarchy(null);
		
		IType[] allSuperClasses = supertypeHierarchy.getAllSuperclasses(primaryType);
		for (IType iType : allSuperClasses) {
			String typeQualified = iType.getFullyQualifiedName();
			if(typeQualified.equals("java.lang.Object")){
				continue;
			}
			types.add(iType);
		}
		
		for (IType iType : types) {
			ITypeRoot typeRootOfType = iType.getTypeRoot();
			ICompilationUnitUtils.findInjectionPoints(typeRootOfType, injectionPoints);
		}
		
		return injectionPoints;
	}

	private static void findInjectionPoints(ITypeRoot typeRoot,
			final List<InjectionPoint> injectionPoints) {
		final CompilationUnit compilationUnit = SharedASTProvider.getAST(
				typeRoot,
				SharedASTProvider.WAIT_YES,
				null);
		compilationUnit.accept(new ASTVisitor(false) {
	
			private String identifier;
	
			@Override
			public boolean visit(FieldDeclaration fieldDeclaration) {
	
				/* We can skip static fields as they cannot be injected. */
				boolean isStatic = FieldDeclarationUtils.isStatic(fieldDeclaration);
				if (isStatic) {
					return false;
				}
	
				fieldDeclaration.accept(new ASTVisitor() {
	
					@Override
					public boolean visit(VariableDeclarationFragment node) {
						SimpleName name = node.getName();
						identifier = name.getIdentifier();
						return false;
					}
	
				});
	
				InjectionPoint injectionPoint = FieldDeclarationUtils.getTypeIfAnnotatedWithInject(
						fieldDeclaration,
						compilationUnit,
						identifier);
	
				if (injectionPoint != null) {
					injectionPoints.add(injectionPoint);
				}
	
				identifier = null;
	
				return false;
			}
	
		});
	}
	
	
}
