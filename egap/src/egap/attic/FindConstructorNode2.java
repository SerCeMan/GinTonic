package egap.attic;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import egap.utils.ASTNodeUtils;


@Deprecated
public class FindConstructorNode2 implements ASTVisitor2 {

	private MethodDeclaration constructorNode;
	
	public MethodDeclaration getConstructorNode() {
		return constructorNode;
	}

	@Override
	public boolean visit(ASTNode node) {
		if (ASTNodeUtils.isConstructor(node)) {
			constructorNode = (MethodDeclaration) node;
			return false; /* We can stop processing here */
		}

		return true;
	}
	
}