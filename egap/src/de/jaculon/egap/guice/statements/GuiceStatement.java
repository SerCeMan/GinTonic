package de.jaculon.egap.guice.statements;

import de.jaculon.egap.guice.ProjectResource;

/**
 * @author tmajunke
 */
public abstract class GuiceStatement extends ProjectResource {
	private static final long serialVersionUID = 2923609470258376603L;

	public abstract String getLabel();

}