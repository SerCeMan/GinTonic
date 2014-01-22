package de.jaculon.egap.guice.statements;

import java.util.HashMap;
import java.util.Map;


/**
 * Define install statements:
 *
 * install(new FactoryModuleBuilder()
 *     .implement(AssistedPrinter.class, AssistedPrinter.class)
 *     .build(AssistedPrinterFactory.class));
 *     
 *     
 * Factory build type = boundType
 * 
 * @author Sergey Tselovalnikov
 * @since Jan 22, 2014
 */
public class InstallBindingStatement extends BindingDefinition {
    private static final long serialVersionUID = -8055858964080365635L;
    
    /**
     * Implements <Source : Target (implementation)>
     * @see com.google.inject.assistedinject.FactoryModuleBuilder.implement(Class<T>, Class<? extends T>)
     */
    private Map<String, String> implementes = new HashMap<String, String>();
    
    public void addImpl(String source, String target) {
        implementes.put(source, target);
    }
}
