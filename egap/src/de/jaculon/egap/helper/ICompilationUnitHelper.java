package de.jaculon.egap.helper;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import de.jaculon.egap.junit.MyPackage;
import de.jaculon.egap.junit.MySourceFolder;
import de.jaculon.egap.utils.EclipseUtils;
import de.jaculon.egap.utils.IFileUtils;

public class ICompilationUnitHelper {

	/**
	 * Returns the currently active {@link ICompilationUnit} or null if there's
	 * nothing currently open.
	 */
	public ICompilationUnit getActiveICompilationUnit() {
		IEditorPart editorPart = EclipseUtils.getActiveEditor();

		if (editorPart == null | !(editorPart instanceof ITextEditor)) {
			return null;
		}
		ITextEditor textEditor = (ITextEditor) editorPart;
		IEditorInput editorInput = textEditor.getEditorInput();

		ITypeRoot editorInputTypeRoot = JavaUI.getEditorInputTypeRoot(editorInput);
		if (!(editorInputTypeRoot instanceof ICompilationUnit)) {
			return null;
		}

		return (ICompilationUnit) editorInputTypeRoot;
	}

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
