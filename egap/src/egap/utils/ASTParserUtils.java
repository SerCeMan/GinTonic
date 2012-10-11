package egap.utils;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class ASTParserUtils {

	/**
	 * synonym for parseCompilationUnitAst3(compilationUnit, true, false);
	 */
	public static CompilationUnit parseCompilationUnitAst3(ICompilationUnit compilationUnit) {
		return parseCompilationUnitAst3(compilationUnit, true, false);
	}

	public static CompilationUnit parseCompilationUnitAst3(ICompilationUnit compilationUnit, boolean resolveBindings, boolean recoverBindings) {
		ASTParser astParser = ASTParser.newParser(AST.JLS3);
		
		astParser.setSource(compilationUnit);
		astParser.setResolveBindings(resolveBindings);
		astParser.setBindingsRecovery(recoverBindings);
		astParser.setKind(ASTParser.K_COMPILATION_UNIT);
		
		CompilationUnit astRoot = (CompilationUnit) astParser.createAST(null);
		return astRoot;
	}

	public static CompilationUnit parseCompilationUnitAst3(String source, boolean resolveBindings, boolean recoverBindings) {
		ASTParser astParser = ASTParser.newParser(AST.JLS3);
		
		astParser.setSource(source.toCharArray());
		astParser.setResolveBindings(resolveBindings);
		astParser.setBindingsRecovery(recoverBindings);
		astParser.setKind(ASTParser.K_COMPILATION_UNIT);
		
		CompilationUnit astRoot = (CompilationUnit) astParser.createAST(null);
		return astRoot;
	}
	
	public static Statement parseStatementAst3(String statementAsString) {
		ASTParser astParser = ASTParser.newParser(AST.JLS3);
		astParser.setSource(statementAsString.toCharArray());
		astParser.setKind(ASTParser.K_STATEMENTS);
		astParser.setResolveBindings(true);
		Block astBindingExpr = (Block) astParser.createAST(null);
		Statement statement = (Statement) astBindingExpr.statements().get(
				0);
		return statement;
	}

	/**
	 * @param methodCode
	 * @return
	 */
	public static TypeDeclaration parseTypeDeclaration(String methodCode) {
		ASTParser astParser = ASTParser.newParser(AST.JLS3);
		astParser.setSource(methodCode.toCharArray());
		astParser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
		astParser.setResolveBindings(true);
		TypeDeclaration typeDeclaration = (TypeDeclaration) astParser.createAST(null);
		return typeDeclaration;
	}

}
