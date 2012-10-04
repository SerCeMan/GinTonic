package egap.guice.indexer;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
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
	 * Analyzes the given file it is a Guice Module (see {@link ITypeBindingUtils#isGuiceModuleType(ITypeBinding)).
	 * 
	 * @return the {@link GuiceModule} or null if it is not a guice module.
	 */
	public GuiceModule index(IProject project, IFile file) {

		if (!file.exists()) {
			return null;
		}

		String filename = file.getName();
		/*
		 * We have to reject non java files (like binary .class files). Note to the developer: 
		 * If you remove this check the following call to
		 * 
		 * <pre>
		 * ICompilationUnit compilationUnit = (ICompilationUnit) JavaCore.create(file);
		 * </pre>
		 * 
		 * can fail with a ClassCastException. 
		 */
		if (!filename.endsWith(ICompilationUnitUtils.JAVA_EXTENSION)) {
			return null;
		}
		
		try {
			ICompilationUnit compilationUnit = (ICompilationUnit) JavaCore.create(file);
			
			
			if (compilationUnit != null) {
				
				try {
					IType primaryType = compilationUnit.findPrimaryType();
					String superclassName = primaryType.getSuperclassName();
					/* Parsing the AST takes long so make sure to check ICompilationUnit first. */
					if(superclassName.equals("AbstractModule") || superclassName.equals("PrivateModule")){
						GuiceModule guiceModule = parseGuiceModule(
								compilationUnit,
								project);
						return guiceModule;
					}
				} catch (JavaModelException e) {
					throw new RuntimeException(e);
				}
				
			}
		} catch (RuntimeException e) {
			EgapPlugin.logException("Error analyzing " + filename, e);
		}
		return null;
	}

	private GuiceModule parseGuiceModule(ICompilationUnit compilationUnit,
			IProject project) {

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
			String[] packageFullyQualified = packageBinding.getNameComponents();
			String guiceModuleName = guiceModuleAsTypeBinding.getName();
			String projectName = project.getName();
			List<String> srcFolderPath = ICompilationUnitUtils.getSrcFolderPathComponents(compilationUnit);

			GuiceModule guiceModule = new GuiceModule();
			guiceModule.setTypeName(guiceModuleName);
			guiceModule.setPackage(Arrays.asList(packageFullyQualified));
			guiceModule.setProjectName(projectName);
			guiceModule.setSrcFolderPathComponents(srcFolderPath);

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
		statement.setPackage(guiceModule.getPackage());
		statement.setSrcFolderPathComponents(guiceModule.getSrcFolderPathComponents());
		statement.setTypeName(guiceModule.getTypeName());
	}

}
