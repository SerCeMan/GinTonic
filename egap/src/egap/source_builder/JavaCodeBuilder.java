package egap.source_builder;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

import com.google.common.collect.Sets;

import egap.utils.TypeUtils;

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
	private static final String STATEMENT_FINISH = ";";
	private static final String BLOCK_FINISH = "}";
	private static final String BLOCK_START = "{";

	private Appendable appendable;

	public JavaCodeBuilder(Appendable appendable) {
		this.appendable = appendable;
	}

	private void append(CharSequence text) {
		try {
			appendable.append(text);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void startBlock() {
		append(BLOCK_START);
	}

	public void finishBlock() {
		append(BLOCK_FINISH);
	}

	public void finishStatement() {
		append(STATEMENT_FINISH);
	}

	public void startClass(String guiceModuleClassName, String extendsDecl) {
		append("public class ");
		append(guiceModuleClassName);
		if (extendsDecl != null) {
			append(" extends " + extendsDecl);
		}
	}

	public void startInterface(String interfaceName) {
		append("public interface ");
		append(interfaceName);
	}

	/**
	 * Starts a method declaration.
	 * 
	 * @param annotations the method annotations, may be null.
	 * @param qualifier public, protected, ...
	 * @param methodName the name of the method. mandatory.
	 * @param returnType the return type.
	 * @param variableDeclarations the variable declarations
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
		
		int i = 0;
		for (SingleVariableDeclaration variableDeclaration : variableDeclarations) {
			
			if(i++ > 0){
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
	 * @param targetClassType
	 * @param variableDeclAnnotatedWithAssisted
	 */
	public void addImports(IType targetClassType,
			List<SingleVariableDeclaration> variableDeclAnnotatedWithAssisted) {

		ImportStatemenentCalculator importStatemenentCalculator = new ImportStatemenentCalculator();
		importStatemenentCalculator.setTargetType(targetClassType);
		importStatemenentCalculator.setVariableDecl(variableDeclAnnotatedWithAssisted);

		List<ITypeBinding> importBindings = importStatemenentCalculator.calculate();
		
		Set<String> importStatementSet = Sets.newHashSet();
		
		for (ITypeBinding importBinding : importBindings) {
			/* Wir müssen die Type Declaration verwenden! */
			ITypeBinding importBindingTypeDecl = importBinding.getTypeDeclaration();
			String importName = importBindingTypeDecl.getQualifiedName();
			
			/* Prevent duplicates */
			if(!importStatementSet.contains(importName)){
				append("import " + importName + ";");
			}
			importStatementSet.add(importName);
		}

	}

}
