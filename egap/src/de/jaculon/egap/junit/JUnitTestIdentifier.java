package de.jaculon.egap.junit;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;

public class JUnitTestIdentifier {

	private List<String> testSuffixes;

	public void setTestSuffixes(List<String> testSuffixes) {
		this.testSuffixes = testSuffixes;
	}

	/**
	 * Returns true if the given ICompilationUnit is a JUnit Testcase. A
	 * ICompilationUnit is considered to be a JUnit Testcase if it ends with any
	 * of the provided test suffixes (see {@link #setTestSuffixes(List)}).
	 */
	public boolean isTestCase(ICompilationUnit icompilationUnit) {

		IType primaryType = icompilationUnit.findPrimaryType();
		String primaryTypeName = primaryType.getElementName();

		for (String suffix : testSuffixes) {
			if (primaryTypeName.endsWith(suffix)) {
				return true;
			}
		}

		return false;
	}

}
