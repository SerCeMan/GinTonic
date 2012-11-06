package egap.junit;

import org.eclipse.jdt.core.ICompilationUnit;

public class JUnitTestResolver {

	private String testSuffix;
	private String testPackagePrefix;

	public void setTestSuffix(String testSuffix) {
		this.testSuffix = testSuffix;
	}

	public void setTestPackagePrefix(String testPackagePrefix) {
		this.testPackagePrefix = testPackagePrefix;
	}

	public ICompilationUnit resolve(ICompilationUnit icompilationUnit) {
		return icompilationUnit;
	}

}
