package ru.naumen.gintonic.context.quickfix.providers;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import ru.naumen.gintonic.context.refactor.Refactorator;
import ru.naumen.gintonic.plugin.icons.Icons;
import ru.naumen.gintonic.project.files.SelectAndReveal;
import ru.naumen.gintonic.utils.TypeUtils;


public class ProposalRemoveProvider implements IJavaCompletionProposal {

	private final ICompilationUnit icompilationUnit;
	private final ParameterizedType providerType;
	private final Type providedType;
	private final FieldDeclaration providerDeclaration;
	private final CompilationUnit compilationUnit;

	public ProposalRemoveProvider(ICompilationUnit compilationUnit2,
			CompilationUnit compilationUnit3, FieldDeclaration providerDeclaration, ParameterizedType providerType,
			Type providedType) {
		icompilationUnit = compilationUnit2;
		this.compilationUnit = compilationUnit3;
		this.providerDeclaration = providerDeclaration;
		this.providerType = providerType;
		this.providedType = providedType;
	}

	@Override
	public Point getSelection(IDocument document) {
		return null;
	}

	@Override
	public Image getImage() {
		return Icons.ginTonicDefaultIconSmall;
	}

	@Override
	/**
	 * Kurz Beschreibung der Aktion,taucht in der QuickfixProposal-Auswahl auf!
	 */
	public String getDisplayString() {
		return getAdditionalProposalInfo();
	}

	@Override
	/**
	 * Ausfuehrliche Beschreibung der Aktion, taucht in einem Fenster rechts
	 * neben der QuickfixProposal-Auswahl auf!
	 */
	public String getAdditionalProposalInfo() {
		String typeDeclarationAsString = TypeUtils.getTypeDeclarationAsString(providedType);
		String proposalInfo = "Convert to " + typeDeclarationAsString + "";
		return proposalInfo;
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}
	
	public static final Pattern pattern = Pattern.compile("(\\w+)(Provider)");
	
	@Override
	public void apply(IDocument document) {
		Refactorator refactorator = Refactorator.create(
				icompilationUnit,
				compilationUnit,
				providerType.getAST());
		refactorator.changeType(providerType, providedType);
		/* Change the variable name (e.g cat => catProvider) */
		
		
		@SuppressWarnings("unchecked")
		List<VariableDeclarationFragment> fragments = providerDeclaration.fragments();
		for (VariableDeclarationFragment variableDeclarationFragment : fragments) {
			SimpleName name = variableDeclarationFragment.getName();
			String identifier = name.getIdentifier();
			Matcher matcher = pattern.matcher(identifier);
			if(matcher.find()){
				String newName = matcher.group(1);
				refactorator.renameVariableIdentifiers(variableDeclarationFragment, newName);
			}
		}
		
		refactorator.refactor(null);

		SelectAndReveal.selectAndReveal(
				(IFile) icompilationUnit.getResource(),
				providedType.getParent().getStartPosition());
	}

	@Override
	public int getRelevance() {
		return 0;
	}

}