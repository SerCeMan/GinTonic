package egap.guice.indexer;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import egap.EgapPlugin;
import egap.guice.GuiceModule;
import egap.guice.statements.BindingStatement;
import egap.guice.statements.GuiceStatement;
import egap.guice.statements.InstallModuleStatement;
import egap.utils.ASTParserUtils;
import egap.utils.ICompilationUnitUtils;

public class GuiceIndexer {

	/**
	 * Analyzes the given file. We only analyze it if the file satisfies all of
	 * the following conditions:
	 * 
	 * <ol>
	 * 
	 * <li>it ends with "Module" (e.g ServiceModule, ChainModule,...)</li>
	 * <li>it is a Guice Module.</li>
	 * </ol>
	 * 
	 * 
	 * @return the {@link GuiceModule} or null if any condition was not
	 *         satisfied.
	 */
	public GuiceModule index(IProject project, IFile file) {

		if (!file.exists()) {
			return null;
		}

		/* We assume guice modules to end with Module! */
		String filename = file.getName();
		if (!filename.endsWith("Module.java")) {
			return null;
		}

		try {
			ICompilationUnit compilationUnit = (ICompilationUnit) JavaCore.create(file);
			if (compilationUnit != null) {
				GuiceModule guiceModule = parseGuiceModule(
						compilationUnit,
						project);
				return guiceModule;
			}
		} catch (JavaModelException e) {
			EgapPlugin.logException("Error analyzing " + filename, e);
		}
		return null;
	}

	private GuiceModule parseGuiceModule(ICompilationUnit compilationUnit,
			IProject project) throws JavaModelException {

		CompilationUnit cu = ASTParserUtils.parseCompilationUnitAst3(
				compilationUnit,
				true,
				true);

		/*
		 * No need to fail if there are errors in the compilationUnit, as it
		 * works also with some errors (tested multiple times and sure it
		 * depends on the type of the error).
		 */

		GuiceIndexerAstVisitor indexer = new GuiceIndexerAstVisitor();

		try {
			cu.accept(indexer);
		} catch (Exception exception) {
			String message = "Unable to analyze "
					+ compilationUnit.getElementName() + "!";
			EgapPlugin.logException(message, exception);
			return null;
		}

		if (indexer.isGuiceModuleType()) {
			ITypeBinding guiceModuleAsTypeBinding = indexer.getGuiceModuleTypeBinding();
			IPackageBinding packageBinding = guiceModuleAsTypeBinding.getPackage();
			String packageFullyQualified = packageBinding.getName();
			String guiceModuleName = guiceModuleAsTypeBinding.getName();
			String projectName = project.getName();
			String srcFolderName = ICompilationUnitUtils.getSrcFolderName(compilationUnit);
			
			GuiceModule guiceModule = new GuiceModule();
			guiceModule.setTypeName(guiceModuleName);
			guiceModule.setPackageFullyQualified(packageFullyQualified);
			guiceModule.setProjectName(projectName);
			guiceModule.setSrcFolderName(srcFolderName);
			
			List<BindingStatement> bindingStatements = indexer.getBindingStatements();
			for (BindingStatement bindingStatement : bindingStatements) {
				copyInfo(guiceModule, bindingStatement);
			}
			List<InstallModuleStatement> installModuleStatements = indexer.getInstallModuleStatements();
			for (InstallModuleStatement installModuleStatement : installModuleStatements) {
				copyInfo(guiceModule, installModuleStatement);
			}
			
			guiceModule.setBindingStatements(bindingStatements);
			guiceModule.setInstalledModules(installModuleStatements);
			
			guiceModule.validate();
			
			return guiceModule;
		}

		return null;
	}

	private void copyInfo(GuiceModule guiceModule, GuiceStatement statement) {
		statement.setProjectName(guiceModule.getProjectName());
		statement.setPackageFullyQualified(guiceModule.getPackageFullyQualified());
		statement.setSrcFolderName(guiceModule.getSrcFolderName());
		statement.setTypeName(guiceModule.getTypeName());
	}
	
}
