package egap.attic;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;

import com.google.common.base.Preconditions;

@Deprecated
public class ASTNodeUtils2 {
	
	/**
	 * Returns true if the AST node is of type {@link Name} and the qualified
	 * name of its type binding is equal to any of the given classNames.
	 * 
	 * <h5>Usage</h5>
	 * 
	 * <pre>
	 * <code>
	 * class X{
	 *  public X(@Assisted Y y){}
	 * }
	 * 
	 * Now if the covering node is y then the call to
	 * MarkerAnnotationUtils#isSimpleNameAnnotatedWithAny(
	 *   coveringNode, 
	 *   "com.google.inject.assistedinject.Assisted"
	 * )
	 * will return true.
	 * 
	 * </code>
	 * </pre>
	 * 
	 * 
	 */
	public static boolean isNameAnnotatedWithAny(ASTNode node,
			String... classNames) {
		Preconditions.checkNotNull(node);
		if (node instanceof Name) {
			Name name = (Name) node;
			ITypeBinding typeBinding = name.resolveTypeBinding();
			Preconditions.checkNotNull(
					typeBinding,
					"No type binding available!");
			String qualifiedName = typeBinding.getQualifiedName();

			for (String className : classNames) {
				if (qualifiedName.equals(className)) {
					return true;
				}
			}
		}

		return false;
	}
	
	/**
	 * Ascends the AST and calls back the visitor for each visited node.
	 * Ascending means calling the {@link ASTNode#getParent()} till we get
	 * <code>null</code> or the visitor returns false.
	 * 
	 * @param node the node which we ascend.
	 * @param visitor the visitor. Return false, if you want to stop ascending
	 *            the nodes. Return true to further visit the nodes parents.
	 */
	public static void ascendNode(ASTNode node, ASTVisitor2 visitor) {
		ASTNode currentNode = node;
		while (true) {
			currentNode = currentNode.getParent();
			boolean proceed = visitor.visit(currentNode);
			if (!proceed || currentNode == null) {
				return;
			}
		}
	}
	
	/**
	 * Tries to find a constructor by ascending the given nodes children.
	 * 
	 * @param node the node
	 * @return the Constructor or null if it could not be found.
	 */
	public static MethodDeclaration findConstructor(ASTNode node) {
		FindConstructorNode2 visitor = new FindConstructorNode2();
		ascendNode(node, visitor);
		return visitor.getConstructorNode();
	}
	
}
