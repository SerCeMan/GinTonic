package de.jaculon.egap.junit;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import de.jaculon.egap.utils.EclipseUtils;

public class GetActiveICompilationUnitProvider {

	/**
	 * Returns the currently active {@link ICompilationUnit} or null if there's
	 * nothing currently open.
	 */
	public ICompilationUnit get() {
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

}
