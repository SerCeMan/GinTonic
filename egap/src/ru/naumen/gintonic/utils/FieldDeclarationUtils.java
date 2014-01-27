package ru.naumen.gintonic.utils;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Modifier;


public class FieldDeclarationUtils {


	/**
	 * Returns true if the field declaration is static.
	 *
	 * @param fieldDeclaration the field declaration
	 */
	public static boolean isStatic(FieldDeclaration fieldDeclaration) {
		@SuppressWarnings("unchecked")
		List<ASTNode> modifiers = fieldDeclaration.modifiers();
		for (ASTNode astNode : modifiers) {
			if (astNode instanceof Modifier) {
				Modifier modifier = (Modifier) astNode;
				if (modifier.isStatic()) {
					return true;
				}
			}
		}
		return false;
	}

}
