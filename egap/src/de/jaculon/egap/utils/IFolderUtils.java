package de.jaculon.egap.utils;

import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;

public class IFolderUtils {

	/**
	 * Checks if the given folder is a source folder by comparing it with the
	 * given sourceFolders. A folder is a source folder if its fullpath starts
	 * any of the source folders. Use
	 * {@link IProjectUtils#getSourceFolders(org.eclipse.core.resources.IProject)}
	 * to obtain the source folders of a project.
	 * 
	 * @param folder the folder
	 * @param sourceFolders the source folder
	 * @return true or false.
	 */
	public static boolean isSourceFolder(IFolder folder,
			List<IPath> sourceFolders) {
		IPath foldersFullPath = folder.getFullPath();
		String foldersFullPathAsPortableString = foldersFullPath.toPortableString();

		for (final IPath srcFolder : sourceFolders) {
			if (foldersFullPathAsPortableString.startsWith(srcFolder.toPortableString())) {
				return true;
			}
		}

		return false;
	}

}
