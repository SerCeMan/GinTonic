package de.jaculon.egap.helper;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;

import de.jaculon.egap.utils.ListUtils;

public class IProjectHelper {

	/**
	 * Returns all project names for open java projects.
	 */
	public List<String> findOpenJavaProjects(String projectNameToIgnore) {
		final IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		List<String> openJavaProjects = ListUtils.newArrayList();

		for (IProject iProject : projects) {
			String projectName = iProject.getName();

			if (projectName.equals(projectNameToIgnore)) {
				continue;
			}

			try {
				if (iProject.isOpen() && iProject.hasNature(JavaCore.NATURE_ID)) {
					openJavaProjects.add(projectName);
				}
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
		}

		return openJavaProjects;
	}

}
