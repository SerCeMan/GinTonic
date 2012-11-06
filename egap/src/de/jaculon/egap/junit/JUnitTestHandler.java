package de.jaculon.egap.junit;

import org.eclipse.jdt.core.ICompilationUnit;

public class JUnitTestHandler {

	private GetActiveICompilationUnitProvider getActiveICompilationUnitProvider;
	private JUnitTestCaseIdentifier jUnitTestCaseIdentifier;

	private ClassUnderTestResolver classUnderTestResolver;
	private JUnitTestResolver jUnitTestResolver;

	private JumpToCompilationUnitHandler jumpToCompilationUnitHandler;

	private JUnitTestCreator jUnitTestCreator;

	public void handle(){
		ICompilationUnit icompilationUnit = getActiveICompilationUnitProvider.get();
		if(icompilationUnit == null){
			return;
		}

		boolean isTestCase = jUnitTestCaseIdentifier.isTestCase(icompilationUnit);

		if(isTestCase){
			ICompilationUnit classUnderTest = classUnderTestResolver.resolve(icompilationUnit);
			if(classUnderTest != null){
				jumpToCompilationUnitHandler.jumpTo(classUnderTest);
			}else{

			}
		}else{ /* class-under-test */
			ICompilationUnit junitTest = jUnitTestResolver.resolve(icompilationUnit);
			if(junitTest != null){
				jumpToCompilationUnitHandler.jumpTo(junitTest);
			}else{
				jUnitTestCreator.create(junitTest);
			}
		}
	}

}
