package egap.command;

import java.util.List;

import egap.utils.IProjectResource;
import egap.utils.IProjectResourceUtils;

public class NavigationCycle {
	
	/**
	 * The project resources that we can jump to.
	 */
	private List<IProjectResource> projectResources;

	/**
	 * The index so we know where to jump next.
	 */
	private int index = 0;
	

	public NavigationCycle(List<IProjectResource> projectResources) {
		super();
		this.projectResources = projectResources;
	}
	
	public boolean jumpTo(IProjectResource projectResource) {
		Integer resourceIndex = getResourceIndexFor(projectResource);
		if (resourceIndex != null) {
			jumpTo(resourceIndex + 1);
			return true;
		}
		return false;
	}
	
	private void jumpTo(int indexToJumpTo) {
		int i = checkIndex(indexToJumpTo);
		IProjectResource jumpTarget = projectResources.get(i);
		IProjectResourceUtils.openEditorWithStatementDeclaration(jumpTarget);
	}
	
	public void jumpToNext() {
		index = checkIndex(index++);
		jumpTo(index);
	}
	
	private int checkIndex(int i){
		int size = projectResources.size();
		if (i < size) {
			return i;
		}
		int n = (i % size);
		return n;
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
