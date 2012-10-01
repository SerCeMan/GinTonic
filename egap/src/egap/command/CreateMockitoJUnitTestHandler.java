package egap.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationMessages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import egap.EgapPlugin;
import egap.guice.ProjectResource;
import egap.refactor.Refactorator;
import egap.source_builder.JavaCodeBuilder;
import egap.source_formatter.JavaSourceFormatter;
import egap.source_formatter.SourceFormatFailedException;
import egap.utils.ASTParserUtils;
import egap.utils.EclipseUtils;
import egap.utils.FieldDeclarationUtils;
import egap.utils.GuiceFieldDeclaration;
import egap.utils.ICompilationUnitUtils;
import egap.utils.IPackageFragmentUtils;
import egap.utils.IProjectResourceUtils;
import egap.utils.StringUtils;

public class CreateMockitoJUnitTestHandler extends AbstractHandler {

	private String testSuffix;
	private String testPackagePrefix;
	private String srcFolderForTests;
	private String srcFolderForNormalClasses;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IEditorPart editorPart = EclipseUtils.getActiveEditor();

		if (editorPart == null | !(editorPart instanceof ITextEditor)) {
			return null;
		}
		final ITextEditor textEditor = (ITextEditor) editorPart;
		IEditorInput editorInput = textEditor.getEditorInput();

		ITypeRoot editorInputTypeRoot = JavaUI.getEditorInputTypeRoot(editorInput);

		if (!(editorInputTypeRoot instanceof ICompilationUnit)) {
			return null;
		}
		ICompilationUnit icompilationUnit = (ICompilationUnit) editorInputTypeRoot;
		CompilationUnit classUnderTestAsCompilationUnit = SharedASTProvider.getAST(
				editorInputTypeRoot,
				SharedASTProvider.WAIT_YES,
				null);

		IPreferenceStore store = EgapPlugin.getEgapPlugin().getPreferenceStore();

		testSuffix = store.getString(EgapPlugin.ID_TEST_SUFFIX);
		testPackagePrefix = store.getString(EgapPlugin.ID_TEST_PACKAGE_PREFIX);
		srcFolderForTests = store.getString(EgapPlugin.ID_TEST_SRC_FOLDER);
		srcFolderForNormalClasses = store.getString(EgapPlugin.ID_SRC_FOLDER);

		ProjectResource javaClass = IProjectResourceUtils.createProjectResource(icompilationUnit);
		if (javaClass == null) {
			return null;
		}

		if (icompilationUnit.getElementName().endsWith(
				testSuffix + ICompilationUnitUtils.JAVA_EXTENSION)) {
			/* JUnit Test */
			ProjectResource normalClass = createClassForJuniTest(javaClass);
			IFile normalClassAsIFile = IProjectResourceUtils.getJavaFile(normalClass);
			if (normalClassAsIFile.exists()) {
				IProjectResourceUtils.openEditorWithStatementDeclaration(normalClass);
			}
			else {

			}

		}
		else {
			ProjectResource junitTest = createJUnitClassFor(javaClass);
			IFile junitTestAsIFile = IProjectResourceUtils.getJavaFile(junitTest);
			if (junitTestAsIFile.exists()) {
				IProjectResourceUtils.openEditorWithStatementDeclaration(junitTest);
			}
			else {
				/* Test does not exist - lets create it! */
				try {
					ProjectResource junitTestAsProjectResource = createJUnitClassFor(javaClass);
					createJUnitTest(
							icompilationUnit.getJavaProject(),
							junitTestAsProjectResource,
							junitTest,
							editorInputTypeRoot,
							icompilationUnit,
							classUnderTestAsCompilationUnit);
				} catch (JavaModelException e) {
					EgapPlugin.logException(e);
				}
			}
		}

		return null;
	}

	private void createJUnitTest(IJavaProject javaProject,
			ProjectResource junitTestAsProjectResource,
			final ProjectResource classUnderTestAsProjectResource,
			ITypeRoot classUnderTestAsTypeRoot,
			ICompilationUnit classUnderTestAsICompilationUnit,
			CompilationUnit classUnderTestAsCompilationUnit)
			throws JavaModelException {

		final List<GuiceFieldDeclaration> injectionPoints = new ArrayList<GuiceFieldDeclaration>(
				30);

		final CompilationUnit compilationUnit = SharedASTProvider.getAST(
				classUnderTestAsTypeRoot,
				SharedASTProvider.WAIT_YES,
				null);
		compilationUnit.accept(new ASTVisitor(false) {

			private String identifier;

			@Override
			public boolean visit(FieldDeclaration fieldDeclaration) {

				/* We can skip static fields as they cannot be injected. */
				boolean isStatic = FieldDeclarationUtils.isStatic(fieldDeclaration);
				if (isStatic) {
					return false;
				}

				fieldDeclaration.accept(new ASTVisitor() {

					@Override
					public boolean visit(VariableDeclarationFragment node) {
						SimpleName name = node.getName();
						identifier = name.getIdentifier();
						return false;
					}

				});

				GuiceFieldDeclaration injectionPoint = FieldDeclarationUtils.getTypeIfAnnotatedWithInject(
						classUnderTestAsProjectResource,
						fieldDeclaration,
						compilationUnit,
						identifier);

				if (injectionPoint != null) {
					injectionPoints.add(injectionPoint);
				}

				identifier = null;

				return false;
			}

		});

		StringBuffer sb = new StringBuffer(300);
		JavaCodeBuilder codeGenerator = new JavaCodeBuilder(sb);

		String typeName = classUnderTestAsProjectResource.getTypeName();

		codeGenerator.append("@RunWith( MockitoJUnitRunner.class )");
		codeGenerator.startClass(typeName);
		codeGenerator.startBlock();

		codeGenerator.startMethod(
				Arrays.asList("Test"),
				"public",
				"test",
				"void",
				null,
				null);
		codeGenerator.startBlock();
		codeGenerator.finishBlock();

		codeGenerator.finishBlock();

		String sourceCode = sb.toString();
		try {
			sourceCode = JavaSourceFormatter.format(sourceCode);
		} catch (SourceFormatFailedException e) {
			EgapPlugin.logException(e);
		}

		String packageFullyQualified = classUnderTestAsProjectResource.getPackageFullyQualified();
		IPackageFragment packageFragment = IPackageFragmentUtils.createPackageFragment(
				javaProject,
				srcFolderForTests,
				packageFullyQualified,
				null);

		if (packageFragment == null) {
			EgapPlugin.logWarning("Unable to create package "
					+ packageFullyQualified + " (project = "
					+ javaProject.getElementName() + ", src folder = "
					+ srcFolderForTests + ")");
			return;
		}

		ICompilationUnit junitTestAsICompilationUnit = ICompilationUnitUtils.createJavaCompilationUnit(
				packageFragment,
				typeName,
				sourceCode);
		CompilationUnit junitTestAsCompilationUnit = ASTParserUtils.parseCompilationUnitAst3(junitTestAsICompilationUnit);

		Refactorator refactorator = Refactorator.create(
				junitTestAsICompilationUnit,
				junitTestAsCompilationUnit,
				junitTestAsCompilationUnit.getAST());

		refactorator.addImport("org.junit.Test");
		refactorator.addImport("org.junit.runner.RunWith");
		refactorator.addImport("org.mockito.runners.MockitoJUnitRunner");
		refactorator.addImport("org.mockito.Mock");

		addInjectionsAsMocksInTestcase(
				injectionPoints,
				junitTestAsCompilationUnit,
				refactorator);

		addClassUnderTestAsField(
				classUnderTestAsCompilationUnit,
				junitTestAsCompilationUnit,
				refactorator);

		/*
		 * Create a new @Before method which injects the mocks into the
		 * class-under-test.
		 */
		AST junitTestAst = junitTestAsCompilationUnit.getAST();
		junitTestAst.newMethodDeclaration();

		refactorator.refactor(null);

		IProjectResourceUtils.openEditorWithStatementDeclaration(junitTestAsProjectResource);

	}

	/**
	 * Adds the injected dependencies from the class-under-test as fields in the
	 * testcase.
	 */
	private void addInjectionsAsMocksInTestcase(
			final List<GuiceFieldDeclaration> injectionPoints,
			CompilationUnit junitTestAsCompilationUnit,
			Refactorator refactorator) {
		for (GuiceFieldDeclaration injectionPoint : injectionPoints) {
			ITypeBinding targetTypeBinding = injectionPoint.getTargetTypeBinding();
			refactorator.addImport(targetTypeBinding);

			FieldDeclaration fieldDeclaration = injectionPoint.getFieldDeclaration();
			FieldDeclaration fieldDeclarationUnparented = (FieldDeclaration) ASTNode.copySubtree(
					junitTestAsCompilationUnit.getAST(),
					fieldDeclaration);

			FieldDeclarationUtils.removeAnnotations(fieldDeclarationUnparented);
			FieldDeclarationUtils.addAnnotation(
					fieldDeclarationUnparented,
					"Mock");

			refactorator.addFieldDeclaration(fieldDeclarationUnparented);
		}
	}

	/**
	 * Adds the class under test as field in the testcase.
	 */
	private void addClassUnderTestAsField(
			CompilationUnit classUnderTestAsCompilationUnit,
			CompilationUnit junitTestAsCompilationUnit,
			Refactorator refactorator) {
		@SuppressWarnings("unchecked")
		List<AbstractTypeDeclaration> types = classUnderTestAsCompilationUnit.types();
		for (AbstractTypeDeclaration abstractTypeDeclaration : types) {
			if (abstractTypeDeclaration instanceof TypeDeclaration) {
				TypeDeclaration typeDecl = (TypeDeclaration) abstractTypeDeclaration;
				AST ast = junitTestAsCompilationUnit.getAST();
				VariableDeclarationFragment variableDeclarationFragment = ast.newVariableDeclarationFragment();
				ITypeBinding typeBinding = typeDecl.resolveBinding();
				String typeName2 = typeBinding.getName();
				String varName = StringUtils.uncapitalize(typeName2);
				variableDeclarationFragment.setName(ast.newSimpleName(varName));
				FieldDeclaration fieldDeclaration = ast.newFieldDeclaration(variableDeclarationFragment);
				fieldDeclaration.setType(ast.newSimpleType(ast.newName(typeName2)));
				@SuppressWarnings("unchecked")
				List<IExtendedModifier> modifiers = fieldDeclaration.modifiers();
				modifiers.add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
				
				Javadoc javadoc= ast.newJavadoc();
				final TagElement tagComment= ast.newTagElement();
				TextElement text= ast.newTextElement();
				text.setText("The class under test");
				tagComment.fragments().add(text);
				javadoc.tags().add(tagComment);
				fieldDeclaration.setJavadoc(javadoc);
				
				refactorator.addImport(typeBinding);
				refactorator.addFieldDeclaration(fieldDeclaration);
				
				
				
				
				break;
			}
		}
	}

	/**
	 * Creates a JUnit class from a given java class. The test is assumed to end
	 * with a {@link #testSuffix}, lies in a source folder named
	 * {@link #srcFolderForTests} and the package is prefixed with
	 * {@link #testPackagePrefix}.
	 * 
	 * <h5>Example:</h5>
	 * 
	 * Given the following class
	 * 
	 * <pre>
	 * src/some.package.AClass
	 * </pre>
	 * 
	 * we assume the test case to be
	 * 
	 * <pre>
	 * src-test/test.some.package.AClassTest
	 * </pre>
	 */
	private ProjectResource createJUnitClassFor(ProjectResource javaClass) {
		ProjectResource junitTest = new ProjectResource();
		junitTest.setProjectName(javaClass.getProjectName());
		junitTest.setSrcFolderPathComponents(Arrays.asList(srcFolderForTests));

		LinkedList<String> junitTestcasePackage = new LinkedList<String>(
				javaClass.getPackage());
		junitTestcasePackage.addFirst(testPackagePrefix);
		junitTest.setPackage(junitTestcasePackage);
		junitTest.setTypeName(javaClass.getTypeName() + testSuffix);
		return junitTest;
	}

	/**
	 * Creates a normal class from a given junit class.
	 * 
	 * <h5>Example:</h5>
	 * 
	 * Given the following class
	 * 
	 * <pre>
	 * src/some.package.AClass
	 * </pre>
	 * 
	 * we assume the test case to be
	 * 
	 * <pre>
	 * src-test/test.some.package.AClassTest
	 * </pre>
	 */
	private ProjectResource createClassForJuniTest(ProjectResource junitTest) {
		ProjectResource javaClass = new ProjectResource();
		javaClass.setProjectName(junitTest.getProjectName());
		javaClass.setSrcFolderPathComponents(Arrays.asList(srcFolderForNormalClasses));

		javaClass.setPackage(junitTest.getPackage().subList(
				1,
				junitTest.getPackage().size()));
		javaClass.setTypeName(junitTest.getTypeName().replace(testSuffix, ""));
		return javaClass;
	}

}
