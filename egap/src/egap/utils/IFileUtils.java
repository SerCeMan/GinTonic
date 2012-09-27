package egap.utils;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;


import egap.EgapPlugin;

public class IFileUtils {

	private static final char PATH_SEPARATOR = '/';

	public static IFile getJavaFile(String projectName,
			List<String> srcFolderPathComponents,
			List<String> packagePathComponents, String typeName) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project = root.getProject(projectName);

		String sourceFolder = StringUtils.join(PATH_SEPARATOR, srcFolderPathComponents)
				+ PATH_SEPARATOR + StringUtils.join(PATH_SEPARATOR,packagePathComponents);

		IFolder folder = project.getFolder(sourceFolder);

		String filename = typeName + ".java";
		IFile file = folder.getFile(filename);

		return file;
	}

	public static void selectAndRevealInEditor(IFile srcFile,
			Integer startPosition, Integer length) {
		try {
			final IWorkbench workbench = PlatformUI.getWorkbench();
			IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
			final IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
			ITextEditor editorPart = (ITextEditor) IDE.openEditor(
					activePage,
					srcFile,
					true);
			if (startPosition != null && length != null) {
				editorPart.selectAndReveal(startPosition, length);
			}
		} catch (final PartInitException pie) {
			EgapPlugin.logException(pie);
		}
	}

	public static void setCaretAndRevealInEditor(IFile srcFile, int position) {
		selectAndRevealInEditor(srcFile, position, 0);
	}

}
