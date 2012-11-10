package de.jaculon.egap.junit;

import org.eclipse.jdt.core.ICompilationUnit;

import de.jaculon.egap.helper.ICompilationUnitHelper;

public class JUnitTestHandler {

	private JUnitTestIdentifier jUnitTestIdentifier;
	private ICompilationUnitResolver classUnderTestResolver;
	private JUnitTestResolver jUnitTestResolver;
	private JumpToCompilationUnitHandler jumpToCompilationUnitHandler;
	private JUnitTestCreator jUnitTestCreator;
	private ICompilationUnitHelper iCompilationUnitHelper;

	public void setiCompilationUnitHelper(
			ICompilationUnitHelper iCompilationUnitHelper) {
		this.iCompilationUnitHelper = iCompilationUnitHelper;
	}

	public void setjUnitTestIdentifier(
			JUnitTestIdentifier jUnitTestIdentifier) {
		this.jUnitTestIdentifier = jUnitTestIdentifier;
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

		boolean isTestCase = jUnitTestIdentifier.isTestCase(icompilationUnit);

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
