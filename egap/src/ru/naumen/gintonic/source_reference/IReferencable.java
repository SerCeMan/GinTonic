package ru.naumen.gintonic.source_reference;

/**
 * An entity which holds a {@link SourceCodeReference}.
 * 
 * @author tmajunke
 */
public interface IReferencable {

    /**
     * Returns the {@link SourceCodeReference}.
     */
    public SourceCodeReference getSourceCodeReference();

}
