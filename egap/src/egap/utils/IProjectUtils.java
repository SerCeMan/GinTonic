package egap.utils;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;


import egap.EgapPlugin;

public class IProjectUtils {

	public static LinkedList<IFile> getFilesInProjectSkippingBinary(
			final IProject project) throws CoreException {
		List<IPath> sourceFolders = getSourceFolders(project);
		CollectFiles fileCollector = new CollectFiles(sourceFolders);
		project.accept(fileCollector);
		return fileCollector.files;
	}

	/**
	 * Returns the source packages as {@link IPath}es of the project assuming
	 * the project to be a Java project.
	 * 
	 * @param project the project
	 * @return the src folders as {@link IPath}es.
	 * @throws CoreException
	 */
	public static List<IPath> getSourceFolders(final IProject project)
			throws CoreException {
		Assert.isNotNull(project);
		final List<IPath> srcFolders = ListUtils.newArrayListWithCapacity(30);
		IJavaProject javaProject = (IJavaProject) project.getNature(JavaCore.NATURE_ID);
		final IPackageFragmentRoot[] packageFragmentRoots = javaProject.getPackageFragmentRoots();
		for (final IPackageFragmentRoot pfr : packageFragmentRoots) {
			if (pfr.getKind() == IPackageFragmentRoot.K_SOURCE) {
				IPath path = pfr.getPath();
				srcFolders.add(path);
			}
		}

		return srcFolders;
	}

	/**
	 * Adds the given nature to the projects natures if it does not yet exist.
	 * Otherwise the nature is removed from the project.
	 * 
	 * @param project the project
	 * @param natureId the nature ID
	 * @return true if the nature was added, otherwise false.
	 */
	public static boolean toggleNature(final IProject project, String natureId) {
		try {
			final IProjectDescription description = project.getDescription();
			final String[] natures = description.getNatureIds();

			for (int i = 0; i < natures.length; ++i) {
				if (natureId.equals(natures[i])) {

					// Remove the nature
					final String[] newNatures = new String[natures.length - 1];
					System.arraycopy(natures, 0, newNatures, 0, i);
					System.arraycopy(
							natures,
							i + 1,
							newNatures,
							i,
							natures.length - i - 1);
					description.setNatureIds(newNatures);
					project.setDescription(description, null);

					return false;
				}
			}

			/* Nature not found -- adding it */
			final String[] newNatures = new String[natures.length + 1];
			System.arraycopy(natures, 0, newNatures, 0, natures.length);
			newNatures[natures.length] = EgapPlugin.ID_NATURE;
			description.setNatureIds(newNatures);
			project.setDescription(description, null);
		} catch (final CoreException e) {
			EgapPlugin.logException(e);
		}
		return true;
	}

	public static List<IProject> getOpenProjectsWithNature(String natureId) {
		final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		List<IProject> projectsWithNature = ListUtils.newArrayList();

		for (IProject iProject : projects) {
			try {
				if (iProject.isOpen() && iProject.hasNature(natureId)) {
					projectsWithNature.add(iProject);
				}
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
		}

		return projectsWithNature;
	}
	
	private static final class CollectFiles implements IResourceVisitor {

		private List<IPath> sourceFolders;
		private LinkedList<IFile> files = ListUtils.newLinkedList();

		private CollectFiles(List<IPath> sourceFolders) {
			super();
			this.sourceFolders = sourceFolders;
		}

		@Override
		public boolean visit(IResource resource) throws CoreException {

			if (resource instanceof IFolder) {
				IFolder folder = (IFolder) resource;
				if (IFolderUtils.isSourceFolder(folder, sourceFolders)) {
					return true;
				}
				/* We must always descend into every folder, as otherwise
				 * folders with more segments (like maven projects with
				 * a src folder src/main/java) are not correctly 
				 * scanned for files (Bug in Rev 3). */
				return true; 
			} else if (resource instanceof IFile) {
				files.add((IFile) resource);
			}

			return true;
		}
	}

}
