package egap.command;

import java.util.Arrays;
import java.util.LinkedList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;

import egap.guice.ProjectResource;
import egap.utils.EclipseUtils;
import egap.utils.IProjectResourceUtils;

public class CreateMockitoJUnitTestHandler extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		ProjectResource javaClass = EclipseUtils.getActiveJavaClass();
		
		if(javaClass == null){
			return null;
		}
		
		ProjectResource junitTest = createJUnitClassFor(javaClass);
		
		IFile junitTestAsIFile = IProjectResourceUtils.getJavaFile(junitTest);
		
		if(junitTestAsIFile.exists()){
			IProjectResourceUtils.openEditorWithStatementDeclaration(junitTest);
		}else{
			System.out.println("-");
		}
		
		return null;
	}

	/**
	 * Creates a JUnit class from a given java class.
	 */
	private ProjectResource createJUnitClassFor(ProjectResource javaClass) {
		ProjectResource junitTest = new ProjectResource();
		junitTest.setProjectName(javaClass.getProjectName());
		junitTest.setSrcFolderPathComponents(Arrays.asList("src-test"));
		
		/* The test differs as it lies in a test package and has the suffix Test. */
		LinkedList<String> junitTestcasePackage = new LinkedList<String>(javaClass.getPackage());
		junitTestcasePackage.addFirst("test");
		junitTest.setPackage(junitTestcasePackage);
		junitTest.setTypeName(javaClass.getTypeName() + "Test");
		return junitTest;
	}

}
