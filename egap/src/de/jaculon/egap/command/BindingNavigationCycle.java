package de.jaculon.egap.command;

import java.util.List;

import de.jaculon.egap.guice.statements.BindingDefinition;
import de.jaculon.egap.navigate.NavigationCycle;
import de.jaculon.egap.source_reference.SourceCodeReference;
import de.jaculon.egap.utils.ListUtils;


/**
 * A navigation cycle which cycles between a binding and its binding definitions.
 *
 * @author tmajunke
 */
public class BindingNavigationCycle extends NavigationCycle<SourceCodeReference>{

	public BindingNavigationCycle(SourceCodeReference origin, List<BindingDefinition> bindingDefinitions) {
		List<SourceCodeReference> sourceCodeReferences = ListUtils.newArrayListWithCapacity(bindingDefinitions.size() + 1);
		sourceCodeReferences.add(origin);
		
		for (BindingDefinition bindingDefinition : bindingDefinitions) {
			sourceCodeReferences.add(bindingDefinition.getSourceCodeReference());
		}
		
		setSourceCodeReferences(sourceCodeReferences);
	}

}
