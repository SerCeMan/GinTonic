package de.jaculon.egap.guice.analyzer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import de.jaculon.egap.EgapPlugin;
import de.jaculon.egap.guice.GuiceModule;
import de.jaculon.egap.guice.statements.BindingDefinition;
import de.jaculon.egap.guice.statements.GuiceStatement;
import de.jaculon.egap.guice.statements.InstallModuleStatement;
import de.jaculon.egap.source_reference.SourceCodeReference;
import de.jaculon.egap.utils.ASTParserUtils;
import de.jaculon.egap.utils.ICompilationUnitUtils;
import de.jaculon.egap.utils.SetUtils;

public class GuiceAnalyzer {

	private static HashSet<String> supportedGuiceTypes = SetUtils.newHashSet(
			"AbstractModule",
			"PrivateModule");

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
		 * We have to reject non java files (like binary .class files). Note to
		 * the developer: If you remove this check the following call to
		 * 
		 * <pre> ICompilationUnit compilationUnit = (ICompilationUnit)
		 * JavaCore.create(file); </pre>
		 * 
		 * can fail with a ClassCastException.
		 */
		if (!filename.endsWith(ICompilationUnitUtils.JAVA_EXTENSION)) {
			return null;
		}

		try {
			ICompilationUnit compilationUnit = (ICompilationUnit) JavaCore.create(file);
			if (compilationUnit == null) {
				return null;
			}

			IType primaryType = compilationUnit.findPrimaryType();
			if (primaryType == null) {
				return null;
			}

			String superclassName = primaryType.getSuperclassName();
			if (superclassName == null) {
				return null;
			}

			if (supportedGuiceTypes.contains(superclassName)) {
				GuiceModule guiceModule = parseGuiceModule(
						compilationUnit,
						project);
				return guiceModule;
			}
		} catch (Exception e) {
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
		GuiceAnalyzerAstVisitor astVisitor = new GuiceAnalyzerAstVisitor();

		try {
			cu.accept(astVisitor);
		} catch (Exception exception) {
			String message = "Unable to analyze '"
					+ compilationUnit.getElementName() + "'";
			EgapPlugin.logException(message, exception);
			return null;
		}

		ITypeBinding guiceModuleAsTypeBinding = astVisitor.getGuiceModuleTypeBinding();
		IPackageBinding packageBinding = guiceModuleAsTypeBinding.getPackage();
		String[] packageFullyQualified = packageBinding.getNameComponents();
		String guiceModuleName = guiceModuleAsTypeBinding.getName();
		String projectName = project.getName();
		List<String> srcFolderPath = ICompilationUnitUtils.getSrcFolderPathComponents(compilationUnit);

		SourceCodeReference sourceCodeReferenceToGuiceModule = new SourceCodeReference();
		sourceCodeReferenceToGuiceModule.setProjectName(projectName);
		sourceCodeReferenceToGuiceModule.setPackageNameComponents(Arrays.asList(packageFullyQualified));
		sourceCodeReferenceToGuiceModule.setSrcFolderPathComponents(srcFolderPath);
		sourceCodeReferenceToGuiceModule.setPrimaryTypeName(guiceModuleName);

		List<BindingDefinition> bindingStatements = astVisitor.getBindingStatements();
		for (BindingDefinition bindingStatement : bindingStatements) {
			copyInfo(sourceCodeReferenceToGuiceModule, bindingStatement);
		}
		List<InstallModuleStatement> installModuleStatements = astVisitor.getInstallModuleStatements();
		for (InstallModuleStatement installModuleStatement : installModuleStatements) {
			copyInfo(sourceCodeReferenceToGuiceModule, installModuleStatement);
		}

		GuiceModule guiceModule = new GuiceModule(
				sourceCodeReferenceToGuiceModule,
				installModuleStatements,
				bindingStatements);

		return guiceModule;
	}

	private void copyInfo(SourceCodeReference guiceModule,
			GuiceStatement statement) {
		SourceCodeReference statementReference = statement.getSourceCodeReference();
		statementReference.setProjectName(guiceModule.getProjectName());
		statementReference.setPackageNameComponents(guiceModule.getPackageNameComponents());
		statementReference.setSrcFolderPathComponents(guiceModule.getSrcFolderPathComponents());
		statementReference.setPrimaryTypeName(guiceModule.getPrimaryTypeName());
	}

}
