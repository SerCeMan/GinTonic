package egap.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import com.google.common.base.Preconditions;

public class MethodDeclarationUtils {

	private static final class FindVariableDeclWithName extends ASTVisitor {
		private final String fieldNameFQ;
		SingleVariableDeclaration declaration;

		private FindVariableDeclWithName(boolean visitDocTags,
				String fieldNameFQ) {
			super(visitDocTags);
			this.fieldNameFQ = fieldNameFQ;
		}

		@Override
		public boolean visit(final SingleVariableDeclaration declNode) {
			SimpleName name = declNode.getName();
			String fullyQualifiedName = name.getFullyQualifiedName();
			if (fullyQualifiedName.equals(fieldNameFQ)) {
				declaration = declNode;
				return false;
			}
			return true;
		}
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
				false,
				fieldNameFQ);
		methodDeclaration.accept(variableDeclWithName);
		return variableDeclWithName.declaration;
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
				return false; /* Stop processing the child nodes */
			}

			return this.methodDeclaration == null;
		}
	}

	public static MethodDeclaration getMethodDeclarationByName(
			ASTNode compilationUnit, String name) {
		ASTVisitorExtension visitorExtension = new ASTVisitorExtension(name);
		compilationUnit.accept(visitorExtension);
		return visitorExtension.getMethodDeclaration();
	}

	public static MethodDeclaration getConfigureMethodDeclaration(
			ASTNode compilationUnit) {
		return getMethodDeclarationByName(compilationUnit, "configure");
	}

}
