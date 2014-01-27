package ru.naumen.gintonic.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/**
 * Base handler for commands
 * 
 * @author Sergey Tselovalnikov
 * @since Jan 16, 2014
 */
public abstract class BaseHandler extends AbstractHandler {
    
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        handleEvent(event);
        return null;
    }

    protected abstract void handleEvent(ExecutionEvent event);
}
