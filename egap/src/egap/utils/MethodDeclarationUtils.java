package egap.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

public class MethodDeclarationUtils {

	/**
	 * Returns true if the given {@link ASTNode} is a constructor, otherwise
	 * false.
	 */
	public static boolean isConstructor(ASTNode node) {
		if (node instanceof MethodDeclaration) {
			MethodDeclaration methodDecl = (MethodDeclaration) node;
			return methodDecl.isConstructor();
		}
		return false;
	}

	/**
	 * Returns the first constructor for the given CompilationUnit if it is
	 * annotated with @Inject or null if not. (Note: There may only be one
	 * constructor with @Inject, see
	 * http://code.google.com/p/google-guice/wiki/InjectionPoints)
	 */
	public static MethodDeclaration getConstructorAnnotatedWithInject(
			CompilationUnit compilationUnit) {
		ASTVisitorFindConstructor visitor = new ASTVisitorFindConstructor();
		compilationUnit.accept(visitor);
		List<MethodDeclaration> constructors = visitor.constructors;

		for (MethodDeclaration constructor : constructors) {
			@SuppressWarnings("unchecked")
			List<ASTNode> modifiers = constructor.modifiers();
			MarkerAnnotationList markerAnnotationList = ASTNodeUtils.getMarkerAnnotationList(modifiers);
			if (markerAnnotationList.containsInjectType()) {
				return constructor;
			}
		}

		return null;
	}

	/**
	 * Returns the <b>first</b> constructor if one parameter is annotated with
	 * an @Assisted annotation. Returns null if noone could be found.
	 */
	public static MethodDeclaration getConstructorAnnotatedWithAssisted(
			ASTNode astNode) {
		ASTVisitorFindConstructor visitor = new ASTVisitorFindConstructor();
		astNode.accept(visitor);
		List<MethodDeclaration> constructors = visitor.constructors;

		for (MethodDeclaration constructor : constructors) {
			@SuppressWarnings("unchecked")
			List<SingleVariableDeclaration> parameters = constructor.parameters();
			for (SingleVariableDeclaration singleVariableDeclaration : parameters) {
				@SuppressWarnings("unchecked")
				List<ASTNode> modifiers = singleVariableDeclaration.modifiers();
				MarkerAnnotationList markerAnnotationList = ASTNodeUtils.getMarkerAnnotationList(modifiers);

				if (markerAnnotationList.containsAssistedAnnotation()) {
					return constructor;
				}
			}
		}

		return null;
	}

	public static List<SingleVariableDeclaration> getVariableDeclAnnotatedWithAssisted(
			MethodDeclaration constructorNode) {

		final List<SingleVariableDeclaration> variableDeclarations = new ArrayList<SingleVariableDeclaration>();
		constructorNode.accept(new ASTVisitor(false) {
			@Override
			public boolean visit(final SingleVariableDeclaration declNode) {
				declNode.accept(new ASTVisitor(false) {
					@Override
					public boolean visit(MarkerAnnotation node) {
						if (ASTNodeUtils.isAssistedAnnotation(node)) {
							variableDeclarations.add(declNode);
						}
						return false;
					}
				});
				return false;
			}
		});
		return variableDeclarations;
	}

	public static SingleVariableDeclaration getVariableDeclarationsByName(
			MethodDeclaration methodDeclaration, final String fieldNameFQ) {
		Preconditions.checkNotNull(methodDeclaration);
		Preconditions.checkNotNull(fieldNameFQ);
		FindVariableDeclWithName variableDeclWithName = new FindVariableDeclWithName(
				fieldNameFQ);
		methodDeclaration.accept(variableDeclWithName);
		return variableDeclWithName.declaration;
	}

	public static MethodDeclaration getMethodDeclarationByName(
			CompilationUnit compilationUnit, String name) {
		ASTVisitorExtension visitorExtension = new ASTVisitorExtension(name);
		compilationUnit.accept(visitorExtension);
		return visitorExtension.getMethodDeclaration();
	}

	public static MethodDeclaration getConfigureMethodDeclaration(
			CompilationUnit compilationUnit) {
		return getMethodDeclarationByName(compilationUnit, "configure");
	}

	private final static class ASTVisitorExtension extends ASTVisitor {

		private String methodnameToFind;
		private MethodDeclaration methodDeclaration;

		public ASTVisitorExtension(String methodnameToFinde) {
			super();
			this.methodnameToFind = methodnameToFinde;
		}

		public MethodDeclaration getMethodDeclaration() {
			return methodDeclaration;
		}

		@SuppressWarnings("synthetic-access")
		@Override
		public boolean visit(MethodDeclaration method) {

			SimpleName simpleName = method.getName();
			String methodname = simpleName.toString();

			if (methodname.equals(methodnameToFind)) {
				this.methodDeclaration = method;
			}

			return false;
		}
	}

	private static final class ASTVisitorFindConstructor extends ASTVisitor {
		public List<MethodDeclaration> constructors = ListUtils.newArrayList();

		private ASTVisitorFindConstructor() {
			super(false);
		}

		@Override
		public boolean visit(MethodDeclaration node) {
			if (isConstructor(node)) {
				constructors.add(node);
			}
			return false;
		}
	}

	private static final class FindVariableDeclWithName extends ASTVisitor {
		private final String fieldNameFQ;
		SingleVariableDeclaration declaration;

		private FindVariableDeclWithName(String fieldNameFQ) {
			super(false);
			this.fieldNameFQ = fieldNameFQ;
		}

		@Override
		public boolean visit(final SingleVariableDeclaration declNode) {
			SimpleName name = declNode.getName();
			String fullyQualifiedName = name.getFullyQualifiedName();
			if (fullyQualifiedName.equals(fieldNameFQ)) {
				declaration = declNode;
			}
			return false;
		}
	}

}
