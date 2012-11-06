package de.jaculon.egap.command;

import java.util.List;

import de.jaculon.egap.guice.statements.GuiceStatement;
import de.jaculon.egap.utils.IProjectResource;
import de.jaculon.egap.utils.ListUtils;


/**
 * A navigation cycle which cycles between a binding and its binding definitions.
 * 
 * @author tmajunke
 */
public class BindingNavigationCycle extends NavigationCycle{

	private final IProjectResource origin;
	private final List<GuiceStatement> bindingDefinitions;

	public BindingNavigationCycle(List<GuiceStatement> bindingDefinitions, IProjectResource origin) {
		this.bindingDefinitions = bindingDefinitions;
		this.origin = origin;
		
		List<IProjectResource> projectResources = ListUtils.newArrayListWithCapacity(bindingDefinitions.size() + 1);
		projectResources.addAll(bindingDefinitions);
		projectResources.add(origin);
		setProjectResources(projectResources);
	}

	public IProjectResource getOrigin() {
		return origin;
	}

	public List<GuiceStatement> getBindingDefinitions() {
		return bindingDefinitions;
	}

}
