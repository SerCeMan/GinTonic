package de.jaculon.egap.utils;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

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

}
