package egap.utils;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.google.common.base.Preconditions;

public final class KindOfChecker extends ASTVisitor {
	private final String typeQualifiedName;
	public ITypeBinding typeBinding;

	public KindOfChecker(String typeQualifiedName) {
		this.typeQualifiedName = typeQualifiedName;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		typeBinding = node.resolveBinding();
		Preconditions.checkNotNull(typeBinding);
		boolean isSubclass = ITypeBindingUtils.isKindOf(
				typeBinding,
				typeQualifiedName);

		if (!isSubclass) {
			typeBinding = null;
		}

		return false;
	}
}