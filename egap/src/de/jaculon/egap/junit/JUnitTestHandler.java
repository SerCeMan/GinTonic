package de.jaculon.egap.junit;

import org.eclipse.jdt.core.ICompilationUnit;

public class JUnitTestHandler {

	private JUnitTestCaseIdentifier jUnitTestCaseIdentifier;
	private ICompilationUnitResolver classUnderTestResolver;
	private JUnitTestResolver jUnitTestResolver;
	private JumpToCompilationUnitHandler jumpToCompilationUnitHandler;
	private JUnitTestCreator jUnitTestCreator;
	private ICompilationUnitHelper iCompilationUnitHelper;

	public void setiCompilationUnitHelper(
			ICompilationUnitHelper iCompilationUnitHelper) {
		this.iCompilationUnitHelper = iCompilationUnitHelper;
	}

	public void setjUnitTestCaseIdentifier(
			JUnitTestCaseIdentifier jUnitTestCaseIdentifier) {
		this.jUnitTestCaseIdentifier = jUnitTestCaseIdentifier;
	}

	public void setClassUnderTestResolver(
			ICompilationUnitResolver classUnderTestResolver) {
		this.classUnderTestResolver = classUnderTestResolver;
	}

	public void setjUnitTestResolver(JUnitTestResolver jUnitTestResolver) {
		this.jUnitTestResolver = jUnitTestResolver;
	}

	public void setJumpToCompilationUnitHandler(
			JumpToCompilationUnitHandler jumpToCompilationUnitHandler) {
		this.jumpToCompilationUnitHandler = jumpToCompilationUnitHandler;
	}

	public void setjUnitTestCreator(JUnitTestCreator jUnitTestCreator) {
		this.jUnitTestCreator = jUnitTestCreator;
	}

	public void handle() {
		ICompilationUnit icompilationUnit = iCompilationUnitHelper.getActiveICompilationUnit();
		if (icompilationUnit == null) {
			return;
		}

		boolean isTestCase = jUnitTestCaseIdentifier.isTestCase(icompilationUnit);

		if (isTestCase) {
			ICompilationUnit classUnderTest = classUnderTestResolver.resolve(icompilationUnit);
			if (classUnderTest != null) {
				jumpToCompilationUnitHandler.jumpTo(classUnderTest);
			}
		}
		else { /* class-under-test */
			ICompilationUnit junitTest = jUnitTestResolver.resolve(icompilationUnit);
			if (junitTest != null) {
				jumpToCompilationUnitHandler.jumpTo(junitTest);
			}
			else {
				jUnitTestCreator.createTestFor(icompilationUnit);
			}

		}
	}

}
