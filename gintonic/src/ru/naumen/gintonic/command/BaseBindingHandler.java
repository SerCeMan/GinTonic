package ru.naumen.gintonic.command;

import org.eclipse.core.commands.ExecutionEvent;

import ru.naumen.gintonic.guice.GuiceIndex;
import ru.naumen.gintonic.guice.injection.IInjectionPoint;
import ru.naumen.gintonic.guice.injection.InjectionPointDao;
import ru.naumen.gintonic.project.navigate.selection.ICompilationUnitSelection;
import ru.naumen.gintonic.project.source.references.SourceCodeReference;
import ru.naumen.gintonic.utils.ICompilationUnitSelectionUtils;

/**
 * Base handler for binding commands
 * 
 * @author Sergey Tselovalnikov
 * @since Jan 27, 2014
 */
public abstract class BaseBindingHandler extends BaseHandler {

    protected InjectionPointDao injectionPointDao;
    protected GuiceIndex guiceIndex;

    @Override
    protected void handleEvent(ExecutionEvent event) {
        if (this.guiceIndex == null) {
            this.guiceIndex = GuiceIndex.get();
        }
        if (this.injectionPointDao == null) {
            injectionPointDao = new InjectionPointDao();
        }
        SourceCodeReference currentCodeLocation = SourceCodeReference.createCurrent();
        ICompilationUnitSelection compilationUnitSelection = ICompilationUnitSelectionUtils
                .getCompilationUnitSelection();

        if (currentCodeLocation == null || compilationUnitSelection == null) {
            return;
        }
        IInjectionPoint injectionPoint = injectionPointDao.findInjectionPointByTextSelection(
                compilationUnitSelection.getICompilationUnit(), compilationUnitSelection.getITextSelection());
        if (injectionPoint != null) {
            makeCommand(compilationUnitSelection, currentCodeLocation, injectionPoint);
        }
    }

    protected abstract void makeCommand(ICompilationUnitSelection compilationUnitSelection,
            SourceCodeReference currentCodeLocation, IInjectionPoint injectionPoint);
}
