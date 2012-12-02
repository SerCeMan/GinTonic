package de.jaculon.egap.utils;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;




public class IFileUtils {

	private static final char PATH_SEPARATOR = '/';

	public static IFile getJavaFile(String projectName,
			List<String> srcFolderPathComponents,
			List<String> packagePathComponents, String typeName) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project = root.getProject(projectName);

		String sourceFolder = StringUtils.join(PATH_SEPARATOR, srcFolderPathComponents)
				+ PATH_SEPARATOR + StringUtils.join(PATH_SEPARATOR, packagePathComponents);

		IFolder folder = project.getFolder(sourceFolder);

		String filename = typeName + ICompilationUnitUtils.JAVA_EXTENSION;
		IFile file = folder.getFile(filename);

		return file;
	}

}
