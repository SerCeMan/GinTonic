package ru.naumen.gintonic.widgets;

import java.util.Collection;

import ru.naumen.gintonic.guice.statements.BindingDefinition;

/**
 * Help manipulate with widgets and user input
 * 
 * @author Sergey Tselovalnikov
 * @since Jan 22, 2014
 */
public final class Widgets {
    
    public static BindingDefinition showUserSelect(Collection<BindingDefinition> definitions, BindingDefinition defaultValue,
            BindingDefinition selectedValue) {
        PossibleBindingsSelect select = new PossibleBindingsSelect(definitions);
        select.open();
        if(selectedValue != null) {
            select.setInput(selectedValue);
        }
        Object[] result = select.getResult();
        if(result == null || result.length == 0) {
            return defaultValue;
        }
        return (BindingDefinition) result[0];
    }
    
    public static  BindingDefinition showUserSelect(Collection<BindingDefinition> definitions) {
        return showUserSelect(definitions, null, null);
    }
    
    public static  BindingDefinition showUserSelectWithSelected(Collection<BindingDefinition> definitions, BindingDefinition selected) {
        return showUserSelect(definitions, null, selected);
    }
    
    private Widgets() {
    }
}
