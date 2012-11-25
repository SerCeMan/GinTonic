package de.jaculon.egap.navigate;

import java.util.List;

import de.jaculon.egap.utils.IProjectResource;
import de.jaculon.egap.utils.IProjectResourceUtils;


/**
 * A {@link NavigationCycle} can be used to jump from one project resource to
 * another. Jump means that the target resource is opened in an eclipse editor.
 *
 * @author tmajunke
 */
public class NavigationCycle{

	/**
	 * The project resources that we can jump to.
	 */
	private List<IProjectResource> projectResources;

	/**
	 * The index so we know where to jump next.
	 */
	private int index = 0;

	public void setProjectResources(List<IProjectResource> projectResources) {
		this.projectResources = projectResources;
	}

	/**
	 * Jumps to the given project resource if the resource is contained in this
	 * navigation cycle.
	 *
	 * @param projectResource the projectResource. May not be null.
	 * @return true, if the resource has been contained in this navigation
	 *         cycle. Otherwise false.
	 */
	public boolean jumpTo(IProjectResource projectResource) {
		Integer resourceIndex = getResourceIndexFor(projectResource);
		if (resourceIndex != null) {
			this.index = resourceIndex;
			jumpToCurrent();
			return true;
		}
		return false;
	}

	/**
	 * Jumps to the next project resource. If we are already at the last project
	 * resource, then we jump to the first one (in other words we cycle through
	 * the project resources).
	 */
	public void jumpToNext() {
		increaseIndex();
		jumpToCurrent();
	}

	private void jumpToCurrent() {
		IProjectResource jumpTarget = projectResources.get(index);
		IProjectResourceUtils.openEditorWithStatementDeclaration(jumpTarget);
	}

	private void increaseIndex() {
		int size = projectResources.size();
		this.index = (index + 1) % size;
	}

	/**
	 * Returns true if the given projectResource is contained in this navigation
	 * cycle, otherwise false.
	 *
	 * @param projectResource the projectResource
	 * @return true if the given projectResource is contained in this navigation
	 *         cycle, otherwise false.
	 */
	private Integer getResourceIndexFor(IProjectResource projectResource) {
		int i = 0;
		for (IProjectResource iProjectResource : projectResources) {
			if (iProjectResource.getStartPosition().equals(
					projectResource.getStartPosition())) {
				if (iProjectResource.getTypeNameFullyQualified().equals(
						projectResource.getTypeNameFullyQualified())) {
					if (iProjectResource.getProjectName().equals(
							projectResource.getProjectName())) {
						return i;
					}
				}
			}
			i++;
		}

		return null;
	}

}
