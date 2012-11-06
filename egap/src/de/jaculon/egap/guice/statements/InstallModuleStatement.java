package de.jaculon.egap.guice.statements;



public class InstallModuleStatement extends GuiceStatement {

	private static final long serialVersionUID = 4998413229504260537L;

	private String moduleNameFullyQualified;

	public void setModuleNameFullyQualified(String moduleNameFullyQualified) {
		this.moduleNameFullyQualified = moduleNameFullyQualified;
	}

	public String getModuleNameFullyQualified() {
		return moduleNameFullyQualified;
	}

	@Override
	public String getLabel() {
		return "Goto module install in '" + getTypeName() + "'";
	}

}
