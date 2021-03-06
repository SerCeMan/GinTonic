package ru.naumen.gintonic.guice.analyzer;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import ru.naumen.gintonic.GinTonicPlugin;
import ru.naumen.gintonic.guice.GuiceModule;
import ru.naumen.gintonic.guice.statements.BindingDefinition;
import ru.naumen.gintonic.guice.statements.GuiceStatement;
import ru.naumen.gintonic.guice.statements.InstallModuleStatement;
import ru.naumen.gintonic.project.source.references.SourceCodeReference;
import ru.naumen.gintonic.utils.ASTParserUtils;
import ru.naumen.gintonic.utils.ICompilationUnitUtils;
import ru.naumen.gintonic.utils.SetUtils;

public class GuiceAnalyzer {

    //@formatter:off
    private static Set<String> supportedGuiceTypes = SetUtils.immutableSetOf(
            "AbstractModule", 
            "PrivateModule", 
            "AbstractGinModule");
    //@formatter:on

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
            ICompilationUnit compilationUnit = JavaCore.createCompilationUnitFrom(file);
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
                return parseGuiceModule(compilationUnit, project);
            }
            // TODO Maybe slow
            IType[] allClasses = primaryType.newSupertypeHierarchy(null).getAllClasses();
            for(IType clazz : allClasses) {
                if(supportedGuiceTypes.contains(clazz.getElementName())) {
                    return parseGuiceModule(compilationUnit, project);                    
                }
            }
        } catch (Exception e) {
            GinTonicPlugin.logException("Error analyzing " + filename, e);
        }
        return null;
    }

    private GuiceModule parseGuiceModule(ICompilationUnit compilationUnit, IProject project) {

        CompilationUnit cu = ASTParserUtils.parseCompilationUnitAst3(compilationUnit, true, true);

        /*
         * No need to fail if there are errors in the compilationUnit, as it
         * works also with some errors (tested multiple times and sure it
         * depends on the type of the error).
         */
        GuiceAnalyzerAstVisitor astVisitor = new GuiceAnalyzerAstVisitor();

        try {
            cu.accept(astVisitor);
        } catch (Exception exception) {
            String message = "Unable to analyze '" + compilationUnit.getElementName() + "'";
            GinTonicPlugin.logException(message, exception);
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

        return new GuiceModule(sourceCodeReferenceToGuiceModule, installModuleStatements,
                bindingStatements);
    }

    private void copyInfo(SourceCodeReference guiceModule, GuiceStatement statement) {
        SourceCodeReference statementReference = statement.getSourceCodeReference();
        statementReference.setProjectName(guiceModule.getProjectName());
        statementReference.setPackageNameComponents(guiceModule.getPackageNameComponents());
        statementReference.setSrcFolderPathComponents(guiceModule.getSrcFolderPathComponents());
        statementReference.setPrimaryTypeName(guiceModule.getPrimaryTypeName());
    }

}
