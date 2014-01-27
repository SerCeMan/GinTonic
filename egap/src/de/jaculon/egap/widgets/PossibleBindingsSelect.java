package de.jaculon.egap.widgets;

import java.util.Collection;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;

import de.jaculon.egap.guice.statements.BindingDefinition;
import de.jaculon.egap.guice.statements.LinkedBindingStatement;

/**
 * Select with Binding Definitions
 * 
 * @author Sergey Tselovalnikov
 * @since Jan 22, 2014
 */
public class PossibleBindingsSelect extends ListDialog {

    public PossibleBindingsSelect(Collection<BindingDefinition> bindingDefinitions) {
        super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
        setTitle("Possible bindigns");
        setInput(bindingDefinitions.toArray());
        setHelpAvailable(false);
        setLabelProvider(new LabelProvider(){
            @Override
            public String getText(Object element) {
                if(element instanceof LinkedBindingStatement) {
                    return ((LinkedBindingStatement) element).getImplType();
                }
                String boundType = ((BindingDefinition)element).getBoundType();
                if(boundType == null) {
                    return ((BindingDefinition)element).getSourceCodeReference().getPrimaryTypeName();
                }
                return boundType;
            }
        });
        setContentProvider(new ArrayContentProvider());
    }
}
