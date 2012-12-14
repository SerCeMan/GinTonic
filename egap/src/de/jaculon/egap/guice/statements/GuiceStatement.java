package de.jaculon.egap.guice.statements;

import java.io.Serializable;

import de.jaculon.egap.source_reference.IReferencable;
import de.jaculon.egap.source_reference.SourceCodeReference;


/**
 * @author tmajunke
 */
public abstract class GuiceStatement implements Serializable, IReferencable {
	
	private static final long serialVersionUID = 2923609470258376603L;

	private SourceCodeReference sourceCodeReference;

	@Override
	public SourceCodeReference getSourceCodeReference() {
		return sourceCodeReference;
	}

	public void setSourceCodeReference(SourceCodeReference sourceCodeReference) {
		this.sourceCodeReference = sourceCodeReference;
	}
	
}
