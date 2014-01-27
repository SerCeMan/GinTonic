package ru.naumen.gintonic.context.quickfix.moving;

import static ru.naumen.gintonic.utils.CollectionUtils.getFirst;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import ru.naumen.gintonic.command.BindingNavigationCycle;
import ru.naumen.gintonic.guice.injection.InjectionPoint;
import ru.naumen.gintonic.guice.statements.BindingDefinition;
import ru.naumen.gintonic.guice.statements.JustInTimeBindingStatement;
import ru.naumen.gintonic.plugin.icons.Icons;
import ru.naumen.gintonic.project.files.SelectAndReveal;
import ru.naumen.gintonic.project.source.references.SourceCodeReference;
import ru.naumen.gintonic.utils.BindingUtils;
import ru.naumen.gintonic.utils.ICompilationUnitSelectionUtils;
import ru.naumen.gintonic.utils.ListUtils;
import ru.naumen.gintonic.utils.StringUtils;
import ru.naumen.gintonic.widgets.Widgets;


public class ProposalGoToImpl implements IJavaCompletionProposal {

	private List<BindingDefinition> bindingDefinitions;
    private String typeName;
    private InjectionPoint injectionPoint;
    
    
    public ProposalGoToImpl(List<BindingDefinition> bindingDefinitions,
			InjectionPoint injectionPoint) {
		this.bindingDefinitions = bindingDefinitions;
        this.typeName = BindingUtils.extractTypeName(injectionPoint, getFirst(bindingDefinitions));
        this.injectionPoint = injectionPoint;
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
	    String type = typeName == null ? "" : " '" + StringUtils.qualifiedNameToSimpleName(typeName) + "'";
	    return "Go to implementation" + type;
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
            bindingDefinition = Widgets.showUserSelect(bindingDefinitions);
            if(bindingDefinition == null) {
                return;
            }
        } else if(!bindingDefinitions.isEmpty()) {
            bindingDefinition = getFirst(bindingDefinitions);
        }
        if (bindingDefinition == null && !bindingDefinitions.isEmpty()) {
            // May be user press Cancel or Escape
            return;
        }
        String typeName = BindingUtils.extractTypeName(injectionPoint, bindingDefinition);
        if (bindingDefinition instanceof JustInTimeBindingStatement) {
            BindingNavigationCycle navigationCycle = new BindingNavigationCycle(SourceCodeReference.createCurrent(), 
                    ListUtils.newArrayList(bindingDefinition));
            navigationCycle.jumpToNext();
            return;
        }
        if (typeName == null) {
            return;
        }
        IProject project = ICompilationUnitSelectionUtils.getCompilationUnitSelection()
                .getICompilationUnit().getResource().getProject();
        SelectAndReveal.selectAndRevealType(typeName, project);
	}

	@Override
	public int getRelevance() {
		return 0;
	}
}
