package de.jaculon.egap.guice;

import java.io.Serializable;
import java.util.List;

import de.jaculon.egap.guice.statements.BindingDefinition;
import de.jaculon.egap.guice.statements.InstallModuleStatement;
import de.jaculon.egap.source_reference.IReferencable;
import de.jaculon.egap.source_reference.SourceCodeReference;

/**
 * A guice module.
 *
 * @author tmajunke
 */
public class GuiceModule implements Serializable, IReferencable{

	private static final long serialVersionUID = -5175606018524563987L;
	
	private SourceCodeReference sourceCodeReference;
	
	/**
	 * The AbstractModule#install(Module) instructions found in the guice
	 * module.
	 */
	private List<InstallModuleStatement> installedModules;
	
	public GuiceModule(SourceCodeReference sourceCodeReference,
			List<InstallModuleStatement> installedModules,
			List<BindingDefinition> bindingDefinitions) {
		this.sourceCodeReference = sourceCodeReference;
		this.installedModules = installedModules;
		this.bindingDefinitions = bindingDefinitions;
	}

	/**
	 * The bindings found in the guice module.
	 */
	private List<BindingDefinition> bindingDefinitions;

	public List<BindingDefinition> getBindingDefinitions() {
		return bindingDefinitions;
	}

	/**
	 * Returns the list of installed module statements.
	 */
	public List<InstallModuleStatement> getInstalledModules() {
		return installedModules;
	}

	@Override
	public SourceCodeReference getSourceCodeReference() {
		return sourceCodeReference;
	}

	public String getPrimaryTypeNameFullyQualified() {
		return getSourceCodeReference().getPrimaryTypeNameFullyQualified();
	}

	public String getProjectName() {
		return getSourceCodeReference().getProjectName();
	}

	public String getPackageNameComponentsFullyQualified() {
		return getSourceCodeReference().getPackageNameComponentsFullyQualified();
	}

	public String getPrimaryTypeName() {
		return getSourceCodeReference().getPrimaryTypeName();
	}

}
