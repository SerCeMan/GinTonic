package de.jaculon.egap.guice;

import java.util.List;

import de.jaculon.egap.guice.statements.BindingDefinition;
import de.jaculon.egap.guice.statements.InstallModuleStatement;
import de.jaculon.egap.project_resource.ProjectResource;
import de.jaculon.egap.utils.Preconditions;



/**
 * A guice module. 
 * 
 * @author tmajunke
 */
public class GuiceModule extends ProjectResource{

	private static final long serialVersionUID = -5175606018524563987L;

	/**
	 * The AbstractModule#install(Module) instructions found in the guice
	 * module.
	 * 
	 * The key is the fully qualified class name of the installed module.
	 */
	private List<InstallModuleStatement> installedModules;

	/**
	 * The bindings found in the guice module.
	 */
	private List<BindingDefinition> bindingDefinitions;
	
	public List<BindingDefinition> getBindingDefinitions() {
		return bindingDefinitions;
	}
	
	public void setInstalledModules(List<InstallModuleStatement> installedModules) {
		this.installedModules = installedModules;
	}

	public void setBindingDefinitions(List<BindingDefinition> bindingDefinitions) {
		this.bindingDefinitions = bindingDefinitions;
	}

	/**
	 * Returns the list of installed module statements.
	 */
	public List<InstallModuleStatement> getInstalledModules() {
		return installedModules;
	}

	public void validate() {
		for (BindingDefinition bindingStatement : bindingDefinitions) {
			Preconditions.checkNotNull(bindingStatement.getBoundType());
			/* Preconditions.checkNotNull(bindingStatement.getImplType()); */
			Integer startPosition2 = bindingStatement.getStartPosition();
			Preconditions.checkNotNull(startPosition2);
			Integer length2 = bindingStatement.getLength();
			Preconditions.checkNotNull(length2);
		}
		
		for (InstallModuleStatement statement : installedModules) {
			Preconditions.checkNotNull(statement.getStartPosition());
			Preconditions.checkNotNull(statement.getLength());
		}
	}

	@Override
	public String toString() {
		return getTypeNameFullyQualified();
	}
	
	

}
