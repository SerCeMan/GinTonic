package egap.guice.indexer;

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


import egap.EgapPlugin;
import egap.guice.annotations.GuiceAnnotation;
import egap.guice.statements.BindingStatement;
import egap.guice.statements.ConstantBindingStatement;
import egap.guice.statements.InstallModuleStatement;
import egap.guice.statements.InstanceBindingStatement;
import egap.guice.statements.LinkedBindingStatement;
import egap.guice.statements.MapBinderCreateStatement;
import egap.guice.statements.ProviderBindingStatement;
import egap.guice.statements.ProviderBindingToMethodStatement;
import egap.guice.statements.SetBinderCreateStatement;
import egap.utils.ASTNodeUtils;
import egap.utils.ExpressionUtils;
import egap.utils.ListUtils;
import egap.utils.AnnotationList;
import egap.utils.MethodInvocationUtils;
import egap.utils.Preconditions;
import egap.utils.SetUtils;
import egap.utils.StringUtils;
import egap.utils.TypeUtils;

/**
 * @author tmajunke
 */
@SuppressWarnings("unchecked")
public final class GuiceIndexerAstVisitor extends ASTVisitor {

	/**
	 * Note: Needed from outside.
	 */
	private ITypeBinding guiceModuleTypeBinding;
	private List<BindingStatement> bindingStatements = ListUtils.newArrayListWithCapacity(300);
	private List<InstallModuleStatement> installModuleStatements = ListUtils.newArrayListWithCapacity(50);

	/* Internals */
	private BindingStatement bindingStatement;
	private GuiceAnnotation guiceAnnotation;
	private String interfaceType;
	private String implType;
	private String scopeType;
	private boolean isEagerSingleton;

	public List<BindingStatement> getBindingStatements() {
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

	private void clearScope() {
		interfaceType = null;
		implType = null;
		guiceAnnotation = null;
		scopeType = null;
		isEagerSingleton = false;
		bindingStatement = null;
	}

	private void addBinding(BindingStatement bindingStatement) {
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

		if (declType.equals(StringUtils.GUICE_ABSTRACT_MODULE)) {
			if (methodname.equals("bind")) {
				interfaceType = ExpressionUtils.getQualifiedTypeName(firstArgument);

				if (bindingStatement == null) {
					/* e.g bind(X.class).in(Scopes.SINGLETON); */
					bindingStatement = new BindingStatement();
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
				ASTNodeUtils.copyStartPositionAndLength(
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
				unsupportedMethod(StringUtils.GUICE_ABSTRACT_MODULE, methodname);
			}
		}

		else if (declType.equals(StringUtils.GUICE_LINKED_BINDING_BUILDER)) {

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
				}
				else if (methodname.equals("toInstance")) {
					bindingStatement = new InstanceBindingStatement();
				}
				else if (methodname.equals("toProvider")) {
					bindingStatement = new ProviderBindingStatement();
				}
				else {
					unsupportedMethod(
							StringUtils.GUICE_LINKED_BINDING_BUILDER,
							methodname);
				}
				implType = ExpressionUtils.getQualifiedTypeName(firstArgument);
			}
		}

		else if (declType.equals(StringUtils.GUICE_ANNOTATED_BINDING_BUILDER)) {
			/* void in(Scope scope); */
			/* void in(Class<? extends Annotation> scopeAnnotation); */
			/* void asEagerSingleton(); */
			if (methodname.equals("annotatedWith")) {
				resolveAnnotations(firstArgument);
			}
			else {
				unsupportedMethod(
						StringUtils.GUICE_ANNOTATED_BINDING_BUILDER,
						methodname);
			}
		}
		else if (declType.equals(StringUtils.GUICE_CONSTANT_BINDING_BUILDER)) {
			if (methodname.equals("to")) {
				bindingStatement = new ConstantBindingStatement();
				implType = ExpressionUtils.getQualifiedTypeName(firstArgument);

				/*
				 * Special case in constants as impl and interface type are
				 * equal
				 */
				interfaceType = implType;
			}
			/* By the way - no scopes for constants! */
			else {
				unsupportedMethod(
						StringUtils.GUICE_CONSTANT_BINDING_BUILDER,
						methodname);
			}
		}
		else if (declType.equals(StringUtils.GUICE_CONSTANT_ANNOTATED_BINDING_BUILDER)) {
			if (methodname.equals("annotatedWith")) {
				resolveAnnotations(firstArgument);
			}
			else {
				unsupportedMethod(
						StringUtils.GUICE_CONSTANT_ANNOTATED_BINDING_BUILDER,
						methodname);
			}
		}
		else if (declType.equals(StringUtils.GUICE_SCOPED_BINDING_BUILDER)) {
			if (methodname.equals("in")) {

				if (firstArgument instanceof QualifiedName) {
					QualifiedName qualifiedName = (QualifiedName) firstArgument;
					String fullyQualifiedName = qualifiedName.getFullyQualifiedName();

					if (fullyQualifiedName.equals("Scopes.SINGLETON")) {
						scopeType = "Singleton";
					}
				}

			}
			else if (methodname.equals("asEagerSingleton")) {
				isEagerSingleton = true;
				scopeType = "SINGLETON";
			}
			else {
				unsupportedMethod(
						StringUtils.GUICE_CONSTANT_ANNOTATED_BINDING_BUILDER,
						methodname);
			}
		}

		else if (declType.equals(StringUtils.GUICE_MAP_BINDER)) {
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
				unsupportedMethod(StringUtils.GUICE_MAP_BINDER, methodname);
			}
		}
		else if (declType.equals(StringUtils.GUICE_SET_BINDER)) {
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
				unsupportedMethod(StringUtils.GUICE_SET_BINDER, methodname);
			}
		}
		else {

		}

		return true;
	}

	private void resolveAnnotations(Expression firstArgument) {
		guiceAnnotation = ExpressionUtils.resolveGuiceAnnotation(firstArgument);
	}

	private void finishBindingStatement(MethodInvocation methodInvocation) {
		bindingStatement.setBoundType(interfaceType);
		if (implType != null) {
			((LinkedBindingStatement) bindingStatement).setImplType(implType);
		}
		bindingStatement.setScopeType(scopeType);
		bindingStatement.setGuiceAnnotation(guiceAnnotation);
		bindingStatement.setEagerSingleton(isEagerSingleton);
		ASTNodeUtils.copyStartPositionAndLength(
				methodInvocation,
				bindingStatement);
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
				ASTNodeUtils.copyStartPositionAndLength(node, bindingStatement);

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
				ASTNodeUtils.copyStartPositionAndLength(node, bindingStatement);
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
			ProviderBindingToMethodStatement providerBindingStatement = new ProviderBindingToMethodStatement();
			Type returnType2 = node.getReturnType2();
			String boundType = TypeUtils.resolveQualifiedName(returnType2);
			providerBindingStatement.setBoundType(boundType);
			ASTNodeUtils.copyStartPositionAndLength(
					node,
					providerBindingStatement);
			guiceAnnotation = markerAnnotationList.getGuiceAnnotation();
			providerBindingStatement.setGuiceAnnotation(guiceAnnotation);

			if(markerAnnotationList.containsSingletonScopeAnnotation()){
				providerBindingStatement.setScopeType(StringUtils.GUICE_SCOPE_SINGLETON_NAME);
			}

			addBinding(providerBindingStatement);
		}

		return true;
	}

}