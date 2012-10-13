package egap.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
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
import egap.utils.InjectionIsAttachedTo;
import egap.utils.StringUtils;

/**
 * Creates a new JUnit 5 Test for the currently active class. The test contains
 * the injections from the class-under-test as mocks and an initialize method,
 * which injects the mocks into the class-under-test.
 * 
 * <h5>Example:</h5>
 * 
 * Given the class RealBillingService
 * 
 * <pre>
 * class RealBillingService implements BillingService {
 * 	private CreditCardProcessor processor;
 * 	private TransactionLog transactionLog;
 * 
 * 	&#064;Inject
 * 	public void setProcessor(CreditCardProcessor processor) {
 * 		this.processor = processor;
 * 	}
 * 
 * 	&#064;Inject
 * 	public void setTransactionLog(TransactionLog transactionLog) {
 * 		this.transactionLog = transactionLog;
 * 	}
 * 
 * 	&#064;Override
 * 	public Receipt chargeOrder(PizzaOrder order, CreditCard creditCard) {
 * 		return null;
 * 	}
 * }
 * </pre>
 * 
 * the generated testcase will look like:
 * 
 * <pre>
 * &#064;RunWith(MockitoJUnitRunner.class)
 * public class RealBillingServiceTest {
 * 
 * 	private RealBillingService realBillingService;
 * 
 * 	&#064;Mock
 * 	private CreditCardProcessor creditCardProcessorMock;
 * 
 * 	&#064;Mock
 * 	private TransactionLog transactionLogMock;
 * 
 * 	&#064;Before
 * 	public void initialize() {
 * 		realBillingService = new RealBillingService();
 * 		realBillingService.setTransactionLog(transactionLogMock);
 * 		realBillingService.setCreditCardProcessor(creditCardProcessorMock);
 * 	}
 * 
 * 	&#064;Test
 * 	public void test() {
 * 	}
 * 
 * }
 * </pre>
 * 
 */
public class CreateMockitoJUnitTestHandler extends AbstractHandler {

	private String testSuffix;
	private String testPackagePrefix;
	private String srcFolderForTests;
	private String srcFolderForNormalClasses;

	private enum Action {
		JUMPED_TO_TEST, JUMPED_TO_CLASS_UNDER_TEST, CREATED_TEST, UNDEFINED
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		Action action = Action.UNDEFINED;
		long now = System.currentTimeMillis();
		try {
			action = execute();
			return null;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			long then = System.currentTimeMillis();
			long elapsed = then - now;
			EgapPlugin.logInfo("Action " + action.name() + " took " + elapsed
					+ " ms.");
		}

	}

	private Action execute() throws RuntimeException {
		IEditorPart editorPart = EclipseUtils.getActiveEditor();

		if (editorPart == null | !(editorPart instanceof ITextEditor)) {
			return Action.UNDEFINED;
		}
		final ITextEditor textEditor = (ITextEditor) editorPart;
		IEditorInput editorInput = textEditor.getEditorInput();

		ITypeRoot editorInputTypeRoot = JavaUI.getEditorInputTypeRoot(editorInput);

		if (!(editorInputTypeRoot instanceof ICompilationUnit)) {
			return Action.UNDEFINED;
		}
		ICompilationUnit icompilationUnit = (ICompilationUnit) editorInputTypeRoot;
		CompilationUnit classUnderTestAsCompilationUnit = SharedASTProvider.getAST(
				editorInputTypeRoot,
				SharedASTProvider.WAIT_YES,
				null);

		initializePreferences();

		ProjectResource javaClass = IProjectResourceUtils.createProjectResource(icompilationUnit);
		if (javaClass == null) {
			return Action.UNDEFINED;
		}

		boolean isJunitTestcase = isJunitTestcase(icompilationUnit);
		if (isJunitTestcase) {
			/* JUnit Test */
			ProjectResource classUnderTest = createClassForJuniTest(javaClass);
			IFile normalClassAsIFile = IProjectResourceUtils.getJavaFile(classUnderTest);
			if (normalClassAsIFile.exists()) {
				IProjectResourceUtils.openEditorWithStatementDeclaration(classUnderTest);
				return Action.JUMPED_TO_CLASS_UNDER_TEST;
			}

		}
		else {
			ProjectResource junitTest = createJUnitClassFor(javaClass);
			IFile junitTestAsIFile = IProjectResourceUtils.getJavaFile(junitTest);
			
			/* TODO check other projects too. */
			
			if (junitTestAsIFile.exists()) {
				IProjectResourceUtils.openEditorWithStatementDeclaration(junitTest);
				return Action.JUMPED_TO_TEST;
			}
			/* Test does not exist - lets create it! */
			try {
				ProjectResource junitTestAsProjectResource = createJUnitClassFor(javaClass);

				createJUnitTest(
						icompilationUnit.getJavaProject(),
						junitTestAsProjectResource,
						junitTest,
						editorInputTypeRoot,
						classUnderTestAsCompilationUnit);

				return Action.CREATED_TEST;
			} catch (CoreException e) {
				EgapPlugin.logException(e);
			}
		}

		return Action.UNDEFINED;
	}

	/**
	 * Returns true if the given ICompilationUnit is a JUnit Test. We assert
	 * that it is if it ends with the test suffix.
	 */
	private boolean isJunitTestcase(ICompilationUnit icompilationUnit) {
		return icompilationUnit.getElementName().endsWith(
				testSuffix + ICompilationUnitUtils.JAVA_EXTENSION);
	}

	/**
	 * 
	 */
	private void initializePreferences() {
		IPreferenceStore store = EgapPlugin.getEgapPlugin().getPreferenceStore();

		testSuffix = store.getString(EgapPlugin.ID_TEST_SUFFIX);
		testPackagePrefix = store.getString(EgapPlugin.ID_TEST_PACKAGE_PREFIX);
		srcFolderForTests = store.getString(EgapPlugin.ID_TEST_SRC_FOLDER);
		srcFolderForNormalClasses = store.getString(EgapPlugin.ID_SRC_FOLDER);
	}

	private void createJUnitTest(IJavaProject javaProject,
			ProjectResource junitTestAsProjectResource,
			final ProjectResource classUnderTestAsProjectResource,
			ITypeRoot classUnderTestAsTypeRoot,
			CompilationUnit classUnderTestAsCompilationUnit)
			throws JavaModelException {

		StringBuffer sb = new StringBuffer(300);
		JavaCodeBuilder codeGenerator = new JavaCodeBuilder(sb);

		String typeName = classUnderTestAsProjectResource.getTypeName();

		codeGenerator.append("@RunWith( MockitoJUnitRunner.class )");
		codeGenerator.startClass(typeName);
		codeGenerator.startBlock();

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
				null,
				true);

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

		CompilationUnit junitTestAsCompilationUnit = ASTParserUtils.parseCompilationUnitAst3(
				junitTestAsICompilationUnit,
				true,
				false);

		Refactorator refactorator = Refactorator.create(
				junitTestAsICompilationUnit,
				junitTestAsCompilationUnit,
				junitTestAsCompilationUnit.getAST());

		refactorator.addImport("org.junit.runner.RunWith");
		refactorator.addImport("org.mockito.runners.MockitoJUnitRunner");
		refactorator.addImport("org.mockito.Mock");

		List<GuiceFieldDeclaration> injectionPoints = findInjectionPoints(
				classUnderTestAsProjectResource,
				classUnderTestAsTypeRoot);

		TypeDeclaration primaryTypeDeclaration = findPrimaryTypeDeclaration(classUnderTestAsCompilationUnit);

		addClassUnderTestAsField(
				primaryTypeDeclaration,
				junitTestAsCompilationUnit,
				refactorator);

		addInjectionsAsMocksInTestcase(
				injectionPoints,
				junitTestAsCompilationUnit,
				refactorator);

		createInitializerMethod(
				junitTestAsCompilationUnit,
				refactorator,
				primaryTypeDeclaration,
				injectionPoints);

		createTestMethod(junitTestAsCompilationUnit.getAST(), refactorator);

		refactorator.refactor(null);

		IProjectResourceUtils.openEditorWithStatementDeclaration(junitTestAsProjectResource);

	}

	/**
	 * Creates a new empty test method:
	 * 
	 * <pre>
	 * &#064;Test
	 * public void test() {
	 * 
	 * }
	 * </pre>
	 * 
	 */
	private void createTestMethod(AST ast, Refactorator refactorator) {
		MethodDeclaration methodDecl = ast.newMethodDeclaration();
		methodDecl.setConstructor(false);
		@SuppressWarnings("unchecked")
		List<IExtendedModifier> modifiers = methodDecl.modifiers();
		MarkerAnnotation markerAnnotation = ast.newMarkerAnnotation();
		markerAnnotation.setTypeName(ast.newName("Test"));
		modifiers.add(markerAnnotation);
		modifiers.add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
		methodDecl.setName(ast.newSimpleName("test"));
		Block methodBody = ast.newBlock();
		methodDecl.setBody(methodBody);

		refactorator.addImport("org.junit.Test");
		refactorator.addMethodDeclaration(methodDecl);
	}

	/**
	 * Creates a new @Before method. In the method there is a new instance of
	 * the class-under-test created and the mocks are injected into it. As there
	 * are 3 possible ways how to inject the mocks (constructor, setter, direct
	 * field access) we assume it to be the same as where the @Inject annotation
	 * is applied to the class-under-test (see
	 * {@link GuiceFieldDeclaration#getInjectionIsAttachedTo()}).
	 * 
	 * <h5>Example 1: The injections were attached to the constructor</h5>
	 * 
	 * <pre>
	 * &#064;Before
	 * public void initialize() {
	 * 	importJobTextRenderer = new ImportJobTextRenderer(
	 * 			datenstandDeltaBerechnerMock,
	 * 			textBuilderFactoryMock);
	 * }
	 * </pre>
	 * 
	 * <h5>Example 2: The injections were attached to the setter methods</h5>
	 * 
	 * <pre>
	 * &#064;Before
	 * public void initialize() {
	 * 	importJobTextRenderer = new ImportJobTextRenderer();
	 * 	importJobTextRenderer.setDatenstandDeltaBerechner(datenstandDeltaBerechnerMock);
	 * 	importJobTextRenderer.setTextBuilderFactory(textBuilderFactoryMock);
	 * }
	 * </pre>
	 * 
	 * <h5>Example 3: The injections were attached to the field</h5>
	 * 
	 * <pre>
	 * &#064;Before
	 * public void initialize() {
	 * 	importJobTextRenderer = new ImportJobTextRenderer();
	 * 	importJobTextRenderer.datenstandDeltaBerechner = datenstandDeltaBerechnerMock;
	 * 	importJobTextRenderer.textBuilderFactory = textBuilderFactoryMock;
	 * }
	 * </pre>
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void createInitializerMethod(
			CompilationUnit junitTestAsCompilationUnit,
			Refactorator refactorator, TypeDeclaration primaryTypeDeclaration,
			List<GuiceFieldDeclaration> injectionPoints) {

		AST ast = junitTestAsCompilationUnit.getAST();
		MethodDeclaration methodDecl = ast.newMethodDeclaration();
		methodDecl.setConstructor(false);
		List<IExtendedModifier> modifiers = methodDecl.modifiers();
		MarkerAnnotation markerAnnotation = ast.newMarkerAnnotation();
		markerAnnotation.setTypeName(ast.newName("Before"));
		modifiers.add(markerAnnotation);
		modifiers.add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
		methodDecl.setName(ast.newSimpleName("initialize"));
		Block methodBody = ast.newBlock();
		List<Statement> statements = methodBody.statements();
		methodDecl.setBody(methodBody);

		if (injectionPoints.isEmpty()) {
			ExpressionStatement expressionStatement = createClassInstanceCreation(
					primaryTypeDeclaration,
					ast);
			statements.add(expressionStatement);
		}
		else {

			/*
			 * We assume all declaration to be attached the same way(e.g all by
			 * setters, all by field, ...) so we only check the first injection
			 * point.
			 */
			GuiceFieldDeclaration firstFieldDeclaration = injectionPoints.get(0);
			InjectionIsAttachedTo injectionIsAttachedTo = firstFieldDeclaration.getInjectionIsAttachedTo();

			ITypeBinding classUnderTestBinding = primaryTypeDeclaration.resolveBinding();
			String classUnderTestTypeNameSimple = classUnderTestBinding.getName();
			String classUnderTestVarName = StringUtils.uncapitalize(classUnderTestTypeNameSimple);

			if (injectionIsAttachedTo == InjectionIsAttachedTo.FIELD) {
				ExpressionStatement expressionStatement = createClassInstanceCreation(
						primaryTypeDeclaration,
						ast);
				statements.add(expressionStatement);

				for (GuiceFieldDeclaration guiceFieldDeclaration : injectionPoints) {
					Assignment assignment = ast.newAssignment();
					assignment.setLeftHandSide(ast.newQualifiedName(
							ast.newSimpleName(classUnderTestVarName),
							ast.newSimpleName(guiceFieldDeclaration.getVariableName())));
					assignment.setRightHandSide(ast.newSimpleName((getMockName(guiceFieldDeclaration))));
					statements.add(ast.newExpressionStatement(assignment));
				}

			}
			else if (injectionIsAttachedTo == InjectionIsAttachedTo.SETTER) {
				ExpressionStatement expressionStatement = createClassInstanceCreation(
						primaryTypeDeclaration,
						ast);
				statements.add(expressionStatement);

				for (GuiceFieldDeclaration guiceFieldDeclaration : injectionPoints) {
					String variableName = guiceFieldDeclaration.getVariableName();
					String variableNameOfMock = getMockName(guiceFieldDeclaration);
					String setter = StringUtils.toSetterMethodname(variableName);

					MethodInvocation methodInvocation = ast.newMethodInvocation();
					methodInvocation.setExpression(ast.newSimpleName(classUnderTestVarName));
					methodInvocation.setName(ast.newSimpleName(setter));
					methodInvocation.arguments().add(
							ast.newSimpleName(variableNameOfMock));

					statements.add(ast.newExpressionStatement(methodInvocation));
				}
			}
			else { /* injectionIsAttachedTo == InjectionIsAttachedTo.CONSTRUCTOR */
				ExpressionStatement expressionStatement = createClassInstanceCreation(
						primaryTypeDeclaration,
						ast, injectionPoints);
				statements.add(expressionStatement);
			}
		}

		refactorator.addImport("org.junit.Before");
		refactorator.addMethodDeclaration(methodDecl);
	}
	
	/**
	 * classUnderTest = new ClassUnderTest(dependency1, dependency2);
	 */
	private ExpressionStatement createClassInstanceCreation(
			TypeDeclaration primaryTypeDeclaration, AST ast,
			List<GuiceFieldDeclaration> injectionPoints) {
		ITypeBinding classUnderTestBinding = primaryTypeDeclaration.resolveBinding();
		String classUnderTestTypeNameSimple = classUnderTestBinding.getName();
		SimpleType classUnderTestType = ast.newSimpleType(ast.newSimpleName(classUnderTestTypeNameSimple));
		String classUnderTestVarName = StringUtils.uncapitalize(classUnderTestTypeNameSimple);
		ClassInstanceCreation classInstanceCreation = ast.newClassInstanceCreation();
		classInstanceCreation.setType(classUnderTestType);
		
		for (GuiceFieldDeclaration guiceFieldDeclaration : injectionPoints) {
			String variableNameOfMock = getMockName(guiceFieldDeclaration);
			@SuppressWarnings("unchecked")
			List<Expression> arguments = classInstanceCreation.arguments();
			arguments.add(ast.newSimpleName(variableNameOfMock));
		}
		
		Assignment assignment = ast.newAssignment();
		assignment.setLeftHandSide(ast.newSimpleName(classUnderTestVarName));
		assignment.setRightHandSide(classInstanceCreation);

		ExpressionStatement expressionStatement = ast.newExpressionStatement(assignment);
		return expressionStatement;
	}

	private String getMockName(GuiceFieldDeclaration guiceFieldDeclaration) {
		return guiceFieldDeclaration.getVariableName()
				+ "Mock";
	}

	/**
	 * classUnderTest = new ClassUnderTest();
	 */
	private ExpressionStatement createClassInstanceCreation(
			TypeDeclaration primaryTypeDeclaration, AST ast) {
		ITypeBinding classUnderTestBinding = primaryTypeDeclaration.resolveBinding();
		String classUnderTestTypeNameSimple = classUnderTestBinding.getName();
		SimpleType classUnderTestType = ast.newSimpleType(ast.newSimpleName(classUnderTestTypeNameSimple));
		String classUnderTestVarName = StringUtils.uncapitalize(classUnderTestTypeNameSimple);
		ClassInstanceCreation classInstanceCreation = ast.newClassInstanceCreation();
		classInstanceCreation.setType(classUnderTestType);

		Assignment assignment = ast.newAssignment();
		assignment.setLeftHandSide(ast.newSimpleName(classUnderTestVarName));
		assignment.setRightHandSide(classInstanceCreation);

		ExpressionStatement expressionStatement = ast.newExpressionStatement(assignment);
		return expressionStatement;
	}

	/**
	 * TODO
	 */
	private List<GuiceFieldDeclaration> findInjectionPoints(
			final ProjectResource classUnderTestAsProjectResource,
			ITypeRoot classUnderTestAsTypeRoot) {
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
		return injectionPoints;
	}

	/**
	 * Adds the injected dependencies from the class-under-test as fields in the
	 * testcase. The annotations and the javadoc from the class-under-test are
	 * not copied to the new fields.
	 */
	private List<FieldDeclaration> addInjectionsAsMocksInTestcase(
			final List<GuiceFieldDeclaration> injectionPoints,
			CompilationUnit junitTestAsCompilationUnit,
			Refactorator refactorator) {
		List<FieldDeclaration> fieldDeclarations = new ArrayList<FieldDeclaration>(
				injectionPoints.size());
		for (GuiceFieldDeclaration injectionPoint : injectionPoints) {
			ITypeBinding targetTypeBinding = injectionPoint.getTargetTypeBinding();
			refactorator.addImport(targetTypeBinding);

			FieldDeclaration fieldDeclaration = injectionPoint.getFieldDeclaration();
			AST ast = junitTestAsCompilationUnit.getAST();
			FieldDeclaration fieldDeclarationUnparented = (FieldDeclaration) ASTNode.copySubtree(
					ast,
					fieldDeclaration);

			@SuppressWarnings("unchecked")
			List<VariableDeclarationFragment> fragments = fieldDeclarationUnparented.fragments();
			VariableDeclarationFragment variableDeclarationFragment = fragments.get(0);
			variableDeclarationFragment.setName(ast.newSimpleName(getMockName(injectionPoint)));

			FieldDeclarationUtils.removeAnnotations(fieldDeclarationUnparented);
			
			@SuppressWarnings("unchecked")
			List<ASTNode> modifiers = fieldDeclarationUnparented.modifiers();
			modifiers.add(ast.newModifier(ModifierKeyword.PRIVATE_KEYWORD));
			
			fieldDeclarationUnparented.setJavadoc(null);
			FieldDeclarationUtils.addMarkerAnnotation(
					fieldDeclarationUnparented,
					"Mock");

			refactorator.addFieldDeclaration(fieldDeclarationUnparented);
			fieldDeclarations.add(fieldDeclarationUnparented);
		}
		return fieldDeclarations;
	}

	private TypeDeclaration findPrimaryTypeDeclaration(
			CompilationUnit classUnderTestAsCompilationUnit) {
		@SuppressWarnings("unchecked")
		List<AbstractTypeDeclaration> types = classUnderTestAsCompilationUnit.types();
		for (AbstractTypeDeclaration abstractTypeDeclaration : types) {
			if (abstractTypeDeclaration instanceof TypeDeclaration) {
				TypeDeclaration typeDecl = (TypeDeclaration) abstractTypeDeclaration;
				return typeDecl;
			}
		}
		return null;
	}

	/**
	 * Adds the class under test as field in the testcase.
	 */
	@SuppressWarnings("unchecked")
	private void addClassUnderTestAsField(TypeDeclaration typeDecl,
			CompilationUnit junitTestAsCompilationUnit,
			Refactorator refactorator) {

		AST ast = junitTestAsCompilationUnit.getAST();

		VariableDeclarationFragment variableDeclarationFragment = ast.newVariableDeclarationFragment();
		ITypeBinding typeBinding = typeDecl.resolveBinding();
		String typeName2 = typeBinding.getName();
		String varName = StringUtils.uncapitalize(typeName2);
		variableDeclarationFragment.setName(ast.newSimpleName(varName));

		FieldDeclaration fieldDeclaration = ast.newFieldDeclaration(variableDeclarationFragment);
		SimpleType simpleType = ast.newSimpleType(ast.newName(typeName2));
		fieldDeclaration.setType(simpleType);
		List<IExtendedModifier> modifiers = fieldDeclaration.modifiers();
		modifiers.add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));

		Javadoc javadoc = ast.newJavadoc();
		final TagElement tagComment = ast.newTagElement();
		TextElement text = ast.newTextElement();
		text.setText("The class under test");
		tagComment.fragments().add(text);
		javadoc.tags().add(tagComment);
		fieldDeclaration.setJavadoc(javadoc);

		refactorator.addImport(typeBinding);
		refactorator.addFieldDeclaration(fieldDeclaration);
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

		if(testPackagePrefix.trim().isEmpty()){
			junitTest.setPackage(javaClass.getPackage());
		}else{
			LinkedList<String> junitTestcasePackage = new LinkedList<String>(
					javaClass.getPackage());
			junitTestcasePackage.addFirst(testPackagePrefix);
			junitTest.setPackage(junitTestcasePackage);
		}
		
		junitTest.setTypeName(javaClass.getTypeName() + testSuffix);
		return junitTest;
	}

	/**
	 * Creates a class-under-test from a given junit class.
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
