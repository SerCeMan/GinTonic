package de.jaculon.egap.selection;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.ITextSelection;

public class ICompilationUnitSelection {

    private ICompilationUnit compilationUnit;
    private ITextSelection textSelection;

    public ICompilationUnitSelection(ICompilationUnit compilationUnit, ITextSelection textSelection) {
        this.compilationUnit = compilationUnit;
        this.textSelection = textSelection;
    }

    public ICompilationUnit getICompilationUnit() {
        return compilationUnit;
    }

    public ITextSelection getITextSelection() {
        return textSelection;
    }

    @Override
    public String toString() {
        return compilationUnit.getElementName() + "(Selection offset = " + textSelection.getOffset() + ", length = "
                + textSelection.getLength() + ")";
    }

}
