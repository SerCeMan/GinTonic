package ru.naumen.gintonic.context.quickfix.moving;

import static ru.naumen.gintonic.utils.CollectionUtils.getFirst;

import java.util.List;

import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import ru.naumen.gintonic.command.BindingNavigationCycle;
import ru.naumen.gintonic.guice.statements.BindingDefinition;
import ru.naumen.gintonic.plugin.icons.Icons;
import ru.naumen.gintonic.project.source.references.SourceCodeReference;
import ru.naumen.gintonic.utils.ListUtils;
import ru.naumen.gintonic.widgets.Widgets;


public class ProposalGoToBindings implements IJavaCompletionProposal {

	private List<BindingDefinition> bindingDefinitions;
	

	public ProposalGoToBindings(List<BindingDefinition> bindingDefinitions) {
		this.bindingDefinitions = bindingDefinitions;
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
	public String getDisplayString() {
		return "Go to bindings";
	}

	@Override
	public String getAdditionalProposalInfo() {
		return null;
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

	@Override
	public void apply(IDocument document) {
        BindingDefinition bindingDefinition = null;
        if(bindingDefinitions.size() > 1) {
            bindingDefinition = Widgets.showUserSelectWithSelected(bindingDefinitions, getFirst(bindingDefinitions));
            if(bindingDefinition == null) {
                return;
            }
        } 
        if(bindingDefinitions.size() > 0) {
            bindingDefinition = getFirst(bindingDefinitions);
        }
        BindingNavigationCycle navigationCycle = new BindingNavigationCycle(SourceCodeReference.createCurrent(), 
                ListUtils.newArrayList(bindingDefinition));
        navigationCycle.jumpToNext();
	}

	@Override
	public int getRelevance() {
		return 0;
	}
}
