package egap.utils;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import egap.guice.ProjectResource;

/**
 * This utilities should be used as a starting point.
 * 
 * @author tmajunke
 */
public class EclipseUtils {

	public static IEditorPart getActiveEditor() {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
		IEditorPart editorPart = activePage.getActiveEditor();
		return editorPart;
	}

	public static ProjectResource getActiveJavaClass() {
		IEditorPart editorPart = getActiveEditor();

		if (editorPart == null | !(editorPart instanceof ITextEditor)) {
			return null;
		}
		final ITextEditor textEditor = (ITextEditor) editorPart;
		IEditorInput editorInput = textEditor.getEditorInput();

		ITypeRoot editorInputTypeRoot = JavaUI.getEditorInputTypeRoot(editorInput);

		if (!(editorInputTypeRoot instanceof ICompilationUnit)) {
			return null;
		}

		ICompilationUnit icompilationUnit = (ICompilationUnit) editorInputTypeRoot;
		ProjectResource javaClass = IProjectResourceUtils.createProjectResource(icompilationUnit);

		return javaClass;
	}

}
