package egap.utils;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.dom.Selection;
import org.eclipse.jdt.internal.corext.dom.SelectionAnalyzer;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

public class EditorUtils {

	public static IEditorPart getActiveEditor() {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
		IEditorPart editorPart = activePage.getActiveEditor();
		return editorPart;
	}

	public static ITextSelection getCurrentSelection() {
		IEditorPart part = getActiveEditor();
		if (part instanceof ITextEditor) {
			final ITextEditor editor = (ITextEditor) part;
			ISelectionProvider selectionProvider = editor.getSelectionProvider();
			ISelection sel = selectionProvider.getSelection();
			if (sel instanceof TextSelection) {
				ITextSelection textSel = (ITextSelection) sel;
				return textSel;
			}
		}
		return null;
	}

	public static IEditorInput getEditorInputForActiveEditor() {
		IEditorPart part = getActiveEditor();
		if (part instanceof ITextEditor) {
			final ITextEditor editor = (ITextEditor) part;
			return editor.getEditorInput();
		}
		return null;
	}

	public static GuiceTypeInfo getGuiceTypeInfoOfSelectedFieldInActiveEditor() {

		IEditorInput editorInput = getEditorInputForActiveEditor();
		ITypeRoot editorInputTypeRoot = JavaUI.getEditorInputTypeRoot(editorInput);

		if (editorInputTypeRoot instanceof ICompilationUnit) {
			ICompilationUnit icompilationUnit = (ICompilationUnit) editorInputTypeRoot;
			CompilationUnit compilationUnit = ASTParserUtils.parseCompilationUnitAst3(icompilationUnit);

			ITextSelection currentSelection = getCurrentSelection();

			int offset = currentSelection.getOffset();
			int length = currentSelection.getLength();
			Selection selection = Selection.createFromStartLength(
					offset,
					length);
			SelectionAnalyzer selectionAnalyzer = new SelectionAnalyzer(
					selection,
					false);
			compilationUnit.accept(selectionAnalyzer);

			ASTNode coveredNode = selectionAnalyzer.getLastCoveringNode();

			GuiceTypeInfo guiceTypeInfo = ASTNodeUtils.getGuiceTypeInfoIfFieldDeclaration(
					coveredNode,
					compilationUnit,
					icompilationUnit);
			return guiceTypeInfo;
		}

		return null;
	}

}
