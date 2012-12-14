package de.jaculon.egap.guice.analyzer;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import de.jaculon.egap.EgapPlugin;
import de.jaculon.egap.guice.GuiceConstants;
import de.jaculon.egap.guice.annotations.GuiceAnnotation;
import de.jaculon.egap.guice.statements.BindingDefinition;
import de.jaculon.egap.guice.statements.ConstantBindingStatement;
import de.jaculon.egap.guice.statements.GuiceStatement;
import de.jaculon.egap.guice.statements.InstallModuleStatement;
import de.jaculon.egap.guice.statements.InstanceBindingStatement;
import de.jaculon.egap.guice.statements.LinkedBindingStatement;
import de.jaculon.egap.guice.statements.MapBinderCreateStatement;
import de.jaculon.egap.guice.statements.ProviderBindingStatement;
import de.jaculon.egap.guice.statements.ProviderMethod;
import de.jaculon.egap.guice.statements.SetBinderCreateStatement;
import de.jaculon.egap.source_reference.SourceCodeReference;
import de.jaculon.egap.utils.ASTNodeUtils;
import de.jaculon.egap.utils.AnnotationList;
import de.jaculon.egap.utils.ExpressionUtils;
import de.jaculon.egap.utils.ListUtils;
import de.jaculon.egap.utils.MethodInvocationUtils;
import de.jaculon.egap.utils.Preconditions;
import de.jaculon.egap.utils.SetUtils;
import de.jaculon.egap.utils.StringUtils;
import de.jaculon.egap.utils.TypeUtils;

/**
 * @author tmajunke
 */
@SuppressWarnings("unchecked")
public final class GuiceAnalyzerAstVisitor extends ASTVisitor {

	private ITypeBinding guiceModuleTypeBinding;
	private List<BindingDefinition> bindingStatements = ListUtils.newArrayListWithCapacity(30);
	private List<InstallModuleStatement> installModuleStatements = ListUtils.newArrayListWithCapacity(50);

	private BindingDefinition bindingStatement;
	private GuiceAnnotation guiceAnnotation;
	private String boundType;
	private String implType;
	private String scopeType;
	private boolean isEagerSingleton;

	public List<BindingDefinition> getBindingStatements() {
		return bindingStatements;
	}

	public ITypeBinding getGuiceModuleTypeBinding() {
		return guiceModuleTypeBinding;
	}

	public List<InstallModuleStatement> getInstallModuleStatements() {
		return installModuleStatements;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		guiceModuleTypeBinding = node.resolveBinding();
		Preconditions.checkNotNull(guiceModuleTypeBinding);
		return true;
	}

	private void addBinding(BindingDefinition bindingStatement) {
		bindingStatements.add(bindingStatement);
	}

	private void addModuleInstallStatement(
			InstallModuleStatement installModuleStatement) {
		installModuleStatements.add(installModuleStatement);
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		SimpleName name = methodInvocation.getName();
		String methodname = name.getIdentifier();

		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();

		if (methodBinding == null) {
			/* Maybe error! */
			return false;
		}

		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		ITypeBinding declaringClassTypeDecl = declaringClass.getTypeDeclaration();

		/* com.google.inject.binder.LinkedBindingBuilder */
		String declType = declaringClassTypeDecl.getQualifiedName();

		List<Expression> arguments = methodInvocation.arguments();

		/* The most method calls only expect one parameter, so we store it here */
		Expression firstArgument = null;
		if (arguments.size() > 0) {
			firstArgument = arguments.get(0);
		}

		if (declType.equals(GuiceConstants.ABSTRACT_MODULE)) {
			if (methodname.equals("bind")) {
				boundType = ExpressionUtils.getQualifiedTypeName(firstArgument);

				if (bindingStatement == null) {
					/* e.g bind(X.class).in(Scopes.SINGLETON); */
					bindingStatement = new BindingDefinition();
				}

				/*
				 * as bind is the last methodInvocation in the method chain, we
				 * can now initialize the binding statement with the types we
				 * recorded (e.g scopeType).
				 */

				finishBindingStatement(methodInvocation);
			}
			else if (methodname.equals("install")) {
				String installType = ExpressionUtils.getQualifiedTypeName(firstArgument);
				InstallModuleStatement installModuleStatement = new InstallModuleStatement();
				installModuleStatement.setModuleNameFullyQualified(installType);

				injectSourceCodeReference(
						methodInvocation,
						installModuleStatement);

				addModuleInstallStatement(installModuleStatement);
				clearScope();
			}
			else if (methodname.equals("bindConstant")) {
				finishBindingStatement(methodInvocation);
			}
			else if (methodname.equals("binder")) {
				/* We don't care */
			}
			else {
				unsupportedMethod(GuiceConstants.ABSTRACT_MODULE, methodname);
			}
		}

		else if (declType.equals(GuiceConstants.LINKED_BINDING_BUILDER)) {

			/*
			 * Special case, as the LinkedBindingBuilder is also used from the
			 * MapBinder and Setbinder. But we don't want these bindings!
			 */

			MethodInvocation firstMethodInvocation = MethodInvocationUtils.resolveFirstMethodInvocation(methodInvocation);
			SimpleName firstMethodName = firstMethodInvocation.getName();
			String firstMethodNameAsString = firstMethodName.toString();
			boolean isMultibinderInChain = firstMethodNameAsString.equals("addBinding");
			if (!isMultibinderInChain) {
				if (methodname.equals("to")) {
					bindingStatement = new LinkedBindingStatement();
					implType = ExpressionUtils.getQualifiedTypeName(firstArgument);
				}
				else if (methodname.equals("toInstance")) {
					bindingStatement = new InstanceBindingStatement();
					implType = ExpressionUtils.getQualifiedTypeName(firstArgument);
				}
				else if (methodname.equals("toProvider")) {
					ProviderBindingStatement providerBindingStatement = new ProviderBindingStatement();
					String providerClassType = ExpressionUtils.getQualifiedTypeName(firstArgument);
					providerBindingStatement.setProviderClassType(providerClassType);
					bindingStatement = providerBindingStatement;
				}
				else {
					unsupportedMethod(
							GuiceConstants.LINKED_BINDING_BUILDER,
							methodname);
				}
			}
		}

		else if (declType.equals(GuiceConstants.ANNOTATED_BINDING_BUILDER)) {
			/* void in(Scope scope); */
			/* void in(Class<? extends Annotation> scopeAnnotation); */
			/* void asEagerSingleton(); */
			if (methodname.equals("annotatedWith")) {
				resolveAnnotations(firstArgument);
			}
			else {
				unsupportedMethod(
						GuiceConstants.ANNOTATED_BINDING_BUILDER,
						methodname);
			}
		}
		else if (declType.equals(GuiceConstants.CONSTANT_BINDING_BUILDER)) {
			if (methodname.equals("to")) {
				bindingStatement = new ConstantBindingStatement();
				implType = ExpressionUtils.getQualifiedTypeName(firstArgument);

				/*
				 * Special case in constants as impl and interface type are
				 * equal
				 */
				boundType = implType;
			}
			/* By the way - no scopes for constants! */
			else {
				unsupportedMethod(
						GuiceConstants.CONSTANT_BINDING_BUILDER,
						methodname);
			}
		}
		else if (declType.equals(GuiceConstants.ANNOTATED_CONSTANT_BINDING_BUILDER)) {
			if (methodname.equals("annotatedWith")) {
				resolveAnnotations(firstArgument);
			}
			else {
				unsupportedMethod(
						GuiceConstants.ANNOTATED_CONSTANT_BINDING_BUILDER,
						methodname);
			}
		}
		else if (declType.equals(GuiceConstants.SCOPED_BINDING_BUILDER)) {
			if (methodname.equals("in")) {

				if (firstArgument instanceof QualifiedName) {
					QualifiedName qualifiedName = (QualifiedName) firstArgument;
					String fullyQualifiedName = qualifiedName.getFullyQualifiedName();

					if (fullyQualifiedName.equals("Scopes.SINGLETON")) {
						scopeType = GuiceConstants.SINGLETON_SCOPE;
					}
				}

			}
			else if (methodname.equals("asEagerSingleton")) {
				isEagerSingleton = true;
				scopeType = GuiceConstants.SINGLETON_SCOPE;
			}
			else {
				unsupportedMethod(
						GuiceConstants.ANNOTATED_CONSTANT_BINDING_BUILDER,
						methodname);
			}
		}

		else if (declType.equals(GuiceConstants.MAP_BINDER)) {
			/*  */
			if (methodname.equals("addBinding")) {
				/*
				 * Nothing to do, but don't delete it as otherwise it will be
				 * logged as missing!
				 */
			}
			else if (methodname.equals("newMapBinder")) {
				inspectNewMapBinderInvocation(methodInvocation);
			}
			else {
				unsupportedMethod(GuiceConstants.MAP_BINDER, methodname);
			}
		}
		else if (declType.equals(GuiceConstants.SET_BINDER)) {
			/* */
			if (methodname.equals("addBinding")) {
				/*
				 * Nothing to do, but don't delete it as otherwise it will be
				 * logged as missing!
				 */
			}
			else if (methodname.equals("newSetBinder")) {
				inspectNewSetBinderInvocation(methodInvocation);
			}
			else {
				unsupportedMethod(GuiceConstants.SET_BINDER, methodname);
			}
		}
		else {

		}

		return true;
	}

	private void injectSourceCodeReference(ASTNode astNode,
			GuiceStatement guiceStatement) {
		SourceCodeReference sourceCodeReference = new SourceCodeReference();
		sourceCodeReference.setOffset(astNode.getStartPosition());
		sourceCodeReference.setLength(astNode.getLength());
		guiceStatement.setSourceCodeReference(sourceCodeReference);
	}

	private void resolveAnnotations(Expression firstArgument) {
		guiceAnnotation = ExpressionUtils.resolveGuiceAnnotation(firstArgument);
	}

	private void finishBindingStatement(MethodInvocation methodInvocation) {
		bindingStatement.setBoundType(boundType);
		if (implType != null) {
			((LinkedBindingStatement) bindingStatement).setImplType(implType);
		}
		bindingStatement.setScopeType(scopeType);
		bindingStatement.setGuiceAnnotation(guiceAnnotation);
		bindingStatement.setEagerSingleton(isEagerSingleton);

		injectSourceCodeReference(methodInvocation, bindingStatement);

		addBinding(bindingStatement);
		clearScope();
	}

	private void inspectNewSetBinderInvocation(MethodInvocation methodInvocation) {
		List<Expression> arguments = methodInvocation.arguments();

		int size = arguments.size();
		if (size == 3) {
			/* 3rd argument is the annotation. */
			Expression expression = arguments.get(2);
			guiceAnnotation = ExpressionUtils.resolveGuiceAnnotation(expression);
			bindingStatement.setGuiceAnnotation(guiceAnnotation);
		}
	}

	private void inspectNewMapBinderInvocation(MethodInvocation methodInvocation) {
		List<Expression> arguments = methodInvocation.arguments();

		if (arguments.size() == 4) {
			/* 3rd argument is the annotation. */
			Expression expression = arguments.get(3);
			guiceAnnotation = ExpressionUtils.resolveGuiceAnnotation(expression);
			bindingStatement.setGuiceAnnotation(guiceAnnotation);
		}
	}

	private Set<String> logMessages = SetUtils.newHashSet();

	private void unsupportedMethod(String type, String methodname) {
		String methodID = type + "#" + methodname;
		boolean contains = logMessages.contains(methodID);
		if (!contains) {
			EgapPlugin.logInfo(type + "#" + methodname + " not supported");
			logMessages.add(methodID);
		}
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		Type type = node.getType();
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;

			boolean isMapBinderType = TypeUtils.isMapBinderType(parameterizedType);
			if (isMapBinderType) {
				bindingStatement = new MapBinderCreateStatement();
				String interfaceType = TypeUtils.wrapInType(
						parameterizedType.typeArguments(),
						StringUtils.MAP_TYPE);
				bindingStatement.setBoundType(interfaceType);
				injectSourceCodeReference(node, bindingStatement);
				addBinding(bindingStatement);
				return true;
			}

			boolean isSetBinderType = TypeUtils.isSetBinderType(parameterizedType);
			if (isSetBinderType) {
				bindingStatement = new SetBinderCreateStatement();
				String boundType = TypeUtils.wrapInType(
						parameterizedType.typeArguments(),
						StringUtils.SET_TYPE);
				bindingStatement.setBoundType(boundType);
				injectSourceCodeReference(node, bindingStatement);
				addBinding(bindingStatement);
				return true;
			}
		}

		return true;
	}

	/*
	 * Here we check the methods in the guice modules if they are provider
	 * methods.
	 */
	@Override
	public boolean visit(MethodDeclaration node) {
		List<ASTNode> modifiers = node.modifiers();
		AnnotationList markerAnnotationList = ASTNodeUtils.getAnnotationList(modifiers);

		if (markerAnnotationList.containsProvidesAnnotation()) {
			ProviderMethod providerMethod = new ProviderMethod();
			Type returnType2 = node.getReturnType2();
			String boundType = TypeUtils.resolveQualifiedName(returnType2);
			providerMethod.setBoundType(boundType);
			injectSourceCodeReference(node, providerMethod);
			guiceAnnotation = markerAnnotationList.getGuiceAnnotation();
			providerMethod.setGuiceAnnotation(guiceAnnotation);

			if (markerAnnotationList.containsSingletonScopeAnnotation()) {
				providerMethod.setScopeType(GuiceConstants.SINGLETON_SCOPE);
			}

			addBinding(providerMethod);
		}

		return true;
	}

	private void clearScope() {
		boundType = null;
		implType = null;
		guiceAnnotation = null;
		scopeType = null;
		isEagerSingleton = false;
		bindingStatement = null;
	}

}