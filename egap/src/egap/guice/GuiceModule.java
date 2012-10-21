package egap.guice;

import java.util.List;


import egap.guice.statements.BindingStatement;
import egap.guice.statements.InstallModuleStatement;
import egap.utils.Preconditions;

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
	private List<BindingStatement> bindingStatements;
	
	public List<BindingStatement> getBindingStatements() {
		return bindingStatements;
	}
	
	public void setInstalledModules(List<InstallModuleStatement> installedModules) {
		this.installedModules = installedModules;
	}

	public void setBindingStatements(List<BindingStatement> bindingStatements) {
		this.bindingStatements = bindingStatements;
	}

	/**
	 * Returns the list of installed module statements.
	 */
	public List<InstallModuleStatement> getInstalledModules() {
		return installedModules;
	}

	public void validate() {
		for (BindingStatement bindingStatement : bindingStatements) {
			Preconditions.checkNotNull(bindingStatement.getInterfaceType());
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
