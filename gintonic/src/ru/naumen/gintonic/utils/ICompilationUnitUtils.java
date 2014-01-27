package ru.naumen.gintonic.utils;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
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
		IType primaryType = compilationUnit.findPrimaryType();
		String elementName = primaryType.getElementName();
		return elementName;
	}

	public static List<String> getSrcFolderPathComponents(
			ICompilationUnit compilationUnit) {
		IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) compilationUnit.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
		IResource underlyingResource;
		try {
			underlyingResource = packageFragmentRoot.getUnderlyingResource();
		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		}
		List<String> folderPathSegments = ListUtils.newArrayList();
		IContainer parent = (IFolder) underlyingResource;
		while (true) {
			if (parent instanceof IProject) {
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

	public static Integer getStartPositionOfTopLevelType(
			ICompilationUnit compilationUnit) {
		IType primaryType = compilationUnit.findPrimaryType();
		if (primaryType == null) {
			return null;
		}
		ISourceRange nameRange;
		try {
			nameRange = primaryType.getNameRange();
			if (nameRange == null) {
				return null;
			}
		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		}
		return nameRange.getOffset();
	}

}
