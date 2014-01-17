package de.jaculon.egap.guice.annotations;

import java.io.Serializable;

public abstract class GuiceAnnotation implements Serializable {

    private static final long serialVersionUID = 4009792266623056715L;

    public abstract String getTypeToImport();

}
