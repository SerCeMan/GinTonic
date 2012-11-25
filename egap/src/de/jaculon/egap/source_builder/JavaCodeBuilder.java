package de.jaculon.egap.source_builder;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

import de.jaculon.egap.utils.SetUtils;


/**
 * @author tmajunke
 *
 */
public class JavaCodeBuilder {

	public static final String KOMMATA = ", ";
	private static final String BRACKET_CLOSE = ")";
	private static final String BRACKET_OPEN = "(";
	private static final String NEWLINE = System.getProperty("line.separator");
	private static final String SINGLE_SPACE = " ";
	private static final String SEMIKOLON = ";";
	private static final String ANGLE_BRACKET_OPEN = "{";
	private static final String ANGLE_BRACKET_CLOSE = "}";

	private Appendable appendable;

	public JavaCodeBuilder(Appendable appendable) {
		this.appendable = appendable;
	}

	public void append(CharSequence text) {
		try {
			appendable.append(text);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void startBlock() {
		append(ANGLE_BRACKET_OPEN);
	}

	public void finishBlock() {
		append(ANGLE_BRACKET_CLOSE);
	}

	public void finishStatement() {
		append(SEMIKOLON);
	}

	public void startInterface(String interfaceName) {
		append("public interface ");
		append(interfaceName);
	}

	/**
	 * Creates a new method declaration.
	 *
	 * @param annotations the method annotations, may be null.
	 * @param qualifier public, protected, .... Must not be null.
	 * @param methodName the name of the method. Must not be null.
	 * @param returnType the return type. Must not be null.
	 * @param variableDeclarations the variable declarations. Can be null
	 * @param javadoc the javadoc comment. Can be null.
	 */
	public void startMethod(List<String> annotations, String qualifier,
			String methodName, String returnType,
			List<SingleVariableDeclaration> variableDeclarations, String javadoc) {

		if (annotations != null) {
			for (String annotation : annotations) {
				append("@");
				append(annotation);
				appendNewline();
			}
		}

		if (javadoc != null) {
			append("/** ");
			appendNewline();
			append(" * ");
			append(javadoc);
			appendNewline();
			append(" * ");
			appendNewline();

			for (SingleVariableDeclaration variableDeclaration : variableDeclarations) {
				SimpleName simpleName = variableDeclaration.getName();
				append("* @param ");
				append(simpleName.toString());
				appendNewline();
			}

			append("*/");
		}

		append(qualifier);
		appendSingleSpace();
		append(returnType);
		appendSingleSpace();
		append(methodName);

		appendSingleSpace();
		appendBracketOpen();

		if (variableDeclarations != null) {
			int i = 0;
			for (SingleVariableDeclaration variableDeclaration : variableDeclarations) {

				if (i++ > 0) {
					append(KOMMATA);
				}

				Type type = variableDeclaration.getType();
				ITypeBinding typeBinding = type.resolveBinding();
				String varDecl = typeBinding.getName();
				append(varDecl);
				appendSingleSpace();
				SimpleName simpleName = variableDeclaration.getName();
				append(simpleName.toString());
			}
		}

		append(BRACKET_CLOSE); //$NON-NLS-1$
	}

	private void appendBracketOpen() {
		append(BRACKET_OPEN);
	}

	private void appendSingleSpace() {
		append(SINGLE_SPACE);
	}

	private void appendNewline() {
		append(NEWLINE);
	}

	/**
	 * Creates import statements for the given variable declarations.
	 *
	 * @param targetClassType the class where the
	 * @param singleVariableDeclaration
	 */
	public void addImports(IType targetClassType,
			List<SingleVariableDeclaration> singleVariableDeclaration) {

		ImportStatemenentCalculator importStatemenentCalculator = new ImportStatemenentCalculator();
		importStatemenentCalculator.setTargetType(targetClassType);
		importStatemenentCalculator.setVariableDecl(singleVariableDeclaration);

		List<ITypeBinding> importBindings = importStatemenentCalculator.calculate();

		Set<String> importStatementSet = SetUtils.newHashSet();

		for (ITypeBinding importBinding : importBindings) {
			/* Wir müssen die Type Declaration verwenden! */
			ITypeBinding importBindingTypeDecl = importBinding.getTypeDeclaration();
			String importName = importBindingTypeDecl.getQualifiedName();

			/* Prevent duplicates */
			if (!importStatementSet.contains(importName)) {
				append("import " + importName + ";");
			}
			importStatementSet.add(importName);
		}

	}

}
