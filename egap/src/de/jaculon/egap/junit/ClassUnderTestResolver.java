package de.jaculon.egap.junit;

import org.eclipse.jdt.core.ICompilationUnit;

public class ClassUnderTestResolver {

	private String srcFolderForNormalClasses;
	private String srcFolderForTests;

	public void setSrcFolderForTests(String srcFolderForTests) {
		this.srcFolderForTests = srcFolderForTests;
	}

	public void setSrcFolderForNormalClasses(String srcFolderForNormalClasses) {
		this.srcFolderForNormalClasses = srcFolderForNormalClasses;
	}

	public ICompilationUnit resolve(ICompilationUnit icompilationUnit) {
		return null;
	}

}
