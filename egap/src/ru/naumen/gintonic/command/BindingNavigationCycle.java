package ru.naumen.gintonic.command;

import java.util.List;

import ru.naumen.gintonic.guice.statements.BindingDefinition;
import ru.naumen.gintonic.navigate.NavigationCycle;
import ru.naumen.gintonic.source_reference.SourceCodeReference;
import ru.naumen.gintonic.utils.ListUtils;


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
