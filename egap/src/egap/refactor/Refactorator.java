package egap.refactor;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.core.CreateFieldOperation;
import org.eclipse.jdt.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;


import egap.utils.ASTParserUtils;
import egap.utils.Preconditions;
import egap.utils.StringUtils;

public class Refactorator {

	private final ICompilationUnit icompilationUnit;
	private final AST ast;
	private final RefactoratorProvider provider;
	private final CompilationUnit compilationUnit;

	private Refactorator(ICompilationUnit icompilationUnit,
			CompilationUnit compilationUnitAstNode,
			AST ast) {
		this.compilationUnit = compilationUnitAstNode;
		Preconditions.checkNotNull(ast);
		Preconditions.checkNotNull(icompilationUnit);
		this.ast = ast;
		this.icompilationUnit = icompilationUnit;
		provider = new RefactoratorProvider(ast, icompilationUnit);
	}

	public static Refactorator create(ICompilationUnit icompilationUnit,
			CompilationUnit compilationUnit, AST ast) {
		return new Refactorator(icompilationUnit, compilationUnit, ast);
	}

	private ImportRewrite getImportRewrite() {
		return provider.getImportRewrite();
	}

	private ASTRewrite getAstRewrite() {
		return provider.getAstRewrite();
	}
	
	public void addFieldDeclaration(FieldDeclaration fieldDeclaration) {
		ASTRewrite astRewrite = getAstRewrite();
		@SuppressWarnings("unchecked")
		List<TypeDeclaration> types = compilationUnit.types();
		ListRewrite container = astRewrite.getListRewrite(types.get(0), TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		container.insertLast(fieldDeclaration, null);
	}
	
	public TrackedStatement addAsLastStatementInMethod(
			MethodDeclaration method, String statement) {
		Statement bindingStatement = ASTParserUtils.parseStatementAst3(statement);
		Block methodBody = method.getBody();
		ASTRewrite astRewrite = getAstRewrite();
		ITrackedNodePosition track = astRewrite.track(bindingStatement);
		ListRewrite statementsListRewrite = astRewrite.getListRewrite(
				methodBody,
				Block.STATEMENTS_PROPERTY);
		statementsListRewrite.insertLast(bindingStatement, null);
		int startPosition = track.getStartPosition();
		int length = track.getLength();
		bindingStatement.setSourceRange(startPosition, length);
		return new TrackedStatement(bindingStatement, track);
	}
	
	/**
	 * Adds the given method.
	 * 
	 * @param methodCode the java code of the method declaration.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TrackedMethodDeclaration addMethod(String methodCode) {
		
		ASTRewrite astRewrite = getAstRewrite();
		
		TypeDeclaration typeDeclarationNew = ASTParserUtils.parseTypeDeclaration(methodCode);
		MethodDeclaration[] methods = typeDeclarationNew.getMethods();
		MethodDeclaration methodDeclaration = methods[0];
		
		ITrackedNodePosition track = astRewrite.track(methodDeclaration);
		
		List<TypeDeclaration> types = compilationUnit.types();
		int nrOfTypes = types.size();
		if(nrOfTypes > 0){
			TypeDeclaration typeDeclSource = types.get(0);
			ListRewrite container= astRewrite.getListRewrite(typeDeclSource, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
			container.insertLast(methodDeclaration, null);
		}

		return new TrackedMethodDeclaration(methodDeclaration, track);
	}
	
	/**
	 * Adds a new import to the rewriter's record and returns a type reference that can be used in the code.
	 */
	public String addImport(IType type) {
		return addImport(type.getFullyQualifiedName());
	}
	
	/**
	 * Adds a new import to the rewriter's record and returns a type reference that can be used in the code.
	 */
	public String addImport(ITypeBinding typeBinding) {
		ImportRewrite importRewrite = getImportRewrite();
		return importRewrite.addImport(typeBinding);
	}
	
	/**
	 * Adds a new import to the rewriter's record and returns a type reference that can be used in the code.
	 */
	public String addImport(String fullyClassifiedTypeName) {
		ImportRewrite importRewrite = getImportRewrite();
		return importRewrite.addImport(fullyClassifiedTypeName);
	}

	public void changeType(Type oldType, Type newType) {
		ASTRewrite astRewrite = getAstRewrite();
		astRewrite.replace(oldType, newType, null);
	}

	/**
	 * Creates a new {@link ParameterizedType} by wrapping the given
	 * {@link Type} inside the wrapperType. We also add the new type as import
	 * statement.
	 * 
	 * <h5>Example:</h5>
	 * 
	 * Given the following field declaration:
	 * 
	 * <pre>
	 * public Snake&lt;?&gt; snake;
	 * </pre>
	 * 
	 * You can change the <code>Snake <?></code> type to
	 * <code>Provider<Snake></code> by coding:
	 * 
	 * <pre>
	 * <code>refactorator.changeTypeByWrappingIt(snakeType, "com.google.inject.Provider");</code>
	 * </pre>
	 * 
	 * 
	 * @param ast
	 * @param parameterizedType
	 * @param targetTypeNameFullyQualified the fully qualified type that wraps
	 *            the
	 * @return the new {@link ParameterizedType}
	 */
	@SuppressWarnings("unchecked")
	public ParameterizedType changeTypeByWrappingIt(Type sourceType,
			String targetTypeNameFullyQualified) {
		AST ast = sourceType.getAST();
		String targetTypeNameSimple = StringUtils.qualifiedNameToSimpleName(targetTypeNameFullyQualified);
		SimpleName simpleName = ast.newSimpleName(targetTypeNameSimple);
		ParameterizedType targetType = ast.newParameterizedType(ast.newSimpleType(simpleName));
		Type typeUnbound = (Type) ASTNode.copySubtree(ast, sourceType);
		List<Type> typeArguments = targetType.typeArguments();
		typeArguments.add(typeUnbound);

		addImport(targetTypeNameFullyQualified);
		changeType(sourceType, targetType);

		return targetType;
	}

	public void renameVariableIdentifiers(
			VariableDeclarationFragment variableDeclarationFragment,
			String newName) {
		ASTRewrite astRewrite = getAstRewrite();
		SimpleName oldVariableName = variableDeclarationFragment.getName();
		SimpleName newVariableName = ast.newSimpleName(newName);
		/* Replace the old with the new name */
		astRewrite.replace(oldVariableName, newVariableName, null);
	}

	/**
	 * Renames all identifiers of the given fieldDeclaration.
	 * 
	 * @param fieldDeclaration the {@link FieldDeclaration}
	 * @param newName the newName. The name can include a $ sign which is then
	 *            replaced by the original identifier. Use this if you want some
	 *            part of the original name to be preserved.
	 */
	public void renameFieldIdentifiers(FieldDeclaration fieldDeclaration,
			String newName) {

		@SuppressWarnings("unchecked")
		List<VariableDeclarationFragment> fragments = fieldDeclaration.fragments();

		ASTRewrite astRewrite = getAstRewrite();
		for (VariableDeclarationFragment variableDeclarationFragment : fragments) {
			SimpleName name = variableDeclarationFragment.getName();
			String oldVariableName = name.getIdentifier();

			/* Substitute $ placeholder with the old variable name */
			String newVariableNameAsString = newName;
			if (newVariableNameAsString.contains("$")) {
				newVariableNameAsString = newVariableNameAsString.replace(
						"$",
						oldVariableName);
			}

			SimpleName newVariableName = ast.newSimpleName(newVariableNameAsString);
			/* Replace the old with the new name */
			astRewrite.replace(name, newVariableName, null);
		}
	}

	public void refactor(IProgressMonitor progressMonitor) {

		try {
			ICompilationUnit workingCopy = icompilationUnit.getWorkingCopy(progressMonitor);

			String originalSource = workingCopy.getSource();
			Document sourceDocument = new Document(originalSource);

			MultiTextEdit multiTextEdit = new MultiTextEdit();

			rewriteImports(progressMonitor, multiTextEdit);
			rewriteAST(sourceDocument, multiTextEdit);

			workingCopy.applyTextEdit(multiTextEdit, progressMonitor);
			workingCopy.reconcile(
					ICompilationUnit.NO_AST,
					false,
					null,
					progressMonitor);
			workingCopy.commitWorkingCopy(true, progressMonitor);
			workingCopy.discardWorkingCopy();

		} catch (Exception e) {
			throw new RefactoratorException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	private void rewriteAST(Document sourceDocument, MultiTextEdit multiTextEdit) {
		IJavaProject javaProject = icompilationUnit.getJavaProject();
		Map options = javaProject.getOptions(true);
		ASTRewrite astRewrite = getAstRewrite();
		TextEdit astRewriteTextEdit = astRewrite.rewriteAST(
				sourceDocument,
				options);
		if (astRewriteTextEdit != null) {
			multiTextEdit.addChild(astRewriteTextEdit);
		}
	}

	private void rewriteImports(IProgressMonitor progressMonitor,
			MultiTextEdit multiTextEdit) throws CoreException {
		ImportRewrite importRewrite = getImportRewrite();
		TextEdit importRewriteTextEdit = importRewrite.rewriteImports(progressMonitor);
		if (importRewriteTextEdit != null) {
			multiTextEdit.addChild(importRewriteTextEdit);
		}
	}

	/**
	 * I don't want to have the ImportRewrite and ASTRewrite as fields of
	 * Refactorator as they are created on demand. So this is what this
	 * RefactoratorProvider is for.
	 * 
	 * @author tmajunke
	 */
	private static class RefactoratorProvider {
		private AST ast;
		private ICompilationUnit compilationUnit;
		private ImportRewrite importRewrite;
		private ASTRewrite astRewrite;

		private RefactoratorProvider(AST ast, ICompilationUnit compilationUnit) {
			super();
			this.ast = ast;
			this.compilationUnit = compilationUnit;
		}

		private ImportRewrite getImportRewrite() {
			if (importRewrite == null) {
				try {
					importRewrite = ImportRewrite.create(compilationUnit, true);
				} catch (JavaModelException e) {
					throw new RefactoratorException(e);
				}
			}
			return importRewrite;
		}

		private ASTRewrite getAstRewrite() {
			if (astRewrite == null) {
				astRewrite = ASTRewrite.create(ast);
			}
			return astRewrite;
		}

	}

	

}
