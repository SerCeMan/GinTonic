package de.jaculon.egap.navigate;

import java.util.List;

import de.jaculon.egap.project_resource.IProjectResource;
import de.jaculon.egap.project_resource.IProjectResourceUtils;

/**
 * A {@link NavigationCycle} can be used to jump from one project resource to
 * another. Jump means that the target resource is opened in the eclipse editor.
 * 
 * @author tmajunke
 */
public class NavigationCycle {

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
	 * Jumps to the project resource that is the follower of the given resource.
	 * It also sets the resource pointer to the follower.
	 * 
	 * To do so we must first check if the given resource is contained in this
	 * navigation cycle. The check compares the resources start position and
	 * qualified name. If the resource is not contained in this navigation cycle
	 * then nothing happens and the method returns false.
	 * 
	 * @param projectResource the projectResource. May not be null.
	 * @return true, if the resource has been contained in this navigation
	 *         cycle. Otherwise false.
	 */
	public boolean jumpToFollower(IProjectResource projectResource) {
		Integer resourceIndex = getResourceIndexFor(projectResource);
		if (resourceIndex != null) {
			this.index = resourceIndex;
			jumpToNext();
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
			Integer startPosition = iProjectResource.getStartPosition();
			Integer startPosition2 = projectResource.getStartPosition();
			if (startPosition.equals(startPosition2)) {
				String typeNameFullyQualified = iProjectResource.getTypeNameFullyQualified();
				String typeNameFullyQualified2 = projectResource.getTypeNameFullyQualified();
				if (typeNameFullyQualified.equals(typeNameFullyQualified2)) {
					return i;
				}
			}
			i++;
		}

		return null;
	}

}
