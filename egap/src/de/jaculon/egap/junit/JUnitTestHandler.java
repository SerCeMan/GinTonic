package de.jaculon.egap.junit;

import org.eclipse.jdt.core.ICompilationUnit;

import de.jaculon.egap.helper.ICompilationUnitHelper;

public class JUnitTestHandler {

	private JUnitTestIdentifier jUnitTestIdentifier;
	private ICompilationUnitResolver classUnderTestResolver;
	private JUnitTestResolver jUnitTestResolver;
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
			jumpToClassUnderTest(icompilationUnit);
		}
		else {
			jumpToOrCreateJUnitTest(icompilationUnit);
		}
	}

	private void jumpToOrCreateJUnitTest(ICompilationUnit icompilationUnit) {
		ICompilationUnit junitTest = jUnitTestResolver.resolve(icompilationUnit);
		if (junitTest != null) {
			iCompilationUnitHelper.viewInEditor(junitTest);
		}
		else {
			jUnitTestCreator.createTestFor(icompilationUnit);
		}
	}

	private void jumpToClassUnderTest(ICompilationUnit icompilationUnit) {
		ICompilationUnit classUnderTest = classUnderTestResolver.resolve(icompilationUnit);
		if (classUnderTest != null) {
			iCompilationUnitHelper.viewInEditor(classUnderTest);
		}
	}

}
